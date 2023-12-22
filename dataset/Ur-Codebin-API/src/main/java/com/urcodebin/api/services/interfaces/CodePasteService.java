package com.urcodebin.api.services.interfaces;

import com.urcodebin.api.controllers.requestbody.UploadPasteRequestBody;
import com.urcodebin.api.entities.CodePaste;
import com.urcodebin.api.enums.PasteSyntax;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CodePasteService {

    Optional<CodePaste> findByCodePasteId(UUID id);

    List<CodePaste> findListOfCodePastesBy(String pasteTitle, PasteSyntax pasteSyntax, int limit);

    List<CodePaste> findListOfCodePastesBy(String pasteTitle, int limit);

    CodePaste createNewCodePaste(UploadPasteRequestBody requestBody);

    void deleteCodePasteById(UUID pasteUUID);

    boolean doesCodePasteWithIdExist(UUID id);
}
