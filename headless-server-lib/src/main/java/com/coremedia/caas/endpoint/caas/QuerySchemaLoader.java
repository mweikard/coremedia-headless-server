package com.coremedia.caas.endpoint.caas;

import graphql.schema.GraphQLSchema;

interface QuerySchemaLoader {

  GraphQLSchema load(Object target);
}
