package com.fu.cinema.controller;

import com.fu.cinema.entity.Movie;
import com.fu.cinema.service.GenreService;
import com.fu.cinema.service.MovieService;
import com.fu.cinema.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;
    private final GenreService genreService;

    @GetMapping({"/", "/movies"})
    public String home(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) Long genreId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Movie> movies = movieService.searchForCustomer(kw, genreId, page, 12);
        model.addAttribute("movies", movies);
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("kw", kw);
        model.addAttribute("genreId", genreId);
        model.addAttribute("pageUrl", PageUtils.currentUrlWithoutPage());
        return "index";
    }
}
