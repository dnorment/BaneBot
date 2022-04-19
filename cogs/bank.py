import logging

import sqlite3
import settings
from disnake import ApplicationCommandInteraction, Color, Embed
from disnake.ext import commands
import yfinance as yf

logger = logging.getLogger('cogs.bank')

STARTING_BALANCE = 100


class BankDB():
    def __init__(self, file='bank.db'):
        self.file = file

    def __enter__(self):
        self.conn = sqlite3.connect(self.file)
        self.conn.row_factory = sqlite3.Row
        return self.conn.cursor()

    def __exit__(self, type, value, traceback):
        self.conn.commit()
        self.conn.close()


class Bank(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot
        self._db = sqlite3.connect('bank.db')
        with BankDB() as cur:
            cur.execute(
                '''CREATE TABLE IF NOT EXISTS BANK (
                    user_id INTEGER PRIMARY KEY,
                    balance REAL
                )''')
            cur.execute(
                '''CREATE TABLE IF NOT EXISTS HOLDINGS (
                    user_id INTEGER,
                    ticker TEXT,
                    amount REAL,
                    buy_price REAL,
                    buy_date TEXT
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

    async def _buy(self, user_id: int, ticker: str, amount: float) -> str:
        if amount <= 0:
            return 'Amount must be greater than 0'

        if ' ' in ticker:
            return 'Ticker cannot contain spaces'

        ticker = ticker.upper()
        logger.info(f'Fetching {ticker} price')
        asset = yf.Ticker(ticker)

        if not asset:
            return 'Invalid ticker'

        buy_price: float = asset.info['regularMarketPrice']
        if not buy_price:
            return 'Source is missing `regularMarketPrice` (invalid ticker?)'

        total: float = buy_price * amount
        balance = self._get_balance(user_id)

        if balance < total or not self.sub(user_id, total):
            return f'You do not have enough money ({amount} {ticker} = ${total:.2f})'

        with BankDB() as cur:
            cur.execute(
                'INSERT INTO HOLDINGS (user_id, ticker, amount, buy_price, buy_date) VALUES (?, ?, ?, ?, datetime("now"))',
                (user_id, ticker, amount, buy_price)
            )

        logger.info(
            f'{user_id} bought {amount} {ticker} for ${total:.2f}')
        return f'Bought {amount} {ticker} for ${total:.2f}'

    @bank.sub_command(description='Buy an asset')
    async def buy(self, inter: ApplicationCommandInteraction, ticker: str, amount: int):
        await inter.response.defer()
        res = await self._buy(inter.author.id, ticker, amount)
        await inter.send(res)

    @bank.sub_command(description='Sell an asset')
    async def sell(self, inter: ApplicationCommandInteraction, ticker: str, amount: int):
        await inter.send('Not implemented', ephemeral=True)

    @bank.sub_command(description='Show holdings')
    async def holdings(self, inter: ApplicationCommandInteraction):
        user_id = inter.author.id
        with BankDB() as cur:
            holdings = cur.execute(
                'SELECT ticker, amount, buy_price, buy_date FROM HOLDINGS WHERE user_id = ?', (
                    user_id,)
            ).fetchall()
        if not holdings:
            await inter.send('You have no holdings')
            return

        holding_strs = []
        for ticker, amount, buy_price, buy_date in holdings:
            holding_strs.append(
                f'`{buy_date}: BUY {int(amount):>5d} * {ticker:>10s} @ {buy_price:>4.2f} USD`')

        embed = Embed(
            title=f'{inter.author.name}\'s Portfolio',
            description='\n'.join(holding_strs),
            color=Color.green(),
        )
        await inter.send(embed=embed)
        logger.info(
            f'{inter.author.name} ({inter.author.id}) checked holdings')

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
        with BankDB() as cur:
            balance = cur.execute(
                'SELECT balance FROM BANK WHERE user_id = ?', (user_id,)
            ).fetchone()
        if not balance:
            self._insert_user(user_id)
            return STARTING_BALANCE
        return balance[0]

    def _add(self, user_id: int, amount: float):
        amount = round(amount, 2)
        self._get_balance(user_id)  # ensure user exists
        with BankDB() as cur:
            cur.execute(
                'UPDATE BANK SET balance = balance + ? WHERE user_id = ?',
                (amount, user_id)
            )

    def _insert_user(self, user_id: int):
        with BankDB() as cur:
            cur.execute(
                'INSERT INTO BANK (user_id, balance) VALUES (?, ?)',
                (user_id, STARTING_BALANCE)
            )
        logger.info(f'Inserted user {user_id}')


def setup(bot: commands.Bot):
    bot.add_cog(Bank(bot))
