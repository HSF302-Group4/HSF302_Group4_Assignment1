package com.fu.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class MovieRevenueDTO {
    private String title;
    private Long ticketCount;
    private BigDecimal revenue;
}