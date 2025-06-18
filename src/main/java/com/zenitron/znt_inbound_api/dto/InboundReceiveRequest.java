package com.zenitron.znt_inbound_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundReceiveRequest {

    @NotBlank(message = "sign 不得為空")
    @JsonProperty("sign")
    private String sign;

    @NotBlank(message = "part_code 不得為空")
    @JsonProperty("part_code")
    private String partCode;

    @NotBlank(message = "request_time 不得為空")
    @JsonProperty("request_time")
    private String requestTime;

    @NotNull(message = "data 不得為 null")
    @Valid
    @JsonProperty("data")
    private InboundReceiveData data;
} 