# iEncourager
This plugin Encourage the players to play in a closer area of the spawn by

1) launching an event (you can turn the schedule off with command in server console) every 2 hours (not configurable) and giving money to the players that are close; the closer they are, the more money they get. every day at 7:00PM (server time) will launch an special event with more money (configurable) and every Saturday with even more (configurable)

2) Increases the hunger (you can turn the hunger off with command in server console) on a factor (configurable) when far from the spawn; The farther you go the more hungrier you get (faster).

Everything except the time of the events is configurable but we can make it customizable as well if you ask for it.

# How to
It needs to be hooked onto another economic system, you can edit this by just edditing the command that will be executed on the config.yml file.
This economic system will be the one that rewards the player and iEncourager will only call the given command adding the name of the player and the ammount for the reward at the end.
