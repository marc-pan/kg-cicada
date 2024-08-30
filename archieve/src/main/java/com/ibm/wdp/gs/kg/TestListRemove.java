package com.ibm.wdp.gs.kg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestListRemove {
    private static final Logger logger = LoggerFactory.getLogger(TestListRemove.class);

    public static void main(String[] args) {
        // List<String> names = new ArrayList<>();
        // int total = 10;
        // for (int i=0; i<=total; i++) {
        //     names.add(String.valueOf(i));
        // }

        // Iterator<String> itr = names.iterator();
        // while (itr.hasNext()) {
        //     var name = itr.next();
        //     logger.info("Removing the name is {}, the size of the name list is {}.", name, names.size());
        //     itr.remove();
        //     logger.info("The name list size is {}.", names.size());
        // }

        // for (int i=0; i<=total; i++) {
        //     names.add(String.valueOf(i));
        // }

        // for (int i=0; i<=names.size(); i++) {
        //     logger.info("Removing the name is {}, the size of the name list is {}.", names.get(i), names.size());
        //     logger.info("The name list size is {}.", names.size());
        // }
        // names.clear();
        // logger.info("Whatever just want to know the size {} of the name list right now.", names.size());

        // Set<String> set1 = new HashSet<>(Arrays.asList("A", "B", "C"));
        // Set<String> set1 = new HashSet<>();
        // Set<String> set2 = new HashSet<>(Arrays.asList("C", "D", "E"));
        // Set<String> set2 = new HashSet<>(Arrays.asList("A", "B", "C"));
        // Set<String> set2 = new HashSet<>(Arrays.asList("C", "D"));

        // Set<String> commonSet = new HashSet<>(set1);
        // commonSet.retainAll(set2);
        // commonSet.stream().filter(o -> !"A".equals(o) || !"B".equals(o) || !"C".equals(o)).collect(Collectors.toList());

        // Set<String> newSet = new HashSet<>(set1);
        // newSet.removeAll(commonSet);

        // Set<String> deleteSet = new HashSet<>(set2);
        // deleteSet.removeAll(commonSet);

        // Map<String, Set<String>> container = new HashMap<>();
        // if (newSet.size() != 0)
        //     container.put("new", newSet);
        // if (commonSet.size() != 0)
        //     container.put("common", commonSet);
        // if (deleteSet.size() != 0)
        //     container.put("delete", deleteSet);

        // logger.info("The container size: {}, the keys is {}", container.size(), container.keySet());

        // logger.info("The set1 size: {}, the set2 size: {}, the common set size: {}, the new set size: {}, the existing set size: {}", set1.size(), set2.size(), commonSet.size(), newSet.size(), deleteSet.size());

        List<String> list1 = new ArrayList(Arrays.asList("A", "B", "C"));
        var constA = "E";
        var size = list1.size();
        for (var i = 0; i < size; i++) {
            logger.info("Current element {}", list1.get(i));
            logger.info("Before replace {}", list1.toString());
            list1.add("D");
            var index = list1.indexOf("A");
            if (index != -1) {
                list1.set(index, constA);
            }
            logger.info("After add {}", list1.toString());
        }
        // List<String> list1 = new ArrayList<>();
        // List<String> list2 = new ArrayList<>();
        // list2.add("C");
        // list2.add("D");
        // list2.removeAll(list1);
        // logger.info(list2.toString());
    }
}
