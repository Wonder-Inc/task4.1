package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(username = "olga", password = "test")
public class UserServiceImplTest extends BaseIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private Keycloak keycloak;
    
    @MockBean
    private List<RoleRepresentation> roleRepresentations;
    
    @MockBean
    private List<GroupRepresentation> groupRepresentations;
    
    @Test
    @Order(0)
    public void createUser() throws Exception {
        UserRequest userRequest = new UserRequest("vanik", "ivan@mail.ru", "12345", "Ivan", "Ivanov");
        Response response = Response
                .status(Response.Status.CREATED)
                .location(new URI("user_id"))
                .build();
        when(keycloak.realm(anyString())).thenReturn(mock(RealmResource.class));
        when(keycloak.realm(anyString()).users()).thenReturn(mock(UsersResource.class));
        when(keycloak.realm(anyString()).users().create(any())).thenReturn(response);
        userService.createUser(userRequest);
        verify(keycloak.realm(anyString()).users(), times(1)).create(any());
    }
    
    @Test
    @Order(1)
    public void getUserById() {
        UserRepresentation user = new UserRepresentation();
        UUID id = UUID.randomUUID();
        user.setId(String.valueOf(id));
        user.setEmail("user@example.com");
        
        when(keycloak.realm(anyString())).thenReturn(mock(RealmResource.class));
        when(keycloak.realm(anyString()).users()).thenReturn(mock(UsersResource.class));
        when(keycloak.realm(anyString()).users().get(anyString())).thenReturn(mock(UserResource.class));
        when(keycloak.realm(anyString()).users().get(anyString()).toRepresentation()).thenReturn(user);
        when(keycloak.realm(anyString()).users().get(anyString()).roles()).thenReturn(mock(RoleMappingResource.class));
        when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll()).thenReturn(mock(MappingsRepresentation.class));
        when(keycloak.realm(anyString()).users().get(anyString()).roles().getAll().getRealmMappings()).thenReturn(roleRepresentations);
        when(keycloak.realm(anyString()).users().get(anyString()).groups()).thenReturn(groupRepresentations);
        
        UserResponse response = userService.getUserById(id);
        assertEquals("user@example.com", response.getEmail());
    }
}
