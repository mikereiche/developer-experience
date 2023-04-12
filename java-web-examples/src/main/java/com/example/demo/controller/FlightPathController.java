package com.example.demo.controller;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;

import com.couchbase.client.java.Scope;
import com.example.demo.model.IValue;
import com.example.demo.service.FlightPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Error;

@RestController
@RequestMapping("/api/flightPaths")
public class FlightPathController extends Controller {

    private final Scope scope;
    private final Bucket bucket;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightPathController.class);

    @Autowired
    public FlightPathController(Scope scope, Bucket bucket) {
        this.scope = scope;
        this.bucket = bucket;
    }

    @RequestMapping("/{from}/{to}")
    public ResponseEntity<? extends IValue> all(@PathVariable("from") String from, @PathVariable("to") String to,
                                                @RequestParam String leave) {
        try {
            Calendar calendar = Calendar.getInstance(Locale.US);
            calendar.setTime(DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse(leave));
            return ResponseEntity.ok(FlightPath.findAll(scope, bucket.name(), from, to, calendar));
        } catch (Exception e) {
            LOGGER.error("Failed with exception", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Error(e.getMessage()));
        }
    }

}
