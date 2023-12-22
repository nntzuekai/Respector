package com.urcodebin.api.repository;

import com.urcodebin.api.entities.CodePaste;
import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.enums.PasteVisibility;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@DataJpaTest
@RunWith(SpringRunner.class)
@ActiveProfiles("tests")
public class CodePasteRepositoryTests {

    @Autowired
    private CodePasteRepository codePasteRepository;

    private final String programTitleSearch = "Program";
    private final String appTitleSearch = "App";
    private final PasteSyntax javaSyntaxSearch = PasteSyntax.JAVA;
    private final PasteSyntax pythonSyntaxSearch = PasteSyntax.Python;

    @Test
    public void findCodePastesContainTitleGetsCorrectResults() {
        final List<CodePaste> foundCodePastes = codePasteRepository.findCodePastesContainTitle(
                programTitleSearch, PageRequest.of(0, 5));
        boolean allPastesContainTheSearchedTitle = foundCodePastes.stream()
                .allMatch(paste -> paste.getPasteTitle().contains(programTitleSearch));

        Assert.assertTrue(allPastesContainTheSearchedTitle);
    }

    @Test
    public void findCodePastesContainTitleOnlyGetsUpToLimit() {
        final Pageable limit = PageRequest.of(0, 1);
        final List<CodePaste> resultList = codePasteRepository.findCodePastesContainTitle(programTitleSearch, limit);

        Assert.assertTrue(resultSizeIsNotGreaterThanTheLimit(resultList, limit));
    }

    @Test
    public void findCodePastesContainTitleAreAllPublic() {
        final List<CodePaste> foundCodePastes = codePasteRepository.findCodePastesContainTitle(
                programTitleSearch, PageRequest.of(0, 5));

        Assert.assertTrue(allCodePastesArePublic(foundCodePastes));
    }

    @Test
    public void findCodePastesContainingGetCorrectResults() {
        final List<CodePaste> foundCodePastes = codePasteRepository.findCodePastesContaining(
                programTitleSearch, javaSyntaxSearch, PageRequest.of(0, 5));

        final boolean allCorrectPastesAreFound = foundCodePastes.stream()
                .allMatch(codePaste -> codePaste.getPasteTitle().contains(programTitleSearch) &&
                        codePaste.getPasteSyntax().equals(javaSyntaxSearch));
        Assert.assertTrue(allCorrectPastesAreFound);
    }

    @Test
    public void findCodePastesContainingOnlyGetsUpToLimit() {
        final Pageable resultLimit = PageRequest.of(0, 1);
        final List<CodePaste> pasteResults = codePasteRepository.findCodePastesContaining(
                appTitleSearch, pythonSyntaxSearch, resultLimit);

        Assert.assertTrue(resultSizeIsNotGreaterThanTheLimit(pasteResults, resultLimit));
    }

    @Test
    public void findCodePastesContainingAreAllPublic() {
        final List<CodePaste> foundCodePastes = codePasteRepository.findCodePastesContaining(
                appTitleSearch, pythonSyntaxSearch,  PageRequest.of(0, 5));

        Assert.assertTrue(allCodePastesArePublic(foundCodePastes));
    }


    private boolean allCodePastesArePublic(List<CodePaste> listOfCodePastes) {
        return listOfCodePastes.stream()
                .allMatch(codePaste -> codePaste.getPasteVisibility().equals(PasteVisibility.PUBLIC));
    }

    private boolean resultSizeIsNotGreaterThanTheLimit(List<CodePaste> results, Pageable limit) {
        return results.size() <= limit.getPageSize();
    }
}
