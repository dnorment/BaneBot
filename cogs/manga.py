import logging

import pymongo
import settings
from api import MangaUpdatesAPI
from disnake import ApplicationCommandInteraction, Guild, TextChannel, User
from disnake.ext import commands, tasks

logger = logging.getLogger('cogs.manga')


class Manga(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self._db = pymongo.MongoClient(settings.MONGO_URI)['banebot']
        self._mangadb = self._db['manga']
        self.check_series_updates.start()
        logger.info('Initialized cog')

    def _set_guild_channel(self, guild: Guild, channel: TextChannel):
        self._mangadb.find_one_and_update(
            {'guild': guild.id},
            {'$set': {'channel': channel.id}},
            upsert=True
        )
        logger.info(f'Set {guild.name} manga notifications to channel #{channel.name}')

    def _get_notification_channel(self, guild: Guild) -> TextChannel:
        channel: TextChannel = None

        doc = self._mangadb.find_one({'guild': guild.id})
        try:
            channel = guild.get_channel(doc['channel'])
        except (KeyError, TypeError):
            pass

        return channel
    
    def _follow_series(self, user: User, series):
        pass

    @tasks.loop(minutes=30)
    async def check_series_updates(self):
        logger.info('Running update loop')

    @commands.slash_command()
    async def manga(self, inter: ApplicationCommandInteraction):
        pass

    @manga.sub_command(description='Show details of a manga')
    async def search(self, inter: ApplicationCommandInteraction, query: str):
        await inter.send(MangaUpdatesAPI.get_series(query))

    @manga.sub_command(description='Sets the channel to show new chapter notifications')
    async def setchannel(self, inter: ApplicationCommandInteraction, channel: TextChannel):
        self._set_guild_channel(inter.guild, channel)
        await inter.send(f'will show notifications in {channel.mention}')


def setup(bot: commands.Bot):
    bot.add_cog(Manga(bot))
