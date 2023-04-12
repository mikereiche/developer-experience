package com.example.demo.service;

import com.couchbase.client.core.error.QueryException;

public class DataRetrievalFailureException extends RuntimeException {
  public DataRetrievalFailureException(String query_error, QueryException e) {
  }
  public DataRetrievalFailureException(String query_error) {
  }
}
