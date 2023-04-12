package com.example.demo.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.couchbase.client.core.error.QueryException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;

import com.example.demo.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Airport {

    private static final Logger LOGGER = LoggerFactory.getLogger(Airport.class);

    /**
     * Find all airports.
     */
    public static Result<List<Map<String, Object>>> findAll(final Scope scope, final String bucket, String params) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT airportname FROM airport WHERE ");
        boolean sameCase = (params.equals(params.toUpperCase()) || params.equals(params.toLowerCase()));
        if (params.length() == 3 && sameCase) {
            builder.append("faa = $val");
            params = params.toUpperCase();
        } else if (params.length() == 4 && sameCase) {
            builder.append("icao = $val");
            params = params.toUpperCase();
        } else {
            // The airport name should start with the parameter value.
            builder.append("POSITION(LOWER(airportname), $val) = 0");
            params = params.toLowerCase();
        }
        String query = builder.toString();

        logQuery(query);
        QueryResult result = null;
        try {
            result = scope.query(query, QueryOptions.queryOptions().raw("$val", params));
        } catch (QueryException e) {
            LOGGER.warn("Query failed with exception: " + e);
            throw new DataRetrievalFailureException("Query error", e);
        }

        List<JsonObject> resultObjects = result.rowsAsObject();
        List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
        for (JsonObject obj : resultObjects) {
            data.add(obj.toMap());
        }

        String querytype = "N1QL query - scoped to inventory: ";
        return Result.of(data, querytype, query);
    }

    /**
     * Helper method to log the executing query.
     */
    private static void logQuery(String query) {
        LOGGER.info("Executing Query: {}", query);
    }

}
