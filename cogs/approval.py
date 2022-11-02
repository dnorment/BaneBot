import settings
from disnake import RawReactionActionEvent
from disnake.ext import commands, tasks
from util.bane import BaneCog
from util.misc import message_older_than_24h


class Approval(BaneCog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.approved_messages = []
        self.prune_old_messages_task.start()

    @tasks.loop(hours=1)
    async def prune_old_messages_task(self):
        for message in self.approved_messages:
            if message_older_than_24h(message):
                self.approved_messages.remove(message)

    @commands.Cog.listener()
    async def on_raw_reaction_add(self, payload: RawReactionActionEvent):
        channel = await self.bot.fetch_channel(payload.channel_id)
        message = await channel.fetch_message(payload.message_id)

        if message_older_than_24h(message) or message in self.approved_messages:
            return

        approving_reactions = [r for r in message.reactions if r.emoji == '👍']
        if not approving_reactions:
            return

        reaction = approving_reactions[0]
        for group, member_ids in settings.APPROVAL_MAP.items():
            reactors = await reaction.users().flatten()

            if all(id in [r.id for r in reactors] for id in member_ids):
                self.approved_messages.append(message)
                await message.reply(f'Approved by {group}')
                self.logger.info(f'{group} approved message {message.id}')


def setup(bot: commands.Bot):
    bot.add_cog(Approval(bot))
