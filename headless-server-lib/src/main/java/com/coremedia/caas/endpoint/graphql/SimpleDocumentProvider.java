package com.coremedia.caas.endpoint.graphql;

import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SimpleDocumentProvider implements PreparsedDocumentProvider {

  private Map<String, PreparsedDocumentEntry> preparsedQueries = new ConcurrentHashMap<>();


  @Override
  public PreparsedDocumentEntry get(String queryString, Function<String, PreparsedDocumentEntry> function) {
    return preparsedQueries.computeIfAbsent(queryString, function);
  }
}
