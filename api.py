import requests


class MangaUpdatesAPI:
    BASE_URL = 'https://api.mangaupdates.com/v1'

    @classmethod
    def get_series(cls, query: str):
        url = f'{cls.BASE_URL}/series/search'
        res = requests.post(url, data={
            'search': query,
            'stype': 'title',
        }).json()

        series = res['results'][0]['record']

        id = series['series_id']
        url = f'{cls.BASE_URL}/series/{id}'
        res = requests.get(url).json()
        return res['title'], res['latest_chapter']
