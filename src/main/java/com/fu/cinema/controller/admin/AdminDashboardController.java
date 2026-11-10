package com.fu.cinema.controller.admin;

import com.fu.cinema.dto.RoomShowtimeCount;
import com.fu.cinema.service.BookingService;
import com.fu.cinema.service.CustomerService;
import com.fu.cinema.service.MovieService;
import com.fu.cinema.service.ShowtimeService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final MovieService movieService;
    private final CustomerService customerService;
    private final BookingService bookingService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public String dashboard(Model model) {
        DashboardStats stats = new DashboardStats(
                movieService.countShowing(),
                customerService.count(),
                bookingService.countPending(),
                showtimeService.countTodayByRoom()
        );
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @Getter
    @AllArgsConstructor
    public static class DashboardStats {
        private final long showingMovies;
        private final long customers;
        private final long pendingBookings;
        private final List<RoomShowtimeCount> todayShowtimesByRoom;
    }
}
