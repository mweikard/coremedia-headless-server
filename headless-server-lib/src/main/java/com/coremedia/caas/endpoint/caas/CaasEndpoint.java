package com.coremedia.caas.endpoint.caas;

import com.coremedia.caas.endpoint.Endpoint;

import graphql.execution.preparsed.PreparsedDocumentProvider;

public class CaasEndpoint implements Endpoint {

  private boolean isEnabled;

  private QueryRegistry queryRegistry;


  public CaasEndpoint(QueryRegistry queryRegistry) {
    this.queryRegistry = queryRegistry;
  }


  @Override
  public boolean isEnabled() {
    return isEnabled;
  }


  public QueryRegistry getQueryRegistry() {
    return queryRegistry;
  }

  public PreparsedDocumentProvider getDocumentProvider() {
    return queryRegistry;
  }


  /*
   * YAML setters
   */

  @SuppressWarnings("unused")
  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }
}
