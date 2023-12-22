package com.urcodebin.api.controllers.requestbody;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.urcodebin.api.enums.PasteExpiration;
import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.enums.PasteVisibility;

/**
 * Object that is used when handling in the POST request to
 * upload a new Code Paste. JSON request body is mapped to this
 * object to be further used.
 */
public class UploadPasteRequestBody {

    @JsonProperty(value = "paste_title")
    @JsonSetter(nulls = Nulls.SKIP)
    private String pasteTitle;

    @JsonProperty(value = "paste_syntax")
    @JsonSetter(nulls = Nulls.SKIP)
    private PasteSyntax pasteSyntax;

    @JsonProperty(value = "paste_visibility")
    @JsonSetter(nulls = Nulls.SKIP)
    private PasteVisibility pasteVisibility;

    @JsonProperty(value = "paste_expiration")
    @JsonSetter(nulls = Nulls.SKIP)
    private PasteExpiration pasteExpiration;

    @JsonProperty(value = "source_code")
    private String sourceCode;

    /*
     * Constructor starts out using the default values for each request body
     * and then Jackson will set non-null values to each, if possible.
     */
    public UploadPasteRequestBody() {
        this.pasteTitle = "Untitled Paste";
        this.pasteSyntax = PasteSyntax.NONE;
        this.pasteVisibility = PasteVisibility.PRIVATE;
        this.pasteExpiration = PasteExpiration.ONEHOUR;
        this.sourceCode = "";
    }

    public void setPasteTitle(String pasteTitle) {
        this.pasteTitle = pasteTitle;
    }

    public void setPasteSyntax(PasteSyntax pasteSyntax) {
        this.pasteSyntax = pasteSyntax;
    }

    public void setPasteVisibility(PasteVisibility pasteVisibility) {
        this.pasteVisibility = pasteVisibility;
    }

    public void setPasteExpiration(PasteExpiration pasteExpiration) {
        this.pasteExpiration = pasteExpiration;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getPasteTitle() {
        return pasteTitle;
    }

    public PasteSyntax getPasteSyntax() {
        return pasteSyntax;
    }

    public PasteVisibility getPasteVisibility() {
        return pasteVisibility;
    }

    public PasteExpiration getPasteExpiration() {
        return pasteExpiration;
    }

    public String getSourceCode() {
        return sourceCode;
    }
}
