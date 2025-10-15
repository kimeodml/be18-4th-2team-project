package com.beyond.specguard.company.common.model.service;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.company.common.model.dto.request.SignupRequestDto;
import com.beyond.specguard.company.common.model.dto.response.SignupResponseDto;
import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class SignupServiceTest {

    @Mock
    private ClientCompanyRepository companyRepository;
    @Mock
    private ClientUserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    public SignupServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ 정상 회원가입 시 회사 및 유저가 저장되고 응답이 반환된다")
    void signup_success() {
        // given
        SignupRequestDto.CompanyDTO companyDTO = new SignupRequestDto.CompanyDTO(
                "테스트기업", "123-45-67890", "testslug", "대표", "홍길동", "test@company.com", "010-1234-5678"
        );
        SignupRequestDto.UserDTO userDTO = new SignupRequestDto.UserDTO(
                "유저홍", "user@test.com", "1234", "010-5678-1234"
        );
        SignupRequestDto request = new SignupRequestDto(companyDTO, userDTO);

        when(companyRepository.existsByBusinessNumber(any())).thenReturn(false);
        when(companyRepository.existsBySlug(any())).thenReturn(false);
        when(companyRepository.existsByName(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pw");

        ClientCompany savedCompany = ClientCompany.builder()
                .id(UUID.randomUUID())
                .name("테스트기업")
                .businessNumber("123-45-67890")
                .slug("testslug")
                .build();
        when(companyRepository.save(any())).thenReturn(savedCompany);

        ClientUser savedUser = ClientUser.builder()
                .id(UUID.randomUUID())
                .company(savedCompany)
                .name("유저홍")
                .email("user@test.com")
                .passwordHash("encoded-pw")
                .role(ClientUser.Role.OWNER)
                .build();
//        when(userRepository.save(any())).thenReturn(savedUser);

        // ✅ save() 호출 시 ID 자동 세팅되도록 Stub 수정
        when(companyRepository.save(any(ClientCompany.class)))
                .thenAnswer(invocation -> {
                    ClientCompany company = invocation.getArgument(0);
                    Field idField = ClientCompany.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(company, UUID.randomUUID());
                    return company;
                });

        when(userRepository.save(any(ClientUser.class)))
                .thenAnswer(invocation -> {
                    ClientUser user = invocation.getArgument(0);
                    Field idField = ClientUser.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(user, UUID.randomUUID());
                    return user;
                });

        // when
        SignupResponseDto response = signupService.signup(request);

        // then
        assertNotNull(response);
        assertEquals("테스트기업", response.getCompany().getName());
        assertEquals("유저홍", response.getUser().getEmail());

        verify(companyRepository).save(any(ClientCompany.class));
        verify(userRepository).save(any(ClientUser.class));
    }

    @Test
    @DisplayName("❌ 중복 사업자등록번호로 회원가입 시 예외 발생")
    void signup_fail_duplicate_company() {
        // given
        SignupRequestDto.CompanyDTO companyDTO = new SignupRequestDto.CompanyDTO(
                "테스트기업", "123-45-67890", "testslug", "대표", "홍길동", "test@company.com", "010-1234-5678"
        );
        SignupRequestDto.UserDTO userDTO = new SignupRequestDto.UserDTO(
                "유저홍", "user@test.com", "1234", "010-5678-1234"
        );
        SignupRequestDto request = new SignupRequestDto(companyDTO, userDTO);

        when(companyRepository.existsByBusinessNumber(any())).thenReturn(true);

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> signupService.signup(request));
        assertEquals(AuthErrorCode.DUPLICATE_COMPANY, ex.getErrorCode());
    }
}
