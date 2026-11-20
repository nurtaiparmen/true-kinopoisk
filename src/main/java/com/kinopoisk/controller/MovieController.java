package com.kinopoisk.controller;

import com.kinopoisk.model.Movie;
import com.kinopoisk.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * MVC-контроллер: отдаёт HTML-страницы (Thymeleaf).
 *  GET /            — главная страница
 *  GET /movies/{id} — страница отдельного фильма
 */
@Controller
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("hero", movieService.findHero().orElse(null));
        model.addAttribute("popular", movieService.findPopular());
        model.addAttribute("newReleases", movieService.findNewReleases());
        model.addAttribute("top", movieService.findTop());
        return "index";
    }

    @GetMapping("/movies/{id}")
    public String movie(@PathVariable Long id, Model model) {
        // Если фильма нет, сервис выбросит MovieNotFoundException,
        // а GlobalExceptionHandler покажет страницу 404.
        Movie movie = movieService.getById(id);
        model.addAttribute("movie", movie);
        model.addAttribute("similar", movieService.findSimilar(movie));
        return "movie";
    }
}
