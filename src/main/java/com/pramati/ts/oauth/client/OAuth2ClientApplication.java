package com.pramati.ts.oauth.client;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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
		String userAuthenticationDetails = authentication.getUserAuthentication().getDetails().toString();		
		String message = "<h3> Client Landing Page </h3> <br> Welcome <i>" + userAuthenticationPrincipal + "</i>! <br><br> <u>User Info:</u> <br>" + userAuthenticationDetails;
	  return message;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http
	      .antMatcher("/**")  //All requests are protected by default
	        .authorizeRequests()
	           //.antMatchers("/loginPage**").permitAll()  
	           .anyRequest().authenticated();  //All other endpoints require an authenticated user
	}
	
	public static void main(String[] args) {
	    new SpringApplicationBuilder(OAuth2ClientApplication.class)
	        .properties("spring.config.name=client").run(args);
	}
	
}
