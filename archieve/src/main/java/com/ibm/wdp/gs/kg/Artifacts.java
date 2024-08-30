package com.ibm.wdp.gs.kg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Artifacts {
    private List<Entity> entities;
    private String id;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the entities
     */
    public List<Entity> getEntities() {
        return entities;
    }
    /**
     * @param entities the entities to set
     */
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public void put(Entity entity) {
        if (getEntities() == null) {
            setEntities(new ArrayList<>(Arrays.asList(entity)));
        }
        else
            getEntities().add(entity);
    }

    public void append(List<Entity> entities) {
        Set<Entity> entitySet = new HashSet<>(this.entities);
        for (Entity entity: entities) {
            entitySet.add(entity);
        }
        this.entities.addAll(entitySet);
    }
}
