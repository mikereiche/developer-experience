package com.example.demo.service;

import static com.couchbase.client.java.kv.LookupInSpec.get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.LookupInResult;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.search.SearchOptions;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.queries.ConjunctionQuery;
import com.couchbase.client.java.search.result.SearchResult;
import com.couchbase.client.java.search.result.SearchRow;

import com.example.demo.controller.Controller;
import com.example.demo.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Hotel {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hotel.class);

    private Bucket bucket;
    private Cluster cluster;

    @Autowired
    public Hotel(Cluster cluster, Bucket bucket) {
        this.cluster = cluster;
        this.bucket = bucket;
    }

    /**
     * Search for a hotel in a particular location.
     */
    public Result<List<Map<String, Object>>> findHotels(final String description,
                                                        final String location) {

        Scope scope = bucket.scope("samples");
        StringBuffer queryString = new StringBuffer("SELECT name, address, city, country, state, description from hotel");
        QueryOptions queryOptions = QueryOptions.queryOptions().metrics(true);
        JsonObject parameters = JsonObject.create();
        if( !empty(location) ) {
            queryString.append(" WHERE ");
            queryString.append("(contains(country, $location) or contains(state, $location) or contains(city, $location))");
            parameters.put("$location",location);
        }
        if( !empty(description) ) {
            queryString.append(empty(location) ? " WHERE " : " AND ");
            queryString.append("contains(description, $description)");
            parameters.put("$description",description);
        }
        queryOptions.parameters(parameters);
        queryString.append(" limit 20");
        QueryResult result = scope.query(queryString.toString(), queryOptions);

        List<JsonObject> resultObjects = result.rowsAsObject();
        List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
        for (JsonObject obj : resultObjects) {
            data.add(obj.toMap());
        }

        String querytype = "N1QL query - scoped to inventory: ";

        if(1==1)
            return Result.of(data, queryString+" "+parameters.toMap(),
              "\n execution : "+result.metaData().metrics().get().executionTime().toMillis()+
              "\n elapsed   : "+result.metaData().metrics().get().elapsedTime().toMillis());


        ConjunctionQuery fts = SearchQuery.conjuncts(SearchQuery.term("hotel").field("type"));

        if (location != null && !location.isEmpty() && !"*".equals(location)) {
            fts.and(SearchQuery.disjuncts(
                    SearchQuery.matchPhrase(location).field("country"),
                    SearchQuery.matchPhrase(location).field("city"),
                    SearchQuery.matchPhrase(location).field("state"),
                    SearchQuery.matchPhrase(location).field("address")
            ));
        }

        if (description != null && !description.isEmpty() && !"*".equals(description)) {
            fts.and(SearchQuery.disjuncts(
                    SearchQuery.matchPhrase(description).field("description"),
                    SearchQuery.matchPhrase(description).field("name")
            ));
        }

        logQuery(fts.export().toString());
        SearchOptions opts = SearchOptions.searchOptions().limit(100);
        SearchResult searchResult = cluster.searchQuery("travel-sample."+scope.name()+".hotels-index", fts, opts);

        String queryType = "FTS search - scoped to: inventory.hotel within fields country, city, state, address, name, description";
        return Result.of(extractResultOrThrow(searchResult), queryType);
    }


    public Result<List<Map<String, Object>>> findHotelById(String id) {
        Collection collection = bucket.scope("samples").collection("hotel");
        GetResult result = collection.get(id, GetOptions.getOptions());
        JsonObject resultObject = result.contentAsObject();
        List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
        data.add(resultObject.toMap());
        return Result.of(data, "get"+" "+id);
    }

    /**
     * Search for an hotel.
     */
    public Result<List<Map<String, Object>>> findHotels(final String description) {
        return findHotels( description, "*");
    }

    /**
     * Find all hotels.
     */
    public Result<List<Map<String, Object>>> findAllHotels() {
        return findHotels("*", "*");
    }

    /**
     * Extract a FTS result or throw if there is an issue.
     */
    private List<Map<String, Object>> extractResultOrThrow(SearchResult result) {
        if (result.metaData().metrics().errorPartitionCount() > 0) {
            LOGGER.warn("Query returned with errors: " + result.metaData().errors());
            throw new RuntimeException("Query error: " + result.metaData().errors());
        }

        List<Map<String, Object>> content = new ArrayList<Map<String, Object>>();
        for (SearchRow row : result.rows()) {

            LookupInResult res;
            try {
                Scope scope = bucket.scope("inventory");
                Collection collection = scope.collection("hotel");
                res = collection.lookupIn(row.id(),
                        Arrays.asList(get("country"), get("city"), get("state"),
                        get("address"), get("name"), get("description")));
            } catch (DocumentNotFoundException ex) {
                continue;
            }

            Map<String, Object> map = new HashMap<String, Object>();

            String country = res.contentAs(0, String.class);
            String city = res.contentAs(1, String.class);
            String state = res.contentAs(2, String.class);
            String address = res.contentAs(3, String.class);

            StringBuilder fullAddr = new StringBuilder();
            if (address != null)
                fullAddr.append(address).append(", ");
            if (city != null)
                fullAddr.append(city).append(", ");
            if (state != null)
                fullAddr.append(state).append(", ");
            if (country != null)
                fullAddr.append(country);

            if (fullAddr.length() > 2 && fullAddr.charAt(fullAddr.length() - 2) == ',')
                fullAddr.delete(fullAddr.length() - 2, fullAddr.length() - 1);

            map.put("name", res.contentAs(4, String.class));
            map.put("description", res.contentAs(5, String.class));
            map.put("address", fullAddr.toString());

            content.add(map);
        }
        return content;
    }

    /**
     * Helper method to log the executing query.
     */
    private static void logQuery(String query) {
        LOGGER.info("Executing FTS Query: {}", query);
    }

    private boolean empty(String s){
        return s == null || s.length() == 0 || s.equals("*");
    }

}
