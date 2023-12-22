package com.urcodebin.api.controllers;

import com.urcodebin.api.controllers.requestbody.SignupRequestBody;
import com.urcodebin.api.dto.UserAccountDTO;
import com.urcodebin.api.entities.UserAccount;
import com.urcodebin.api.error.exception.AccountInformationTakenException;
import com.urcodebin.api.error.exception.UserAccountNotFoundException;
import com.urcodebin.api.services.interfaces.UserAccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/account")
public class UserAccountController {

    private final UserAccountService userAccountService;

    private final ModelMapper modelMapper;

    @Autowired
    public UserAccountController(@Qualifier("AccountService") UserAccountService userAccountService,
                                 ModelMapper modelMapper) {
        this.userAccountService = userAccountService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{account_id}")
    public UserAccountDTO getUserAccountById(@PathVariable("account_id") Long accountId) {
        Optional<UserAccount> foundUserAccount = userAccountService.getUserAccountById(accountId);
        if(!foundUserAccount.isPresent())
            throw new UserAccountNotFoundException("User Account with given id was not found.");
        return convertToDTO(foundUserAccount.get());
    }

    @PostMapping("/signup")
    public UserAccountDTO signupForNewAccount(@RequestBody SignupRequestBody signupAccount) {
        boolean isEmailValidFormat = validateEmailFormat(signupAccount.getEmail());
        if(!isEmailValidFormat)
            throw new IllegalArgumentException("Email is not in the correct format. Make sure email is valid.");

        boolean usernameIsTaken = userAccountService.isAccountUsernameTaken(signupAccount.getUsername());
        if(usernameIsTaken)
            throw new AccountInformationTakenException("Username provided is already in use. Please use another username.");

        boolean emailIsTaken = userAccountService.isAccountEmailTaken(signupAccount.getEmail());
        if(emailIsTaken)
            throw new AccountInformationTakenException("Email provided is already in use. Please use another email.");

        final UserAccount signedUpAccount = userAccountService.signupNewUserAccount(signupAccount);
        return convertToDTO(signedUpAccount);
    }

    private boolean validateEmailFormat(String email) {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+↵\n" +
                ")*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        Pattern emailPattern = Pattern.compile(emailRegex);

        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private UserAccountDTO convertToDTO(UserAccount userAccount) {
        return modelMapper.map(userAccount, UserAccountDTO.class);
    }
}
