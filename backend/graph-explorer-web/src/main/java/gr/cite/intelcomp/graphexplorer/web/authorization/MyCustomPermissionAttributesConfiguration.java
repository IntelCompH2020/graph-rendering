package gr.cite.intelcomp.graphexplorer.web.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@EnableConfigurationProperties(MyCustomPermissionAttributesProperties.class)
public class MyCustomPermissionAttributesConfiguration {

    private final MyCustomPermissionAttributesProperties properties;

    @Autowired
    public MyCustomPermissionAttributesConfiguration(MyCustomPermissionAttributesProperties properties) {
        this.properties = properties;
    }

    public HashMap<String, MyCustomPermissionAttributesProperties.MyPermission> getMyPolicies() {
        return properties.getPolicies();
    }

}
