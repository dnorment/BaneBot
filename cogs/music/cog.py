import asyncio
import logging
import subprocess
from typing import Optional

import wavelink
from disnake import ApplicationCommandInteraction, Color, Embed
from disnake.ext import commands
from settings import SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET
from wavelink.ext import spotify

logger = logging.getLogger('cogs.music.cog')


class Music(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self.ready = False

        logger.info('Starting Lavalink audio server')
        subprocess.Popen(['java', '-jar', './lib/Lavalink-3.4.jar'])
        bot.loop.create_task(self.connect_nodes())

        logger.info('Initialized cog')

    async def cog_slash_command_error(self, inter: ApplicationCommandInteraction, error: Exception):
        logger.info(error)
        if isinstance(error, commands.CommandInvokeError):
            await inter.send(embed=Embed(title='The fire rises.', description=error), ephemeral=True)

    async def connect_nodes(self):
        spotify_client = spotify.SpotifyClient(client_id=SPOTIFY_CLIENT_ID, client_secret=SPOTIFY_CLIENT_SECRET)

        await asyncio.sleep(15)  # Wait for Lavalink audio server to start
        await wavelink.NodePool.create_node(bot=self.bot,
                                            host='0.0.0.0',
                                            port=2012,
                                            password='thefirerises',
                                            spotify_client=spotify_client)
        self.ready = True

    @commands.Cog.listener()
    async def on_wavelink_node_ready(self, node: wavelink.Node):
        logger.info(f'Wavelink audio client node {node.identifier} ready')

    @commands.Cog.listener()
    async def on_wavelink_track_end(self, player: wavelink.Player, track: wavelink.Track, *, reason):
        if not player.queue.is_empty:
            await player.play(player.queue.get())

    @commands.Cog.listener()
    async def on_wavelink_track_start(self, player: wavelink.Player, track: wavelink.Track):
        logger.info(f'Playing track {track.title}')

        mins, secs = map(int, divmod(track.duration, 60))
        pretty_duration = f'{mins}:{secs:02}'

        embed = Embed(title=track.title, url=track.uri, color=Color.green())
        embed.add_field('Author', track.author, inline=False)
        embed.add_field('Duration', pretty_duration)

        await player.channel.send(embed=embed)

    @commands.slash_command()
    async def music(self, inter: ApplicationCommandInteraction):
        if not self.ready:
            raise commands.CommandInvokeError('Audio not yet ready')

        player: wavelink.Player = inter.guild.voice_client

        if not inter.author.voice:
            raise commands.CommandInvokeError(
                'You must be connected to a voice channel')

        if not player:
            player = await inter.author.voice.channel.connect(cls=wavelink.Player)

        inter.player = player

    @music.sub_command(description='Search for media and add it to the queue. No query will fall back to a banger playlist')
    async def play(self, inter: ApplicationCommandInteraction, query: Optional[str] = None):
        if not query:
            query = 'https://open.spotify.com/playlist/53A9RRcS7hIO4HzYNu1sJr'

        player: wavelink.Player = inter.player

        if 'open.spotify.com/track/' in query:
            track = await spotify.SpotifyTrack.search(query, return_first=True)
        elif 'open.spotify.com/album/' in query or 'open.spotify.com/playlist/' in query:
            await inter.response.defer()

            for track in await spotify.SpotifyTrack.search(query):
                player.queue.put(track)
                logger.info(f'Added track {track.title} from Spotify playlist')

            await inter.delete_original_message()
        else:
            track = await wavelink.YouTubeMusicTrack.search(query, return_first=True)

        player.queue.put(track)
        if player.is_playing():
            await inter.send(f'Added track `{track.title}` to the queue', ephemeral=True)
            logger.info(f'Added track {track.title} to the queue')
        else:
            await inter.send(f'Playing track `{track.title}`', ephemeral=True)
            await player.play(player.queue.get())

    @music.sub_command(description='Skip currently playing media')
    async def skip(self, inter: ApplicationCommandInteraction):
        player: wavelink.Player = inter.player

        await player.stop()
        await inter.send('Skipped 👍')

    @music.sub_command(description='Stop playing media and leave the channel')
    async def stop(self, inter: ApplicationCommandInteraction):
        player: wavelink.Player = inter.player

        await player.stop()
        await player.disconnect()

        await inter.send('Stopped 👍')


def setup(bot: commands.Bot):
    bot.add_cog(Music(bot))
