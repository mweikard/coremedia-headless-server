package com.coremedia.caas.schema.type.scalar;

import com.coremedia.caas.schema.Types;
import com.coremedia.caas.service.repository.content.StructProxy;

import com.google.common.collect.ImmutableMap;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapOfScalars {

  private static final Logger LOG = LoggerFactory.getLogger(MapOfScalars.class);


  private static ConversionService conversionService;

  private static final Map<String, GraphQLScalarType> typeMap;

  private static final GraphQLScalarType CmsMapOfBoolean = graphQLObjectScalar("MapOfBoolean", new CoercingMap<>(Boolean.class));
  private static final GraphQLScalarType CmsMapOfFloat = graphQLObjectScalar("MapOfFloat", new CoercingMap<>(Float.class));
  private static final GraphQLScalarType CmsMapOfInt = graphQLObjectScalar("MapOfInt", new CoercingMap<>(Integer.class));
  private static final GraphQLScalarType CmsMapOfLong = graphQLObjectScalar("MapOfLong", new CoercingMap<>(Long.class));
  private static final GraphQLScalarType CmsMapOfShort = graphQLObjectScalar("MapOfShort", new CoercingMap<>(Short.class));
  private static final GraphQLScalarType CmsMapOfString = graphQLObjectScalar("MapOfString", new CoercingMap<>(String.class));

  static {
    ImmutableMap.Builder<String, GraphQLScalarType> builder = ImmutableMap.builder();
    builder.put(Types.BOOLEAN, CmsMapOfBoolean);
    builder.put(Types.FLOAT, CmsMapOfFloat);
    builder.put(Types.INT, CmsMapOfInt);
    builder.put(Types.LONG, CmsMapOfLong);
    builder.put(Types.SHORT, CmsMapOfShort);
    builder.put(Types.STRING, CmsMapOfString);
    typeMap = builder.build();
  }


  private static GraphQLScalarType graphQLObjectScalar(String name, CoercingMap<?> coercing) {
    return new GraphQLScalarType(name, "Built-in map of scalar type", coercing);
  }

  public static GraphQLScalarType getType(String baseTypeName) {
    return typeMap.get(baseTypeName);
  }


  public MapOfScalars(ConversionService conversionService) {
    MapOfScalars.conversionService = conversionService;
  }


  public Map<String, GraphQLScalarType> getTypes() {
    return typeMap;
  }


  private static class CoercingMap<T> implements Coercing<Object, Map<String, T>> {

    private Class<T> targetClass;

    private CoercingMap(Class<T> targetClass) {
      this.targetClass = targetClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, T> serialize(Object dataFetcherResult) {
      Map<String, ?> properties = null;
      if (dataFetcherResult instanceof StructProxy) {
        properties = ((StructProxy) dataFetcherResult).getProperties();
      }
      else if (dataFetcherResult instanceof Map) {
        properties = (Map<String, ?>) dataFetcherResult;
      }
      if (properties != null) {
        return properties.entrySet().stream()
                .filter(e -> {
                  Object v = e.getValue();
                  return v != null && conversionService.canConvert(v.getClass(), targetClass);
                })
                .collect(HashMap<String, T>::new, (m, e) -> {
                  try {
                    m.put(e.getKey(), conversionService.convert(e.getValue(), targetClass));
                  } catch (Exception ex) {
                    LOG.warn("Type conversion failed for {}", e);
                  }
                }, HashMap::putAll);
      }
      return Collections.emptyMap();
    }

    @Override
    public Object parseValue(Object input) {
      throw new CoercingParseValueException("Parsing unsupported");
    }

    @Override
    public Object parseLiteral(Object input) {
      return null;
    }
  }
}
