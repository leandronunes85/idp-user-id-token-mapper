[![Build Status](https://travis-ci.org/leandronunes85/idp-user-id-token-mapper.svg?branch=master)](https://travis-ci.org/leandronunes85/idp-user-id-token-mapper)

# Identity Provider (IdP) User ID Token Mapper 

This is a [Keycloak](https://github.com/keycloak/keycloak) Service Provider Interface (SPI - aka plugin) that exposes a third-party user identifier in tokens issued by Keycloak.

When a third-party IdP (either public - like Google or Facebook - or private/internal to your organization) is set-up in Keycloak it can be useful to allow their ids to be exposed to certain clients. This SPI tries to make this goal easier to achieve by creating a token mapper that works in a similar way as the ones that ship with Keycloak.

## Installing

If you have Keycloak already running in your machine and want to test this SPI there, running 
```bash
mvn wildfly:deploy
``` 
should do the trick. 
###### Note 
>Make sure your WildFly server has the "Http management interface" listening on port 9990.

You can also simply
```bash
mvn package
```
and use the `idp-user-id-token-mapper.jar` inside the `target` directory to install it as you would install other SPIs to running instances of Keycloak (check [Keycloak's documentation on SPIs](https://www.keycloak.org/docs/5.0/server_development/#_providers) for more details).

## Configuring a mapper

Before setting up this token mapper you'll need to set up a OpenID Connect Identity Provider (more info [here](https://www.keycloak.org/docs/5.0/server_admin/index.html#openid-connect-v1-0-identity-providers)). Make sure you take note of the "Alias" you used because you'll need it later.

This mapper can be applied to a "Client Scope" so make sure you either create a new one (more info [here](https://www.keycloak.org/docs/5.0/server_admin/index.html#_client_scopes)) or pick an existing one. Navigate to the "Mappers" tab and hit the "Create" button. Now you only need to pick a name (always hard, right?) for your mapper, choose "Identity Provider's User ID Mapper" as "Mapper Type" and type down your "Identity Provider Alias" (I told you to note it down :grin:) and choose the "Token Claim Name". You can also pick and choose which tokens should include this third-party identityfier. 

And you should be all set by now! 
###### Note 
>Make sure that whatever client you're using to test this has access to the scope you added the mapping to and that you
>include this scope where appropriate.

Happy tokenning!
