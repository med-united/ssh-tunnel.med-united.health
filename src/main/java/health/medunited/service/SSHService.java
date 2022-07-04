package health.medunited.service;

import io.quarkus.runtime.Startup;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Startup
public class SSHService {

    public static void main(String[] args) {
        System.out.print("Hi, from SSH Service");
    }
}
