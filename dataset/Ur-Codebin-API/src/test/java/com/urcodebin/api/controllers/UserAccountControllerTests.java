package com.urcodebin.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.JsonPath;
import com.urcodebin.api.controllers.requestbody.SignupRequestBody;
import com.urcodebin.api.dto.UserAccountDTO;
import com.urcodebin.api.entities.UserAccount;
import com.urcodebin.api.error.exception.UserAccountNotFoundException;
import com.urcodebin.api.services.interfaces.UserAccountService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserAccountController.class)
public class UserAccountControllerTests {

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("AccountService")
    public UserAccountService userAccountService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    private final ModelMapper modelMapper = new ModelMapper();

    UserAccount fakeAccount = new UserAccount();
    UserAccountDTO fakeAccountDTO;

    private final static String GET_ACCOUNT_FROM_ID_PATH = "/api/account/{accountId}";
    private final static String SIGNUP_ACCOUNT_PATH = "/api/account/signup";

    private final static String ACCOUNT_USERNAME = "account_username";
    private final static String ACCOUNT_EMAIL = "account_email";
    private final static String ACCOUNT_PASSWORD = "account_password";

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        fakeAccount.setId(100L);
        fakeAccount.setUsername("Fake Account");
        fakeAccount.setEmail("fakeEmail@gmail.com");
        fakeAccountDTO = convertToDTO(fakeAccount);
    }

    private UserAccountDTO convertToDTO(UserAccount userAccount) {
        return modelMapper.map(userAccount, UserAccountDTO.class);
    }

    @Test
    public void getUserAccountByIdWithCorrectIdReturnsRightAccount() throws Exception {
        when(userAccountService.getUserAccountById(fakeAccount.getId())).thenReturn(Optional.of(fakeAccount));

        mockMvc.perform(get(GET_ACCOUNT_FROM_ID_PATH, fakeAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".*", is(convertToListFormat(fakeAccountDTO))));

        verify(userAccountService, times(1)).getUserAccountById(any());
    }

    @Test
    public void getUserAccountByIdWithWrongIdReturnsHttpNotFound() throws Exception {
        when(userAccountService.getUserAccountById(any())).thenThrow(UserAccountNotFoundException.class);

        mockMvc.perform(get(GET_ACCOUNT_FROM_ID_PATH, 1001))
                .andExpect(status().isNotFound());

        verify(userAccountService, times(1)).getUserAccountById(any());
    }

    @Test
    public void signupForNewAccountWithValidRequestBodyReturnsCreatedAccount() throws Exception {
        when(userAccountService.signupNewUserAccount(any(SignupRequestBody.class))).thenReturn(fakeAccount);

        String requestBody = new JSONObject()
                .put(ACCOUNT_USERNAME, fakeAccount.getUsername())
                .put(ACCOUNT_PASSWORD, fakeAccount.getPassword())
                .put(ACCOUNT_EMAIL, fakeAccount.getEmail()).toString();

        final MockHttpServletRequestBuilder request = post(SIGNUP_ACCOUNT_PATH)
                .contentType(APPLICATION_JSON_UTF8).content(requestBody);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath(".*", is(convertToListFormat(fakeAccountDTO))));
    }

    @Test
    public void signupForNewAccountWithTakenUsernameResultsInHttpConflict() throws Exception {
        when(userAccountService.isAccountUsernameTaken("Taken Username")).thenReturn(true);

        String requestBody = new JSONObject()
                .put(ACCOUNT_USERNAME, "Taken Username")
                .put(ACCOUNT_PASSWORD, "Password")
                .put(ACCOUNT_EMAIL, "email@fake.com").toString();

        final MockHttpServletRequestBuilder request = post(SIGNUP_ACCOUNT_PATH)
                .contentType(APPLICATION_JSON_UTF8).content(requestBody);

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void signupForNewAccountWithTakenEmailResultsInHttpConflict() throws Exception {
        when(userAccountService.isAccountEmailTaken("Taken@email.com")).thenReturn(true);

        String requestBody = new JSONObject()
                .put(ACCOUNT_USERNAME, "Username")
                .put(ACCOUNT_PASSWORD, "Password")
                .put(ACCOUNT_EMAIL, "Taken@email.com").toString();

        final MockHttpServletRequestBuilder request = post(SIGNUP_ACCOUNT_PATH)
                .contentType(APPLICATION_JSON_UTF8).content(requestBody);

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void signupForNewAccountWithInvalidEmailFormatResultsInHttpBadRequest() throws Exception {
        String requestBody = new JSONObject()
                .put(ACCOUNT_USERNAME, "Username")
                .put(ACCOUNT_PASSWORD, "Password")
                .put(ACCOUNT_EMAIL, "WrongFormat@").toString();

        final MockHttpServletRequestBuilder request = post(SIGNUP_ACCOUNT_PATH)
                .contentType(APPLICATION_JSON_UTF8).content(requestBody);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    private List<Object> convertToListFormat(UserAccountDTO codePastes) {
        ObjectWriter writer = new ObjectMapper().writer();
        JSONArray jsonArray;
        try {
            String initialJson = writer.writeValueAsString(codePastes);
            jsonArray = new JSONArray(JsonPath.read(initialJson, ".*").toString());
        } catch (JsonProcessingException | JSONException e) {
            return Collections.emptyList();
        }

        return IntStream.range(0, jsonArray.length())
                .mapToObj(index -> {
                    try {
                        return jsonArray.get(index);
                    } catch (JSONException e) {
                        return "";
                    }
                })
                .collect(Collectors.toList());
    }
}
