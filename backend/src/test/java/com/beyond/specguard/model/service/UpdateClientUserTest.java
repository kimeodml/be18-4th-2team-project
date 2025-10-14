package com.beyond.specguard.model.service;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.company.management.model.dto.request.UpdateUserRequestDto;
import com.beyond.specguard.company.management.model.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateClientUserTest {

    @Mock
    private ClientUserRepository clientUserRepository;

    @Mock
    private ClientCompanyRepository clientCompanyRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 정보 수정 단위 테스트")
    void updateUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        ClientCompany company = ClientCompany.builder()
                .name("Beyond Systems")
                .build();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .name("기존 이름")
                .email("test@beyond.com")
                .phone("01011112222")
                .company(company)
                .role(ClientUser.Role.MANAGER)
                .build();

        UpdateUserRequestDto dto = UpdateUserRequestDto.builder()
                .name("수정 후 이름")
                .phone("01099998888")
                .build();

        when(clientUserRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.updateMyInfo(userId, dto);

        // then
        assertThat(user.getName()).isEqualTo("수정 후 이름");
        assertThat(user.getPhone()).isEqualTo("01099998888");

        verify(clientUserRepository, times(1)).findById(userId);
        System.out.println(" 수정 완료: " + user.getName() + ", " + user.getPhone());
    }
}
