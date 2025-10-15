package com.beyond.specguard.model.service;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
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
class DeleteClientUserTest {

    @Mock
    private ClientUserRepository clientUserRepository;

    @Mock
    private ClientCompanyRepository clientCompanyRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 삭제 단위 테스트")
    void deleteClientUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        ClientCompany company = ClientCompany.builder()
                .id(UUID.randomUUID())
                .name("Beyond Systems")
                .build();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .name("테스트유저")
                .email("test@beyond.com")
                .company(company)
                .role(ClientUser.Role.MANAGER)
                .build();

        when(clientUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        userService.deleteMyAccount(userId);

        // then
        verify(clientUserRepository, times(1)).findById(userId);
        verify(clientUserRepository, times(1)).delete(user);

        System.out.println(" 유저 삭제 완료: " + user.getEmail());
    }
}
