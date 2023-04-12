package com.example.demo.controller;

import com.couchbase.client.java.Bucket;

import com.couchbase.client.java.Scope;
import com.example.demo.model.IValue;
import com.example.demo.model.Error;
import com.example.demo.service.Airport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/airports")
public class AirportController extends Controller {

    private final Scope scope;
    private final Bucket bucket;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportController.class);

    @Autowired
    public AirportController(Scope scope, Bucket bucket) {
        this.scope = scope;
        this.bucket = bucket;
    }

    @RequestMapping
    public ResponseEntity<? extends IValue> airports(@RequestParam("search") String search) {
        try {
            return ResponseEntity.ok(Airport.findAll(scope, bucket.name(), search));
        } catch (Exception e) {
            LOGGER.error("Failed with exception blah", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Error(e.getMessage()));
        }
    }

}
