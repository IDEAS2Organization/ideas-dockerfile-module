package es.us.isa.ideas.controller.dockerfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
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

    /**
     * Inicializa los contenedores de pruebas
     * 
     * @throws IOException
     */
    @BeforeAll
    public void setup() throws IOException {
        System.out.println("==================================");
        System.out.println("START SETUP");
        operations.executeCommand("docker run -d --privileged --name " + username + " docker:dind dockerd", "/");
        operations.executeCommand("docker start " + username, "/");
    }

    /**
     * Elimina los contenedores de prueba
     * 
     * @throws IOException
     */
    @AfterAll
    public void stopTestContainer() throws IOException {
        System.out.println("==================================");
        System.out.println("STOPPING TEST CONTAINER");

        operations.executeCommand("docker kill " + username, "/");
        operations.executeCommand("docker rm " + username, "/");
        System.out.println("==================================");
        System.out.println("TESTS FINISHED");
    }

    /**
     * Comprueba que existe una imagen
     */
    public Boolean imageExists(String username, String flags, String imageName) {
        AppResponse appResponse = new AppResponse();

        operations.showImages(username, flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));
        return appResponse.getHtmlMessage().contains(imageName);
    }

    /**
     * Crea una imagen, luego comprueba que existe, luego la borra y vuelve a
     * comprobar si no existe
     */
    @Test
    @Order(1)
    public void testBuildShowAndDeleteImage() {
        System.out.println("==================================");
        System.out.println("TEST BUILD IMAGE");

        String imageName = "image_test";
        String flags = "";
        AppResponse appResponse = new AppResponse();

        operations.buildImage(documents.get("Dockerfile"), imageName, this.username, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        System.out.println("==================================");
        System.out.println("TEST IMAGE EXISTS");

        assertTrue(imageExists(this.username, flags, imageName));

        System.out.println("==================================");
        System.out.println("TEST SHOW IMAGE");

        appResponse = new AppResponse();

        operations.deleteImage(username, imageName, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        System.out.println("==================================");
        System.out.println("TEST IMAGE DOES NOT EXISTS");

        assertFalse(imageExists(username, flags, imageName));

    }

    /**
     * Comprueba si un contenedor está corriendo o si un contenedor existe
     */
    public Boolean runningOrExistingContainer(String username, String containerName, Boolean checkRunning) {
        AppResponse appResponse = new AppResponse();

        String flags = checkRunning ? "" : "-a";

        operations.showContainers(username, flags, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        return appResponse.getHtmlMessage().contains(containerName);
    }

    /**
     * Testea las distintas operaciones que se puede hacer sobre un contenedor:
     * Primero crea un contenedor a partir de una imagen existente. Después,
     * tras 5 segundos, comprueba que existe. Luego para el contenedor y comprueba
     * que está parado. Tras esto lo vuelve a arrancar y parar. Por último lo borra
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Order(2)
    public void testContainerOperations() throws IOException, InterruptedException {
        System.out.println("==================================");
        System.out.println("TEST RUN CONTAINER");

        String imageName = "httpd:alpine3.15";
        AppResponse appResponse = new AppResponse();

        operations.executeCommand(operations.inContainer(username, "docker pull " + imageName), "/");

        operations.run(this.username, imageName, "Y", appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        String containerId = appResponse.getHtmlMessage().replaceAll("[\\s\\S]*<pre>([\\s\\S]*?)\\n</pre>[\\s\\S]*", "$1");

        TimeUnit.SECONDS.sleep(5);
        System.out.println("==================================");
        System.out.println("TEST CHECK CONTAINER IS RUNNING");

        assertTrue(runningOrExistingContainer(username, containerId.substring(0, 5), true));

        appResponse = new AppResponse();
        System.out.println("==================================");
        System.out.println("TEST STOP CONTAINER");

        operations.stop(username, containerId, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        TimeUnit.SECONDS.sleep(11);
        System.out.println("==================================");
        System.out.println("TEST CHECK CONTAINER IS STOPPED");

        assertFalse(runningOrExistingContainer(username, containerId.substring(0, 5), true));
        assertTrue(runningOrExistingContainer(username, containerId.substring(0, 5), false));

        appResponse = new AppResponse();
        System.out.println("==================================");
        System.out.println("TEST START A STOPPED CONTAINER");

        operations.run(this.username, containerId, "N", appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        TimeUnit.SECONDS.sleep(5);
        System.out.println("==================================");
        System.out.println("TEST CHECK CONTAINER IS RUNNING");

        assertTrue(runningOrExistingContainer(username, containerId.substring(0, 5), true));
        
        appResponse = new AppResponse();
        System.out.println("==================================");
        System.out.println("TEST STOP AND REMOVE CONTAINER");

        operations.stop(this.username, containerId, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));

        appResponse = new AppResponse();
        operations.deleteContainer(username, containerId, appResponse);
        assertEquals(appResponse.getStatus(), Status.OK);
        assertFalse(appResponse.getHtmlMessage().contains("<h3>Errors found:</h3>"));
        
        System.out.println("==================================");
        System.out.println("TEST CONTAINER DOES NOT EXISTS");

        assertFalse(runningOrExistingContainer(username, containerId.substring(0, 5), true));
        assertFalse(runningOrExistingContainer(username, containerId.substring(0, 5), false));

    }

}