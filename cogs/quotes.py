import asyncio
import logging
import os
import random

from disnake import (FFmpegPCMAudio, PCMVolumeTransformer, VoiceChannel,
                     VoiceClient)
from disnake.ext import commands, tasks

logger = logging.getLogger('cogs.quotes')


class Quotes(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.voice_client: VoiceClient = None
        self.say_quote.start()
        logger.info('Initialized cog')

    @tasks.loop(minutes=60)
    async def say_quote(self):
        logger.info('Running quote loop')
        if self.voice_client and self.voice_client.is_playing():
            return

        vc_with_users = []
        for guild in await self.bot.fetch_guilds().flatten():
            resolved_guild = self.bot.get_guild(guild.id)
            if not resolved_guild:
                return
            for vc in resolved_guild.voice_channels:
                if len(vc.members) > 0:
                    vc_with_users.append(vc)

        if not vc_with_users:
            return

        chosen_channel = random.choice(vc_with_users)
        await self._join_channel(chosen_channel)

        await self._play_random_quote()

        await self.voice_client.disconnect()

        self.say_quote.change_interval(minutes=random.choice(range(45, 75)))

    async def _join_channel(self, channel: VoiceChannel):
        if self.voice_client and self.voice_client.is_connected():
            return await self.voice_client.move_to(channel)

        self.voice_client = await channel.connect()

    async def _play_random_quote(self):
        chosen_name = random.choice(
            [f'./resources/quotes/{name}' for name in os.listdir('./resources/quotes/')])
        source = PCMVolumeTransformer(FFmpegPCMAudio(chosen_name))
        self.voice_client.play(source=source)
        await asyncio.sleep(4)


def setup(bot: commands.Bot):
    bot.add_cog(Quotes(bot))
