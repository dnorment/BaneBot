import settings

from commands.command import Command
from exceptions import ArgumentNumberError


class Help(Command):
    """Show the usage of a command"""

    def __init__(self):
        description = f'Shows the usage of *{settings.PREFIX}command*'
        params = [
            {'name': 'command', 'desc': 'Command for which to show usage'}
        ]
        super().__init__(description, params)

    async def handle(self, params, message, client):
        from message_handler import COMMAND_HANDLERS

        try:
            if len(params) != 1:
                raise ArgumentNumberError

            if params[0] in COMMAND_HANDLERS.keys():
                await COMMAND_HANDLERS[params[0]].show_usage(message)
            else:
                await COMMAND_HANDLERS['commands'].handle(params, message, client, error=True)
        except ArgumentNumberError:
            await super().show_usage(message, error=True)
            return
