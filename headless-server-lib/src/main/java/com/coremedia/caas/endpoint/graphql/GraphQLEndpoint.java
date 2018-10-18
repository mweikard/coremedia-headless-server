package com.coremedia.caas.endpoint.graphql;

import com.coremedia.caas.endpoint.Endpoint;
import com.coremedia.caas.schema.SchemaService;

import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.List;

import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;

public class GraphQLEndpoint implements Endpoint {

  private boolean isEnabled;

  private GraphQLSchema querySchema;
  private SchemaService schemaService;

  private SimpleDocumentProvider documentProvider = new SimpleDocumentProvider();


  public GraphQLEndpoint(SchemaService schemaService) {
    this.schemaService = schemaService;
  }


  @Override
  public boolean isEnabled() {
    return isEnabled;
  }


  public PreparsedDocumentProvider getDocumentProvider() {
    return documentProvider;
  }

  public GraphQLSchema getQuerySchema() {
    return querySchema;
  }


  /*
   * YAML setters
   */

  @SuppressWarnings("unused")
  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  @SuppressWarnings("unused")
  public void setQueryFieldBuilders(List<QueryFieldBuilder> queryFieldBuilders) {
    GraphQLObjectType.Builder typeBuilder = newObject().name("CaaSQuery");
    // add query fields
    for (QueryFieldBuilder fieldBuilder : queryFieldBuilders) {
      typeBuilder.fields(fieldBuilder.build());
    }
    this.querySchema = newSchema().query(typeBuilder.build()).build(schemaService.getTypes(), schemaService.getDirectives());
  }
}
