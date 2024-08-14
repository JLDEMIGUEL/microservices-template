package com.jldemiguel.microservice1.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = {@Server(url = "https://localhost:8080")},
        info = @Info(title = "Department Service APIs",
                description = "This lists all the Department Service API Calls. The Calls are OAuth2 secured, "
                        + "so please use your client ID and Secret to test them out.",
                version = "v1.0"))
@SecurityScheme(name = "security_auth", type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode = @OAuthFlow(tokenUrl = "http://localhost:9000/oauth2/token",
                authorizationUrl = "http://127.0.0.1:9000/oauth2/authorize",
                scopes = {@OAuthScope(name = "openid", description = "openid scope")
                })))
public class SwaggerConfig {
}