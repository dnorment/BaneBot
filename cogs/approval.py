import logging
from datetime import datetime, timedelta

import settings
from disnake import RawReactionActionEvent
from disnake.ext import commands

logger = logging.getLogger('cogs.approval')


class Approval(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.approved = []
        logger.info('Initialized cog')

    @commands.Cog.listener()
    async def on_raw_reaction_add(self, payload: RawReactionActionEvent):
        channel = await self.bot.fetch_channel(payload.channel_id)
        message = await channel.fetch_message(payload.message_id)

        if message in self.approved:
            return

        # Skip messages older than 24h
        now_timestamp = datetime.now().timestamp()
        seconds_in_24h = timedelta(days=1).total_seconds()
        timestamp_24h_ago = now_timestamp - seconds_in_24h
        if timestamp_24h_ago >= message.created_at.timestamp():
            return

        approving_reactions = [r for r in message.reactions if r.emoji == '👍']
        if not approving_reactions:
            return

        reaction = approving_reactions[0]
        for group, member_ids in settings.APPROVAL_MAP.items():
            reactors = await reaction.users().flatten()

            if all(id in [r.id for r in reactors] for id in member_ids):
                self.approved.append(message)
                await message.reply(f'Approved by {group}')
                logger.info(f'{group} approved message {message.id}')


def setup(bot: commands.Bot):
    bot.add_cog(Approval(bot))
