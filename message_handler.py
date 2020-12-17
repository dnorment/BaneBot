import logging
import settings

from commands.command import Command
# import all classes inside the commands package, using __all__ defined in __init__.py
from commands import *

logger = logging.getLogger('message_handler')

# register all available commands as {name: class}
COMMAND_HANDLERS = {c.__name__.lower(): c() for c in Command.__subclasses__()}


async def handle_command(command, args, message, bot_client):
    if command not in COMMAND_HANDLERS:
        return

    logger.info(
        f"{message.author.name}#{message.author.discriminator}: {settings.PREFIX}{command} " + " ".join(args))

    cmd_obj = COMMAND_HANDLERS[command]
    await cmd_obj.handle(args, message, bot_client)
