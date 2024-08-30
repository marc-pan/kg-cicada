package com.ibm.wdp.gs.kg;

import java.util.HashMap;
import java.util.Map;

public class Property {
    private String name = null;
    private Boolean hasLocalizedDoc = false;
    private Map<String, Property> properties = null;

    public Property(String name) {
        this.name = name;
        this.hasLocalizedDoc = false;
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
     * @return the isExist
     */
    public Boolean hasProperties() {
        return hasLocalizedDoc;
    }

    /**
     * @param isExist the isExist to set
     */
    public void setHasLocalizedDoc(Boolean hasLocalizedDoc) {
        this.hasLocalizedDoc = hasLocalizedDoc;
    }

    /**
     * @return the properties
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
        Property other = (Property) obj;
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

    public void merge(Property otherProp) {
        if (!this.name.equals(otherProp.getName())) {
            throw new IllegalArgumentException("Property names do not match");
        }

        // Merge each property in otherProp into this Property's properties
        for (String propertyName : otherProp.getProperties().keySet()) {
            Property otherChildProp = otherProp.getProperties().get(propertyName);

            if (this.properties.containsKey(propertyName)) {
                Property thisChildProp = this.properties.get(propertyName);
                thisChildProp.merge(otherChildProp);
            } else {
                this.properties.put(propertyName, otherChildProp);
            }
        }
    }

    public static void main(String[] args) {
        var prop = new Property("common");
        var prop1 = new Property("1");
        prop1.setProperties(new HashMap<>());
        Map<String, Property> pop1 = new HashMap<>();
        pop1.put("1", prop);
        Map<String, Property> pop2 = new HashMap<>();
        pop2.put("2", prop);
        prop1.getProperties().putAll(pop1);
        prop1.getProperties().putAll(pop2);
        System.out.println("The prop1 contains " + prop1.getProperties().size());
        prop1.getProperties().clear();
        var exist = prop1.getProperties().containsKey("2");
        System.out.println("The prop1 doesn't contain prop2 " + exist);
    }
}
