package com.project.heritagelink.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "HTTP Basic Authentication. Use admin/admin123 for full access or viewer/viewer123 for read-only."
)
public class OpenApiConfig {

    @Bean
    public OpenAPI heritageLinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HeritageLink API")
                        .description("""
                                **HeritageLink** is the backend inventory and heirloom management API for Heritage Transitions,
                                a boutique Senior Move Management firm.
                                
                                ## Authentication
                                All `/api/**` endpoints require HTTP Basic Authentication.
                                | Username | Password | Role | Access |
                                |----------|----------|------|--------|
                                | `admin`  | `admin123` | ADMIN | Full read/write |
                                | `viewer` | `viewer123`| VIEWER| GET only |
                                
                                Click the **Authorize** button above and enter your credentials.
                                
                                ## Item Lifecycle
                                Items move through four stages:
                                `IDENTIFIED` -> `APPRAISED` -> `ASSIGNED` -> `DISPOSED`
                                
                                ## Disposition Types
                                | Type | Description |
                                |------|-------------|
                                | GIFTING | Given to a named family member |
                                | DONATION| Donated to a charity |
                                | SALE | Sold; requires appraisal value > $0 |
                                | RELOCATION | Moved with client to new residence |
                                
                                ## Mediation Workflow
                                When two or more family members claim the same item with a **sentimental score >= 7**,
                                the item is automatically flagged as **MEDIATION_REQUIRED** and all status transitions
                                are blocked until the conflict is resolved via the `/api/claims/{id}/resolve` endpoint.
                                
                                ## Audit Trail
                                Every status change is recorded immutably in `/api/audit`, tagged with the authenticated user. 
                                """)
                        .version("1.0.0")
                )
                .servers(List.of(new Server().url("http://localhost:8080").description("Local Development")))
                .security(List.of(new SecurityRequirement().addList("basicAuth")));
    }

}
