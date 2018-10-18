package com.coremedia.caas.endpoint.graphql;

import graphql.schema.GraphQLFieldDefinition;

import java.util.List;

public interface QueryFieldBuilder {

  List<GraphQLFieldDefinition> build();
}
