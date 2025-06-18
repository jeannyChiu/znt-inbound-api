package com.zenitron.znt_inbound_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "batch_no",
    "bin_code",
    "date_code",
    "invoice_no",
    "lot_no",
    "po_no",
    "qty",
    "ref_sku_code",
    "sku_code",
    "stock_uuid",
    "store_cate",
    "wms_list_id"
})
public class InboundReceiveDetail {

    @JsonProperty("batch_no")
    private String batchNo;

    @JsonProperty("bin_code")
    private String binCode;

    @JsonProperty("date_code")
    private String dateCode;

    @JsonProperty("invoice_no")
    private String invoiceNo;

    @JsonProperty("lot_no")
    private String lotNo;

    @JsonProperty("po_no")
    private String poNo;

    @NotBlank(message = "qty 不得為空")
    @JsonProperty("qty")
    private String qty;

    @JsonProperty("ref_sku_code")
    private String refSkuCode;

    @NotBlank(message = "sku_code 不得為空")
    @JsonProperty("sku_code")
    private String skuCode;

    @JsonProperty("stock_uuid")
    private String stockUuid;

    @JsonProperty("store_cate")
    private String storeCate;

    @JsonProperty("wms_list_id")
    private String wmsListId;
} 