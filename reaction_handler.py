import logging

from karma_handler import KarmaHandler

logger = logging.getLogger('reaction_handler')


async def handle_reaction(payload, client):
    if KarmaHandler.is_karma_reaction(payload.emoji, payload.guild_id):
        await KarmaHandler.handle_reaction(payload, client)
