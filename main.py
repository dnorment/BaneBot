import logging

from disnake.ext import commands

from cogs import cogs
from config import Config

logging.getLogger('disnake.gateway').setLevel(logging.WARN)
logging.basicConfig(
    level=logging.INFO,
    format='[%(asctime)s][%(name)s][%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

logger = logging.getLogger('main')


def main():
    logger.info(f'Starting bot, env={Config.env}')

    bot = commands.InteractionBot(test_guilds=Config.test_guilds)

    for cog in cogs:
        bot.add_cog(cog(bot))

    bot.run(Config.discord_token)


if __name__ == "__main__":
    main()
