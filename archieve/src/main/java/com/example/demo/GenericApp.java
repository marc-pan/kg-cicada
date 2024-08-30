package com.example.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericApp {
    private static final Logger logger = LoggerFactory.getLogger(GenericApp.class);

    private <T> String convert(T t, int num) {
        String ret= null;
        String className = t.getClass().getName();
        logger.info("class name is {} and the number is {}", className, num);

        if (t instanceof Long || t instanceof String || t instanceof Integer)
            return t.toString();

        return ret;
    }

    public <T> void getArtifactList(List<T> ids, int number) {
        List<String> idList = ids.stream().map(o -> convert(o, number)).collect(Collectors.toList());
        for (String id: idList) {
            logger.info(id);
        }
    }

    public static void main(String[] args) {
        List<Long> ids = Arrays.asList(11L, 20L, 30L, 40L);
        GenericApp app = new GenericApp();
        app.getArtifactList(ids, 1);

        List<String> idStrings = Arrays.asList("a", "b", "c", "d");
        app.getArtifactList(idStrings, 2);

        List<Integer> idIntegers = Arrays.asList(1, 2, 3, 4);
        app.getArtifactList(idIntegers, 3);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("1", "a");
        testMap.put("2", "b");
        testMap.put("3", "c");
        testMap.put("4", "d");
        for (Map.Entry<String, String> test: testMap.entrySet()) {
            logger.info("key: " + test.getKey() + ", value: " + test.getValue());
            logger.info("key: " + test.getKey() + ", value: " + test.getValue());
        }
    }
}
