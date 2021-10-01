from os import listdir
from os.path import basename, dirname

# list all names of cogs
cog_names = [basename(f)[:-3] for f in listdir(dirname(__file__))
             if f[-3:] == ".py" and not f.endswith("__init__.py")]
