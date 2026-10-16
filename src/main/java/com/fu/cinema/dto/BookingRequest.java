package com.fu.cinema.dto;

import lombok.Data;

import java.util.Collection;
import java.util.List;


    @Data
    public class BookingRequest {
        private Long showtimeId;
        private List<String> seatCodes; // Phải có <String>, không để List trần
    }


