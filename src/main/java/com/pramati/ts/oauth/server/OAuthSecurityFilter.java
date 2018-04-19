package com.pramati.ts.oauth.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@RestController
//@EnableOAuth2Sso  //This can be used for auto configuration of OAuth2 client
@EnableOAuth2Client //Manual configuration of OAuth2 client
@EnableAuthorizationServer
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class OAuthSecurityFilter extends WebSecurityConfigurerAdapter {
	
	@Autowired
	OAuth2ClientContext oauth2ClientContext;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http
	      .csrf().disable() //To disable CSRF which is enabled by default by spring security
	      .antMatcher("/**")  //All requests are protected by default
	        .authorizeRequests()
	           .antMatchers("/", "/login**", "/ts/**", "/thumbsign_widget.js", "/webjars/**").permitAll()  //The home(index) page, login and ts endpoints are explicitly excluded
	           .anyRequest().authenticated()  //All other endpoints require an authenticated user
	        .and().exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))  //Unauthenticated users are re-directed to the home page	        
	        .and().logout().logoutSuccessUrl("/").permitAll()
	        //.and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) //Adding CSRF filter which will generate the XSRF-TOKEN cookie to prevent CSRF attacks (CSRF needs to be enabled)
	        .and().addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}
	
	private Filter ssoFilter() {
		  CompositeFilter compositeFilter = new CompositeFilter();
		  List<Filter> filters = new ArrayList<>();
		  filters.add(ssoFilter(facebook(), "/login/facebook"));
		  filters.add(ssoFilter(github(), "/login/github"));
		  compositeFilter.setFilters(filters);
		  return compositeFilter;
    }
	
	private Filter ssoFilter(OAuthProviderInfo oAuthProvider, String path) {
		  /*
		   * An OAuth2 client filter can be used to acquire an OAuth2 access token from an authorization server, and load an
		   * authentication object into the SecurityContext
		   */
		  OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
		  OAuth2RestTemplate template = new OAuth2RestTemplate(oAuthProvider.getAuthorizationServer(), oauth2ClientContext);
		  filter.setRestTemplate(template);
		  UserInfoTokenServices tokenServices = new UserInfoTokenServices(
				  oAuthProvider.getResourceServer().getUserInfoUri(), oAuthProvider.getAuthorizationServer().getClientId());
		  tokenServices.setRestTemplate(template);
		  filter.setTokenServices(tokenServices);
		  return filter;
    }
	
	class OAuthProviderInfo {

		  @NestedConfigurationProperty
		  private AuthorizationCodeResourceDetails authorizationServer = new AuthorizationCodeResourceDetails();

		  @NestedConfigurationProperty
		  private ResourceServerProperties resourceServer = new ResourceServerProperties();

		  public AuthorizationCodeResourceDetails getAuthorizationServer() {
		    return authorizationServer;
		  }

		  public ResourceServerProperties getResourceServer() {
		    return resourceServer;
		  }
	}
	
	@Bean
	@ConfigurationProperties("github")
	public OAuthProviderInfo github() {
	  return new OAuthProviderInfo();
	}

	@Bean
	@ConfigurationProperties("facebook")
	public OAuthProviderInfo facebook() {
	  return new OAuthProviderInfo();
	}
	
	//Handling the Redirects
	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(
	    OAuth2ClientContextFilter filter) {
	  FilterRegistrationBean registration = new FilterRegistrationBean();
	  registration.setFilter(filter);
	  registration.setOrder(-100);
	  return registration;
	}
	
}
