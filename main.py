import logging

from disnake.ext import commands

import settings
from cogs import cog_names

logging.getLogger('disnake.gateway').setLevel(logging.WARN)
logging.basicConfig(level=logging.INFO,
                    format='[%(asctime)s][%(name)s][%(levelname)s] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

logger = logging.getLogger('main')


def main():
    logger.info('Starting bot')
    bot = commands.InteractionBot(test_guilds=settings.GUILD_IDS)

    logger.info(f'Loading extensions ({len(cog_names)})')
    for cog in cog_names:
        try:
            bot.load_extension(f'cogs.{cog}')
        except commands.errors.ExtensionNotFound as e:
            logger.error(e)

    bot.run(settings.BOT_TOKEN)


if __name__ == "__main__":
    main()
