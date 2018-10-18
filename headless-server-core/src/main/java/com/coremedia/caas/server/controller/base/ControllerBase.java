package com.coremedia.caas.server.controller.base;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.caas.server.monitoring.CaasMetrics;
import com.coremedia.caas.server.resolver.TargetResolver;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.caas.service.repository.RootContextFactory;
import com.coremedia.caas.service.request.RequestContext;
import com.coremedia.caas.service.security.AccessControlViolation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class ControllerBase {

  private static final Logger LOG = LoggerFactory.getLogger(ControllerBase.class);

  private static final String TENANT_ID = "tenantId";


  private String timerName;

  @Autowired
  protected CaasMetrics metrics;

  @Autowired
  protected RequestContext requestContext;

  @Autowired
  protected RootContextFactory rootContextFactory;

  @Autowired
  protected SitesService sitesService;

  @Autowired
  @Qualifier("settingsService")
  protected SettingsService settingsService;

  @Autowired
  protected List<TargetResolver> targetResolvers;


  public ControllerBase(String timerName) {
    this.timerName = timerName;
  }


  private List<Site> resolveSites(String tenantId) {
    return sitesService.getSites().stream().filter(site -> tenantId.equals(settingsService.setting(TENANT_ID, String.class, site.getSiteIndicator()))).collect(Collectors.toList());
  }

  private Site resolveSite(String tenantId, String siteId) {
    Site site = resolveSites(tenantId).stream().filter(item -> siteId.equals(item.getId())).findFirst().orElse(null);
    if (site != null && site.getSiteRootDocument() != null) {
      return site;
    }
    return null;
  }

  private Object resolveTarget(Site site, String targetId) {
    for (TargetResolver resolver : targetResolvers) {
      Object target = resolver.resolveTarget(site, targetId);
      if (target != null) {
        if (validateTarget(site, target)) {
          return target;
        }
        return null;
      }
    }
    return null;
  }

  private boolean validateTarget(Site site, Object target) {
    return !(target instanceof Content) || sitesService.isContentInSite(site, (Content) target);
  }


  protected ClientIdentification resolveClient(RootContext rootContext, ServletWebRequest request) {
    return ClientIdentification.from(rootContext, settingsService, request);
  }


  protected RootContext resolveRootContext(String tenantId, String siteId, ServletWebRequest request) throws AccessControlViolation {
    Site site = resolveSite(tenantId, siteId);
    if (site == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    // site indicator itself is the query root
    return rootContextFactory.createRootContext(site, null, site.getSiteIndicator(), requestContext);
  }

  protected RootContext resolveRootContext(String tenantId, String siteId, String targetId, ServletWebRequest request) throws AccessControlViolation {
    Site site = resolveSite(tenantId, siteId);
    if (site == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    // resolve query root
    Object target = resolveTarget(site, targetId);
    if (target == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return rootContextFactory.createRootContext(site, null, target, requestContext);
  }


  protected <E> E execute(Supplier<E> supplier, String... tags) {
    return metrics.timer(supplier, timerName, tags);
  }


  protected <T> ResponseEntity<T> handleError(Exception error, ServletWebRequest request) {
    // unexpected error, log with stacktrace
    LOG.warn("Request failed with unexpected error: {}", error.getMessage(), error);
    return new ResponseEntity<T>(HttpStatus.BAD_REQUEST);
  }

  protected <T> ResponseEntity<T> handleError(AccessControlViolation error, ServletWebRequest request) {
    LOG.debug("Access forbidden: {}", error.getMessage());
    switch (error.getErrorCode()) {
      case INVALID_OBJECT:
        return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
      case INSUFFICIENT_RIGHTS:
        return new ResponseEntity<T>(HttpStatus.FORBIDDEN);
      default:
        return new ResponseEntity<T>(HttpStatus.BAD_REQUEST);
    }
  }

  protected <T> ResponseEntity<T> handleError(ResponseStatusException error, ServletWebRequest request) {
    LOG.debug("Request failed: {}", error.getMessage());
    return new ResponseEntity<T>(error.getStatus());
  }


  protected long getMaxAge(long cacheFor) {
    ZonedDateTime nextInvalidation = requestContext.getNextDateTimeChange();
    if (nextInvalidation != null) {
      ZonedDateTime now = requestContext.getRequestTime();
      // return either the configured cache time or the number of seconds until validity/visibility
      // settings change the result
      return Math.min(cacheFor, Duration.between(now, nextInvalidation).getSeconds());
    }
    return cacheFor;
  }
}
