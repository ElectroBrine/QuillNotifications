package me.electrobrine.quill_notifications;

import mc.microconfig.Comment;
import mc.microconfig.ConfigData;

public class Config implements ConfigData {
    public String databaseName = "QuillNotificationData";
    @Comment("supported database, SQLite and MYSQl")
    public String databaseType = "SQlite";
    @Comment("Only for SQlite")
    public String databaseDirectory = "/path/to/folder";
    @Comment("Only for MYSQL")
    public String databaseIP = "192.168.1.1";
    public String databasePort = "1111";
    public String databaseUser = "Quillium";
    public String databasePassword = "Kugu";
}
