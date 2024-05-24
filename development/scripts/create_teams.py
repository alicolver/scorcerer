import json
from os import environ
from client import Client
from models import Team
from dotenv import load_dotenv


if __name__ == "__main__":
    load_dotenv()
    client = Client(base_url=environ.get("BASE_URL"), token=environ.get("API_TOKEN"))
    with open("teams.json", "r") as file:
        data = json.load(file)
        for team_name in data:
            print(f"Posting team: {team_name}")
            client.post_team(Team(team_name=team_name, flag_uri=f"s3://{environ.get('FLAGS_BUCKET_NAME')}/{team_name.casefold()}.svg"))
