import os
from typing import Set


def _get_cog_names():
    '''Get a list of all cogs to be loaded on startup. Cogs must be defined
    in `./cogs` or in a subfolder's `cog.py` file.'''

    cog_names: Set[str] = set()

    files = os.listdir('./cogs')
    for f in files:
        if f in ['__init__.py', '__pycache__']:
            continue

        cog_names.add(f[:-3] if f.endswith('.py') else f'{f}.cog')

    return cog_names

cog_names = _get_cog_names()
