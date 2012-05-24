package jmdb.spikes;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

import static java.lang.System.out;
import static jmdb.spikes.Neo4j.RelTypes.KNOWS;

public class Neo4j {

    public enum RelTypes implements RelationshipType {
        KNOWS
    }

    private static String DB_PATH = "~/tmp/hello-neo.db";

    @BeforeClass
    public static void clearDb() {
        File f = new File(DB_PATH);
        f.delete();
    }

    @Test
    public void simple_persistent_storage() throws Exception {
        long firstNodeId = createDb();

        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

        try {
            Node firstNode = graphDb.getNodeById(firstNodeId);

            Relationship relationship = firstNode.getSingleRelationship(KNOWS, Direction.OUTGOING);
            Node secondNode = relationship.getEndNode();

            out.println("I just loaded this from the data store:");
            out.println(firstNode.getProperty("message"));
            out.println(firstNode.getProperty("someJson"));
            out.println(relationship.getProperty("message"));
            out.println(secondNode.getProperty("message"));
        } finally {
            graphDb.shutdown();
        }


    }

    private long createDb() {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

        long firstNodeId = -1;

        Transaction tx = graphDb.beginTx();
        try {
            Node firstNode = graphDb.createNode();
            firstNodeId = firstNode.getId();
            firstNode.setProperty("message", "Hello, ");
            firstNode.setProperty("someJson", "{\n  \"is\" : \"vcard\",\n  \"name\" : \"Jim Barritt\"\n}");

            Node secondNode = graphDb.createNode();
            secondNode.setProperty("message", "World!");

            Relationship relationship = firstNode.createRelationshipTo(secondNode, KNOWS);
            relationship.setProperty("message", "brave Neo4j ");


            tx.success();

            return firstNodeId;
        } finally {
            tx.finish();
            graphDb.shutdown();
        }


    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
}