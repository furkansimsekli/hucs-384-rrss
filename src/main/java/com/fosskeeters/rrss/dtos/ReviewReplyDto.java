package com.fosskeeters.rrss.dtos;

import com.fosskeeters.rrss.models.Review;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewReplyDto {
    @NotNull
    @Size(min = 10, max = 4096, message = "Reply length must be between 10-4096 characters")
    private String merchantReplyBody;

    public ReviewReplyDto() {}

    public ReviewReplyDto(Review review) {
        this.merchantReplyBody = review.getMerchantReplyBody();
    }

    public String getMerchantReplyBody() {
        return merchantReplyBody;
    }

    public void setMerchantReplyBody(String merchantReplyBody) {
        this.merchantReplyBody = merchantReplyBody.trim();
    }
}
