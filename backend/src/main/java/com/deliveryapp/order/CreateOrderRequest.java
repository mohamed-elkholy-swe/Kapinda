package com.deliveryapp.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
  @NotBlank(message = "اسم العميل مطلوب / Customer name is required") @Size(max = 100, message = "الاسم يجب ألا يتجاوز ١٠٠ حرف / Name must not exceed 100 characters") String customerName,
  @NotBlank @Size(min = 10) String customerPhone,
  @NotBlank @Size(min = 8) String customerAddress,
  @NotBlank String area,
  @NotEmpty List<@Valid CreateOrderItemRequest> items
) {}
