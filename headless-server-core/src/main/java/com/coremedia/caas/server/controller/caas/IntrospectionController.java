package com.coremedia.caas.server.controller.caas;

import com.coremedia.caas.server.controller.base.ResponseStatusException;
import com.coremedia.caas.server.service.request.ClientIdentification;
import com.coremedia.caas.service.repository.RootContext;
import com.coremedia.caas.service.security.AccessControlViolation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
@RequestMapping("/caas/v1/{tenantId}/sites/{siteId}/introspection")
public class IntrospectionController extends AbstractCaasController {

  public IntrospectionController() {
    super("caas.server.content.introspection");
  }


  private Object execute(String tenantId, String siteId, String queryName, String viewName, ServletWebRequest request) {
    try {
      RootContext rootContext = resolveRootContext(tenantId, siteId, request);
      // determine client
      ClientIdentification clientIdentification = resolveClient(rootContext, request);
      String clientId = clientIdentification.getId().toString();
      String definitionName = clientIdentification.getDefinitionName();
      // run query
      return execute(() -> introspectQuery(rootContext, clientIdentification, queryName, viewName), "tenant", tenantId, "site", siteId, "client", clientId, "pd", definitionName, "query", queryName, "view", viewName);
    } catch (AccessControlViolation e) {
      return handleError(e, request);
    } catch (ResponseStatusException e) {
      return handleError(e, request);
    } catch (Exception e) {
      return handleError(e, request);
    }
  }


  @RequestMapping(path = "/{queryName}/{viewName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Object introspect(@PathVariable("tenantId") String tenantId,
                           @PathVariable("siteId") String siteId,
                           @PathVariable("queryName") String queryName,
                           @PathVariable("viewName") String viewName,
                           ServletWebRequest request) {
    return execute(tenantId, siteId, queryName, viewName, request);
  }
}
