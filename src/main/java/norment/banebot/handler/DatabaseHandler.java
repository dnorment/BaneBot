package norment.banebot.handler;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import norment.banebot.config.ReadConfig;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler {
    public static MongoDatabase db;
    public static MongoCollection<Document> karmaCollection;
    public static MongoCollection<Document> reactionsCollection;
    public static MongoCollection<Document> scoresCollection;

    public static void init() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        connectDatabase();
    }

    public static void connectDatabase() {
        ReadConfig cfg = new ReadConfig();
        String mongoUri = cfg.properties.getProperty("mongoUri");

        MongoClientURI mongoClientURI = new MongoClientURI(mongoUri);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        db = mongoClient.getDatabase("banebot");
        karmaCollection = db.getCollection("karma");
        reactionsCollection = db.getCollection("reactions");
        scoresCollection = db.getCollection("scores");
    }
}
