import logging

import settings
from commands.command import Command

# import all classes inside the commands package, using __all__ defined in __init__.py

logger = logging.getLogger('message_handler')

# register all available commands as {name: class}
COMMAND_HANDLERS = {c.__name__.lower(): c for c in Command.__subclasses__()}


async def handle_message(message, client):
    if message.author == client.user:
        return

    if message.content.startswith(settings.PREFIX) and message.content != settings.PREFIX:
        cmd_split = message.content[len(settings.PREFIX):].split()

        await handle_command(cmd_split[0].lower(), cmd_split[1:], message, client)


async def handle_command(command, args, message, bot_client):
    if command not in COMMAND_HANDLERS:
        return

    logger.info(
        f"{message.author.name}#{message.author.discriminator}: {settings.PREFIX}{command} " + " ".join(args))

    cmd_obj = COMMAND_HANDLERS[command]
    await cmd_obj.handle(args, message, bot_client)
