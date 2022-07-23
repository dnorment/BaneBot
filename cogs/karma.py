import datetime
import logging

import disnake
import pymongo
import settings
from disnake import (ApplicationCommandInteraction, Color, Embed,
                     RawReactionActionEvent, User)
from disnake.errors import NotFound
from disnake.ext import commands

logger = logging.getLogger('cogs.karma')


class Karma(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self._db = pymongo.MongoClient(settings.MONGO_URI)['banebot']
        self._karma = self._db['karma']
        logger.info('Initialized cog')

    @commands.Cog.listener()
    async def on_raw_reaction_add(self, payload: RawReactionActionEvent):
        await self.handle_reaction_event(payload)

    @commands.Cog.listener()
    async def on_raw_reaction_remove(self, payload: RawReactionActionEvent):
        await self.handle_reaction_event(payload)

    @commands.slash_command()
    async def karma(self, inter: ApplicationCommandInteraction):
        pass

    @karma.sub_command(description='Shows leaderboard')
    async def leaderboard(self, inter: ApplicationCommandInteraction):
        karma_list = await self.get_leaderboard_docs(inter.guild_id)

        if karma_list:
            desc = ''
            for i, user_doc in enumerate(karma_list):
                user_id = user_doc['user']

                # use user's name, if not found then update name
                try:
                    user = self.bot.get_user(user_id)
                    user_name = user_doc['name']
                except (AttributeError, KeyError, TypeError):
                    try:
                        user = await self.bot.fetch_user(user_id)
                        user_name = f'{user.name}#{user.discriminator}'
                        # add name to document
                        self._karma.find_one_and_update(
                            {'guild': str(inter.guild_id),
                             'user': str(user.id)},
                            {'$set': {'name': user_name}}
                        )
                        logger.info(
                            f'{inter.guild.name}: Added {user_name}\'s name to their document')
                    except NotFound:
                        logger.warn(
                            f'{inter.guild.name}: User {user_id} not found in guild, deleting their document')
                        await self.remove_user(user_id, inter.guild.id)
                        continue

                icon = ['🥇', '🥈', '🥉', '🐝']
                desc += f'`{i + 1:02d}.` {icon[min(i, 3)]} `{user_doc["karma"]:5d}` ' \
                        f'{user_name}\n'

            await inter.send(
                embed=Embed(
                    title=f'Top karma for {inter.guild.name}',
                    description=desc,
                    color=Color.green()
                ).set_thumbnail(url=inter.guild.icon.url)
            )

        logger.info(
            f'{inter.guild.name}: Showing leaderboard to {inter.author.name}#{inter.author.discriminator}')

    @commands.user_command(name='Get karma')
    async def get_user_karma(self, inter: ApplicationCommandInteraction, user: User) -> int:
        # don't get bot karma
        if user == self.bot.user or user.bot:
            await inter.send(embed=Embed(
                title='The fire rises.',
                color=Color.red()
            ).set_thumbnail(url=self.bot.user.display_avatar.url), ephemeral=True)
            logger.info(
                f'{inter.guild.name}: {inter.author.name}#{inter.author.discriminator} reacted to a bot, ignoring')
            return

        user_doc = self._karma.find_one({
            'guild': str(inter.guild_id),
            'user': str(user.id)
        })

        try:
            karma = user_doc['karma']
        except (AttributeError, KeyError, TypeError):
            karma = 0

        await inter.send(embed=Embed(
            title=f'{user.name}#{user.discriminator}',
            color=Color.green()
        ).add_field(name='Karma', value=karma).set_thumbnail(url=user.display_avatar.url))

        logger.info(
            f'{inter.guild.name}: {inter.author.name}#{inter.author.discriminator} got {user.name}#{user.discriminator}\'s karma')

    async def handle_reaction_event(self, payload: RawReactionActionEvent):
        channel = self.bot.get_channel(payload.channel_id)

        # only handle in a guild
        if not channel or isinstance(channel, disnake.abc.PrivateChannel):
            return
        message = await channel.fetch_message(payload.message_id)

        # skip messages older than 24h
        now_timestamp = datetime.datetime.now().timestamp()
        seconds_in_24h = datetime.timedelta(days=1).total_seconds()
        timestamp_24h_ago = now_timestamp - seconds_in_24h
        if timestamp_24h_ago >= message.created_at.timestamp():
            return

        # skip ignored users
        if await self.is_ignored_user(payload.user_id, payload.guild_id):
            return

        # skip voting on all bots
        if message.author.bot:
            return

        # skip users reacting to themselves
        if payload.user_id == message.author.id:
            return

        # get upvote/downvote
        vote = payload.emoji.name
        if vote not in ['upvote', 'downvote']:
            return

        vote_direction = 1 if vote == 'upvote' else -1
        voted = vote

        # flip direction if removing reaction
        if payload.event_type == 'REACTION_REMOVE':
            vote_direction *= -1
            voted = f'remove {vote}'

        # update author's karma
        self._karma.find_one_and_update(
            {'guild': str(payload.guild_id),
             'user': str(message.author.id)},
            {'$inc': {'karma': vote_direction},
             '$set': {'name': f'{message.author.name}#{message.author.discriminator}'}},
            upsert=True
        )

        # get name for logging
        user = self.bot.get_user(payload.user_id)
        if not user:
            user = await self.bot.fetch_user(payload.user_id)

        logger.info(
            f'{message.guild.name}: {user.name}#{user.discriminator} - {voted} - {message.author.name}#{message.author.discriminator}')

    async def is_ignored_user(self, user_id: int, guild_id: int) -> bool:
        user_doc = self._karma.find_one({
            'guild': str(guild_id),
            'user': str(user_id)
        })

        try:
            return user_doc['ignored']
        except (AttributeError, KeyError, TypeError):
            return False

    async def toggle_ignore_user(self, user_id: int, guild_id: int):
        ignored = self.is_ignored_user(user_id, guild_id)

        self._karma.find_one_and_update(
            {'guild': str(guild_id),
             'user': str(user_id)},
            {'$set': {'ignored': not ignored}},
            upsert=True
        )

        guild = self.bot.get_guild(guild_id)

        logger.info(f'{guild.name}: Toggled ignore of {user_id}')

    async def remove_user(self, user_id: int, guild_id: int):
        self._karma.remove({
            'guild': str(guild_id),
            'user': str(user_id)
        })

        guild = self.bot.get_guild(guild_id)

        logger.info(f'{guild.name}: Removed user {user_id} from collection')

    async def get_leaderboard_docs(self, guild_id: int) -> list:
        karma_docs = self._karma.find({'guild': str(guild_id)})
        return sorted(karma_docs, key=lambda item: item['karma'], reverse=True)


def setup(bot: commands.Bot):
    bot.add_cog(Karma(bot))
