import logging

import discord
from discord.ext import commands
import settings

logging.getLogger('discord').setLevel(logging.WARN)
logging.basicConfig(level=logging.INFO,
                    format='[%(asctime)s][%(name)s][%(levelname)s] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

logger = logging.getLogger('main')


def main():
    logger.info('Starting bot')
    bot = commands.Bot(command_prefix='!', intents=discord.Intents.default())

    logger.info('Loading extensions')

    bot.load_extension('cogs.karma')
    bot.load_extension('cogs.uptime')

    bot.run(settings.BOT_TOKEN)


if __name__ == "__main__":
    main()
