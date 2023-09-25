package io.github.LuizYokoyama.keycloak_testcontainer;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.URIBuilder;
import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KeycloakIntegrationTests {

    //Keycloak login
    protected static String KC_REALM = "test_realm";
    protected static String KC_CLIENTE = "test_client";
    protected static String KC_USER = "test_user";
    protected static String KC_PASS = "test";

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakIntegrationTests.class.getName());
    @LocalServerPort
    private int LOCAL_PORT;

    @Container
    public static final DockerComposeContainer dockerComposeContainer =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService("keycloak", 8080)
                    .withLocalCompose(true);

    static {
        dockerComposeContainer.start();
    }

    private static String AUTH_URL = "http://" + dockerComposeContainer.getServiceHost("keycloak", 8080)
            + ":" + dockerComposeContainer.getServicePort("keycloak", 8080) + "/auth";

    // A porta do servidor http é aleatória
    @PostConstruct
    public void init() {
        RestAssured.baseURI = "http://localhost:" + LOCAL_PORT;
    }

    // Atualiza dinamicamento a configuração do Spring Security, já que a porta do testcontainer é aleatória
    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () ->
                AUTH_URL + "/realms/" + KC_REALM);
    }

    protected String getBearer() {

        try {
            URI authorizationURI = new URIBuilder(AUTH_URL + "/realms/" + KC_REALM
                    + "/protocol/openid-connect/token").build();
            WebClient webclient = WebClient.builder()
                    .build();
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.put("grant_type", Collections.singletonList("password"));
            formData.put("client_id", Collections.singletonList(KC_CLIENTE));
            formData.put("username", Collections.singletonList(KC_USER));
            formData.put("password", Collections.singletonList(KC_PASS));

            String result = webclient.post()
                    .uri(authorizationURI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JacksonJsonParser jsonParser = new JacksonJsonParser();

            return "Bearer " + jsonParser.parseMap(result)
                    .get("access_token")
                    .toString();
        } catch (URISyntaxException e) {
            LOGGER.error("Can't obtain an access token from Keycloak!", e);
        }

        return null;
    }


    @Test
    public void getJwtKeycloakTest() {

        var jwt = getBearer();
        Assertions.assertNotNull(jwt);

    }

    @Test
    public void givenAuthenticatedUser_whenGetName_shouldReturnUserName() {

        given().header("Authorization", getBearer())
                .when()
                .get("/name")
                .then()
                .body("userName", equalTo("userName Test Ok"));

    }

}
