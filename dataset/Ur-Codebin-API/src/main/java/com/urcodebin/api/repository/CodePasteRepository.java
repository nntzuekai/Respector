package com.urcodebin.api.repository;

import com.urcodebin.api.entities.CodePaste;
import com.urcodebin.api.enums.PasteSyntax;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CodePasteRepository extends JpaRepository<CodePaste, UUID> {

    @Query(value = "FROM \n" +
            "   #{#entityName} c \n" +
            "WHERE \n" +
            "   c.pasteTitle LIKE %?1% AND \n" +
            "   c.pasteVisibility = 'PUBLIC'")
    List<CodePaste> findCodePastesContainTitle(String pasteTitle, Pageable pageLimit);

    @Query(value = "FROM \n" +
            "   #{#entityName} c \n" +
            "WHERE \n" +
            "   c.pasteTitle LIKE %?1% AND \n" +
            "   c.pasteVisibility = 'PUBLIC' AND \n" +
            "   c.pasteSyntax = ?2")
    List<CodePaste> findCodePastesContaining(String pasteTitle, PasteSyntax pasteSyntax, Pageable pageLimit);
}
