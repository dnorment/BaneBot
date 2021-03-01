from datetime import datetime

import discord

from main import start_time
from exceptions import ArgumentNumberError
from commands.command import Command


class Uptime(Command):
    """Command for checking the bot's uptime"""

    def __init__(self):
        description = f"Displays the bot's uptime"
        params = [
            {'usage': 'uptime', 'desc': "Get the bot's current uptime"}
        ]
        super().__init__(description, params)

    async def handle(self, params, message, client):
        try:
            if len(params) == 0:
                end_time = datetime.now()
                diff = end_time - start_time
                seconds = diff.seconds % 60
                minutes = (diff.seconds // 60) % 60
                hours = (diff.seconds // 3600) % 24
                days = diff.days
                await message.channel.send(
                    embed=discord.Embed(
                        title='Uptime',
                        description=f'{days} days, {hours} hours, {minutes} minutes, {seconds} seconds',
                        color=discord.Color.green()
                    )
                )
            else:
                raise ArgumentNumberError
        except ArgumentNumberError:
            await super().show_usage(message, error=True)
