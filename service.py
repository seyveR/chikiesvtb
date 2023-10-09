import pandas as pd
from typing import Optional
from  math import radians, cos, sin, asin, sqrt
import osmnx as ox
import networkx as nx
import requests

coord = pd.read_csv('office.csv')

def get_distance_and_duration_2gis(lat1, lon1, lat2, lon2, api_key):
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

    response = requests.post(base_url, params=params, json=payload)

    if response.status_code == 200:
        data = response.json()
        route = data["result"][0]
        distance = route["total_distance"] / 1000
        duration = route["total_duration"] / 60
        return distance, duration
    else:
        print("Ошибка при запросе к API 2ГИС")
        return None, None
