from typing import Union, Optional
from fastapi import FastAPI
from fastapi.responses import FileResponse, RedirectResponse
import pandas as pd
import json
import joblib
import numpy as np
from service import get_distance_and_duration_2gis, get_distance_and_duration_osm_with_roads, get_N_nearest_distance_and_duration_points, get_radial_dist, get_working_points, get_distance_points, get_query_filtered_data
# from service import *

app = FastAPI()

offices = pd.read_csv('office_info.csv')
terminals = pd.read_csv('terminals_info.csv')

@app.get("/")
def read_root():
    return RedirectResponse("/docs/")


@app.get("/distance")
def get_distance(lat1: float, lon1: float, count: Optional[int] = 3):
    """
    Возвращает "ближайшую" точку
    """
    return get_distance_points(offices, lat1, lon1, count)


#подгружаем веса обученной модели
model = joblib.load('model.pkl')


@app.get("/distance_model")
def distance_model(lat: float, lon: float, office: Optional[bool] = True, venicle: Optional[bool] = False, filters: Optional[str] = None, count: Optional[int] = 3) -> list[dict]:
    """
    Получаем лучшую точку на основе (время_пути_до_точки и загруженность_точки) с помощью машинного обучения
    """
    # Request pattern
    # /distance_model?lat={float}&lon={float}&car={Optional[bool]}&filters={Optional[str]}
    #  filters=servLe=={},work_with_bank_account=={},working_with_debit_credit_cards=={},credits=={},currency_cash=={},saving_account_deposits=={},money=={},bigmoney=={},insurance=={},investment_managment=={},transfers_and_payments=={},blind=={},invalid=={}
    if office:
        data = offices
        filtered_data = get_query_filtered_data(data, filters)
        points = get_distance_points(filtered_data, lat, lon, office, venicle, count)
        points = pd.DataFrame(points)
        if venicle:
            points = points.drop(columns=['distance', 'duration'])
            index_point = np.argmin(model.predict(points[['duration_car', 'workload']].values))
        else:
            points = points.drop(columns=['distance_car', 'duration_car'])
            index_point = np.argmin(model.predict(points[['duration', 'workload']].values))
    else:
        data = terminals
        filtered_data = get_query_filtered_data(data, filters)
        points = get_distance_points(filtered_data, lat, lon, office, venicle, count)[0:1]
        points = pd.DataFrame(points)
        if venicle:
            points = points.drop(columns=['distance', 'duration'])
        else:
            points = points.drop(columns=['distance_car', 'duration_car'])
        index_point = 0
    
    return json.loads(points.iloc[index_point:index_point + 1, :].to_json(orient="records", force_ascii=False))
    

@app.get("/data")
def get_data() -> list[dict]:
    """
    Возвращает данные об отделениях банков в формате JSON
    """
    # Условное обращение к базе данных
    df = pd.read_csv('office_info.csv')  
    df.fillna(value=0, inplace=True)
    data = df.to_dict(orient="records")
    return data

@app.get("/dataTerm")
def get_term() -> list[dict]:
    """
    Возвращает данные о банкоматах ВТБ в формате JSON
    """
    # Условное обращение к базе данных
    df = pd.read_csv('terminals_info.csv')  
    df.fillna(value=0, inplace=True)
    data = df.to_dict(orient="records")
    return data


@app.get("/route")
def get_route(lat1: float, lon1: float, lat2: float, lon2: float) -> dict:
    """
    /route?lat1={}&lon1={}&lat2={}&lon2={}

    """
    distance, duration, distance_car, duration_car = get_distance_and_duration_2gis(lat1, lon1, lat2, lon2)
    return {'distance': distance,
                        'duration': duration,
                        'distance_car': distance_car,
                        'duration_car': duration_car}

