# Minecraft Event Plugin: Magische Wereld Events

Magische Wereld Events is a cinematic Minecraft plugin and event plugin for Paper servers. It starts a long location based event with animated particles, item effects, hearts, lightning, fireworks, sounds, phase titles, participation points and rewards for nearby players.

## Install

1. Use a Paper Minecraft server running version 1.21.11 or a compatible 1.21 server.
2. Make sure the server uses Java 21.
3. Build the plugin with Maven:

   ```bash
   mvn package
   ```

4. Copy the generated `target/plugin.jar` file to the server's `plugins` folder.
5. Start or restart the server.
6. The plugin registers the `event` command and gives access to players with the `grandevent.use` permission. This permission is granted to server operators by default.

## Use

1. Stand at the location where the event should appear.
2. Run:

   ```text
   /event start
   ```

3. Watch the eight phase event at that location. The event lasts approximately 190 seconds.
4. Players within 18 blocks of the event earn participation points over time and receive phase bonuses.
5. View the current event phase and remaining time with:

   ```text
   /event status
   ```

6. View your participation points with:

   ```text
   /event points
   ```

7. Stop the active event early with:

   ```text
   /event stop
   ```

The event temporarily changes the event world's time to night and clears stormy weather. The previous time and weather settings are restored when the event ends or is stopped.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/6a269716-11e9-4f7d-a09c-a31b24f3f5f4?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=6a269716-11e9-4f7d-a09c-a31b24f3f5f4
