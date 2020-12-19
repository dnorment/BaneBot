class BaneBotError(Exception):
    """Basic exception for any error in Bane"""


class ArgumentNumberError(BaneBotError):
    """Wrong number of arguments"""
