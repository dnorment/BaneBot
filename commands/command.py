import discord

import settings


class Command:
    """Base Command class for any commands"""

    def __init__(self, description, params):
        self.name = type(self).__name__.lower()
        self.params = params

        desc = f'**{settings.PREFIX}{self.name}**'

        if self.params:
            desc += ' ' + ' '.join(f'*<{p["name"]}>*' for p in params)

        desc += f": {description}"
        self.description = desc

    async def show_usage(self, message, error=False):
        desc = self.description + '\n'

        if self.params:
            desc += '\n'.join(f'*{p["name"]}*: {p["desc"]}' for p in self.params)

        embed = discord.Embed(
            title=settings.PREFIX + self.name,
            description=desc,
            color=discord.Color.green() if not error else discord.Color.red()
        )
        await message.channel.send(embed=embed)

    async def handle(self, params, message, client):
        raise NotImplementedError
