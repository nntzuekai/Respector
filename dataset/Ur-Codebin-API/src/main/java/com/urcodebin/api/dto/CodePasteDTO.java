package com.urcodebin.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.enums.PasteVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

public class CodePasteDTO {

    @JsonProperty("paste_id")
    private UUID pasteId;

    @JsonProperty("source_code")
    private String sourceCode;

    @JsonProperty("paste_title")
    private String pasteTitle;

    @JsonProperty("paste_syntax")
    private PasteSyntax pasteSyntax;

    @JsonProperty("paste_expiration_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime pasteExpirationDate;

    @JsonProperty("paste_visibility")
    private PasteVisibility pasteVisibility;

    public UUID getPasteId() {
        return UUID.fromString(pasteId.toString());
    }

    public void setPasteId(UUID pasteId) {
        this.pasteId = UUID.fromString(pasteId.toString());
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getPasteTitle() {
        return pasteTitle;
    }

    public void setPasteTitle(String pasteTitle) {
        this.pasteTitle = pasteTitle;
    }

    public PasteSyntax getPasteSyntax() {
        return pasteSyntax;
    }

    public void setPasteSyntax(PasteSyntax pasteSyntax) {
        this.pasteSyntax = pasteSyntax;
    }

    public LocalDateTime getPasteExpirationDate() {
        return LocalDateTime.from(pasteExpirationDate);
    }

    public void setPasteExpirationDate(LocalDateTime pasteExpirationDate) {
        this.pasteExpirationDate = LocalDateTime.from(pasteExpirationDate);
    }

    public PasteVisibility getPasteVisibility() {
        return pasteVisibility;
    }

    public void setPasteVisibility(PasteVisibility pasteVisibility) {
        this.pasteVisibility = pasteVisibility;
    }
}
