import logging

from disnake.ext import commands

import settings
from cogs import cog_names

logging.getLogger('discord').setLevel(logging.WARN)
logging.basicConfig(level=logging.INFO,
                    format='[%(asctime)s UTC][%(name)s][%(levelname)s] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

logger = logging.getLogger('main')


def main():
    logger.info('Starting bot')
    bot = commands.Bot()

    logger.info('Loading extensions')
    for cog in cog_names:
        bot.load_extension(f'cogs.{cog}')

    bot.run(settings.BOT_TOKEN)


if __name__ == "__main__":
    main()
