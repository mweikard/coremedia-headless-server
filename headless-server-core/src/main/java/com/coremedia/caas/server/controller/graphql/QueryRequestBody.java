package com.coremedia.caas.server.controller.graphql;

import com.google.common.base.MoreObjects;

import java.util.Map;

public class QueryRequestBody {

  private String query;
  private String operationName;
  private Map<String, Object> variables;


  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("query", query)
            .add("operationName", operationName)
            .add("variables", variables)
            .toString();
  }
}
