import asyncio
import logging

import discord

from commands.command import Command
from exceptions import ArgumentNumberError
from karma_handler import KarmaHandler

logger = logging.getLogger('karma_handler')


class Karma(Command):
    """Command for karma management"""

    def __init__(self):
        description = f'Displays karma or sets up the karma system'
        params = [
            {'usage': 'karma', 'desc': 'Get your current karma'},
            {'usage': 'karma `<@user>`', 'desc': 'Get `@user`\'s current karma'},
            {'usage': 'karma leaderboard', 'desc': 'Show the current karma leaderboard for this server'},
            {'usage': 'karma setreactions', 'desc': 'Set up the reactions for upvoting and downvoting'},
            {'usage': 'karma ignore `<@user>`', 'desc': 'Ignores/unignores all reactions from `@user`'}
        ]
        super().__init__(description, params)

    async def handle(self, params, message, client):
        try:
            if len(params) == 0:
                # get self karma
                karma = await KarmaHandler.get_karma(message.author.id, message.guild.id)
                await message.channel.send(
                    embed=discord.Embed(
                        title=f'{message.author.name}#{message.author.discriminator}',
                        color=discord.Color.green()
                    ).set_thumbnail(url=message.author.avatar_url)
                    .add_field(name='Karma', value=karma)
                )
            elif len(params) == 1:
                if len(message.mentions) == 1:
                    # get other's karma
                    target_user = message.mentions[0]
                    karma = await KarmaHandler.get_karma(target_user.id, message.guild.id)
                    await message.channel.send(
                        embed=discord.Embed(
                            title=f'{target_user.name}#{target_user.discriminator}',
                            color=discord.Color.green()
                        ).set_thumbnail(url=target_user.avatar_url)
                        .add_field(name='Karma', value=karma)
                    )
                elif params[0] == 'leaderboard':
                    # show karma leaderboard
                    if len(karma_list := await KarmaHandler.get_leaderboard(message)) >= 1:
                        desc = ''
                        for i, user_doc in enumerate(karma_list):
                            user_id = user_doc['user']
                            user = client.get_user(user_id)
                            if user is None:
                                try:
                                    user = await client.fetch_user(user_id)
                                except discord.errors.NotFound:
                                    logger.warn(f'User {user_id} not found in guild {message.guild.id}, deleting their document')
                                    await KarmaHandler.remove_user(user_id, message.guild.id)
                                    continue
                            icon = ['🥇', '🥈', '🥉', '🐝']
                            desc += f'`{i + 1:02d}.` {icon[i] if i < 3 else icon[3]} `{user_doc["karma"]:4d}` ' \
                                    f'{user.name}#{user.discriminator}\n'
                        await message.channel.send(
                            embed=discord.Embed(
                                description=desc,
                                color=discord.Color.green()
                            ).set_author(name=f'Top karma for {message.guild.name}', icon_url=message.guild.icon_url)
                        )
                elif params[0] == 'setreactions':
                    if not message.channel.permissions_for(message.author).administrator:
                        raise PermissionError

                    # set karma reactions
                    embed_message = await message.channel.send(
                        embed=discord.Embed(
                            description='React to this message with the emoji/emote you want as the upvote reaction',
                            color=discord.Color.orange()
                        ).set_footer(text='Skipping in 30 seconds...')
                    )

                    upvote = downvote = None

                    def is_author(_, reacting_user):
                        return reacting_user == message.author

                    try:
                        reaction, user = await client.wait_for('reaction_add', timeout=30, check=is_author)
                        await KarmaHandler.set_reaction(reaction.emoji, message.guild, 'upvote')
                        upvote = reaction
                    except asyncio.TimeoutError:
                        pass

                    await embed_message.edit(
                        embed=discord.Embed(
                            description='React to this message with the emoji/emote you want as the downvote reaction',
                            color=discord.Color.blue()
                        ).set_footer(text='Skipping in 30 seconds...')
                    )

                    try:
                        reaction, user = await client.wait_for('reaction_add', timeout=30, check=is_author)
                        await KarmaHandler.set_reaction(reaction.emoji, message.guild, 'downvote')
                        downvote = reaction
                    except asyncio.TimeoutError:
                        pass

                    await embed_message.clear_reactions()

                    desc = ''
                    if upvote is not None:
                        desc = f'Upvote: {upvote.emoji if type(upvote) != str else upvote}\n'
                    if downvote is not None:
                        desc += f'Downvote: {downvote.emoji if type(downvote) != str else downvote}'

                    if upvote is not None or downvote is not None:
                        await embed_message.edit(
                            embed=discord.Embed(
                                title='Set reactions',
                                description=desc,
                                color=discord.Color.green()
                            )
                        )
                    else:
                        await embed_message.delete()
                else:
                    raise ValueError
            elif len(params) == 2:
                if not message.channel.permissions_for(message.author).administrator:
                    raise PermissionError

                # ignore user
                if len(message.mentions) == 1:
                    mentioned_user = message.mentions[0]

                    if mentioned_user == client.user:
                        await message.channel.send(
                            embed=discord.Embed(
                                description='The fire rises.',
                                color=discord.Color.red()
                            )
                        )
                    else:
                        await KarmaHandler.toggle_ignore_user(mentioned_user, message.guild)
                        await message.channel.send(
                            embed=discord.Embed(
                                description=f'Toggled ignore of {mentioned_user.name}#{mentioned_user.discriminator}',
                                color=discord.Color.green()
                            )
                        )
                else:
                    raise ValueError
            else:
                raise ArgumentNumberError
        except (ArgumentNumberError, ValueError):
            await super().show_usage(message, error=True)
        except PermissionError:
            await message.channel.send(
                embed=discord.Embed(
                    description='This function is only available for server administrators',
                    color=discord.Color.red()
                )
            )
