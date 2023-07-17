import importlib
import logging
import os
from datetime import datetime, timedelta

import pymongo
from disnake import ApplicationCommandInteraction, Color, Embed, Message
from disnake.ext import commands

import settings


class BaneCog(commands.Cog):
    bot: commands.InteractionBot
    logger: logging.Logger

    db = pymongo.MongoClient(settings.MONGO_URI)['banebot']

    async def cog_load(self):
        cog_name = type(self).__name__.lower()
        self.logger = logging.getLogger(f'cogs.{cog_name}')

        self.logger.info('Loaded cog')

    def cog_unload(self):
        self.logger.info('Unloaded cog')

    async def cog_message_command_error(self, inter: ApplicationCommandInteraction, error: Exception):
        await self.handle_command_error(inter, error)

    async def cog_slash_command_error(self, inter: ApplicationCommandInteraction, error: Exception):
        await self.handle_command_error(inter, error)

    async def cog_user_command_error(self, inter: ApplicationCommandInteraction, error: Exception):
        await self.handle_command_error(inter, error)

    async def handle_command_error(self, inter: ApplicationCommandInteraction, error: Exception):
        embed = Embed(
            title='The fire rises.',
            description=str(error),
            color=Color.red()
        ).set_thumbnail(url=self.bot.user.display_avatar.url)

        await inter.send(
            embed=embed,
            ephemeral=True
        )

    @staticmethod
    def message_older_than_24h(message: Message) -> bool:
        '''
        True if `message` is older than 24 hours.
        '''

        now_timestamp = datetime.now().timestamp()
        seconds_in_24h = timedelta(days=1).total_seconds()
        timestamp_24h_ago = now_timestamp - seconds_in_24h

        return timestamp_24h_ago >= message.created_at.timestamp()


# Cogs must be defined in `/cogs` or a subfolder's `cog.py` file
_cog_files = set(os.listdir('./cogs')) - {'__init__.py', '__pycache__'}
_cog_names = {f[:-3] if f.endswith('.py') else f'{f}.cog' for f in _cog_files}

cogs = []

for cog_name in _cog_names:
    module = importlib.import_module(f'cogs.{cog_name}')
    cog_classname = cog_name.replace('.cog', '').capitalize()
    cogs.append(getattr(module, cog_classname))
