package com.jin.practice.config;

import com.jin.practice.auth.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void userRoleCannotCallReception() throws Exception {
        String token = jwtProvider.createToken(new UsernamePasswordAuthenticationToken(
                "user@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        )).accessToken();

        mockMvc.perform(patch("/api/reception/1/call")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void hospitalAdminCannotCreateHospitalAdminMembers() throws Exception {
        String token = jwtProvider.createToken(new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_HOSPITAL_ADMIN"))
        )).accessToken();

        mockMvc.perform(post("/api/admin/members/hospital-admin")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "new-admin@example.com",
                                  "password": "password1234",
                                  "name": "Admin",
                                  "age": 30,
                                  "phone": "010-9999-1111",
                                  "gender": "NONE",
                                  "region": "Seoul",
                                  "extra": "",
                                  "hospitalId": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
