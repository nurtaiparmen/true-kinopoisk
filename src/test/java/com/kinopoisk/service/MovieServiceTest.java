package com.kinopoisk.service;

import com.kinopoisk.exception.MovieNotFoundException;
import com.kinopoisk.model.Movie;
import com.kinopoisk.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Модульные тесты сервиса MovieService.
 * Репозиторий заменён mock-объектом (Mockito) — база данных не используется.
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie dune;
    private Movie oppenheimer;
    private Movie interstellar;

    @BeforeEach
    void setUp() {
        dune = movie(1L, "Дюна: Часть вторая", 8.2, "Фантастика, Приключения, Драма");
        oppenheimer = movie(2L, "Оппенгеймер", 8.5, "Драма, Биография, История");
        interstellar = movie(3L, "Интерстеллар", 8.6, "Фантастика, Драма, Приключения");
    }

    private static Movie movie(Long id, String title, double rating, String genres) {
        Movie m = new Movie();
        m.setId(id);
        m.setTitle(title);
        m.setRating(rating);
        m.setGenres(genres);
        return m;
    }

    // ---------- 1. Получение списка фильмов ----------

    @Test
    @DisplayName("findAll() возвращает все фильмы из репозитория")
    void findAll_returnsAllMovies() {
        when(movieRepository.findAll()).thenReturn(List.of(dune, oppenheimer, interstellar));

        List<Movie> result = movieService.findAll();

        assertThat(result).hasSize(3)
                .extracting(Movie::getTitle)
                .containsExactly("Дюна: Часть вторая", "Оппенгеймер", "Интерстеллар");
        verify(movieRepository, times(1)).findAll();
    }

    // ---------- 2. Поиск по существующему ID ----------

    @Test
    @DisplayName("findById() возвращает фильм, если он существует")
    void findById_existingId_returnsMovie() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(dune));

        Optional<Movie> result = movieService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Дюна: Часть вторая");
    }

    @Test
    @DisplayName("getById() возвращает фильм, если он существует")
    void getById_existingId_returnsMovie() {
        when(movieRepository.findById(2L)).thenReturn(Optional.of(oppenheimer));

        Movie result = movieService.getById(2L);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("Оппенгеймер");
    }

    // ---------- 3. Поведение при несуществующем ID ----------

    @Test
    @DisplayName("findById() возвращает пустой Optional для несуществующего ID")
    void findById_missingId_returnsEmpty() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Movie> result = movieService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getById() выбрасывает MovieNotFoundException для несуществующего ID")
    void getById_missingId_throwsException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getById(999L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findById(null) возвращает пустой Optional и не обращается к репозиторию")
    void findById_nullId_returnsEmptyWithoutRepositoryCall() {
        Optional<Movie> result = movieService.findById(null);

        assertThat(result).isEmpty();
        verify(movieRepository, never()).findById(anyLong());
    }

    // ---------- 4. Пустой список фильмов ----------

    @Test
    @DisplayName("findAll() корректно возвращает пустой список")
    void findAll_emptyRepository_returnsEmptyList() {
        when(movieRepository.findAll()).thenReturn(List.of());

        List<Movie> result = movieService.findAll();

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("findHero() при пустой базе возвращает пустой Optional")
    void findHero_emptyRepository_returnsEmpty() {
        when(movieRepository.findByHeroTrueOrderByIdAsc()).thenReturn(List.of());
        when(movieRepository.findAll()).thenReturn(List.of());

        assertThat(movieService.findHero()).isEmpty();
    }

    @Test
    @DisplayName("findSimilar() при пустой базе возвращает пустой список")
    void findSimilar_emptyRepository_returnsEmptyList() {
        when(movieRepository.findAll()).thenReturn(List.of());

        assertThat(movieService.findSimilar(dune)).isEmpty();
    }

    // ---------- Дополнительно: похожие фильмы ----------

    @Test
    @DisplayName("findSimilar() подбирает фильмы по общим жанрам и исключает сам фильм")
    void findSimilar_returnsMoviesWithCommonGenresExcludingSelf() {
        Movie comedy = movie(4L, "1+1", 8.9, "Драма, Комедия");
        Movie horror = movie(5L, "Ужастик", 5.0, "Ужасы");
        when(movieRepository.findAll()).thenReturn(List.of(dune, oppenheimer, interstellar, comedy, horror));

        List<Movie> result = movieService.findSimilar(dune);

        assertThat(result).extracting(Movie::getId)
                .doesNotContain(1L)      // сам фильм исключён
                .doesNotContain(5L)      // нет общих жанров
                .containsExactly(3L, 4L, 2L); // Интерстеллар (3 общих жанра), затем по рейтингу
    }

    @Test
    @DisplayName("findHero() возвращает фильм с флагом hero")
    void findHero_returnsHeroMovie() {
        dune.setHero(true);
        when(movieRepository.findByHeroTrueOrderByIdAsc()).thenReturn(List.of(dune));

        assertThat(movieService.findHero()).contains(dune);
        verify(movieRepository, never()).findAll();
    }
}
