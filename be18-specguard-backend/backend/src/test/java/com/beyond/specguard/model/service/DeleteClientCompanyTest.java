package com.beyond.specguard.model.service;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientCompanyRepository;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.company.management.model.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteClientCompanyTest {

    @Mock
    private ClientCompanyRepository clientCompanyRepository;

    @Mock
    private ClientUserRepository clientUserRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("회사 삭제 단위 테스트")
    void deleteClientCompany_success() {
        // given
        UUID userId = UUID.randomUUID();
        ClientCompany company = ClientCompany.builder()
                .id(UUID.randomUUID())
                .slug("beyond")
                .name("Beyond Systems")
                .contactEmail("test@beyond.com")
                .contactMobile("01012345678")
                .build();

        ClientUser user = ClientUser.builder()
                .id(userId)
                .role(ClientUser.Role.OWNER)
                .company(company)
                .build();

        when(clientCompanyRepository.findBySlug("beyond"))
                .thenReturn(Optional.of(company));
        when(clientUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        companyService.deleteCompany("beyond", userId);

        // then
        verify(clientCompanyRepository, times(1)).findBySlug("beyond");
        verify(clientUserRepository, times(1)).findById(userId);
        verify(clientUserRepository, times(1)).deleteAllByCompanyId(company.getId());
        verify(clientCompanyRepository, times(1)).delete(company);

        System.out.println(" 회사 삭제 완료: " + company.getName());
    }
}
