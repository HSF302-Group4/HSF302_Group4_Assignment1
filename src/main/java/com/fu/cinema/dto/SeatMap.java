package com.fu.cinema.dto;

import com.fu.cinema.entity.Showtime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMap {
    private Showtime showtime;
    private List<String> rows;
    private Integer seatsPerRow;
    private Set<String> soldSeats;
    public boolean isSold(String seatCode) {
        return soldSeats != null && soldSeats.contains(seatCode);
    }
}