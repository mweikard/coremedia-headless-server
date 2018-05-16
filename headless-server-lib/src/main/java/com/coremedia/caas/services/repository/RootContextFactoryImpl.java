package com.coremedia.caas.services.repository;

import com.coremedia.caas.services.request.RequestContext;
import com.coremedia.caas.services.security.AccessValidator;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.List;

public class RootContextFactoryImpl implements RootContextFactory, BeanFactoryAware {

  private BeanFactory beanFactory;

  private List<AccessValidator> accessValidators;
  private List<ProxyModelFactory> proxyModelFactories;
  private ContentRepository contentRepository;


  public RootContextFactoryImpl(List<AccessValidator> accessValidators, List<ProxyModelFactory> proxyModelFactories, ContentRepository contentRepository) {
    this.accessValidators = accessValidators;
    this.proxyModelFactories = proxyModelFactories;
    this.contentRepository = contentRepository;
  }


  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }


  @Override
  public RootContext createRootContext(Content siteIndicator, Content rootDocument, Object currentContext, Object target, RequestContext requestContext) {
    return beanFactory.getBean(RootContext.class, siteIndicator, rootDocument, currentContext, target, requestContext, accessValidators, proxyModelFactories, contentRepository);
  }
}