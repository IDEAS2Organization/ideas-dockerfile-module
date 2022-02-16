package es.us.isa.ideas.controller.dockerfile;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import es.us.isa.ideas.module.common.AppResponse;
import es.us.isa.ideas.module.common.AppResponse.Status;
import es.us.isa.ideas.module.controller.BaseLanguageController;

import javax.servlet.http.HttpServletRequest;


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

	@RequestMapping(value = "/operation/{id}/execute", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public AppResponse executeOperation(String id, String content, String fileUri, String auxArg0,
			HttpServletRequest request) {
		AppResponse appResponse = new AppResponse();
		if (id.equals("build")) {
			operations.buildImage(content, fileUri, appResponse);
		} else if (id.equals("delete")) {
			operations.deleteImage(content, fileUri, appResponse);
		} else if(id.equals("showImages")) {
			operations.showImages(appResponse);
		} else if(id.equals("showAllContainers")) {
			operations.showAllContainers(appResponse);
		}
		return appResponse;
	}

}
