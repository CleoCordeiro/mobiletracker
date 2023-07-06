package br.com.cleo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

@MongoEntity(collection = "client")
public class Client extends ReactivePanacheMongoEntity implements Serializable {

    public String name;

    public Location lastLocation;

    public List<Location> locations = new ArrayList<>();

    public void addLocations(Location locations2) {
        locations.add(locations2);
    }

}
