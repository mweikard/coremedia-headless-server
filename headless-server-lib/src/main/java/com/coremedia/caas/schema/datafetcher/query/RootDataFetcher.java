package com.coremedia.caas.schema.datafetcher.query;

import com.coremedia.caas.schema.datafetcher.common.AbstractDataFetcher;

import graphql.schema.DataFetchingEnvironment;

public class RootDataFetcher extends AbstractDataFetcher {

  @Override
  public Object get(DataFetchingEnvironment environment) {
    return environment.getRoot();
  }
}
