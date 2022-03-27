import logging

import settings
from disnake import ApplicationCommandInteraction, Message, File
from disnake.ext import commands

from PIL import Image

logger = logging.getLogger('cogs.images')


class Images(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        logger.info('Initialized cog')

    @commands.message_command(description='Feet', guild_ids=settings.GUILD_IDS)
    async def feet(self, inter: ApplicationCommandInteraction, msg: Message):
        for attachment in msg.attachments:
            if attachment.content_type in ['image/jpeg', 'image/png']:
                filename = f'./temp/{attachment.filename}'
                await attachment.save(filename)
                logger.info(f'Saved file {attachment.filename}')

                await self.add_feet(filename)
                await inter.send(file=File(filename))
            else:
                inter.send('no image attached', ephemeral=True)
                logger.warn('Missing attached file')

    async def add_feet(self, filename: str):
        img = Image.open(filename)
        feet = Image.open('./resources/feet01.png').resize(img.size)
        img.paste(feet, mask=feet)
        img.save(filename)


def setup(bot: commands.Bot):
    bot.add_cog(Images(bot))
