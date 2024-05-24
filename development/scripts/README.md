# Scorcerer Setup Scripts

These Python scripts are used to set up teams and matches for a football tournament. The scripts interact with an API and require certain environment variables to be set for proper execution.

## Prerequisites

Before running the scripts, ensure you have Python installed on your system. You'll also need to set up a virtual environment and install the required packages using `pip`:

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Environment Variables
Edit the following values in the .env file in the project directory:

| Variable | Description |
| -------- | ------- |
| API_TOKEN | Your API token for authentication. |
| BASE_URL | The base URL of the API. |
| FLAGS_BUCKET_NAME | The name of the bucket where team flags are stored. |

## Usage
1. Create Teams

Run the `create_teams.py` script to create teams for the tournament:
```bash
python create_teams.py
```

2, Create Matches

Run the `create_matches.py` script to create teams for the tournament:
```bash
python create_matches.py
```
