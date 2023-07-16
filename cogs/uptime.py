from datetime import datetime

from disnake import ApplicationCommandInteraction, Color, Embed
from disnake.ext import commands

from cogs import BaneCog


class Uptime(BaneCog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.start_time = datetime.now()

    @commands.slash_command(description='Shows bot uptime')
    async def uptime(self, inter: ApplicationCommandInteraction):
        diff = datetime.now() - self.start_time
        seconds = diff.seconds % 60
        minutes = (diff.seconds // 60) % 60
        hours = (diff.seconds // 3600) % 24
        days = diff.days
        uptime_str = f'{days} days, {hours} hours, {minutes} minutes, {seconds} seconds'

        await inter.send(embed=Embed(
            title='Uptime',
            description=uptime_str,
            color=Color.green()
        ))

        self.logger.info(f'{inter.guild.name}: Uptime: {uptime_str}')


def setup(bot: commands.Bot):
    bot.add_cog(Uptime(bot))
