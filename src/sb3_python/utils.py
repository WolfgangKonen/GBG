from datetime import datetime
import yaml
from typing import Dict, Any

def get_config() -> Dict[str, Any]:
    with open("./config.yaml", "r", encoding="utf-8") as file:
        config = yaml.safe_load(file)

    return config

def get_save_dir() -> str:
    return get_config()["gbg_save_location"]

def get_gbg_port() -> int:
    return get_config()["gbg_port"]

def get_port() -> int:
    return get_config()["port"]


DATE_FORMAT: str = "%d_%m_%Y,%H_%M_%S"

def get_date_and_time() -> str:
    now = datetime.now()
    dt_string = now.strftime(DATE_FORMAT)
    return dt_string

def get_latest_file(files: str):
    return max(files, key=lambda x: datetime.strptime(x, f"{DATE_FORMAT}.zip"))

def get_gbg_ip() -> str:
    return get_config()["gbg_ip"]

def get_prefer_device() -> str:
    return get_config()["prefer_device"]