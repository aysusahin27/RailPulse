package com.basespringboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationRequest {

    @NotBlank(message = "Station name must not be blank")
    @Size(min = 1, max = 150, message = "Station name must be between 1 and 150 characters")
    private String name;

    @NotBlank(message = "City must not be blank")
    @Size(min = 1, max = 100, message = "City must be between 1 and 100 characters")
    private String city;

    @NotBlank(message = "UIC code must not be blank")
    @Size(min = 2, max = 50, message = "UIC code must be between 2 and 50 characters")
    private String uicCode;

    public StationRequest(String name, String uicCode) {
        this.name = name;
        this.city = name;
        this.uicCode = uicCode;
    }
}
