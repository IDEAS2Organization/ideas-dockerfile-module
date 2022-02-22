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

public class DockerfileOperations {

    private String inContainer(String username, String command){
        return "docker exec " + username + " " + command;
    }

    public void buildImage(String content, String imageName, String username, AppResponse appResponse) {
        try {
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

            String message = executeCommand(inContainer(username, "docker build -t " + imageName + " /dockerfiles/"), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void getImages(String username, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker images"), "/");
            String finalMsg = "{";
            for(String line: message.split("\n")){
                if(!line.contains("<")){
                    String tmp = line + "\n";
                    finalMsg += tmp.replaceAll("(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S[\\s\\S]*)\\s\\s+(\\d*\\S*)\n", "'$1':'$5',"); // Se queda con el nombre y el tamaño
                }
            }
            finalMsg += "}";
            finalMsg = finalMsg.replace(",}", "}"); // Da fallo el JSON.parse si tiene la coma al final
            finalMsg = finalMsg.replace("'", "\"");
            appResponse.setData(finalMsg);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void deleteImage(String username, String imageName,  AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker rmi " + imageName), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void showImages(String username, AppResponse appResponse) {
        try {
            String message = executeCommand(inContainer(username, "docker images"), "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void showAllContainers(AppResponse appResponse) {
        try {
            String message = executeCommand("docker ps -a", "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void run(String imageName,  AppResponse appResponse) {
        try {
            String message = executeCommand("docker run -d " + imageName, "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void stop(String nameOrId,  AppResponse appResponse) {
        try {
            String message = executeCommand("docker stop " + nameOrId, "/");

            appResponse.setHtmlMessage(message);
            appResponse.setStatus(Status.OK);
        } catch (IOException e) {
            generateAppResponseError(appResponse, e);
        }
    }

    public void delete_container(String nameOrId,  AppResponse appResponse) {
        try {
            String message = executeCommand("docker rm " + nameOrId, "/");

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

    public void generateAppResponseError(AppResponse appResponse, IOException e) {
        appResponse
                .setHtmlMessage("<h1>An error has ocurred. </h1><br><b><pre>" + e.toString() + "'</pre></b>");
        appResponse.setStatus(Status.OK_PROBLEMS); // Si se pone Status.ERRORS no muestra el mensaje HTML
    }

    
}
