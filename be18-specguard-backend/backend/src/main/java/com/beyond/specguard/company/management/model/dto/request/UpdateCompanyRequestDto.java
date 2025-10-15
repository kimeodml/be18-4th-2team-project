package com.beyond.specguard.company.management.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompanyRequestDto {

    @Size(max = 20)
    private String name;

    @Size(max = 10)
    private String managerName;

    @Size(max = 10)
    private String managerPosition;

    @Email
    private String contactEmail;

    @Size(max = 20)
    private String contactMobile;
}
