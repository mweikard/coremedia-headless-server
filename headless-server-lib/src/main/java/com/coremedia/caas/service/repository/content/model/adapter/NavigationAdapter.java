package com.coremedia.caas.service.repository.content.model.adapter;

import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.caas.service.repository.content.ContentProxy;
import com.coremedia.cap.content.Content;

import java.util.Collections;
import java.util.List;

public class NavigationAdapter {

  private Content content;
  private ContextStrategy<Content, Content> contextStrategy;
  private TreeRelation<Content> treeRelation;
  private RootContext rootContext;


  public NavigationAdapter(Content content, ContextStrategy<Content, Content> contextStrategy, TreeRelation<Content> treeRelation, RootContext rootContext) {
    this.content = content;
    this.contextStrategy = contextStrategy;
    this.treeRelation = treeRelation;
    this.rootContext = rootContext;
  }


  public boolean isHidden() {
    return content.getType().isSubtypeOf("CMNavigation") && content.getBoolean("hidden");
  }

  public boolean isHiddenInSitemap() {
    return content.getType().isSubtypeOf("CMNavigation") && (content.getBoolean("hidden") || content.getBoolean("hiddenInSitemap"));
  }


  public ContentProxy getContext() {
    return rootContext.getProxyFactory().makeContentProxy(findContext());
  }

  public List<ContentProxy> getChildren() {
    return rootContext.getProxyFactory().makeContentProxyList(treeRelation.getChildrenOf(content));
  }

  public List<ContentProxy> getPathToRoot() {
    if (content.getType().isSubtypeOf("CMNavigation")) {
      return rootContext.getProxyFactory().makeContentProxyList(treeRelation.pathToRoot(content));
    }
    else {
      Content context = findContext();
      if (context != null) {
        return rootContext.getProxyFactory().makeContentProxyList(treeRelation.pathToRoot(context));
      }
    }
    return Collections.emptyList();
  }


  private Content findContext() {
    Object currentContext = rootContext.getCurrentContext();
    Content activeContext = (currentContext != null && currentContext instanceof Content) ? (Content) currentContext : rootContext.getSite().getSiteRootDocument();
    return contextStrategy.findAndSelectContextFor(content, activeContext);
  }
}
