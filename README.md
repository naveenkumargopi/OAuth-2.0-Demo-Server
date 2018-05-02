# ThumbSignIn OAuth Identity Provider (IdP)

This is a sample Identity Provider developed based on OAuth 2.0 protocol. This project shows how ThumbSignIn can be used as an Identity Provider's authentication mechanism.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

This is a Spring Boot java application which can be deployed on any application server. To run this project locally on the embedded Tomcat server (which comes with Spring Boot by default), follow the below steps.

Step 1: Go to the project folder and run the command:

`mvn clean install`

Step 2: Go to {project folder}/target and run below command:

`java -jar ThumbSignIn-OAuth-2.0-Server-0.0.1-SNAPSHOT.jar`

This IDP server can now be accessed locally via the URL: `http://localhost:8080`

Note: This IDP server has already been deployed in a test environment and can be accessed using URL `https://idp-stage.thumbsignin.com/`

## IDP Endpoints and Client Credentials

Authorization Endpoint: `<server_name>/oauth/authorize` (for e.g., `https://idp-stage.thumbsignin.com/oauth/authorize`)

Token Endpoint: `<server_name>/oauth/token`

Resource Endpoint: `<server_name>/me`

This sample IDP project supports only the below Client credentials. 

`client-id: oauthClient`

`client-secret: oauthClientSecret123`

## Steps for ThumbSignIn Integration

### Step 1: Adding ThumbSignIn widgets on the IDP's login (index.html) & registration pages (landingPage.html).

In the index.html & landingPage.html page, below things are configured to add the ThumbSignIn widget

a) "`thumbsign_widget.js`" file is imported in the header (Note: We have added this JS file locally)

b) A div container (`loginWidgetContainer` & `registerWidgetContainer`) is added. This div will render the ThumbSignIn widget

c) The ThumbSignin javascript widget configuration code is added in the body section. Here, the "`actionUrl`" & "`statusUrl`" needs to be updated
   to point to the internal java routes/controller classes. 
   
Note: Once the authentication/registration is invoked through the "actionUrl", then the transaction status URL will be polled for every 2 seconds to know the transaction status.

### Step 2: Adding Controller classes for the Authentication, Registration & Transaction Status API calls

a) The controller logic for authentication, registration & transaction status are written in `ThumbsigninApiController.java`.

b) The ThumbSignin App key & secret needs to be registered in ThumbSignIn website and needs to be configured in a property file.

Note: All the ThumbSignin java sdk files required for the integration are present in the "`com.pramati.ts.oauth.server.thumbsignin.sdk`" package.
   
