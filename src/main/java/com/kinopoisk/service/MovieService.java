package com.kinopoisk.service;

import com.kinopoisk.exception.MovieNotFoundException;
import com.kinopoisk.model.Movie;
import com.kinopoisk.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Бизнес-логика работы с фильмами.
 * Контроллеры не обращаются к репозиторию напрямую — только через этот сервис.
 */
@Service
public class MovieService {

    /** Сколько похожих фильмов показывать на странице фильма. */
    public static final int SIMILAR_LIMIT = 6;

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /** Все фильмы. */
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    /** Поиск фильма по ID. Возвращает пустой Optional, если фильма нет. */
    public Optional<Movie> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return movieRepository.findById(id);
    }

    /** Получение фильма по ID. Если фильма нет — выбрасывает MovieNotFoundException. */
    public Movie getById(Long id) {
        return findById(id).orElseThrow(() -> new MovieNotFoundException(id));
    }

    /** Фильм для главного баннера. Если флаг hero не установлен ни у кого — берётся первый фильм. */
    public Optional<Movie> findHero() {
        List<Movie> heroes = movieRepository.findByHeroTrueOrderByIdAsc();
        if (!heroes.isEmpty()) {
            return Optional.of(heroes.get(0));
        }
        return movieRepository.findAll().stream().findFirst();
    }

    public List<Movie> findPopular() {
        return movieRepository.findByPopularTrueOrderByIdAsc();
    }

    public List<Movie> findNewReleases() {
        return movieRepository.findByNewReleaseTrueOrderByIdAsc();
    }

    public List<Movie> findTop() {
        return movieRepository.findByTopRankNotNullOrderByTopRankAsc();
    }

    /**
     * Похожие фильмы: совпадает хотя бы один жанр, сам фильм исключается.
     * Сначала идут фильмы с большим числом общих жанров, затем — с более высоким рейтингом.
     */
    public List<Movie> findSimilar(Movie movie) {
        if (movie == null) {
            return List.of();
        }
        List<String> genres = movie.getGenreList();
        return movieRepository.findAll().stream()
                .filter(other -> !other.getId().equals(movie.getId()))
                .filter(other -> other.getGenreList().stream().anyMatch(genres::contains))
                .sorted((a, b) -> {
                    long commonA = a.getGenreList().stream().filter(genres::contains).count();
                    long commonB = b.getGenreList().stream().filter(genres::contains).count();
                    if (commonA != commonB) {
                        return Long.compare(commonB, commonA);
                    }
                    double ratingA = a.getRating() == null ? 0 : a.getRating();
                    double ratingB = b.getRating() == null ? 0 : b.getRating();
                    return Double.compare(ratingB, ratingA);
                })
                .limit(SIMILAR_LIMIT)
                .toList();
    }

    /** Сохранение фильма (используется при заполнении базы и в тестах). */
    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    public long count() {
        return movieRepository.count();
    }
}
