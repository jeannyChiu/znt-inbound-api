package com.zenitron.znt_inbound_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class InboundOrderData {

    @NotBlank(message = "ref_no 不得為空")
    @JsonProperty("ref_no")
    private String refNo;

    @JsonProperty("shipped_time")
    private String shippedTime;

    @JsonProperty("wms_no")
    private String wmsNo;

    @JsonProperty("wms_uuid")
    private String wmsUuid;

    @JsonProperty("busi_date")
    private String busiDate;

    @NotEmpty(message = "detail 列表不得為空")
    @Valid // This annotation ensures that the validation rules inside InboundOrderDetail are triggered.
    @JsonProperty("detail")
    private List<InboundOrderDetail> detail;
} 