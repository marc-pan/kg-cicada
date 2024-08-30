package com.ibm.wdp.gs.kg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SmallTest {
    private static final String CUSTOM_ATTRIBUTE_NAME = "custom_attributes.attribute_name";

    private enum support_types { AAA, BBB, CCC }

    public static void main(String[] args) {
        var opt = Optional.empty();
        var strs = String.format("%s_%s_%s", "abc", opt.orElse(null), "123");
        log.info("1st: " + strs);
        opt = Optional.of("AAA");
        strs = String.format("%s_%s_%s", "abc", opt.orElse(null), "123");
        log.info("2nd: " + strs);

        var set1 = new HashSet<>();
        set1.add(1);
        set1.add(2);
        set1.add(3);
        var set2 = new HashSet<>();
        set2.add(2);
        set2.add(3);
        set2.add(4);

        set1.retainAll(set2);
        log.info(set1.toString());
        set2.removeAll(set1);
        log.info(set2.toString());

        var locales = new ArrayList<String>();
        locales.add("");
        locales.add("zh");

        assert false: "assert always false.";
        if (locales.isEmpty()) {
            throw new AssertionError("assert always false.");
        }

        for (String locale: locales) {
            if (locale == null || locale.length() == 0 || locale.trim().length() == 0) {
                log.info("empty string");
            }
        }
        var customAttributes = CUSTOM_ATTRIBUTE_NAME.split("[.]");
        log.info(customAttributes[0]);

        String message = "Error executing POST http://localhost:8089/retryable_path/throw_global_exception.  \n"
                            + "Caused by Maximum number of attempts reached attempting to call POST http://localhost:8089/retryable_path/throw_global_exception.  \n"
                            + "Aborting..  Details: POST http://localhost:8089/retryable_path/throw_global_exception\n"
                            + "Headers     \n"
                            + "Params      \n"
                            + "Form Params \n"
                            + "Body        {\"user_access_token\":\"xxxxxxxx\"}.";
        String expected = "{\"user_access_token\":\"xxxxxxxx\"}";
        boolean matched = message.contains(expected);
        if (matched) {
            log.info("The message is matched.");
        } else {
            log.info("Didn't match.");
        }

        Set<String> types = Arrays.stream(support_types.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> newTypes = new HashSet<>();
        boolean is = types.containsAll(newTypes);
        String msg = String.format("the new type does exist %b in immutabale types.", is);
        log.info(msg);
    }
}
