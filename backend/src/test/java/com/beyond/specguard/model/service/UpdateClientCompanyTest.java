package com.beyond.specguard.model.service;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.company.management.model.dto.request.UpdateCompanyRequestDto;
import com.beyond.specguard.company.management.model.service.CompanyService;
import com.beyond.specguard.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateClientCompanyTest {

    @Mock
    private ClientCompanyRepository clientCompanyRepository;

    @Mock
    private ClientUserRepository clientUserRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("회사 정보 수정 단위 테스트 (정상 케이스)")
    void updateCompany_success() {
        // given
        UUID userId = UUID.randomUUID();

        ClientCompany company = ClientCompany.builder()
                .id(UUID.randomUUID())
                .slug("beyond")
                .name("Beyond Systems")
                .contactEmail("test@test.com")
                .contactMobile("01012345678")
                .build();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .name("테스트")
                .role(ClientUser.Role.OWNER)
                .company(company)
                .build();

        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .name("Beyond AI")
                .contactEmail("update@test.com")
                .contactMobile("01099998888")
                .build();

        when(clientCompanyRepository.findBySlug("beyond"))
                .thenReturn(Optional.of(company));
        when(clientUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        companyService.updateCompany("beyond", dto, userId);

        // then
        assertThat(company.getContactEmail()).isEqualTo("update@test.com");
        assertThat(company.getContactMobile()).isEqualTo("01099998888");
        verify(clientCompanyRepository, times(1)).findBySlug("beyond");
        verify(clientUserRepository, times(1)).findById(userId);

        System.out.println(
                " 수정 완료: " + user.getName() + ", " + company.getContactEmail() + ", " + company.getContactMobile());
    }

    @Test
    @DisplayName("회사 정보 수정 실패 - 권한 없음")
    void updateCompany_accessDenied() {
        // given
        UUID userId = UUID.randomUUID();

        ClientCompany company = ClientCompany.builder()
                .id(UUID.randomUUID())
                .slug("beyond")
                .build();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .role(ClientUser.Role.MANAGER)
                .company(company)
                .build();

        when(clientCompanyRepository.findBySlug("beyond"))
                .thenReturn(Optional.of(company));
        when(clientUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .contactEmail("noauth@test.com")
                .contactMobile("01099998888")
                .build();

        // expect
        assertThatThrownBy(() -> companyService.updateCompany("beyond", dto, userId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.ACCESS_DENIED);
    }
}
