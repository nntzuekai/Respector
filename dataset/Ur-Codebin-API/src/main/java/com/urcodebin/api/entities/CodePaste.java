package com.urcodebin.api.entities;

import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.enums.PasteVisibility;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "code_paste")
@SecondaryTable(name = "source_table",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "source_id", referencedColumnName = "paste_id"))
public class CodePaste {

    @Id
    @Column(name = "paste_id", unique = true, nullable = false, length = 16)
    private final UUID pasteId = UUID.randomUUID();

    @NotNull
    @Lob
    @Column(name = "source_code", table = "source_table")
    private String sourceCode;

    @Column(name = "paste_title")
    private String pasteTitle;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "paste_syntax")
    private PasteSyntax pasteSyntax;

    @NotNull
    @Column(name = "paste_Expiration")
    private LocalDateTime pasteExpirationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "paste_visibility")
    private PasteVisibility pasteVisibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private UserAccount userAccount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodePaste)) return false;
        CodePaste codePaste = (CodePaste) o;
        return pasteId.equals(codePaste.pasteId) &&
                sourceCode.equals(codePaste.sourceCode) &&
                Objects.equals(pasteTitle, codePaste.pasteTitle) &&
                pasteSyntax.equals(codePaste.pasteSyntax) &&
                pasteExpirationDate.equals(codePaste.pasteExpirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pasteId, sourceCode, pasteTitle, pasteSyntax, pasteExpirationDate);
    }

    public UUID getPasteId() {
        return pasteId;
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
        return pasteExpirationDate;
    }

    public void setPasteExpirationDate(LocalDateTime pasteExpirationDate) {
        this.pasteExpirationDate = pasteExpirationDate;
    }

    public PasteVisibility getPasteVisibility() {
        return pasteVisibility;
    }

    public void setPasteVisibility(PasteVisibility pasteVisibility) {
        this.pasteVisibility = pasteVisibility;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
}
