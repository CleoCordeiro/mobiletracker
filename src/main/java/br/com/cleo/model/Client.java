package br.com.cleo.model;

import java.io.Serializable;
import java.util.List;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

@MongoEntity(collection = "client")
public class Client extends ReactivePanacheMongoEntity implements Serializable {

    public String name;

    public List<Location> locations;

    public void addLocations(List<Location> locations2) {
        locations2.forEach(location -> {
            this.locations.add(location);
        });
    }

}
