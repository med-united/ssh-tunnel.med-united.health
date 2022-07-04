package health.medunited.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {

    @ConfigProperty(name = "idp.base.url")
    String idpBaseURL;

    @ConfigProperty(name = "idp.auth.request.redirect.url")
    String idpAuthRequestRedirectURL;

    @ConfigProperty(name = "idp.client.id")
    String idpClientId;

    @ConfigProperty(name = "ere.workflow-service.prescription.server.url")
    String prescriptionServiceURL;

    public String getIdpBaseURL() {
        return idpBaseURL;
    }

    public String getIdpAuthRequestRedirectURL() {
        return idpAuthRequestRedirectURL;
    }

    public String getIdpClientId() {
        return idpClientId;
    }

    public String getPrescriptionServiceURL() {
        return prescriptionServiceURL;
    }
}
