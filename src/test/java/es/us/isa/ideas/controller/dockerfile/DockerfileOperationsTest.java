package es.us.isa.ideas.controller.dockerfile;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;


@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class DockerfileOperationsTest {

    DockerfileOperations operations = new DockerfileOperations();

    String username = "Username1";
    Map<String, String> documents = Map.of("Dockerfile", "FROM node:16.14.0-alpine3.15", "Dockerfile.local", "");

    
    @Test
    @Order(0)
    @BeforeAll
    public void setup() throws IOException{
        System.out.println("==================================");
        System.out.println("START SETUP");
        operations.executeCommand("docker run -d --privileged --name " + username + " docker:dind dockerd", "/");
        operations.executeCommand("docker start " + username, "/");
    }

    // Para y elimina los contenedores de prueba
    
    @AfterAll
    @Test
    @Order(3)
    public void stopTestContainer() throws IOException {
        System.out.println("==================================");
        System.out.println("STOPPING TEST CONTAINER");

        operations.executeCommand("docker kill " + username, "/");
        operations.executeCommand("docker rm " + username, "/");
        System.out.println("==================================");
        System.out.println("TESTS FINISHED");
    }

    @Test
    @Order(1)
    public void testBuildImage() {
        System.out.println("==================================");
        System.out.println("TEST BUILD IMAGE");

        AppResponse appResponse = new AppResponse();

        operations.buildImage(documents.get("Dockerfile"), "imageTest", username, appResponse);
        System.out.println(appResponse.getHtmlMessage());
        assertEquals(appResponse.getStatus(), Status.OK);

    }

    @Test
    @Order(2)
    public void testShowImages() {
        System.out.println("==================================");
        System.out.println("TEST SHOW IMAGE");

        AppResponse appResponse = new AppResponse();
        String flags = "";

        operations.showImages(username, flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);

        System.out.println(appResponse.getHtmlMessage());

    }
}