package es.us.isa.ideas.controller.dockerfile;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;
import es.us.isa.ideas.module.controller.BaseLanguageController;

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

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/ideas-dockerfile-language/language")
public class DockerfileLanguageController extends BaseLanguageController {

	@RequestMapping(value = "/format/{format}/checkLanguage", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public AppResponse checkLanguage(String id, String content, String fileUri, HttpServletRequest request) {

		AppResponse appResponse = new AppResponse();

		boolean problems = false;

		// System.out.println("CheckSyntax: " + res );
		appResponse.setFileUri(fileUri);

		if (problems)
			appResponse.setStatus(Status.OK_PROBLEMS);
		else
			appResponse.setStatus(Status.OK);

		return appResponse;
	}

	@RequestMapping(value = "/convert", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public AppResponse convertFormat(String currentFormat, String desiredFormat, String fileUri, String content,
			HttpServletRequest request) {
		AppResponse appResponse = new AppResponse();

		return appResponse;
	}

	@RequestMapping(value = "/operation/{id}/execute", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public AppResponse executeOperation(String id, String content, String fileUri, String auxArg0,
			HttpServletRequest request) {
		AppResponse appResponse = new AppResponse();
		if (id.equals("build")) {
			try {
				Path path = Paths.get("/dockerfiles");
				Files.createDirectories(path);

				File tmpDockerfile = new File("/dockerfiles/Dockerfile");
				FileWriter fw = new FileWriter(tmpDockerfile);
				fw.write(content);
				fw.close();


				String[] splits = fileUri.split("/");
				Integer splitLength = splits.length;
				String imageName = splits[splitLength-1].split(".dockerfile")[0];
				String message = executeCommand("docker build -t " + imageName + " .", "/dockerfiles");
				
				tmpDockerfile.delete();
				
				appResponse.setHtmlMessage(message);
				appResponse.setStatus(Status.OK);
			} catch (IOException e) {
				appResponse.setHtmlMessage("<h1>An error has ocurred. </h1><br><b><pre>" + e.toString() + "'</pre></b>");
				appResponse.setStatus(Status.OK_PROBLEMS); // Si se pone Status.ERRORS no muestra el mensaje HTML
			}
		}
		return appResponse;
	}

	private String executeCommand(String command, String inputPath) throws IOException {
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

		builder.append("<h3>Latex compilation output:</h3>\n");
		builder.append("<p><pre>" + output + "</pre></p>\n");

		if (!errors.equals("")) {
			builder.append("<h3>Errors found:</h3>\n");
			builder.append("<p><pre>" + errors + "</pre></p>\n");
		}
		return builder.toString();

	}

}
