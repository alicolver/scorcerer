import requests

from models import Team, Match


class Client:
    def __init__(self, base_url: str, token: str):
        self.base_url = base_url
        self.headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}",
        }

    def get_team_id(self, team_name: str) -> int:
        url = f"{self.base_url}/team/name/{team_name.lower()}"
        response = requests.get(url, headers=self.headers)
        json = self._handle_response(response)
        return int(json["teamId"])

    def post_team(self, team: Team):
        url = f"{self.base_url}/team"
        team_json = team.model_dump(by_alias=True, mode="json")
        response = requests.post(url, headers=self.headers, json=team_json)
        return self._handle_response(response)

    def post_match(self, match: Match):
        url = f"{self.base_url}/match"
        match_json = match.model_dump(by_alias=True, mode="json")
        response = requests.post(url, headers=self.headers, json=match_json)
        return self._handle_response(response)

    def _handle_response(self, response):
        if response.status_code in range(200, 300):
            return response.json()
        else:
            response.raise_for_status()
