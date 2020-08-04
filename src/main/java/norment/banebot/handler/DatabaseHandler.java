package norment.banebot.handler;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import norment.banebot.config.ReadConfig;
import org.bson.Document;

public class DatabaseHandler {
    public static MongoDatabase db;
    public static MongoCollection<Document> karmaCollection;
    public static MongoCollection<Document> scoresCollection;

    public static void init() {
        connectDatabase();
    }

    public static void connectDatabase() {
        ReadConfig cfg = new ReadConfig();
        String mongoUri = cfg.properties.getProperty("mongoUri");

        MongoClientURI mongoClientURI = new MongoClientURI(mongoUri);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        db = mongoClient.getDatabase("banebot");
        scoresCollection = db.getCollection("scores");
        karmaCollection = db.getCollection("karma");
    }
}
