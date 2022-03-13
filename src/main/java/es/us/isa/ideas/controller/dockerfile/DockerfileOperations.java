package es.us.isa.ideas.controller.dockerfile;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;

public class DockerfileOperations {

    public String inContainer(String username, String command) {
        return "docker exec " + username + " " + command;
    }

    public void buildImage(String content, String imageName, String username, AppResponse appResponse) {
        try {

            if (imageName.contains("&") || imageName.contains(" ")) {
                throw new InvalidFileNameException(imageName,
                        "Se ha detectado una posible injección de código en el nombre. Usa otro nombre para construir la imagen.");
            }
            imageName = imageName.replace("'", "\\'").replace("\"", "\\\""); // Reemplaza los ' por \' y los " por \"
                                                                             // para evitar inyecciones de código

            executeCommand(inContainer(username, "mkdir /dockerfiles"), "/");
            executeCommand(inContainer(username, "touch /dockerfiles/Dockerfile"), "/");

            Path path = Paths.get("/dockerfiles");
            Files.createDirectories(path);
            File tmpDockerfile = new File("/dockerfiles/" + username);
            FileWriter fw = new FileWriter(tmpDockerfile);
            fw.write(content);
            fw.close();

            executeCommand("docker cp /dockerfiles/" + username + " " + username + ":/dockerfiles/Dockerfile", "/");
            tmpDockerfile.delete();

            String message = executeCommand(inContainer(username, "docker build -t " + imageName + " /dockerfiles/"),
                    "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        } catch (InvalidFileNameException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void deleteImage(String username, String imageName, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker rmi " + imageName), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void showImages(String username, String flags, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker images " + flags), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void showContainers(String username, String flags, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker ps " + flags), "/");
            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void run(String username, String name, String isNew, AppResponse appResponse) {
        try {
            String message = null;
            if (isNew.equals("Y")) {
                message = executeCommand(inContainer(username, "docker run -d " + name), "/");
            } else {
                message = executeCommand(inContainer(username, "docker start " + name), "/");
            }

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void stop(String username, String containerId, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker stop " + containerId), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void deleteContainer(String username, String containerId, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker rm " + containerId), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void logsFromContainer(String username, String containerId, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker logs " + containerId), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public String executeCommand(String command, String inputPath) throws IOException {
        long start = System.currentTimeMillis();

        System.out.println(System.currentTimeMillis() + " - Executing command: '" + command + "' at path: '"
                + inputPath + "'");

        String[] commands = command.split(" ");
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File(inputPath));

        Path output = Files.createTempFile("", "-outuput.log");
        Path errors = Files.createTempFile("", "-error.log");
        pb.redirectError(Redirect.appendTo(errors.toFile()));
        pb.redirectOutput(Redirect.appendTo(output.toFile()));

        Process p = pb.start();
        while (p.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger("Docker").log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(System.currentTimeMillis() + " - Command execution finished with code: " + p.exitValue());
        String outputString = org.assertj.core.util.Files.contentOf(output.toFile(), Charset.defaultCharset());
        String errorString = org.assertj.core.util.Files.contentOf(errors.toFile(), Charset.defaultCharset());
        return generateHTMLMessage(outputString, errorString, System.currentTimeMillis() - start);
    }

    public String generateHTMLMessage(String output, String errors, long duration) {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>Execution duration:</b>" + duration + " ms<br>\n");

        builder.append("<h3>Operation output:</h3>\n");
        builder.append("<p><pre>" + output + "</pre></p>\n");

        if (!errors.equals("")) {
            builder.append("<h3>Errors found:</h3>\n");
            builder.append("<p><pre>" + errors + "</pre></p>\n");
        }
        return builder.toString();

    }

    public void generateAppResponseError(AppResponse appResponse, Exception e) {
        appResponse
                .setHtmlMessage("<h1>An error has ocurred. </h1><br><b><pre>" + e.toString() + "'</pre></b>");
        appResponse.setStatus(Status.OK_PROBLEMS); // Si se pone Status.ERRORS no muestra el mensaje HTML
    }

}
