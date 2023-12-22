package com.urcodebin.api.controllers;

import com.urcodebin.api.controllers.requestbody.UploadPasteRequestBody;
import com.urcodebin.api.dto.CodePasteDTO;
import com.urcodebin.api.entities.CodePaste;
import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.error.exception.MissingRequiredSourceCodeException;
import com.urcodebin.api.error.exception.PasteNotFoundException;
import com.urcodebin.api.services.interfaces.CodePasteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paste")
public class CodePasteController {

    private final CodePasteService codePasteService;

    private final ModelMapper modelMapper;

    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MIN_TITLE_LENGTH = 1;
    private static final int MAX_SEARCH_LIMIT = 20;
    private static final int MIN_SEARCH_LIMIT = 1;

    @Autowired
    public CodePasteController(@Qualifier("PasteService") CodePasteService codePasteService,
                               ModelMapper modelMapper) {
        this.codePasteService = codePasteService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(path = "/{paste_id}")
    public CodePasteDTO getCodePasteFromId(@PathVariable("paste_id") String pasteId) {
        UUID pasteUUID = createUUIDFromString(pasteId);
        Optional<CodePaste> foundPasteId = codePasteService.findByCodePasteId(pasteUUID);
        final CodePaste codePaste = foundPasteId.orElseThrow(() ->
                            new PasteNotFoundException("No CodePaste has been found with the given id."));
        return convertToDTO(codePaste);
    }

    @DeleteMapping(path = "/{paste_id}")
    public void deleteCodePasteWithId(@PathVariable("paste_id") String pasteId) {
        UUID pasteUUID = createUUIDFromString(pasteId);
        if(!codePasteService.doesCodePasteWithIdExist(pasteUUID))
            throw new PasteNotFoundException("No CodePaste has been found with the given id.");

        codePasteService.deleteCodePasteById(pasteUUID);
    }

    @GetMapping(path = "/public")
    public List<CodePasteDTO> getListOfPublicPastesWith(
                @RequestParam(value = "paste_title", defaultValue = "") String pasteTitle,
                @RequestParam(value = "paste_syntax", required = false) String pasteSyntax,
                @RequestParam(value = "limit", defaultValue = "5") int limit) {
        if(limit > MAX_SEARCH_LIMIT || limit < MIN_SEARCH_LIMIT)
            throw new IllegalArgumentException("limit number must be between 1 and 20.");

        List<CodePaste> listOfPastes;
        if(pasteSyntax == null) {
            listOfPastes = codePasteService.findListOfCodePastesBy(pasteTitle, limit);
        } else {
            PasteSyntax pasteSyntaxToSearch = createPasteSyntaxFromString(pasteSyntax);
            listOfPastes = codePasteService.findListOfCodePastesBy(pasteTitle, pasteSyntaxToSearch, limit);
        }
        return listOfPastes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public CodePasteDTO postNewCodePasteWith(@RequestBody UploadPasteRequestBody pasteRequestBody) {
        if(pasteRequestBody.getSourceCode().isEmpty())
            throw new MissingRequiredSourceCodeException("Required field (source_code) is missing.");

        int titleLength = pasteRequestBody.getPasteTitle().length();
        if(titleLength > MAX_TITLE_LENGTH || titleLength < MIN_TITLE_LENGTH) {
            String errorMsg = String.format("Paste Title field length must be within %s and %s",
                                                MIN_TITLE_LENGTH, MAX_TITLE_LENGTH);
            throw new IllegalArgumentException(errorMsg);
        }

        final CodePaste newCodePaste = codePasteService.createNewCodePaste(pasteRequestBody);
        return convertToDTO(newCodePaste);
    }

    private CodePasteDTO convertToDTO(CodePaste codePaste) {
        return modelMapper.map(codePaste, CodePasteDTO.class);
    }

    private UUID createUUIDFromString(String stringId) {
        try {
            return UUID.fromString(stringId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format, please format properly!");
        }
    }

    private PasteSyntax createPasteSyntaxFromString(String pasteSyntax) {
        try {
            return PasteSyntax.valueOf(pasteSyntax);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("paste_syntax parameter is not any of the given options.");
        }
    }
}
