import logging

import sqlite3
import settings
from disnake import ApplicationCommandInteraction
from disnake.ext import commands
import yfinance as yf

logger = logging.getLogger('cogs.bank')


class Bank(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self._db = sqlite3.connect('bank.db')
        with self._db as conn:
            conn.execute(
                '''CREATE TABLE IF NOT EXISTS BANK (
                    user_id INTEGER PRIMARY KEY,
                    balance REAL
                )''')
            conn.execute(
                '''CREATE TABLE IF NOT EXISTS HOLDINGS (
                    user_id INTEGER,
                    ticker TEXT,
                    amount REAL,
                    buy_price REAL
                )''')
        logger.info('Initialized cog')

    @commands.slash_command(guild_ids=settings.GUILD_IDS)
    async def bank(self, inter: ApplicationCommandInteraction):
        pass

    @bank.sub_command(description='Show balance')
    async def balance(self, inter: ApplicationCommandInteraction):
        balance = self._get_balance(inter.author.id)
        await inter.send(f'Your balance is ${balance:.2f}')
        logger.info(
            f'{inter.author.name} ({inter.author.id}) checked balance')

    @bank.sub_command(description='Buy an asset')
    async def buy(self, inter: ApplicationCommandInteraction, ticker: str, amount: int):
        await inter.response.defer()

        if amount <= 0:
            await inter.send('Amount must be greater than 0')
            return

        if ' ' in ticker:
            await inter.send('Ticker cannot contain spaces')
            return

        ticker = ticker.upper()
        logger.info(f'Fetching {ticker} price')
        asset = yf.Ticker(ticker)

        if not asset:
            await inter.send('Invalid ticker')
            return

        price = asset.info['regularMarketPrice']
        if not price:
            await inter.send('Source is missing data (invalid ticker?)')
            return

        price = round(price, 2)
        total: float = round(price * amount, 2)
        user_id = inter.author.id
        balance = self._get_balance(user_id)

        if balance < total or not self.sub(user_id, total):
            await inter.send(f'You do not have enough money (${price:.2f} * {amount} = ${total:.2f})')
            return

        with self._db as conn:
            conn.execute(
                'INSERT INTO HOLDINGS (user_id, ticker, amount, buy_price) VALUES (?, ?, ?, ?)',
                (user_id, ticker, amount, price)
            )

        await inter.send(f'Bought {amount} {ticker} for ${total:.2f} (${price:.2f} ea)')
        logger.info(
            f'{user_id} bought {amount} {ticker} for ${total:.2f}')

    @bank.sub_command(description='Sell an asset')
    async def sell(self, inter: ApplicationCommandInteraction, ticker: str, amount: int):
        await inter.send('Not implemented', ephemeral=True)

    def get_balance(self, user_id: int) -> float:
        return self._get_balance(user_id)

    def add(self, user_id: int, amount: float) -> bool:
        self._add(user_id, amount)
        logger.info(f'Added ${amount:.2f} to account {user_id}')
        return True

    def sub(self, user_id: int, amount: float) -> bool:
        if self._get_balance(user_id) < amount:
            logger.info(
                f'User {user_id} does not have enough money, cancelled sub')
            return False

        self._add(user_id, -amount)
        logger.info(f'Subtracted ${amount:.2f} from account {user_id}')
        return True

    def _get_balance(self, user_id: int) -> float:
        balance = self._db.execute(
            'SELECT balance FROM BANK WHERE user_id = ?', (user_id,)
        ).fetchone()
        if not balance:
            self._insert_user(user_id)
            return 0
        return balance[0]

    def _add(self, user_id: int, amount: float):
        amount = round(amount, 2)
        self._get_balance(user_id)  # ensure user exists
        with self._db as conn:
            conn.execute(
                'UPDATE BANK SET balance = balance + ? WHERE user_id = ?',
                (amount, user_id)
            )

    def _insert_user(self, user_id: int):
        with self._db as conn:
            conn.execute(
                'INSERT INTO BANK (user_id, balance) VALUES (?, ?)',
                (user_id, 0)
            )
        logger.info(f'Inserted user {user_id}')

    def cog_unload(self):
        self._db.close()
        logger.info('Closed connection')


def setup(bot: commands.Bot):
    bot.add_cog(Bank(bot))
