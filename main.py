import logging

from disnake.ext import commands

import settings
from cogs import cogs

logging.getLogger('disnake.gateway').setLevel(logging.WARN)
logging.basicConfig(
    level=logging.INFO,
    format='[%(asctime)s][%(name)s][%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

logger = logging.getLogger('main')


def main():
    logger.info('Starting bot')
    bot = commands.InteractionBot(test_guilds=settings.GUILD_IDS)

    for cog in cogs:
        bot.add_cog(cog(bot))

    bot.run(settings.BOT_TOKEN)


if __name__ == "__main__":
    main()
