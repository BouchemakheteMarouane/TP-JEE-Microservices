package org.sid.gatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.client.simple.instances.customer-service[0].uri=http://localhost:8081"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GatewayServiceApplicationTests {
    @Autowired
    private DiscoveryClientRouteDefinitionLocator dynamicRoutes;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void createsOneDynamicLocatorAndDiscoversCustomerRoute() {
        assertThat(applicationContext.getBeansOfType(DiscoveryClientRouteDefinitionLocator.class)).hasSize(1);
        var routes = dynamicRoutes.getRouteDefinitions().collectList().block(Duration.ofSeconds(5));
        assertThat(routes).isNotNull().anySatisfy(route -> {
            assertThat(route.getUri().toString()).isEqualTo("lb://customer-service");
            assertThat(route.getPredicates()).anySatisfy(predicate ->
                    assertThat(predicate.getArgs()).containsValue("/customer-service/**"));
        });
    }
}
