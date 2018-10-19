package com.coremedia.caas.endpoint.graphql;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;

import java.util.function.Function;

class SimpleDocumentProvider implements PreparsedDocumentProvider {

  private Cache<String, PreparsedDocumentEntry> cache;


  SimpleDocumentProvider() {
    this.cache = Caffeine.from("maximumWeight=100000")
                         .weigher((Weigher<String, PreparsedDocumentEntry>) (key, value) -> key.length())
                         .build();
  }


  @Override
  public PreparsedDocumentEntry get(String queryString, Function<String, PreparsedDocumentEntry> function) {
    return cache.get(queryString, function);
  }
}
