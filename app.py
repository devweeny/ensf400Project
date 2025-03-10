from flask import Flask
import test

app = Flask(__name__)

@app.route('/')
def root():
    return "<a href='/teams'>Team List</a>"

@app.route('/teams')
def teams():
    out = "<ul>"
    for team in test.get_all_teams()["data"]:
        if test.get_team_roster(team["triCode"]) is not None:
          out += f"<li><a href='/teams/{team['triCode']}'>{team['fullName']}</a></li>"
    out += "</ul>"
    return out
        

def pretty_print_team(team: dict):
    positions = ["defensemen", "forwards", "goalies"]
    out = "<div><a href='/teams'>Back to team list</a>"
    for position in positions:
        for player in team[position]:
            # print(f"{player['firstName']['default']} {player['lastName']['default']} - {player['sweaterNumber']}")
            out += f"<p>{player['firstName']['default']} {player['lastName']['default']} - {player['sweaterNumber']}</p> <img src='{player['headshot']}' alt='headshot' style='width:200px;height:200px;'>"
    out += "</div>"
    return out

@app.route('/teams/<team>')
def team_roster(team):
    if test.get_team_roster(team) is None:
        return "<p>This team does not exist, go back to the <a href='/teams'>team list</a></p>"
    return pretty_print_team(test.get_team_roster(team))

if __name__ == "__main__":
    app.run(debug=True)

