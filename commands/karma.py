import discord

from commands.command import Command
from exceptions import ArgumentNumberError
from karma_handler import KarmaHandler


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
                    if len(karma_list := await KarmaHandler.get_leaderboard(message, client)) >= 1:
                        desc = ''
                        for i, user_doc in enumerate(karma_list):
                            user = client.get_user(user_doc['user'])
                            if user is None:
                                user = await client.fetch_user(user_doc['user'])
                            icon = ['🥇', '🥈', '🥉', '🐝']
                            desc += f'`{i + 1:02d}.` {icon[i] if i < 3 else icon[3]} `{user_doc["karma"]:4d}` ' \
                                    f'{user.name}#{user.discriminator}\n'
                        await message.channel.send(
                            embed=discord.Embed(
                                description=desc,
                                color=discord.Color.green()
                            ).set_author(name=f'Top karma for {message.guild.name}', icon_url=message.guild.icon_url)
                        )
                else:
                    raise ValueError
            else:
                raise ArgumentNumberError
        except (ArgumentNumberError, ValueError):
            await super().show_usage(message, error=True)
