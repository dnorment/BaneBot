import discord

import settings


class Command:
    """Base Command class for any commands"""

    def __init__(self, description, params):
        self.name = type(self).__name__.lower()
        self.params = params

        desc = f'**{settings.PREFIX}{self.name}**: {description}'

        self.description = desc

    async def show_usage(self, message, error=False):
        desc = self.description + '\n'

        if self.params:
            desc = '\n\n'.join(f'**{settings.PREFIX}{p["usage"]}**\n{p["desc"]}' for p in self.params)

        embed = discord.Embed(
            title=type(self).__name__,
            description=desc,
            color=discord.Color.green() if not error else discord.Color.red()
        )
        await message.channel.send(embed=embed)

    async def handle(self, params, message, client):
        raise NotImplementedError
