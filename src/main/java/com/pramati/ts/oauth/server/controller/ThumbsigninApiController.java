package com.pramati.ts.oauth.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pramati.ts.oauth.server.service.ThumbsigninApiService;

@RestController
@RequestMapping(value = "/ts/secure")
public class ThumbsigninApiController {

	@Autowired
    private ThumbsigninApiService thumbsigninApiService;
	
	@RequestMapping(value = { "/authenticate", "/register/{userId}", "/authStatus/{transactionId}", "/regStatus/{transactionId}" }, method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public String handleThumbSigninRequest(HttpServletRequest servletRequest) throws IOException {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String thumbsigninResponseStr = "";
		//String apiKey = authentication.getPrincipal().toString();
		//String apiSecret = authentication.getCredentials().toString();
		String apiKey = "f825fccb97b541989858c233576df0bc";
		String apiSecret = "7c02776988b395f7cbedded756eb71c65e8fca41619fa77f85433f4bdb709f29";
		
		thumbsigninResponseStr = thumbsigninApiService.processThumbsigninRequest(servletRequest, apiKey, apiSecret);
    	
		return thumbsigninResponseStr;
    	
	}
	
}
