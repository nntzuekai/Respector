package com.urcodebin.api.services;

import com.urcodebin.api.controllers.requestbody.UploadPasteRequestBody;
import com.urcodebin.api.entities.CodePaste;
import com.urcodebin.api.enums.PasteExpiration;
import com.urcodebin.api.enums.PasteSyntax;
import com.urcodebin.api.enums.PasteVisibility;
import com.urcodebin.api.repository.CodePasteRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CodePasteServiceTests {

    @Mock
    private CodePasteRepository codePasteRepository;

    @InjectMocks
    private CodePasteServiceImpl codePasteService;

    CodePaste fakeCodePaste;
    UploadPasteRequestBody pasteRequestBody;

    @Before
    public void setup() {
        fakeCodePaste = new CodePaste();
        fakeCodePaste.setPasteTitle("Fake Title");
        fakeCodePaste.setPasteSyntax(PasteSyntax.NONE);
        fakeCodePaste.setPasteVisibility(PasteVisibility.PUBLIC);
        fakeCodePaste.setSourceCode("SOURCE CODE");
        LocalDateTime offsetDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .plusMinutes(PasteExpiration.THIRTYMINUTES.getOffsetMin());
        fakeCodePaste.setPasteExpirationDate(offsetDateTime);

        pasteRequestBody = new UploadPasteRequestBody();
        pasteRequestBody.setPasteSyntax(PasteSyntax.NONE);
        pasteRequestBody.setPasteExpiration(PasteExpiration.THIRTYMINUTES);
        pasteRequestBody.setPasteTitle("Fake Title");
        pasteRequestBody.setPasteVisibility(PasteVisibility.PUBLIC);
        pasteRequestBody.setSourceCode("SOURCE CODE");
    }

    @Test
    public void findCodePasteWithCorrectIdReturnsCorrectPaste() {
        when(codePasteRepository.findById(any(UUID.class))).thenReturn(Optional.of(fakeCodePaste));

        final Optional<CodePaste> foundCodePaste = codePasteService.findByCodePasteId(fakeCodePaste.getPasteId());
        Assert.assertTrue(foundCodePaste.isPresent());
        Assert.assertEquals(fakeCodePaste, foundCodePaste.get());
    }

    @Test
    public void findCodePasteWithWrongIdReturnsNoResults() {
        when(codePasteRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        final Optional<CodePaste> foundCodePaste = codePasteService.findByCodePasteId(UUID.randomUUID());
        Assert.assertFalse(foundCodePaste.isPresent());
    }

    @Test
    public void findListOfCodePasteByCorrectTitleReturnsCorrectPastes() {
        when(codePasteRepository.findCodePastesContainTitle(anyString(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(fakeCodePaste));

        final List<CodePaste> listOfCodePastes = codePasteService.findListOfCodePastesBy(fakeCodePaste.getPasteTitle(), 1);
        Assert.assertEquals(listOfCodePastes, Collections.singletonList(fakeCodePaste));
    }

    @Test
    public void findListOfCodePasteWithWrongTitleReturnsEmptyList() {
        when(codePasteRepository.findCodePastesContainTitle(anyString(), any(Pageable.class)))
                .thenReturn(List.of());

        final List<CodePaste> listOfCodePastes = codePasteService.findListOfCodePastesBy("Wrong Title", 1);
        Assert.assertTrue(listOfCodePastes.isEmpty());
    }

    @Test
    public void findListOfCodePastesWithCorrectTitleAndPasteSyntaxReturnsCorrectPastes() {
        when(codePasteRepository.findCodePastesContaining(anyString(), any(PasteSyntax.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(fakeCodePaste));

        final List<CodePaste> listOfCodePastes = codePasteService.findListOfCodePastesBy(
                fakeCodePaste.getPasteTitle(), fakeCodePaste.getPasteSyntax(), 1);
        Assert.assertEquals(listOfCodePastes, Collections.singletonList(fakeCodePaste));
    }

    @Test
    public void findListOfCodePastesWithWrongTitleAndCorrectPasteSyntaxReturnsEmptyList() {
        when(codePasteRepository.findCodePastesContaining(anyString(), any(PasteSyntax.class), any(Pageable.class)))
                .thenReturn(List.of());

        final List<CodePaste> listOfCodePastes = codePasteService.findListOfCodePastesBy(
                "Wrong Title", fakeCodePaste.getPasteSyntax(), 1);
        Assert.assertTrue(listOfCodePastes.isEmpty());
    }

    @Test
    public void findListOfCodePastesWithCorrectTitleAndWrongPasteSyntaxReturnsEmptyList() {
        when(codePasteRepository.findCodePastesContaining(anyString(), any(PasteSyntax.class), any(Pageable.class)))
                .thenReturn(List.of());

        final List<CodePaste> listOfCodePastes = codePasteService.findListOfCodePastesBy(
                    fakeCodePaste.getPasteTitle(), PasteSyntax.CLANG, 1);
        Assert.assertTrue(listOfCodePastes.isEmpty());
    }

    @Test
    public void doesCodePasteWithIdExistWithCorrectIdReturnsTrue() {
        UUID expectedID = UUID.randomUUID();
        when(codePasteRepository.existsById(expectedID)).thenReturn(true);

        final boolean doesCodePasteExist = codePasteService.doesCodePasteWithIdExist(expectedID);
        Assert.assertTrue(doesCodePasteExist);
    }

    @Test
    public void doesCodePasteWithIdExistWithWrongIdReturnsFalse() {
        when(codePasteRepository.existsById(any(UUID.class))).thenReturn(false);

        final boolean doesCodePasteExist = codePasteService.doesCodePasteWithIdExist(UUID.randomUUID());
        Assert.assertFalse(doesCodePasteExist);
    }

    @Test
    public void deleteCodePasteByIdWithValidIdCausesRepositoryDeleteByIdToBeCalled() {
        codePasteService.deleteCodePasteById(UUID.randomUUID());

        verify(codePasteRepository, times(1)).deleteById(any(UUID.class));
    }

    @Test
    public void createNewCodePasteWithValidRequestBodyCorrectlyReturnsCodePaste() {
        when(codePasteRepository.save(any(CodePaste.class))).thenAnswer(args -> args.getArgument(0));

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

        final CodePaste codePaste = codePasteService.createNewCodePaste(pasteRequestBody);
        Assert.assertEquals(codePaste.getPasteExpirationDate().format(formatter),
                fakeCodePaste.getPasteExpirationDate().format(formatter));
        Assert.assertEquals(codePaste.getPasteSyntax(), fakeCodePaste.getPasteSyntax());
        Assert.assertEquals(codePaste.getPasteTitle(), fakeCodePaste.getPasteTitle());
        Assert.assertEquals(codePaste.getSourceCode(), fakeCodePaste.getSourceCode());
        Assert.assertEquals(codePaste.getPasteVisibility(), fakeCodePaste.getPasteVisibility());
    }

    @Test
    public void createNewCodePasteWithValidRequestBodyCallsRepositorySaveOnce() {
        when(codePasteRepository.save(any(CodePaste.class))).thenReturn(fakeCodePaste);

        codePasteService.createNewCodePaste(pasteRequestBody);
        verify(codePasteRepository, times(1)).save(any(CodePaste.class));
    }
}
