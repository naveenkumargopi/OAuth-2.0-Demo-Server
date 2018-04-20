package com.pramati.ts.oauth.server.thumbsignin.sdk;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/ts/secure")
public class ThumbsigninApiController {

	@Autowired
    private ThumbsigninApiService thumbsigninApiService;
	
	@Value("${thumbsignin.apiKey}")
    private String thumbSignInApiKey;
	
	@Value("${thumbsignin.apiSecret}")
    private String thumbSignInApiSecret;
	
	@RequestMapping(value = { "/authenticate", "/register/{userId}", "/authStatus/{transactionId}", "/regStatus/{transactionId}" }, method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody	
	public String handleThumbSigninRequest(HttpServletRequest servletRequest) throws IOException {
		
		//Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String thumbsigninResponseStr = "";
				
		thumbsigninResponseStr = thumbsigninApiService.processThumbsigninRequest(servletRequest, thumbSignInApiKey, thumbSignInApiSecret);
		
		/*thumbsigninResponseStr = "{\"transactionId\":\"48df89eb2559c8c5858e2a7b4645518a3ba98543069c508\",\"status\":\"COMPLETED_SUCCESSFUL\",\"expireInSeconds\":\"67\",\"userId\":\"814944a8-4e01-49ad-8711-235e6021cbfe\",\"redirectUrl\":\"/ts/secure/loginSuccess\"}";
		if (servletRequest.getServletPath().contains("authStatus"))
			thumbsigninApiService.setUserAuthenticationInContext("814944a8-4e01-49ad-8711-235e6021cbfe");*/
		
		return thumbsigninResponseStr;
    	
	}
	
	@RequestMapping(value = { "/loginSuccess", "/registrationSuccess"})
	public String handleThumbSigninLoginSuccess(HttpServletRequest request) {
		HttpSession session = request.getSession();
		//Spring automatically stores the original request in the session
		//Redirecting the user to the path they were originally trying to access
		DefaultSavedRequest savedRequest = (DefaultSavedRequest)session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
		String redirectUrl = "";
		if (savedRequest != null) {
			//Redirecting to the original client application which invoked the OAuth server for login
			redirectUrl = savedRequest.getRedirectUrl();
		} else {
			//If no external client had invoked the IDP for login, then we are redirecting to an internal success page
			redirectUrl = (request.getRequestURI().contains("loginSuccess"))?"/ts/secure/authSuccessPage":"/ts/secure/regSuccessPage";
		}		
		return "redirect:"+redirectUrl;
	}
	
	@RequestMapping("/authSuccessPage")
	@ResponseBody
	public String authSuccessPage() {
		return "You have successfully authenticated via ThumbSignIn";
	}
	
	@RequestMapping("/regSuccessPage")
	@ResponseBody
	public String regSuccessPage() {
		return "You have successfully registered via ThumbSignIn";
	}
	
}
