package com.kinopoisk;

import com.kinopoisk.model.Movie;
import com.kinopoisk.service.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты: поднимается всё приложение с тестовой базой H2,
 * DataLoader заполняет её реальными фильмами сайта.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KinopoiskApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieService movieService;

    @Test
    @DisplayName("Контекст запускается, база заполнена 18 фильмами сайта")
    void contextLoads_andDatabaseIsSeeded() {
        List<Movie> all = movieService.findAll();

        assertThat(all).hasSize(18);
        assertThat(all).extracting(Movie::getTitle).contains(
                "Человек-паук: Новый день", "Дюна: Часть вторая", "Оппенгеймер", "Бэтмен",
                "Интерстеллар", "Крёстный отец", "Аватар: Путь воды", "Джокер", "Побег из Шоушенка",
                "Медельин", "Годзилла x Конг", "Мёртвый рай", "Плохие парни 4", "Форсаж 10", "Фуриоса",
                "Зелёная миля", "Список Шиндлера", "1+1");
        assertThat(movieService.findHero()).isPresent();
        assertThat(movieService.findPopular()).hasSize(8);
        assertThat(movieService.findNewReleases()).hasSize(6);
        assertThat(movieService.findTop()).hasSize(5);
    }

    @Test
    @DisplayName("У каждого фильма есть постер, и он отдаётся сервером")
    void everyMovieHasPosterThatIsServed() throws Exception {
        for (Movie movie : movieService.findAll()) {
            assertThat(movie.getPoster()).as("постер фильма %s", movie.getTitle()).isNotBlank();
            mockMvc.perform(get(movie.getPosterUrl()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Каждый фильм открывается по своему URL /movies/{id}")
    void everyMoviePageOpens() throws Exception {
        for (Movie movie : movieService.findAll()) {
            mockMvc.perform(get("/movies/" + movie.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(movie.getTitle())));
        }
    }

    @Test
    @DisplayName("Главная страница содержит ссылки на все карточки из секций")
    void indexContainsLinksToSectionMovies() throws Exception {
        var result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        for (Movie movie : movieService.findPopular()) {
            assertThat(html).contains("/movies/" + movie.getId());
        }
        for (Movie movie : movieService.findNewReleases()) {
            assertThat(html).contains("/movies/" + movie.getId());
        }
        for (Movie movie : movieService.findTop()) {
            assertThat(html).contains("/movies/" + movie.getId());
        }
        assertThat(html).contains("Популярное сейчас", "Новинки кино", "Топ-250");
    }

    @Test
    @DisplayName("REST API отдаёт все 18 фильмов из базы")
    void apiReturnsAllMoviesFromDatabase() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(18))
                .andExpect(jsonPath("$[0].title").value("Человек-паук: Новый день"));
    }

    @Test
    @DisplayName("Несуществующий фильм и несуществующий адрес дают 404")
    void unknownMovieAndUnknownUrl_return404() throws Exception {
        mockMvc.perform(get("/movies/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("404")));
        mockMvc.perform(get("/api/movies/999"))
                .andExpect(status().isNotFound());
    }
}
