import discord

from commands.command import Command
from exceptions import ArgumentNumberError


class Commands(Command):
    """Shows all available commands"""

    def __init__(self):
        description = 'Displays a list of all base commands'
        params = None
        super().__init__(description, params)

    async def handle(self, params, message, client, error=False):
        from message_handler import COMMAND_HANDLERS

        try:
            if len(params) != 0:
                raise ArgumentNumberError

            # list all command names with their descriptions
            desc = ''
            for cmd in sorted(COMMAND_HANDLERS.items()):
                desc += '\n' + cmd[1].description

            # build embed and send
            embed = discord.Embed(
                title=type(self).__name__,
                description=desc,
                color=discord.Color.green() if not error else discord.Color.red()
            )
            await message.channel.send(embed=embed)
        except ArgumentNumberError:
            await super().show_usage(message, error=True)
