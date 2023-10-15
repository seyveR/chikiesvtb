import pandas as pd
from typing import Optional
from  math import radians, cos, sin, asin, sqrt
import osmnx as ox
import networkx as nx
import requests
import osmnx as ox
import openrouteservice
from  math import radians, cos, sin, asin, sqrt
from fastapi.responses import JSONResponse
import datetime
from env import openrouteservice_key, gis2

offices = pd.read_csv('office_info.csv')
 
client = openrouteservice.Client(key=openrouteservice_key)
api_key = gis2


def get_distance_and_duration_2gis(lat1, lon1, lat2, lon2):
    """
    Обращаемся к 2гис api для рассчета расстояния от точки A до B, а также получаем время пути с учетом пробок
    Мы получаем пеший и автомобильный маршруты
    """
    base_url = "https://routing.api.2gis.com/carrouting/6.0.0/global"

    params = {
        "key": api_key,
    }

    payload = {
        "points": [
            {
                "type": "walking",
                "x": lon1,
                "y": lat1
            },
            {
                "type": "walking",
                "x": lon2,
                "y": lat2
            }
        ],
        "type": "pedestrian"
    }
    payload_car = {
        "points": [
            {
                "type": "walking",
                "x": lon1,
                "y": lat1
            },
            {
                "type": "walking",
                "x": lon2,
                "y": lat2
            }
        ],
        "type": "jam"
    }

    response = requests.post(base_url, params=params, json=payload)
    response_car = requests.post(base_url, params=params, json=payload_car)

    if response.status_code == 200 and response_car.status_code == 200:
        data = response.json()
        data_car = response_car.json()
        route = data["result"][0]
        route_car = data_car["result"][0]
        distance = route["total_distance"] / 1000
        duration = route["total_duration"] / 60
        distance_car = route_car["total_distance"] / 1000
        duration_car = route_car["total_duration"] / 60
        return distance, duration, distance_car, duration_car
    else:
        print(response.status_code)
        print(response.text)
        print("Ошибка при запросе к API 2ГИС")
        return None, None, None, None


def get_distance_and_duration_osm_with_roads(lat1: float, lon1: float, lat2: float, lon2: float) -> (float, float, float, float):
    """
    Мы используем данный метод для получения ближайших точек с учетом дорог но без учета пробок, чтобы не тратить много запросов
    в бесплатном ключе от апи 2гис
    """
    start = (lon1, lat1)
    end = (lon2, lat2)

    directions_car = client.directions(coordinates=[start, end], profile='driving-car', format='geojson')
    directions = client.directions(coordinates=[start, end], profile='foot-walking', format='geojson')
    distance_m = directions['features'][0]['properties']['segments'][0]['distance']
    duration_s = directions['features'][0]['properties']['segments'][0]['duration']

    distance_car_m = directions_car['features'][0]['properties']['segments'][0]['distance']
    duration_car_s = directions_car['features'][0]['properties']['segments'][0]['duration']

    # метры -> киллометры
    # минуты -> часы
    distance = distance_m / 1000
    duration = duration_s / 60

    distance_car = distance_car_m / 1000
    duration_car = duration_car_s / 60

    return distance, duration, distance_car, duration_car


def get_N_nearest_distance_and_duration_points(radial_points: pd.DataFrame, lat1: float, lon1: float, count: Optional[int] = 5) -> list[dict]:
    """
    Данный метод обращается к openrouteservice два раза, для получения (дистанции и времени) 1.автомобиля и 2.пешехода \n
    !!! Ограничение openrouteservice 40 запросов в минуту !!!
    """
    distances = []
    
    for index, row in radial_points.iterrows():
        lat2 = row['Lat']
        lon2 = row['Lon']
        
        distance, duration, distance_car, duration_car = get_distance_and_duration_osm_with_roads(lat1, lon1, lat2, lon2)

        if distance is not None and duration is not None:
            distances.append({"index": index, "distance": distance, "duration": duration, "distance_car": distance_car, "duration_car": duration_car})

    res = radial_points.join(pd.DataFrame(distances).set_index('index'))
    return res.sort_values(by='duration').head(count).to_dict("records")


def dist(x, lat2, long2):
    """
    Calculate the great circle distance between two points 
    on the earth (specified in decimal degrees)
    """
    lat1 = x['Lat']
    long1 = x['Lon']
    # convert decimal degrees to radians
    lat1, long1, lat2, long2 = map(radians, [lat1, long1, lat2, long2])
    # haversine formula 
    dlon = long2 - long1 
    dlat = lat2 - lat1 
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a)) 
    # Radius of earth in kilometers is 6371
    km = 6371* c
    return km

def get_radial_dist(points: pd.DataFrame, lat: float, lon: float, count: Optional[int] = 5) -> pd.DataFrame:
    """
    Вычиляет радиальную дистанцию от текущей точки до всех точек из points
    Возвращает count радиально ближайших точек
    """
    points['radial_dist'] = points.apply(lambda x: dist(x, lat, lon), axis=1)
    return points.sort_values(by='radial_dist').head(count)


def get_working_points(data: pd.DataFrame, weekday: int, hour: int) -> pd.DataFrame:
    """
    Возвращает точки работающие в день недели weekday и время суток hour
    """
    return data[data[f'openHours/{weekday}/busy/{hour}'] > 0]


day_of_week = {'mon':0, 'tues':1, 'wed':2, 'thur':3, 'fri':4, 'sat':5, 'sun':6}
day_of_week_reverse = {v: k for k,v in day_of_week.items()}
time_map = {v+6:v for v in range(18)}
time_map_reverse = {v: k for k,v in time_map.items()}

def get_distance_points(data: pd.DataFrame, lat1:float, lon1: float, office: bool, venicle: bool, count: Optional[int] = 3):
    """
    Возвращает count "ближайших" точек
    Включает себя фильтрацию Работающие точки -> Радиально близкие точки -> Дорожно близкие точки 
    """
    now: datetime.datetime = datetime.datetime.now()
    weekday = now.weekday()
    hour = time_map[now.hour]

    if office:
        # Выбираем работающие сейчас отделения
        working_points = get_working_points(data, weekday, hour)
    else:
        working_points = data
    
    # Выбираем ближайшие в радиусе точки
    nearest_radial_points = get_radial_dist(working_points, lat1, lon1, count + 3) # В openrouteservice пойдет (count+3) * 2 запросов
    print(f'{len(nearest_radial_points)=}')

    # Выбираем ближайшие по дорожному расстоянию точку
    nearest_points: list[dict] = get_N_nearest_distance_and_duration_points(nearest_radial_points, lat1, lon1, count) # В 2gis пойдет count*2 запросов
    print(f'{len(nearest_points)=}')

    # Получаем из 2GIS данные о времени и дистанции до точки
    points_2gis = []
    for point in nearest_points:
        distance, duration, distance_car, duration_car = get_distance_and_duration_2gis(lat1, lon1, point['Lat'], point['Lon'])
        if office:
            points_2gis.append({'Lat': point['Lat'],
                                'Lon': point['Lon'],
                                'workload': point[f'openHours/{weekday}/busy/{hour}'],
                                'distance': distance,
                                'duration': duration,
                                'distance_car': distance_car,
                                'duration_car': duration_car})
        else:
            points_2gis.append({'Lat': point['Lat'],
                                'Lon': point['Lon'],
                                'distance': distance,
                                'duration': duration,
                                'distance_car': distance_car,
                                'duration_car': duration_car})
        
            return sorted(points_2gis, key=lambda x: x["duration"] if not venicle else x["duration_car"])
    return points_2gis


def get_query_filtered_data(data: pd.DataFrame, filters: Optional[str] = None) -> pd.DataFrame:
    """
    Возвращает отфильтрованные отделения банков
    """
    # Фильтры принимаются как один query параметр в виде:
    # filters=filter1==number,filter2=<number,filter3=>number,filter4==number
    if filters is not None:
        filters: dict = {k:{'cond': v[0], 'value': v[1:]} for k,v in [filt.split('=', maxsplit=1) for filt in filters.split(',')]}
        filtered_data = data.copy()

        # all True mask
        mask = filtered_data.iloc[:, 0] == filtered_data.iloc[:, 0]
        
        for k,v in filters.items():
            cond = v['cond']
            value = int(v['value'])
            if cond == '=':
                mask &= filtered_data.loc[:, k] == value
            elif cond == '<':
                mask &= filtered_data.loc[:, k] < value
            else:
                mask &= filtered_data.loc[:, k] > value
    else:
        return data
    return filtered_data[mask]