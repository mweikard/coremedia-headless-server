package com.coremedia.caas.services.repository;

import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.caas.services.repository.content.ContentModelAdapterFactory;
import com.coremedia.caas.services.repository.content.ContentProxyModelAccessor;
import com.coremedia.caas.services.request.RequestContext;
import com.coremedia.caas.services.security.AccessValidator;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

import java.util.List;
import java.util.Map;

@Configuration
public class RepositoryConfig {

  @Bean("proxyFactory")
  @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
  public ProxyFactory proxyFactory(ContentRepository contentRepository, RootContext rootContext) {
    return new ProxyFactoryImpl(contentRepository, rootContext);
  }

  @Bean("rootContext")
  @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
  public RootContext rootContext(Content siteIndicator, Content rootDocument, Object currentContext, Object target, RequestContext requestContext, List<AccessValidator> accessValidators, List<ProxyModelFactory> proxyModelFactories, ContentRepository contentRepository) {
    return new RootContextImpl(siteIndicator, rootDocument, currentContext, target, requestContext, accessValidators, proxyModelFactories, contentRepository);
  }


  @Bean("contentModelAdapterFactory")
  public ContentModelAdapterFactory createContentModelAdapterFactory(ContentBackedPageGridService contentBackedPageGridService, ContextStrategy<Content, Content> contextStrategy, Map<String, TreeRelation<Content>> treeRelations, SettingsService settingsService) {
    return new ContentModelAdapterFactory(contentBackedPageGridService, contextStrategy, treeRelations, settingsService);
  }

  @Bean("rootContextFactory")
  public RootContextFactory rootContextFactory(List<AccessValidator> accessValidators, List<ProxyModelFactory> proxyModelFactories, ContentRepository contentRepository) {
    return new RootContextFactoryImpl(accessValidators, proxyModelFactories, contentRepository);
  }


  @Bean("spelPropertyAccessors")
  public List<PropertyAccessor> spelPropertyAccessors() {
    return ImmutableList.of(
            new ContentProxyModelAccessor(),
            new MapAccessor(),
            new ReflectivePropertyAccessor());
  }
}