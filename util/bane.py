import logging
from disnake.ext import commands


class BaneCog(commands.Cog):
    logger: logging.Logger

    async def cog_load(self):
        self.logger = logging.getLogger(f'cogs.{type(self).__name__.lower()}')
        self.logger.info('Loaded cog')

    def cog_unload(self):
        self.logger.info('Unloaded cog')
