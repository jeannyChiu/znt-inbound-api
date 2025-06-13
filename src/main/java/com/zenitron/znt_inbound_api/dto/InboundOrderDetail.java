package com.zenitron.znt_inbound_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderDetail {

    @JsonProperty("buyer_sku_code")
    private String buyerSkuCode;

    @JsonProperty("c_lpn_seq")
    private String cLpnSeq;

    @JsonProperty("c_lpn_uuid")
    private String cLpnUuid;

    @JsonProperty("c_vol_height")
    private String cVolHeight;

    @JsonProperty("c_vol_length")
    private String cVolLength;

    @JsonProperty("c_vol_width")
    private String cVolWidth;

    @JsonProperty("date_code")
    private String dateCode;

    @JsonProperty("gross_weight")
    private String grossWeight;

    @JsonProperty("lot_no")
    private String lotNo;

    @JsonProperty("net_weight")
    private String netWeight;

    @JsonProperty("origin_country_ename")
    private String originCountryEname;

    @JsonProperty("out_po_line")
    private String outPoLine;

    @NotBlank(message = "out_po_no 不得為空")
    @JsonProperty("out_po_no")
    private String outPoNo;

    @NotBlank(message = "qty 不得為空")
    @JsonProperty("qty")
    private String qty;

    @NotBlank(message = "ref_no 不得為空")
    @JsonProperty("ref_no")
    private String refNo;

    @JsonProperty("shipping_mark")
    private String shippingMark;

    @NotBlank(message = "sku_code 不得為空")
    @JsonProperty("sku_code")
    private String skuCode;

    @JsonProperty("stock_mode")
    private String stockMode;

    @JsonProperty("stock_uuid")
    private String stockUuid;
} 