package com.zenitron.znt_inbound_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "ref_no",
    "wms_no",
    "wms_uuid",
    "arrived_time",
    "busi_date",
    "detail"
})
public class InboundReceiveData {

    @NotBlank(message = "ref_no 不得為空")
    @JsonProperty("ref_no")
    private String refNo;

    @JsonProperty("wms_no")
    private String wmsNo;

    @JsonProperty("wms_uuid")
    private String wmsUuid;

    @JsonProperty("arrived_time")
    private String arrivedTime;

    @JsonProperty("busi_date")
    private String busiDate;

    @NotEmpty(message = "detail 列表不得為空")
    @Valid
    @JsonProperty("detail")
    private List<InboundReceiveDetail> detail;
} 