import logging

import pymongo

import settings

logger = logging.getLogger('karma_handler')


class KarmaHandler:
    client = pymongo.MongoClient(settings.MONGO_URI)['banebot']
    karma_collection = client['karma']
    reaction_collection = client['reactions']

    @classmethod
    def is_karma_reaction(cls, emoji, guild_id):
        guild_doc = cls.reaction_collection.find_one({'guild': str(guild_id)})
        id_or_codepoint = str(emoji.id) if emoji.is_custom_emoji() else emoji.name
        return guild_doc is not None and id_or_codepoint in guild_doc.values()

    @classmethod
    def is_ignored_user(cls, user_id, guild_id):
        user_doc = cls.karma_collection.find_one({
            'guild': str(guild_id),
            'user': str(user_id)
        })
        try:
            return user_doc['ignored'] is not None and user_doc['ignored']
        except (AttributeError, KeyError):
            return False

    @classmethod
    async def handle_reaction(cls, payload, client):
        # skip ignored users
        if cls.is_ignored_user(payload.user_id, payload.guild_id):
            return

        # find author of message
        channel = client.get_channel(payload.channel_id)
        message = await channel.fetch_message(payload.message_id)
        author_id = message.author.id

        # skip voting on self
        if payload.user_id == author_id:
            return

        # determine upvote/downvote
        guild_doc = cls.reaction_collection.find_one({'guild': str(payload.guild_id)})
        id_or_codepoint = str(payload.emoji.id) if payload.emoji.is_custom_emoji() else payload.emoji.name

        # already validated as karma reaction, so it must be in either upvote or downvote field
        vote_direction = 1 if guild_doc['upvote'] == id_or_codepoint else -1
        voted = 'upvote' if vote_direction == 1 else 'downvote'

        # flip direction if removing reaction
        if payload.event_type == 'REACTION_REMOVE':
            vote_direction *= -1
            voted = f'remove {voted}'

        # update author's karma
        cls.karma_collection.find_one_and_update(
            {'guild': str(payload.guild_id),
             'user': str(author_id)},
            {'$inc': {'karma': vote_direction}},
            upsert=True
        )

        # get names for logging
        user = client.get_user(payload.user_id)
        author = client.get_user(author_id)
        guild = client.get_guild(payload.guild_id)
        logger.info(f'{guild.name}: {user.name}#{user.discriminator} - {voted} - {author.name}#{author.discriminator}')
