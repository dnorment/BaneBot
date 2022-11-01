import logging
import re
import time

import feedparser
import pymongo
import settings
from disnake import (ApplicationCommandInteraction, ButtonStyle, Embed, Guild,
                     MessageInteraction, TextChannel)
from disnake.ext import commands, tasks
from disnake.ui import Button, View, button

logger = logging.getLogger('cogs.manga')


class SubscribeButtonView(View):
    def __init__(self):
        super().__init__()

        self.subscribed = False

    @button(emoji='📰', style=ButtonStyle.grey, custom_id='subscribe', label='Subscribe')
    async def subscribe_button(self, button: Button, inter: MessageInteraction):
        self.subscribe_button.disabled = True
        self.subscribe_button.emoji = '✔'
        self.subscribe_button.label = 'Subscribed'

        await inter.response.edit_message(view=self)


class Manga(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self._db = pymongo.MongoClient(settings.MONGO_URI)['banebot']
        self._mangadb = self._db['manga']
        self.check_series_updates.start()
        logger.info('Initialized cog')

    def _set_notification_channel(self, guild: Guild, channel: TextChannel):
        self._mangadb.find_one_and_update(
            {'guild': guild.id},
            {'$set': {'channel': channel.id}},
            upsert=True
        )
        logger.info(
            f'Set {guild.name} manga notifications to channel #{channel.name}')

    def _get_notification_channel(self, guild: Guild) -> TextChannel:
        channel: TextChannel = None

        doc = self._mangadb.find_one({'guild': guild.id})
        try:
            channel = guild.get_channel(doc['channel'])
        except (KeyError, TypeError):
            pass

        return channel

    @tasks.loop(minutes=30)
    async def check_series_updates(self):
        for guild in self.bot.guilds:
            channel = self._get_notification_channel(guild)

            if not channel:
                continue

        logger.info('Running update loop')

    @commands.slash_command()
    async def manga(self, inter: ApplicationCommandInteraction):
        pass

    @manga.sub_command(description='Show details of a manga from MangaSee123 URL')
    async def search(self, inter: ApplicationCommandInteraction, url: str):
        match = re.match(
            r'https://mangasee123\.com/(manga|rss)/(?P<series_id>[\w\s-]+)(\.xml)?', url)

        if not match:
            await inter.send('No match for URL')
            return

        series_id = match['series_id']

        url = f'https://mangasee123.com/rss/{series_id}.xml'
        feed = feedparser.parse(url, agent='GitHub dnorment/BaneBot')

        title = feed['feed']['title']
        updated: time.struct_time = feed['feed']['updated_parsed']
        last_updated_str = f'{updated.tm_year}/{updated.tm_mon:02}/{updated.tm_mday:02}'
        link = feed['feed']['image']['link']
        cover_url = feed['feed']['image']['href']

        emb = Embed(title=title, url=link)
        emb.set_footer(text=f'Last updated: {last_updated_str}')
        emb.set_image(url=cover_url)

        await inter.send(embed=emb, view=SubscribeButtonView())

    @manga.sub_command(description='Sets the text channel in which to show chapter notifications')
    async def setchannel(self, inter: ApplicationCommandInteraction, channel: TextChannel):
        self._set_notification_channel(inter.guild, channel)
        await inter.send(f'Will show manga updates in {channel.mention}')


def setup(bot: commands.Bot):
    bot.add_cog(Manga(bot))
