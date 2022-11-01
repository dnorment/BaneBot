from datetime import datetime, timedelta

import disnake


def message_older_than_24h(msg: disnake.Message) -> bool:
    '''
    True if `message` is older than 24 hours.
    '''

    now_timestamp = datetime.now().timestamp()
    seconds_in_24h = timedelta(days=1).total_seconds()
    timestamp_24h_ago = now_timestamp - seconds_in_24h

    if timestamp_24h_ago >= msg.created_at.timestamp():
        return True

    return False
