package com.example.demo.controller;
import com.couchbase.client.java.Cluster;

import com.example.demo.model.Result;
import com.example.demo.service.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/hotels")
// jsonview - https://chrome.google.com/webstore/detail/jsonvue/chklaanhfefbnpoihckbnefhakgolnmc
public class HotelController extends Controller {

  private final HotelService hotelService;

  private static final Logger LOGGER = LoggerFactory.getLogger(HotelController.class);
  private static final String LOG_FAILURE_MESSAGE = "Failed with exception";

  @Autowired
  public HotelController(HotelService hotelService) {
    this.hotelService = hotelService;
  }

  @RequestMapping(value = "/{description}/{location}/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<? extends Object> findHotelsByDescriptionAndLocation(
     @PathVariable("description") String desc, @PathVariable("location") String location) {
    try {
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(hotelService.findHotels(desc, location));
    } catch (Exception e) {
      LOGGER.error(LOG_FAILURE_MESSAGE, e);
      return ResponseEntity.badRequest().body(new Error(e.getMessage()));
    }
  }

  @RequestMapping(value = "/{description}/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<? extends Object> findHotelsByDescription(@PathVariable("description") String desc) {
    try {
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(hotelService.findHotels(desc));
    } catch (Exception e) {
      LOGGER.error(LOG_FAILURE_MESSAGE, e);
      return ResponseEntity.badRequest().body(new Error(e.getMessage()));
    }
  }

  @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<? extends Object> findAllHotels() {
    try {
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(hotelService.findAllHotels());
    } catch (Exception e) {
      LOGGER.error(LOG_FAILURE_MESSAGE, e);
      return ResponseEntity.badRequest().body(new Error(e.getMessage()));
    }
  }


}
