package com.urcodebin.api.services;

import com.urcodebin.api.controllers.requestbody.SignupRequestBody;
import com.urcodebin.api.entities.UserAccount;
import com.urcodebin.api.repository.UserAccountRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserAccountServiceTests {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountServiceImpl userAccountService;

    UserAccount testAccount;
    SignupRequestBody signupRequestBody;

    @Before
    public void setup() {
        testAccount = new UserAccount();
        testAccount.setUsername("Test Username");
        testAccount.setEmail("testemail@gmail.com");
        testAccount.setId(105L);

        signupRequestBody = new SignupRequestBody();
        signupRequestBody.setUsername("Test Username");
        signupRequestBody.setEmail("testemail@gmail.com");
        signupRequestBody.setPassword("password");
    }

    @Test
    public void getUserAccountByIdWithCorrectIdReturnsRightUserAccount() {
        when(userAccountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        final Optional<UserAccount> foundUserAccount = userAccountService.getUserAccountById(testAccount.getId());
        Assert.assertTrue(foundUserAccount.isPresent());
        Assert.assertEquals(foundUserAccount.get(), testAccount);
    }

    @Test
    public void getUserAccountByIdWithWrongIdReturnsEmptyOptional() {
        when(userAccountRepository.findById(100L)).thenReturn(Optional.empty());

        final Optional<UserAccount> foundUserAccount = userAccountService.getUserAccountById(100L);
        Assert.assertFalse(foundUserAccount.isPresent());
    }

    @Test
    public void signupNewUserAccountWithValidSignupRequestBodySavesAndReturnsUserAccount() {
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(arg -> arg.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("Encoded Password");

        final UserAccount userAccount = userAccountService.signupNewUserAccount(signupRequestBody);
        Assert.assertEquals(userAccount.getEmail(), signupRequestBody.getEmail());
        Assert.assertEquals(userAccount.getUsername(), signupRequestBody.getUsername());
        //Confirm that the password has been encoded and is not the same plain password.
        Assert.assertNotEquals(userAccount.getPassword(), signupRequestBody.getPassword());
    }
}