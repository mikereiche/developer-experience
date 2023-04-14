package com.example.demo.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.couchbase.client.core.error.QueryException;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;

import com.example.demo.controller.Controller;
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
        builder.append("SELECT airportname FROM airport ");
        boolean sameCase = (params.equals(params.toUpperCase()) || params.equals(params.toLowerCase()));
        if (params.length() == 3 && sameCase) {
            builder.append(" WHERE faa = $val");
            params = params.toUpperCase();
        } else if (params.length() == 4 && sameCase) {
            builder.append("WHERE icao = $val");
            params = params.toUpperCase();
        } else if(!params.equals("*")){
            // The airport name should start with the parameter value.
            builder.append("WHERE POSITION(LOWER(airportname), $val) = 0");
            params = params.toLowerCase();
        }
        builder.append(" LIMIT 20");
        String query = builder.toString();

        logQuery(query);
        long t0=System.currentTimeMillis();
        QueryResult result = null;
        try {
            result = scope.query(query, QueryOptions.queryOptions().raw("$val", params).metrics(true));
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
        return Result.of(Controller.GO_BACK, data, builder+" "+params,
          "\n execution : "+result.metaData().metrics().get().executionTime().toMillis()+
            "\n elapsed   : "+result.metaData().metrics().get().elapsedTime().toMillis()+
          "\n client    : "+(System.currentTimeMillis()-t0));

    }

    /**
     * Helper method to log the executing query.
     */
    private static void logQuery(String query) {
        LOGGER.info("Executing Query: {}", query);
    }

}
