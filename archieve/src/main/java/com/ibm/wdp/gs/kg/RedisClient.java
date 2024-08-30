package com.ibm.wdp.gs.kg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.schemafields.NumericField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;

public class RedisClient {
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private JedisPool pool;
    private List<User> defaultUserGroup;
    private static final String key = "user-session:123";

    public RedisClient() {
        init();
    }

    private void init() {
        pool = new JedisPool("localhost", 6379);
        defaultUserGroup = new ArrayList<>();
        constructDefaultData();
        // indexing();
    }

    private void constructDefaultData() {
        User user1 = new User("Paul John", "paul.john@example.com", 42, "London");
        defaultUserGroup.add(user1);
        User user2 = new User("Eden Zamir", "eden.zamir@example.com", 29, "Tel Aviv");
        defaultUserGroup.add(user2);
        User user3 = new User("Paul Zamir", "paul.zamir@example.com", 35, "Tel Aviv");
        defaultUserGroup.add(user3);
    }

    private void indexing() {
        try (Jedis jedis = this.pool.getResource()) {
            // jedis.ftCreate("idx:users",
            //                 FTCreateParams.createParams()
            //                     .on(IndexDataType.JSON)
            //                     .addPrefix("user:"),
            //                 TextField.of("$.name").as("name"),
            //                 TagField.of("$.city").as("city"),
            //                 NumericField.of("$.age").as("age")
            //         );
        }

    }

    public static void main(String[] args) {
        RedisClient client = new RedisClient();

        try (Jedis jedis = client.pool.getResource()) {
            // Store & Retrieve a simple string
            jedis.set("foo", "bar");
            logger.info(jedis.get("foo")); // prints bar

            // Store & Retrieve a HashMap
            Map<String, String> hash = new HashMap<>();
            hash.put("name", "John");
            hash.put("surname", "Smith");
            hash.put("company", "Redis");
            hash.put("age", "29");
            jedis.hset(key, hash);
            if (jedis.hexists(key, "name")) {
                // Prints: name=John, surname=Smith, company=Redis, age=29
                logger.info(jedis.hgetAll(key).toString());
            }
        }
    }
}
