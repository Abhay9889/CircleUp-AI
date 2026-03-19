import json
import logging
from typing import Optional,Any
import redis
from config import get_settings
logger=logging.getLogger(__name__)
settings=get_settings()
_client: Optional[redis.Redis]=None

def get_client()->redis.Redis:
    global _client
    if _client is None:
        _client=redis.from_url(settings.redis_url,decode_responses=True)
        return _client
    
def get(key:str)-> Optional[Any]:
    try:
        val=get_client().get(key)
        return json.load(val) if val else None
    except Exception as e:
        logger.warning("Redis GET failed for %s: %s",key,e)
        return None
    
def set(key:str, value:Any,ttl_seconds:int = 3600):
    try:
        get_client().setex(key,ttl_seconds,json.dumps(value))
    except Exception as e:
        logger.warning("Redis SET failed for %s: %s",key,e)

def delete(key:str):
    try:
        get_client().delete(key)
    except Exception as e:
        logger.warning("Redis DELETE failed for %s : %s",key,e)    