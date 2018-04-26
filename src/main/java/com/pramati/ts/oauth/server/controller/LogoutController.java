package com.pramati.ts.oauth.server.controller;


import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class LogoutController {
    
	@Autowired
    private ConsumerTokenServices consumerTokenServices;


    @RequestMapping(value="/invalidateSession", method= RequestMethod.POST)
    @ResponseBody
    public String logout(/*@RequestParam(name = "access_token") String accessToken,*/ HttpServletRequest request, HttpServletResponse response) {

        //consumerTokenServices.revokeToken(accessToken);
        
        new SecurityContextLogoutHandler().logout(request, null, null);
            //sending back to client app
            //response.sendRedirect(request.getHeader("referer"));            
            return "Success";
    }
	
}