import logging

from api import MangaUpdatesAPI
from disnake import ApplicationCommandInteraction, TextChannel
from disnake.ext import commands, tasks

logger = logging.getLogger('cogs.manga')


class Manga(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.check_series_updates.start()
        logger.info('Initialized cog')

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
        await inter.send(f'will show notifications in {channel.mention}')


def setup(bot: commands.Bot):
    bot.add_cog(Manga(bot))
