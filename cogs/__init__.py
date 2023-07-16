import logging
import os
from datetime import datetime, timedelta
from typing import Set

import disnake
from disnake.ext import commands


class BaneCog(commands.Cog):
    logger: logging.Logger

    async def cog_load(self):
        cog_name = type(self).__name__.lower()
        self.logger = logging.getLogger(f'cogs.{cog_name}')

        self.logger.info('Loaded cog')

    def cog_unload(self):
        self.logger.info('Unloaded cog')

    @staticmethod
    def message_older_than_24h(message: disnake.Message) -> bool:
        '''
        True if `message` is older than 24 hours.
        '''

        now_timestamp = datetime.now().timestamp()
        seconds_in_24h = timedelta(days=1).total_seconds()
        timestamp_24h_ago = now_timestamp - seconds_in_24h

        if timestamp_24h_ago >= message.created_at.timestamp():
            return True

        return False


# Cogs must be defined in `/cogs` or a subfolder's `cog.py` file
_cog_files = set(os.listdir('./cogs')) - {'__init__.py', '__pycache__'}
for f in _cog_files:
    cog_names = {f[:-3] if f.endswith('.py') else f'{f}.cog' for f in _cog_files}
