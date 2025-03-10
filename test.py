import requests
import json

API_URL = "https://api-web.nhle.com/v1/"

response = requests.get(API_URL + "player/8478402/game-log/20232024/2", params={"Content_Type": "application.json"})
data = response.json()

# Save the response to a JSON file
with open("player_data.json", "w") as json_file:
    json.dump(data, json_file, indent=4)

print("Data saved to player_data.json")
print(data)