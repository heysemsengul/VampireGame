# VampireGame
---
A multiplayer social deduction game built in Java that use multiple libraries.
---
## Features
- 5–11 player rooms with 4-character codes
- Four roles: Vampire, Seer, Doctor, Peasant
- Phase loop: Night → Dawn → Discussion → Vote
- Day/night theme transitions
- Custom-painted UI components
- Sound effects with mute toggle
- Persistent settings across sessions
---
## Roles

- **Vampire** — kills one player each night. Wins when vampires outnumber the village.
- **Seer** — investigates one player per night, learns their team. Wins with the village.
- **Doctor** — protects one player per night. Wins with the village.
- **Peasant** — no night action. Wins with the village.
---
## Notes
- Gui and sound system is entirely made by Ai
- If you want to connect it to some kind of server u need to go to /client/GameClient.java\
and find "private static final String SERVER_HOST = "localhost";" and change it to ip of the server
