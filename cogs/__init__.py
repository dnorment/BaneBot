from os import listdir
from os.path import basename, dirname

# list all names of cogs
cog_names: list[str] = []
for f in listdir(dirname(__file__), ):
    is_file = '.' in basename(f)
    is_top_level = is_file and basename(f).count('.') == 1
    is_py_file = is_top_level and f.endswith('.py')
    is_init_py = f.endswith('__init__.py')

    if is_py_file and not is_init_py:
        cog_name = basename(f)[:-3]
        cog_names.append(cog_name)
