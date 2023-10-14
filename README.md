[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Naereen/StrapDown.js/graphs/commit-activity)\
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

<img src="https://raw.githubusercontent.com/ElectroBrine/QuillNotifications/master/src/main/resources/icon.png" width="300" height="300" alt="">

# Quill Notifications
A small Library mod to handle sending notifications to players both online and offline with style!

# Usage
This mod requires both [Fabric API](https://modrinth.com/mod/fabric-api), [SQLib](https://modrinth.com/mod/sqlib), and [Micro Config](https://github.com/SilverAndro/Microconfig).

Use of [Adventure API](https://docs.advntr.dev/index.html) is not required but encouraged.

Once the mod is installed on your server, a config will be generated after the first launch. Be sure to edit this config and point it to a file path or a my sql database.

# For Developers

### Getting Started
To include this mod in your project simply add it as a dependency:
``` gradle
repositories {
    maven { url "https://api.modrinth.com/maven" }
    // adventure api is not strictly necessary but is helpful and will allow the use of the event system
    maven {
        name = "sonatype-oss-snapshots1"
        url = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    modImplementation("maven.modrinth:quill:1.0.3")
    // adventure api is not strictly necessary but is helpful and will allow the use of the event system
    modImplementation include("net.kyori:adventure-platform-fabric:5.10.0")
}
```

### General Usage
``` java
Pigeon.send(playerUUID, "Hello World!", Scribe.INFO);              // Send a notification with basic formatting
Pigeon.send(playerUUID, mutableText);                              // Send a notification with a custom mutable text
Pigeon.send(playerUUID, mutableText, jsonData);                     // Send a notification with custom metadata that can be used in the event system;
Pigeon.send(playerUUID, mutableText, SoundEvents.BLOCK_BELL_USE);  // Send a notification with a custom mutable text and play a sound
Pigeon.send(playerUUID, adventureAPITextComponent); // Send a notification using Adventure API's special formatting
```

### Event System
``` java
/*
 'message' is a object in Pigeon that holds the 
 UUID of the receiver, 
 MutableText of the message, 
 Component of the message, 
 JsonElement of the metadata,
 and SoundEvent of sound. 
 This allows you to edit everything (but the uuid) 
 before the message actually sends to the users
*/
QuillEvents.PRE_SEND_NOTIFICATION.register((message) -> {
  System.out.println(receiver);
  //returning true allows the message to be sent, returning false will stop the
  //returning false will stop the message from being seen
  return true;
  // Do cool things with metadata or the other varibles idk the event system is your oyster.
});
```