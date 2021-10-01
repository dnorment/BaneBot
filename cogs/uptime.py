import logging
from datetime import datetime

import settings
from discord.app.commands import slash_command
from discord.app.context import ApplicationContext
from discord.colour import Color
from discord.embeds import Embed
from discord.ext import commands

logger = logging.getLogger('cogs.uptime')


class Uptime(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.start_time = datetime.now()
        logger.info('Initialized cog')

    @slash_command(description='Shows bot uptime', guild_ids=settings.GUILD_IDS)
    async def uptime(self, ctx: ApplicationContext):
        end_time = datetime.now()
        diff = end_time - self.start_time
        seconds = diff.seconds % 60
        minutes = (diff.seconds // 60) % 60
        hours = (diff.seconds // 3600) % 24
        days = diff.days
        uptime_str = f'{days} days, {hours} hours, {minutes} minutes, {seconds} seconds'

        await ctx.respond(embed=Embed(
            title='Uptime',
            description=uptime_str,
            color=Color.green()
        ))

        logger.info(f'{ctx.guild.name}: Uptime: {uptime_str}')


def setup(bot: commands.Bot):
    bot.add_cog(Uptime(bot))
