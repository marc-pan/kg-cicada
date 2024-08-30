package com.ibm.wdp.gs.kg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FilteredMap {
    private static final Logger logger = LoggerFactory.getLogger(FilteredMap.class);
    private final static String DEFAULT = "default";
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static void filterLocalizedType(String locale, Map<String, Map<String, String>> localized_metadata_attributes) {
        if (localized_metadata_attributes.isEmpty()) {
            return;
        }

        List<String> languages = List.of(locale, DEFAULT);
        localized_metadata_attributes.entrySet().stream().forEach(entry -> entry.getValue().entrySet().removeIf(o -> !languages.contains(o.getKey())));
    }

    public static void main(String[] args) throws IOException {
        String data = "test/localized_data.json";
        String path = FilteredMap.class.getClassLoader().getResource(data).getPath();
        File file = new File(path);
        try {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            Map<String, Map<String, String>> localized_metadata_attributes = gson.fromJson(text, Map.class);
            filterLocalizedType("th", localized_metadata_attributes);
            logger.info(localized_metadata_attributes.toString());

            assert localized_metadata_attributes.get("data").keySet().size() == 2;
            assert localized_metadata_attributes.get("column").keySet().size() == 2;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

        Map<Integer, String> map = new HashMap<>();
        map.put(1, "value 1");
        map.put(2, "value 2");
        map.put(3, "value 3");
        map.put(4, "value 4");
        map.put(5, "value 5");

        List<Integer> nums = List.of(1, 3);
        map.keySet().removeIf(key -> !nums.contains(key));
        logger.info(map.toString());
    }
}
