import datetime
import logging

import settings

import discord
import pymongo
from discord import Embed, User
from discord.app.commands import slash_command, user_command
from discord.app.context import ApplicationContext
from discord.colour import Color
from discord.ext import commands
from discord.raw_models import RawReactionActionEvent

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

    @slash_command(description='Shows leaderboard', guild_ids=settings.GUILD_IDS)
    async def leaderboard(self, ctx: ApplicationContext):
        karma_list = await self.get_leaderboard_docs(ctx.guild_id)

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
                            {'guild': str(ctx.guild_id),
                            'user': str(user.id)},
                            {'$set': {'name': user_name}}
                        )
                        logger.info(f'{ctx.guild.name}: Added {user_name}\'s name to their document')
                    except discord.errors.NotFound:
                        logger.warn(
                            f'{ctx.guild.name}: User {user_id} not found in guild, deleting their document')
                        await self.remove_user(user_id, ctx.guild.id)
                        continue

                icon = ['🥇', '🥈', '🥉', '🐝']
                desc += f'`{i + 1:02d}.` {icon[i] if i < 3 else icon[3]} `{user_doc["karma"]:4d}` ' \
                        f'{user_name}\n'

            await ctx.respond(
                embed=Embed(
                    title=f'Top karma for {ctx.guild.name}',
                    description=desc,
                    color=Color.green()
                ).set_thumbnail(url=ctx.guild.icon.url)
            )

        logger.info(
            f'{ctx.guild.name}: Showing leaderboard to {ctx.author.name}#{ctx.author.discriminator}')

    @user_command(name='Get karma', guild_ids=settings.GUILD_IDS)
    async def get_user_karma(self, ctx: ApplicationContext, user: User):
        # don't get bot karma
        if user == self.bot.user:
            await ctx.respond(embed=Embed(
                title='The fire rises.',
                color=Color.red()
            ).set_thumbnail(url=self.bot.user.display_avatar.url), ephemeral=True)
            return

        user_doc = self._karma.find_one({
            'guild': str(ctx.guild_id),
            'user': str(user.id)
        })

        try:
            karma = user_doc['karma']
        except (AttributeError, KeyError, TypeError):
            karma = 0

        await ctx.respond(embed=Embed(
            title=f'{user.name}#{user.discriminator}',
            color=Color.green()
        ).add_field(name='Karma', value=karma).set_thumbnail(url=user.display_avatar.url))

        logger.info(
            f'{ctx.guild.name}: {ctx.author.name}#{ctx.author.discriminator} got {user.name}#{user.discriminator}\'s karma')

    async def handle_reaction_event(self, payload: RawReactionActionEvent):
        # skip ignored users
        if await self.is_ignored_user(payload.user_id, payload.guild_id):
            return

        # find author of message
        channel = self.bot.get_channel(payload.channel_id)
        message = await channel.fetch_message(payload.message_id)
        author_id = message.author.id

        # skip messages older than 24h
        if datetime.datetime.now().timestamp() - datetime.timedelta(days=1).total_seconds() >= message.created_at.timestamp():
            return

        # skip voting on bane
        if self.bot.user == message.author:
            return

        # skip voting on self
        if payload.user_id == author_id:
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
             'user': str(author_id)},
            {'$inc': {'karma': vote_direction},
             '$set': {'name': f'{message.author.name}#{message.author.discriminator}'}},
            upsert=True
        )

        # get name for logging
        user = self.bot.get_user(payload.user_id)
        if user is None:
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
