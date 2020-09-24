# iEncourager
This plugin Encourage the players to play closer to the area of the spawn by

1) Launching an event (you can turn the schedule off with a command at the server console) every 2 hours (not configurable yet) and giving money to the players that are close ; the closer they are, the more money (amount) they get. every day at 23:00 UTC will launch a special event with more money (configurable) and every Saturday with even more (configurable)

2) Increases the hunger (you can turn the hunger off with command in server console) on a factor (configurable) when far from the spawn; The farther you go the hungrier you get (faster).

Everything except the time of the events is configurable but we can make it customizable as well if you ask for how will you want it to be configured.

# How to
It needs to be hooked onto another economic system, you can edit this by just editing the command that will be executed on the config.yml file.
This economic system will be the one that rewards the player (by default MobHunter and Essentials) and iEncourager will only call the given command adding the name of the player and the amount for the reward at the end.
