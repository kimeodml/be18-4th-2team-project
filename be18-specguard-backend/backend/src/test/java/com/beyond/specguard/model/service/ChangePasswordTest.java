package com.beyond.specguard.model.service;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.company.management.model.dto.request.ChangePasswordRequestDto;
import com.beyond.specguard.company.management.model.service.UserService;
import com.beyond.specguard.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordTest {

    @Mock
    private ClientUserRepository clientUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("비밀번호 수정 단위 테스트 (LOCAL 계정)")
    void changePassword_user_success() {
        // given
        UUID userId = UUID.randomUUID();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .email("user@test.com")
                .passwordHash("encoded_old_pw")
                .role(ClientUser.Role.MANAGER)
                .provider("local")
                .build();

        ChangePasswordRequestDto dto = ChangePasswordRequestDto.builder()
                .oldPassword("old1234")
                .newPassword("new1234")
                .build();

        when(clientUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old1234", "encoded_old_pw")).thenReturn(true);
        when(passwordEncoder.encode("new1234")).thenReturn("encoded_new_pw");

        // when
        userService.changePassword(userId, dto);

        // then
        assertThat(user.getPasswordHash()).isEqualTo("encoded_new_pw");
        verify(clientUserRepository, times(1)).findById(userId);
        verify(passwordEncoder).matches("old1234", "encoded_old_pw");
        verify(passwordEncoder).encode("new1234");

        System.out.println("비밀번호 변경 성공: " + user.getEmail());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 테스트 (소셜 계정)")
    void changePassword_socialUser_fail() {
        // given
        UUID userId = UUID.randomUUID();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .email("social@kakao.com")
                .passwordHash("encoded_pw")
                .role(ClientUser.Role.MANAGER)
                .provider("naver")
                .build();

        ChangePasswordRequestDto dto = ChangePasswordRequestDto.builder()
                .oldPassword("old1234")
                .newPassword("new1234")
                .build();

        when(clientUserRepository.findById(userId)).thenReturn(Optional.of(user));

        // expect
        assertThatThrownBy(() -> userService.changePassword(userId, dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("비밀번호를 번경할 수 없습니다");

        verify(clientUserRepository, times(1)).findById(userId);
        verifyNoInteractions(passwordEncoder);

        System.out.println("소셜 계정은 비밀번호 변경 불가 확인 완료: " + user.getProvider());
    }
}
