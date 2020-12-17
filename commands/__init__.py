from os import listdir
from os.path import dirname, basename


# import every class inside the commands package
__all__ = [basename(f)[:-3] for f in listdir(dirname(__file__))
           if f[-3:] == ".py" and not f.endswith("__init__.py")]
