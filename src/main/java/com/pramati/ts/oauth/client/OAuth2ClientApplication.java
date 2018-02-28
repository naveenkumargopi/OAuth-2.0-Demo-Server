package com.pramati.ts.oauth.client;

import java.security.Principal;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableAutoConfiguration
@Configuration
@RestController
//@Controller
@EnableOAuth2Sso 
public class OAuth2ClientApplication extends WebSecurityConfigurerAdapter {

	@RequestMapping("/")
	public String home(OAuth2Authentication authentication) {
		String userAuthenticationPrincipal = authentication.getUserAuthentication().getPrincipal().toString();
		//String userAuthenticationName = authentication.getUserAuthentication().getName();
		String userAuthenticationDetails = authentication.getUserAuthentication().getDetails().toString();
		//String userAuthenticationCredentials = authentication.getUserAuthentication().getCredentials().toString();
		//Object userAuthenticationAuthorities = authentication.getUserAuthentication().getAuthorities();
		
		/*String authPrincipal = authentication.getPrincipal().toString();
		String authName = authentication.getName();
		String authDetails = authentication.getDetails().toString();
		String authCredentials = authentication.getCredentials().toString();
		Object authAuthorities = authentication.getAuthorities();*/
		
		String message = "<h3> Client Landing Page </h3> <br> Welcome <i>" + userAuthenticationPrincipal + "</i>! <br><br> <u>User Info:</u> <br>" + userAuthenticationDetails;
	  return message;
	}
	
	@RequestMapping("/loginPage")
	public String loginPage() {
	  return "loginPage.html";
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http
	      .antMatcher("/**")  //All requests are protected by default
	        .authorizeRequests()
	           .antMatchers("/loginPage**").permitAll()  //The home(index) page and login endpoints are explicitly excluded
	           .anyRequest().authenticated();  //All other endpoints require an authenticated user
	}
	
	public static void main(String[] args) {
	    new SpringApplicationBuilder(OAuth2ClientApplication.class)
	        .properties("spring.config.name=client").run(args);
	}
	
}
