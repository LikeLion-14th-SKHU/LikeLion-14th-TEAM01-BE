package org.skhuconnect.mcmbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mcm;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "openapi.server-url=https://mcm-api.i1000u.store",
        "auth.kakao.client-id=test-client-id",
        "auth.kakao.client-secret=",
        "auth.kakao.redirect-uri=http://localhost:8080/detective/auth/kakao/callback",
        "auth.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "auth.jwt.access-token-expiration=1800000",
        "auth.jwt.refresh-token-expiration=1209600000"
})
@AutoConfigureMockMvc
class McmBeApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void protectedRequestIsStatelessAndDoesNotCreateSessionCookie() throws Exception {
        mockMvc.perform(get("/detective/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void kakaoLoginRedirectsWithoutCreatingSession() throws Exception {
        mockMvc.perform(get("/detective/auth/kakao/login"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString(
                        "https://kauth.kakao.com/oauth/authorize"
                )))
                .andExpect(header().string("Location", containsString(
                        "redirect_uri=http://localhost:8080/detective/auth/kakao/callback"
                )))
                .andExpect(header().string("Location", containsString("state=")))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void swaggerDocumentsDetectiveAuthPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url")
                        .value("https://mcm-api.i1000u.store"))
                .andExpect(jsonPath("$.paths['/detective/auth/kakao/login']").exists())
                .andExpect(jsonPath("$.paths['/detective/auth/kakao/callback']").exists())
                .andExpect(jsonPath("$.paths['/detective/auth/refresh']").exists())
                .andExpect(jsonPath("$.paths['/detective/auth/logout'].post").exists())
                .andExpect(jsonPath("$.paths['/detective/members/me'].delete").exists())
                .andExpect(jsonPath("$.paths['/detective/auth/kakao/authorization-url']").doesNotExist());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/detective/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void withdrawalRequiresAuthentication() throws Exception {
        mockMvc.perform(delete("/detective/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void swaggerDocumentsProductRecommendationPath() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/detective/products/recommendation'].get"
                ).exists());
    }

    @Test
    void productRecommendationRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/detective/products/recommendation"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void allowsLocalFrontendCorsRequest() throws Exception {
        mockMvc.perform(get("/detective/auth/kakao/login")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isFound())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void allowsDeployedFrontendCorsRequest() throws Exception {
        mockMvc.perform(get("/detective/auth/kakao/login")
                        .header("Origin", "https://seongju-detective.vercel.app"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "https://seongju-detective.vercel.app"
                ));
    }

    @Test
    void allowsCorsPreflightWithoutAuthentication() throws Exception {
        mockMvc.perform(options("/detective/games/final-deduction")
                        .header("Origin", "https://seongju-detective.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "https://seongju-detective.vercel.app"
                ))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("authorization")));
    }

}
