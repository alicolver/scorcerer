import pydantic
from pydantic.alias_generators import to_camel
from datetime import datetime


class BaseModel(pydantic.BaseModel):
    model_config = pydantic.ConfigDict(
        alias_generator=to_camel,
        arbitrary_types_allowed=True,
        extra="ignore",
        frozen=True,
        populate_by_name=True,
        validate_assignment=True,
    )


class Team(BaseModel):
    team_name: str
    flag_uri: str


class Match(BaseModel):
    home_team_id: str
    away_team_id: str
    venue: str
    datetime: datetime
    match_day: int
