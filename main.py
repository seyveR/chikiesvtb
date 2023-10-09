from typing import Union
from fastapi import FastAPI
from service import get_distance_and_duration_2gis

app = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}


# @app.get("/coordinates")
# def get_coordinates(lat: Union[float, None] = None, lon: Union[float, None] = None):
#     if lat is None or lon is None:
#         return {"error": "Methods does not get a coordinates"}

#     closest = get_closet_list(lat, lon, 3)
#     return {"points": closest}

@app.get("/distance")
def get_distance(lat1: float, lon1: float, lat2: float, lon2: float):
    api_key = "c6c23c9e-4015-4b75-ba9c-6a5f7bbe59d2"
    distance, duration = get_distance_and_duration_2gis(lat1, lon1, lat2, lon2, api_key)
    
    if distance is not None and duration is not None:
        return {"distance": distance, "duration": duration}
    else:
        return {"error": "Ошибка при запросе к API 2ГИС"}

