import logging

from discord.ext import commands
from discord.ext.commands import CommandError
from discord.ext.commands.context import Context
from discord.ext.commands.errors import CommandNotFound

logger = logging.getLogger('cogs.command_error')


class CommandError(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        logger.info('Initialized cog')

    @commands.Cog.listener()
    async def on_command_error(self, ctx: Context, error: CommandError):
        if isinstance(error, CommandNotFound):
            logger.debug(f'{ctx.guild.name}: Ignoring command: {error}')


def setup(bot: commands.Bot):
    bot.add_cog(CommandError(bot))
