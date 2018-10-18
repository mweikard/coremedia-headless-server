package com.coremedia.caas.endpoint.caas;

import com.coremedia.caas.schema.InvalidQueryDefinition;
import com.coremedia.caas.schema.SchemaService;
import com.coremedia.caas.schema.Types;

import com.google.common.base.Ascii;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static graphql.schema.GraphQLSchema.newSchema;

public class QueryDefinition {

  private String query;
  private String queryName;
  private String viewName;
  private String typeName;

  private Map<String, String> args;

  private Map<String, String> typedQueries = new ConcurrentHashMap<>();

  private SchemaService schema;

  private QuerySchemaLoader querySchemaLoader;

  private GraphQLSchema introspectionSchema;


  QueryDefinition(SchemaService schema) {
    this.schema = schema;
  }


  public String getOption(String name) {
    return args.get(name);
  }

  public String getQuery() {
    return getQuery(typeName);
  }

  public String getQuery(String targetType) {
    return typedQueries.computeIfAbsent(targetType, (key) -> "# baseType=" + typeName + " targetType=" + key + "\n" + query);
  }

  void setQuery(String query) {
    this.query = query;
  }

  String getQueryName() {
    return queryName;
  }

  void setQueryName(String queryName) {
    if (Strings.isNullOrEmpty(queryName)) {
      throw new InvalidQueryDefinition("Name not set");
    }
    this.queryName = queryName;
  }

  String getViewName() {
    return viewName;
  }

  void setViewName(String viewName) {
    if (Strings.isNullOrEmpty(viewName)) {
      throw new InvalidQueryDefinition("View not set");
    }
    this.viewName = viewName;
  }

  String getTypeName() {
    return typeName;
  }

  void setTypeName(String typeName) {
    if (Strings.isNullOrEmpty(typeName)) {
      throw new InvalidQueryDefinition("Type not set");
    }
    this.typeName = typeName;
  }

  Map<String, String> getArgs() {
    return args;
  }

  void setArgs(Map<String, String> args) {
    this.args = args;
  }


  public GraphQLSchema getQuerySchema(Object target) {
    return querySchemaLoader.load(target);
  }

  public GraphQLSchema getIntrospectionSchema() {
    return introspectionSchema;
  }


  QueryDefinition resolve() throws InvalidQueryDefinition {
    // validate
    if (Strings.isNullOrEmpty(query) || Strings.isNullOrEmpty(queryName) || Strings.isNullOrEmpty(typeName) || Strings.isNullOrEmpty(viewName)) {
      throw new InvalidQueryDefinition("Invalid query definition: " + this);
    }
    String baseTypeName = Types.getBaseTypeName(typeName);
    if (!schema.hasType(baseTypeName)) {
      throw new InvalidQueryDefinition("Unknown query root type: " + this);
    }
    // create schema loaders
    if (Types.isList(typeName)) {
      querySchemaLoader = new ListQueryLoader(queryName, typeName, schema);
      // fetch default introspection schema
      introspectionSchema = querySchemaLoader.load(Collections.emptyList());
    }
    else {
      querySchemaLoader = new ObjectQueryLoader(typeName, schema);
      // create special introspection schema for base type
      GraphQLObjectType queryType = (GraphQLObjectType) schema.getTypes().stream().filter(e -> e.getName().equals(typeName)).findFirst().orElse(null);
      if (queryType == null) {
        throw new InvalidQueryDefinition("No introspection type '" + typeName + "' found for query '" + queryName + "#" + viewName + "'");
      }
      introspectionSchema = newSchema().query(queryType).build(schema.getTypes(), schema.getDirectives());
    }
    return this;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("queryName", queryName)
            .add("viewName", viewName)
            .add("typeName", typeName)
            .add("args", args)
            .add("query", Ascii.truncate(Strings.nullToEmpty(query), 40, "..."))
            .toString();
  }
}