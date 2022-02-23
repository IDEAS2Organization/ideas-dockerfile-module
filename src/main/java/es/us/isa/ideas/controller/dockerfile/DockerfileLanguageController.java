package es.us.isa.ideas.controller.dockerfile;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;
import es.us.isa.ideas.module.controller.BaseLanguageController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/ideas-dockerfile-language/language")
public class DockerfileLanguageController extends BaseLanguageController {


	DockerfileOperations operations = new DockerfileOperations();

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

	@RequestMapping(value = "/operation/{id}/javascript", method = RequestMethod.GET)
	@ResponseBody
	public AppResponse getJavascriptFile(@PathVariable(value="id") String id, HttpServletResponse response) {
		AppResponse appResponse = new AppResponse();
		try{
			InputStream jsFile = new ClassPathResource("actions/" + id + ".js").getInputStream();
			OutputStream os = response.getOutputStream();
			os.write(jsFile.readAllBytes());
			response.setContentType("application/javascript");
			os.close();
		}catch(IOException e){
			operations.generateAppResponseError(appResponse, e);
		}
		return appResponse;
	}

	@RequestMapping(value = "/operation/{id}/execute", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public AppResponse executeOperation(String id, String content, String fileUri, String username,
			HttpServletRequest request) {
		AppResponse appResponse = new AppResponse();
		try{
			// Si el contenedor está iniciado, el primer comando falla y el segundo no hace nada
			// Si el contenedor está parado, el primer comando falla y el segundo inicia el contenedor
			// Si el contenedor no existe, el primer comando lo crea y el segundo no hace nada
			operations.executeCommand("docker run -d --privileged --name " + username + " docker:dind dockerd", "/");
			operations.executeCommand("docker start " + username, "/");
		}catch(IOException e){
			operations.generateAppResponseError(appResponse, e);
			return appResponse;
		}
		switch(id){
			case "build":
				operations.buildImage(content, request.getParameter("imageName"), username, appResponse);
				break;

			case "delete_image":
				operations.deleteImage(username, request.getParameter("imageName"), appResponse);
				break;
			
			case "show_images":
				operations.showImages(username, request.getParameter("flags"), appResponse);
				break;
			
			case "show_containers":
				operations.showContainers(username, request.getParameter("flags"), appResponse);
				break;
			
			case "run":
				operations.run(username, request.getParameter("name"), request.getParameter("isNew"), appResponse);
				break;
			
			case "stop":
				operations.stop(username, request.getParameter("containerId"), appResponse);
				break;
			
			case "delete_container":
				operations.delete_container(username, request.getParameter("containerId"), appResponse);
				break;

			case "logs":
				operations.logs_from_container(username, request.getParameter("containerId"), appResponse);
				break;
			
			default:
				String htmlMessage = operations.generateHTMLMessage("", "'" + id + "' no es un id válido.", 0);
				appResponse.setHtmlMessage(htmlMessage);
		}
		return appResponse;
	}

}
