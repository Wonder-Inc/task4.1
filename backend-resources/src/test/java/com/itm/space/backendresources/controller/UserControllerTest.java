package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(username = "olga", password = "test", authorities = "ROLE_MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {
    
    @MockBean
    private Keycloak keycloak;
    
    @MockBean
    private List<RoleRepresentation> roleRepresentations;
    
    @MockBean
    private List<GroupRepresentation> groupRepresentations;
    
    private ObjectMapper mapper = new ObjectMapper();
    private UserRequest userRequest;
    
    @BeforeEach
    void setUp() {
        when(keycloak.realm(anyString())).thenReturn(mock(RealmResource.class));
        when(keycloak.realm(anyString()).users()).thenReturn(mock(UsersResource.class));
        userRequest = new UserRequest("vanik", "ivan@mail.ru", "12345", "Ivan", "Ivanov");
    }
    
    @Test
    @Order(0)
    void testHello() throws Exception {
        MvcResult result = mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        assertEquals("olga", responseContent);
    }
    
    @Test
    @Order(1)
    void create() {
        try {
            Response response = Response.status(Response.Status.CREATED).location(new URI("user_id")).build();
            when(keycloak.realm(anyString())).thenReturn(mock(RealmResource.class));
            when(keycloak.realm(anyString()).users()).thenReturn(mock(UsersResource.class));
            when(keycloak.realm(anyString()).users().create(any())).thenReturn(response);
            mvc.perform(requestWithContent(post("/api/users"), userRequest));
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(keycloak.realm(anyString()).users(), times(1)).create(any());
    }
    
    @Test
    @Order(2)
    void getUserById() {
        try {
            UUID id = UUID.randomUUID();
            UserRepresentation user = new UserRepresentation();
            user.setId(String.valueOf(id));
            user.setFirstName("Petr");
            
            when(keycloak.realm(anyString()).users().get(any())).thenReturn(mock(UserResource.class));
            when(keycloak.realm(anyString()).users().get(any()).toRepresentation()).thenReturn(user);
            when(keycloak.realm(anyString()).users().get(any()).roles()).thenReturn(mock(RoleMappingResource.class));
            when(keycloak.realm(anyString()).users().get(any()).roles().getAll()).thenReturn(mock(MappingsRepresentation.class));
            when(keycloak.realm(anyString()).users().get(any()).roles().getAll().getRealmMappings()).thenReturn(roleRepresentations);
            when(keycloak.realm(anyString()).users().get(any()).groups()).thenReturn(groupRepresentations);
            
            MvcResult result = mvc.perform(get("/api/users/{id}", id)).andReturn();
            UserResponse userResponse = mapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
            assertEquals("Petr", userResponse.getFirstName());
            verify(keycloak.realm(anyString()).users(), times(8)).get(any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
