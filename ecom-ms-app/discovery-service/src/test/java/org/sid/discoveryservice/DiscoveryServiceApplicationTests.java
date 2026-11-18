package org.sid.discoveryservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.service-url.defaultZone=http://localhost:0/eureka/")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DiscoveryServiceApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void exposesEurekaDashboard() {
        var response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Eureka");
    }
}
