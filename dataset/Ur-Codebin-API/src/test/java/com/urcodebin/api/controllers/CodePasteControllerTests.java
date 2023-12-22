package com.urcodebin.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.JsonPath;
import com.urcodebin.api.controllers.requestbody.UploadPasteRequestBody;
import com.urcodebin.api.dto.CodePasteDTO;
import com.urcodebin.api.entities.CodePaste;
import com.urcodebin.api.enums.PasteExpiration;
import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.enums.PasteVisibility;
import com.urcodebin.api.services.interfaces.CodePasteService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CodePasteController.class)
public class CodePasteControllerTests {

    private MockMvc mockMvc;

    @MockBean
    @Qualifier("PasteService")
    public CodePasteService codePasteService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    private final ModelMapper modelMapper = new ModelMapper();

    CodePaste firstPaste = new CodePaste();
    CodePasteDTO firstPasteDTO;
    CodePaste secondPaste = new CodePaste();
    CodePasteDTO secondPasteDTO;

    private static final String PUBLIC_PASTE_PATH = "/api/paste/public";
    private static final String PASTE_FROM_ID_PATH = "/api/paste/{pasteId}";
    private static final String POST_NEW_PASTE_PATH = "/api/paste";

    private static final String PASTE_TITLE = "paste_title";
    private static final String PASTE_VISIBILITY = "paste_visibility";
    private static final String PASTE_EXPIRATION = "paste_expiration";
    private static final String PASTE_SYNTAX = "paste_syntax";
    private static final String SOURCE_CODE = "source_code";

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);

    private final String LIMIT = "limit";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        firstPaste.setPasteVisibility(PasteVisibility.PUBLIC);
        firstPaste.setPasteSyntax(PasteSyntax.JAVA);
        firstPaste.setPasteTitle("My Fake Paste");
        firstPaste.setSourceCode("System.out.println('this is code);");
        firstPaste.setPasteExpirationDate(pasteExpirationToDateTime(PasteExpiration.TENMINUTES));
        firstPasteDTO = convertToDTO(firstPaste);

        secondPaste.setPasteVisibility(PasteVisibility.PUBLIC);
        secondPaste.setPasteSyntax(PasteSyntax.JAVA);
        secondPaste.setPasteTitle("My Java Program");
        secondPaste.setSourceCode("Object myObj = new Object();");
        secondPaste.setPasteExpirationDate(pasteExpirationToDateTime(PasteExpiration.ONEHOUR));
        secondPasteDTO = convertToDTO(secondPaste);
    }

    private CodePasteDTO convertToDTO(CodePaste codePaste) {
        return modelMapper.map(codePaste, CodePasteDTO.class);
    }

    private LocalDateTime pasteExpirationToDateTime(PasteExpiration expiration) {
        return LocalDateTime.now().plusMinutes(expiration.getOffsetMin());
    }

    @Test
    public void getPasteFromIdWithCorrectIdReturnsFoundCodePaste() throws Exception {
        when(codePasteService.findByCodePasteId(firstPaste.getPasteId())).thenReturn(Optional.of(firstPaste));

        mockMvc.perform(get(PASTE_FROM_ID_PATH, firstPaste.getPasteId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".*", is(convertPasteToList(firstPasteDTO))));

        verify(codePasteService, times(1)).findByCodePasteId(any(UUID.class));
    }

    @Test
    public void getPasteFromIdWithWrongIdResultsInHttpNotFound() throws Exception {
        when(codePasteService.findByCodePasteId(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get(PASTE_FROM_ID_PATH, UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());

        verify(codePasteService, times(1)).findByCodePasteId(any(UUID.class));
    }

    @Test
    public void getPasteWithInvalidIdFormatResultsInHttpBadRequest() throws Exception {
        mockMvc.perform(get(PASTE_FROM_ID_PATH, "Invalid UUID Format"))
                .andExpect(status().isBadRequest());

        verify(codePasteService, times(0)).findByCodePasteId(any(UUID.class));
    }

    @Test
    public void deletePasteWithCorrectIdResultsInHttpOkRequest() throws Exception {
        when(codePasteService.doesCodePasteWithIdExist(firstPaste.getPasteId())).thenReturn(true);

        mockMvc.perform(delete(PASTE_FROM_ID_PATH, firstPaste.getPasteId().toString()))
                .andExpect(status().isOk());

        verify(codePasteService, times(1))
                .doesCodePasteWithIdExist(firstPaste.getPasteId());
        verify(codePasteService, times(1))
                .deleteCodePasteById(firstPaste.getPasteId());
    }

    @Test
    public void deletePasteWithInvalidIdFormatResultsInHttpBadRequest() throws Exception {
        mockMvc.perform(delete(PASTE_FROM_ID_PATH, "Wrong ID Format"))
                .andExpect(status().isBadRequest());

        verify(codePasteService, times(0))
                .doesCodePasteWithIdExist(any(UUID.class));
        verify(codePasteService, times(0))
                .deleteCodePasteById(any(UUID.class));
    }

    @Test
    public void deletePasteWithWrongIdResultsInHttpNotFound() throws Exception {
        when(codePasteService.doesCodePasteWithIdExist(any(UUID.class))).thenReturn(false);

        mockMvc.perform(delete(PASTE_FROM_ID_PATH, UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());

        verify(codePasteService, times(1))
                .doesCodePasteWithIdExist(any(UUID.class));
        verify(codePasteService, times(0))
                .deleteCodePasteById(any(UUID.class));
    }

    @Test
    public void getPublicPastesWithAllValidParametersFilledReturnsListOfFoundPastes() throws Exception {
        when(codePasteService.findListOfCodePastesBy("My", PasteSyntax.JAVA, 2))
                .thenReturn(Arrays.asList(firstPaste, secondPaste));

        final MockHttpServletRequestBuilder request =  get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "My")
                .param(PASTE_SYNTAX, "JAVA")
                .param(LIMIT, "2");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].*", is(convertPasteToList(firstPasteDTO))))
                .andExpect(jsonPath("$[1].*", is(convertPasteToList(secondPasteDTO))));

        verify(codePasteService, times(0)).findListOfCodePastesBy(anyString(), anyInt());
    }

    @Test
    public void getPublicPastesWithWrongPasteTitleReturnsNoFoundPastes() throws Exception {
        when(codePasteService.findListOfCodePastesBy("Wrong", PasteSyntax.JAVA, 2))
                .thenReturn(Collections.emptyList());

        final MockHttpServletRequestBuilder request = get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "Wrong")
                .param(PASTE_SYNTAX, "JAVA")
                .param(LIMIT, "2");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath(".*", is(Collections.emptyList())));

        verify(codePasteService, times(0)).findListOfCodePastesBy(anyString(), anyInt());
    }

    @Test
    public void getPublicPastesWithMissingPasteTitleParameterReturnsListOfFoundPastes() throws Exception {
        when(codePasteService.findListOfCodePastesBy("", PasteSyntax.JAVA, 2))
                .thenReturn(Arrays.asList(firstPaste, secondPaste));

        final MockHttpServletRequestBuilder request = get(PUBLIC_PASTE_PATH)
                .param(PASTE_SYNTAX, "JAVA")
                .param(LIMIT, "2");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].*", is(convertPasteToList(firstPasteDTO))))
                .andExpect(jsonPath("$[1].*", is(convertPasteToList(secondPasteDTO))));

        verify(codePasteService, times(0)).findListOfCodePastesBy(anyString(), anyInt());
    }

    @Test
    public void getPublicPasteWithMissingPasteSyntaxParameterReturnsListOfFoundPastes() throws Exception {
        when(codePasteService.findListOfCodePastesBy("My", 2))
                .thenReturn(Arrays.asList(firstPaste, secondPaste));

        final MockHttpServletRequestBuilder request = get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "My")
                .param(LIMIT, "2");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].*", is(convertPasteToList(convertToDTO(firstPaste)))))
                .andExpect(jsonPath("$[1].*", is(convertPasteToList(secondPasteDTO))));

        verify(codePasteService, times(0))
                .findListOfCodePastesBy(anyString(), any(PasteSyntax.class), anyInt());
    }

    @Test
    public void getPublicPasteWithInvalidPasteSyntaxParameterResultsInHttpBadRequest() throws Exception {
        final MockHttpServletRequestBuilder request = get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "My")
                .param(PASTE_SYNTAX, "Invalid Input")
                .param(LIMIT, "2");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(codePasteService, times(0))
                .findListOfCodePastesBy(anyString(), any(PasteSyntax.class), anyInt());
        verify(codePasteService, times(0))
                .findListOfCodePastesBy(anyString(), anyInt());
    }

    @Test
    public void getPublicPasteWithMissingLimitParameterReturnsListOfFoundPastes() throws Exception {
        when(codePasteService.findListOfCodePastesBy("My", PasteSyntax.JAVA, 5))
                .thenReturn(Arrays.asList(firstPaste, secondPaste));

        final MockHttpServletRequestBuilder request = get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "My")
                .param(PASTE_SYNTAX, "JAVA");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].*", is(convertPasteToList(firstPasteDTO))))
                .andExpect(jsonPath("$[1].*", is(convertPasteToList(secondPasteDTO))));

        verify(codePasteService, times(0))
                .findListOfCodePastesBy(anyString(), anyInt());
    }

    @Test
    public void getPublicPasteWithOutOfBoundsLimitParameterResultsInHttpBadRequest() throws Exception {
        final MockHttpServletRequestBuilder belowLimitRequest = get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "My")
                .param(PASTE_SYNTAX, "Invalid Input")
                .param(LIMIT, "0");

        final MockHttpServletRequestBuilder moreThanLimitRequest = get(PUBLIC_PASTE_PATH)
                .param(PASTE_TITLE, "My")
                .param(PASTE_SYNTAX, "Invalid Input")
                .param(LIMIT, "21");

        mockMvc.perform(belowLimitRequest)
                .andExpect(status().isBadRequest());
        mockMvc.perform(moreThanLimitRequest)
                .andExpect(status().isBadRequest());

        verify(codePasteService, times(0))
                .findListOfCodePastesBy(anyString(), any(PasteSyntax.class), anyInt());
        verify(codePasteService, times(0))
                .findListOfCodePastesBy(anyString(), anyInt());
    }

    @Test
    public void postNewCodePasteWithCorrectFormatReturnsUploadedCodePaste() throws Exception {
        when(codePasteService.createNewCodePaste(any(UploadPasteRequestBody.class))).thenReturn(firstPaste);

        String requestBody = new JSONObject()
                .put(PASTE_TITLE, firstPaste.getPasteTitle())
                .put(PASTE_SYNTAX, firstPaste.getPasteSyntax().toString())
                .put(SOURCE_CODE, firstPaste.getSourceCode())
                .put(PASTE_VISIBILITY, firstPaste.getPasteVisibility().toString())
                .put(PASTE_EXPIRATION, PasteExpiration.TENMINUTES.toString())
                .toString();

        final MockHttpServletRequestBuilder request = post(POST_NEW_PASTE_PATH)
                .contentType(APPLICATION_JSON_UTF8).content(requestBody);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath(".*", is(convertPasteToList(firstPasteDTO))));

        verify(codePasteService, times(1)).createNewCodePaste(any());
    }

    private List<Object> convertPasteToList(CodePasteDTO codePastes) {
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
