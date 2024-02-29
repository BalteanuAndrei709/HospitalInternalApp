package com.registerservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SaloonAllocatedResponseDTO {

    private UUID identifier;

    private Integer saloonNumber;

    private boolean status;
}
