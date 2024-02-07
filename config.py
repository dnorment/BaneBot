import os


class Config:
    env = os.getenv('BOT_ENV', 'development')
    is_dev = env != 'production'

    discord_token = os.environ['DISCORD_TOKEN']
    mongo_uri = os.environ['MONGO_URI']

    dev_guild = os.getenv('DEV_GUILD_ID')
    test_guilds = [int(dev_guild)] if is_dev else None
