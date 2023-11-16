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
    // adventure api is not strictly necessary but is helpful and will allow you to use Component messages
    maven {
        name = "sonatype-oss-snapshots1"
        url = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    modImplementation("maven.modrinth:quill:1.1.2")
    // adventure api is not strictly necessary but is helpful and allow you to use Component messages
    modImplementation include("net.kyori:adventure-platform-fabric:5.10.0")
}
```

### General Usage
``` java
Notification notification = NotificationBuilder.Notification(receiverUUID) // Initalize a new notification to be sent
    .setMessage(message) // setMessage() accepts String, MutableText, or Component variables (note that the notification will only save the last message set)
    .setStyle(Scribe.INFO) // setStyle() only works for String messages
    .setMetadata(jsonData) // inject json data into a message to be used with the event system
    .setSound(SoundEvents.BLOCK_BELL_USE) // set a soundevent to be played when notification is received
    .setCommands(commandString, commandString2) // set commands to be run when the notification is received
    .setCommandDelay(10, TimeUnit.SECONDS) // set a delay that your commands will delayed for after the notification is sent (you can also pass in just a number for the ammount of millies to delay by)
    .build();
Pigeon.send(notification); // send the notification to the player
```

### Event System
``` java
//the event system gives you a notification object to modify the notification data before it gets sent
QuillEvents.PRE_SEND_NOTIFICATION.register((notification) -> {
  System.out.println(notification.getPlayerEntity().getName().getString());
  //returning true allows the message to be sent, returning false will stop the
  //returning false will stop the message from being seen
  return true;
  // Do cool things with metadata or the other varibles idk the event system is your oyster.
});
```