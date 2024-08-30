package com.ibm.wdp.gs.kg;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Type {
    private String name = null;
    private Map<String, Map<String, String>> localized_metadata_attributes;

    private Map<String, Property> properties = null;
    private static int count = 0;

    /**
     * @param name
     */
    public Type(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the types
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    /**
     * @return the localized_metadata_attributes
     */
    public Map<String, Map<String, String>> getLocalized_metadata_attributes() {
        return localized_metadata_attributes;
    }

    /**
     * @param localized_metadata_attributes the localized_metadata_attributes to set
     */
    public void setLocalized_metadata_attributes(Map<String, Map<String, String>> localized_metadata_attributes) {
        this.localized_metadata_attributes = localized_metadata_attributes;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    public static Type mergeTypes(Type tree1, Type tree2) {
        Map<String, Property> mergedProperties = new HashMap<>(tree1.getProperties());

        for (Map.Entry<String, Property> entry : tree2.getProperties().entrySet()) {
            String key = entry.getKey();
            Property value = entry.getValue();

            if (mergedProperties.containsKey(key)) {
                // Merge the properties if the key already exists
                mergedProperties.put(key, mergeProperties(mergedProperties.get(key), value));
            } else {
                mergedProperties.put(key, value);
            }
        }

        Type mergedType = new Type("merged");
        mergedType.setProperties(mergedProperties);
        return mergedType;
    }

    public static Type mergeTypes2(Type type1, Type type2) {
        Type mergedType = type1;

        // Merge properties from type2
        for (Map.Entry<String, Property> entry : type2.getProperties().entrySet()) {
            String propName = entry.getKey();
            Property prop = entry.getValue();

            if (mergedType.getProperties().containsKey(propName)) {
                Property mergedProp = mergeProperties(mergedType.getProperties().get(propName), prop);
                mergedProp.setHasLocalizedDoc(true);
                mergedType.getProperties().put(propName, mergedProp);
            } else {
                mergedType.getProperties().put(propName, prop);
            }
        }

        return mergedType;
    }

    public static Property mergeProperties(Property prop1, Property prop2) {
        if (prop2.getProperties() == null) return prop2;

        Map<String, Property> mergedProperties = new HashMap<>(prop1.getProperties());

        for (Map.Entry<String, Property> entry : prop2.getProperties().entrySet()) {
            String key = entry.getKey();
            Property value = entry.getValue();

            if (mergedProperties.containsKey(key)) {
                // Merge the properties if the key already exists
                mergedProperties.put(key, mergeProperties(mergedProperties.get(key), value));
                mergedProperties.get(key).setHasLocalizedDoc(true);
            } else {
                mergedProperties.put(key, value);
            }
        }

        Property mergedProp = new Property("merged");
        mergedProp.setProperties(mergedProperties);
        return mergedProp;
    }

    public void merge(Type otherType) {
        if (!this.name.equals(otherType.getName())) {
            throw new IllegalArgumentException("Type names do not match");
        }

        // Merge each property in otherType into this Type's properties
        for (String propertyName : otherType.getProperties().keySet()) {
            Property otherProp = otherType.getProperties().get(propertyName);

            if (this.properties.containsKey(propertyName)) {
                Property thisProp = this.properties.get(propertyName);
                thisProp.merge(otherProp);
            } else {
                this.properties.put(propertyName, otherProp);
            }
        }
    }

    public static void printPropertyInfo(Property prop, boolean verbose) {
        if (prop.getProperties() == null) return;

        Iterator<Map.Entry<String, Property>> itr = prop.getProperties().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            var key = entry.getKey();
            var value = entry.getValue();

            printPropertyInfo(value, verbose);
            count++;
            if (verbose)
                System.out.println("The property name: " + entry.getKey() + ",  need update doc: " + entry.getValue().hasProperties());

            if ("3".equals(key) || "7".equals(key)) {
                itr.remove();
            }
        }
    }

    public static void main(String[] args) {
        Type tree1 = new Type("new");
        Type tree2 = new Type("old");

        Property node1 = new Property("node1");
        Property node4 = new Property("node4");
        node1.setProperties((Map.of("4", node4)));

        Property node5 = new Property("nodd5");
        Property node9 = new Property("node9");
        node5.setProperties(Map.of("9", node9));

        Property node6 = new Property("node6");
        Property node10 = new Property("node10");
        node6.setProperties(Map.of("10", node10));

        Property node7 = new Property("node7");
        Property node11 = new Property("node11");
        node7.setProperties(Map.of("11", node11));

        Property node2 = new Property("node2");
        var props0 = new HashMap<String, Property>();
        props0.put("5", node5);
        props0.put("6", node6);
        node2.setProperties(props0);

        Property node_2 = new Property("node_2");
        var props1 = new HashMap<String, Property>();
        props1.put("6", node6);
        props1.put("7", node7);
        node_2.setProperties(props1);

        Property node3 = new Property("node3");
        Property node8 = new Property("node8");
        node3.setProperties(Map.of("8", node8));

        var props2 = new HashMap<String, Property>();
        props2.put("1", node1);
        props2.put("2", node2);
        tree1.setProperties(props2);

        var props3 = new HashMap<String, Property>();
        props3.put("2", node_2);
        props3.put("3", node3);
        tree2.setProperties(props3);

        Type merge = Type.mergeTypes2(tree1, tree2);
        System.out.println("The merge is done now, print out all elements in the map.");

        Iterator<Map.Entry<String, Property>> itr = merge.getProperties().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            var key = entry.getKey();
            var value = entry.getValue();

            printPropertyInfo(value, true);
            System.out.println("The property name: " + entry.getKey() + ",  need update doc: " + entry.getValue().hasProperties());
            count++;
            if ("3".equals(key) || "7".equals(key)) {
                itr.remove();
            }
        }
        System.out.println("There are " + count + " elements in the total.");
        count = 0; // reset the count to zero.

        // Can not remove an element in forEach function
        System.out.println("\n\n\nAfter remove node 3 and node 7, print out all elements in the map.");
        for (Map.Entry<String, Property> entry: merge.getProperties().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            printPropertyInfo(value, true);
            System.out.println("The property name: " + key + ", need update doc: " + value.hasProperties());
            count++;
        }
        System.out.println("There are " + count + " elements in the total.");
    }
}
