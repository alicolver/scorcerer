import json
from os import environ
from client import Client
from models import Match
from dotenv import load_dotenv


if __name__ == "__main__":
    load_dotenv()
    client = Client(base_url=environ["BASE_URL"], token=environ["API_TOKEN"])
    with open("matches.json", "r") as file:
        data = json.load(file)
        for match in data:
            home_team_id = client.get_team_id(match["homeTeam"])
            away_team_id = client.get_team_id(match["awayTeam"])
            print(f"Posting match: {match['homeTeam']} vs {match['awayTeam']}")
            client.post_match(Match.model_validate(match | {"homeTeamId": home_team_id, "awayTeamId": away_team_id}))
