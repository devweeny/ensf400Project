import requests

API_URL = "https://api-web.nhle.com/"

response = requests.get(API_URL + "player/8478402/game-log/20232024/2", params={"Content_Type": "application.json"})
data = response.json()

print(data)