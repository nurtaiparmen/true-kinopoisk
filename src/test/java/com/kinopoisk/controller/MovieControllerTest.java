package com.kinopoisk.controller;

import com.kinopoisk.exception.GlobalExceptionHandler;
import com.kinopoisk.exception.MovieNotFoundException;
import com.kinopoisk.model.Movie;
import com.kinopoisk.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Тесты контроллеров через MockMvc (слой Web без реального сервера и базы).
 * Сервис заменён mock-объектом.
 */
@WebMvcTest(controllers = {MovieController.class, MovieRestController.class})
@Import(GlobalExceptionHandler.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    private Movie dune;
    private Movie oppenheimer;

    @BeforeEach
    void setUp() {
        dune = new Movie("Дюна: Часть вторая", "Описание Дюны", 2024, 8.2, 118540,
                "США, Канада", 166, "12+", "Фантастика, Приключения, Драма", "dune2.jpg",
                "Дени Вильнёв", "Тимоти Шаламе, Зендея");
        dune.setId(1L);
        dune.setPopular(true);

        oppenheimer = new Movie("Оппенгеймер", "Описание", 2023, 8.5, 97210,
                "США, Великобритания", 180, "16+", "Драма, Биография, История", "oppenheimer.webp",
                "Кристофер Нолан", "Киллиан Мёрфи");
        oppenheimer.setId(2L);
        oppenheimer.setHero(true);

        when(movieService.findHero()).thenReturn(Optional.of(oppenheimer));
        when(movieService.findPopular()).thenReturn(List.of(dune));
        when(movieService.findNewReleases()).thenReturn(List.of());
        when(movieService.findTop()).thenReturn(List.of());
        when(movieService.findAll()).thenReturn(List.of(dune, oppenheimer));
        when(movieService.findById(1L)).thenReturn(Optional.of(dune));
        when(movieService.findById(999L)).thenReturn(Optional.empty());
        when(movieService.getById(1L)).thenReturn(dune);
        when(movieService.getById(999L)).thenThrow(new MovieNotFoundException(999L));
        when(movieService.findSimilar(any())).thenReturn(List.of(oppenheimer));
    }

    // ---------- 1. Главная страница ----------

    @Test
    @DisplayName("GET / — главная страница доступна и содержит фильмы")
    void index_returnsOkWithMovies() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("hero", "popular", "newReleases", "top"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Кинопоиск")))
                .andExpect(content().string(containsString("Дюна: Часть вторая")))
                .andExpect(content().string(containsString("/movies/1")))
                .andExpect(content().string(containsString("/images/dune2.jpg")));
    }

    // ---------- 2. Страница существующего фильма ----------

    @Test
    @DisplayName("GET /movies/1 — страница фильма открывается и содержит данные")
    void moviePage_existingId_returnsOk() throws Exception {
        mockMvc.perform(get("/movies/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("movie"))
                .andExpect(model().attributeExists("movie", "similar"))
                .andExpect(content().string(containsString("Дюна: Часть вторая")))
                .andExpect(content().string(containsString("Дени Вильнёв")))
                .andExpect(content().string(containsString("Тимоти Шаламе")))
                .andExpect(content().string(containsString("2ч 46мин")))
                .andExpect(content().string(containsString("Похожие фильмы")))
                .andExpect(content().string(containsString("Смотреть позже")));
    }

    // ---------- 3. Несуществующий ID ----------

    @Test
    @DisplayName("GET /movies/999 — страница 404, а не ошибка сервера")
    void moviePage_missingId_returns404Page() throws Exception {
        mockMvc.perform(get("/movies/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Страница не найдена")));
    }

    @Test
    @DisplayName("GET /movies/abc — некорректный ID даёт 400, а не 500")
    void moviePage_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/movies/abc"))
                .andExpect(status().isBadRequest());
    }

    // ---------- 4. REST API: все фильмы ----------

    @Test
    @DisplayName("GET /api/movies — JSON со всеми фильмами")
    void api_getAll_returnsJsonList() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Дюна: Часть вторая"))
                .andExpect(jsonPath("$[1].title").value("Оппенгеймер"));
    }

    // ---------- 5. REST API: фильм по ID ----------

    @Test
    @DisplayName("GET /api/movies/1 — JSON фильма")
    void api_getById_returnsJson() throws Exception {
        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Дюна: Часть вторая"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.rating").value(8.2))
                .andExpect(jsonPath("$.director").value("Дени Вильнёв"))
                .andExpect(jsonPath("$.posterUrl").value("/images/dune2.jpg"));
    }

    // ---------- 6. Корректные HTTP-статусы ----------

    @Test
    @DisplayName("GET /api/movies/999 — статус 404 для несуществующего фильма")
    void api_getById_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/movies/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Статические ресурсы отдаются (style.css, script.js, постер)")
    void staticResources_areServed() throws Exception {
        mockMvc.perform(get("/style.css")).andExpect(status().isOk());
        mockMvc.perform(get("/script.js")).andExpect(status().isOk());
        mockMvc.perform(get("/images/dune2.jpg")).andExpect(status().isOk());
    }
}
