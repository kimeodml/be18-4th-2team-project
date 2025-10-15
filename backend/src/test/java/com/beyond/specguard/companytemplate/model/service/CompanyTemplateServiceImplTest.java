package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.companytemplate.model.dto.command.CreateBasicCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateBasicCommand;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateResponseDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("기본 회사 템플릿 테스트")
@ExtendWith(MockitoExtension.class)
class CompanyTemplateServiceImplTest {

    private final UUID clientCompanyId = UUID.randomUUID();
    private final UUID templateId = UUID.randomUUID();

    @Mock
    CompanyTemplateRepository companyTemplateRepository;

    @InjectMocks
    private CompanyTemplateServiceImpl companyTemplateService;

    // 테스트용 목업 데이터
    private CompanyTemplateBasicRequestDto basicRequestDto() {
        CompanyTemplateBasicRequestDto dto = new CompanyTemplateBasicRequestDto();
        ReflectionTestUtils.setField(dto, "name", "백엔드 채용");
        ReflectionTestUtils.setField(dto, "department", "플랫폼개발팀");
        ReflectionTestUtils.setField(dto, "category", "백엔드 개발");
        ReflectionTestUtils.setField(dto, "yearsOfExperience", 3);
        return dto;
    }



    @Nested
    @DisplayName("CreateBasicTemplate 테스트")
    class CreateBasicTemplate {

        @Test
        @DisplayName("OWNER 권한 사용자는 채용 공고를 생성할 수 있다.")
        void createBasicTemplate_owner_success() {
            // given
            ClientCompany company = ClientCompany.builder().id(clientCompanyId).name("Test Company").build();
            ClientUser owner = ClientUser.builder().company(company).role(ClientUser.Role.OWNER)
                                         .email("owner@test.com").build();


            CompanyTemplateBasicRequestDto createDto = basicRequestDto();

            CompanyTemplate fakeEntity = CompanyTemplate.builder()
                                                        .name("백엔드 채용")
                                                        .department("플랫폼개발팀")
                                                        .category("백엔드 개발")
                                                        .yearsOfExperience(3)
                                                        .build();

            // when
            when(companyTemplateRepository.saveAndFlush(any(CompanyTemplate.class))).thenReturn(fakeEntity);
            CompanyTemplateResponseDto.BasicDto result = companyTemplateService.createBasicTemplate(
                    new CreateBasicCompanyTemplateCommand(owner, createDto)
            );

            // then
            assertThat(result.getName()).isEqualTo(createDto.getName());
            verify(companyTemplateRepository, times(1)).saveAndFlush(any());
        }

        @Test
        @DisplayName("VIEWER 권한 사용자는 채용 공고를 생성할 수 없다.")
        void createBasicTemplate_viewer_fail() {
            // given
            ClientCompany company = ClientCompany.builder().id(clientCompanyId).name("test_user").build();
            ClientUser viewer = ClientUser.builder().company(company).role(ClientUser.Role.VIEWER)
                                          .email("viewer@test.com").build();


            CompanyTemplateBasicRequestDto createDto = basicRequestDto();

            CreateBasicCompanyTemplateCommand command = new CreateBasicCompanyTemplateCommand(viewer, createDto);

            // then
            assertThatThrownBy(() -> companyTemplateService.createBasicTemplate(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("접근 권한이 없습니다");
        }

    }

    @Nested
    @DisplayName("UpdateBasicTemplate 테스트")
    class UpdateBasicTemplate {

        @Test
        @DisplayName("MANAGER 권한 사용자이면서 동일 회사의 템플릿이면 채용 공고를 수정할 수 있다.")
        void updateBasicTemplate_managerAndSameCompany_success() {
            ClientCompany company = ClientCompany.builder().id(clientCompanyId).name("Test Company").build();
            ClientUser manager = ClientUser.builder().company(company).role(ClientUser.Role.MANAGER)
                                          .email("manager@test.com").build();

            CompanyTemplateBasicRequestDto updateDto = basicRequestDto();
            UpdateTemplateBasicCommand command = new UpdateTemplateBasicCommand(templateId, updateDto, manager);

            CompanyTemplate existingTemplate = CompanyTemplate.builder()
                                                              .id(templateId)
                                                              .clientCompany(company)
                                                              .name("기존 공고")
                                                              .department("기존 부서")
                                                              .category("기존 카테고리")
                                                              .yearsOfExperience(1)
                                                              .build();

            // when
            when(companyTemplateRepository.findById(templateId)).thenReturn(java.util.Optional.of(existingTemplate));
            when(companyTemplateRepository.saveAndFlush(any(CompanyTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CompanyTemplateResponseDto.BasicDto result = companyTemplateService.updateBasic(command);

            // then
            assertThat(result.getName()).isEqualTo(updateDto.getName());
            assertThat(result.getDepartment()).isEqualTo(updateDto.getDepartment());
            verify(companyTemplateRepository, times(1)).saveAndFlush(any());


        }

        @Test
        @DisplayName("VIEWER 권한 사용자는 채용 공고를 수정할 수 없다.")
        void updateBasicTemplate_viewer_fail() {
            ClientCompany company = ClientCompany.builder().id(clientCompanyId).name("test_user").build();
            ClientUser viewer = ClientUser.builder().company(company).role(ClientUser.Role.VIEWER)
                                         .email("viewer@test.com").build();

            CompanyTemplateBasicRequestDto dto = basicRequestDto();

            UpdateTemplateBasicCommand command = new UpdateTemplateBasicCommand(templateId, dto, viewer);


            assertThatThrownBy(() -> companyTemplateService.updateBasic(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("접근 권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 공고를 수정할 수 없다.")
        void updateBasicTemplate_templateNotFound_fail() {
            ClientCompany company = ClientCompany.builder().id(clientCompanyId).name("Test Company").build();
            ClientUser manager = ClientUser.builder().company(company).role(ClientUser.Role.MANAGER)
                                           .email("manager@test.com").build();

            CompanyTemplateBasicRequestDto updateDto = basicRequestDto();

            UpdateTemplateBasicCommand command = new UpdateTemplateBasicCommand(templateId, updateDto, manager);

            when(companyTemplateRepository.findById(templateId)).thenReturn(java.util.Optional.empty());


            assertThatThrownBy(() -> companyTemplateService.updateBasic(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("해당 템플릿을 찾을 수 없습니다.");



        }

        @Test
        @DisplayName("다른 회사 공고를 수정할 수 없다.")
        void updateBasicTemplate_differentCompany_fail() {
            ClientCompany companyA = ClientCompany.builder().id(clientCompanyId).name("Test CompanyA").build();
            ClientCompany companyB = ClientCompany.builder().id(UUID.randomUUID()).name("Test CompanyB").build();
            ClientUser managerA = ClientUser.builder().company(companyA).role(ClientUser.Role.MANAGER)
                                           .email("managerA@test.com").build();

            CompanyTemplateBasicRequestDto updateDto = basicRequestDto();

            UpdateTemplateBasicCommand command = new UpdateTemplateBasicCommand(templateId, updateDto, managerA);

            CompanyTemplate otherCompanyTemplate = CompanyTemplate.builder()
                                                              .id(templateId)
                                                              .clientCompany(companyB)
                                                              .name("다른 회사 공고")
                                                              .department("다른 회사 부서")
                                                              .category("다른 회사 카테고리")
                                                              .yearsOfExperience(1)
                                                              .build();


            when(companyTemplateRepository.findById(templateId)).thenReturn(java.util.Optional.of(otherCompanyTemplate));


            assertThatThrownBy(() -> companyTemplateService.updateBasic(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("접근 권한이 없습니다");

        }

    }




}
