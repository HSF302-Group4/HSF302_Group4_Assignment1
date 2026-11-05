package com.fu.cinema.desktop.model;

import java.math.BigDecimal;

/** Doanh thu va so luong ve cua mot phim trong ky bao cao. */
public record RevenueRow(String title, long tickets, BigDecimal revenue) {}
