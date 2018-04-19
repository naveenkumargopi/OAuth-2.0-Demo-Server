package com.pramati.ts.oauth.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pramati.ts.oauth.server.service.ThumbsigninApiService;

//@RestController //This is equivalent to @Controller + @ResponseBody
@Controller
@RequestMapping(value = "/ts/secure")
public class ThumbsigninApiController {

	@Autowired
    private ThumbsigninApiService thumbsigninApiService;
	
	@RequestMapping(value = { "/authenticate", "/register/{userId}", "/authStatus/{transactionId}", "/regStatus/{transactionId}" }, method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody	//It converts the response to JSON/XML automatically.
	public String handleThumbSigninRequest(HttpServletRequest servletRequest) throws IOException {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String thumbsigninResponseStr = "";
		//String apiKey = authentication.getPrincipal().toString();
		//String apiSecret = authentication.getCredentials().toString();
		//String apiKey = "f825fccb97b541989858c233576df0bc";
		//String apiSecret = "7c02776988b395f7cbedded756eb71c65e8fca41619fa77f85433f4bdb709f29";
		String apiKey = "3322486d41784e19bd3b4faa526a76ed";
		String apiSecret = "6d91efa1e5c882ea3ea4538c24517aaee200368f404b3e1fe9fc979f2b75c6c0";
				
		thumbsigninResponseStr = thumbsigninApiService.processThumbsigninRequest(servletRequest, apiKey, apiSecret);
		
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
			redirectUrl = savedRequest.getRedirectUrl();
		} else {			
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
