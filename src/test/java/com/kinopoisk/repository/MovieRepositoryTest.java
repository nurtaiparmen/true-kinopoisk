package com.kinopoisk.repository;

import com.kinopoisk.model.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты репозитория на отдельной тестовой базе H2 (профиль test).
 * @DataJpaTest поднимает только JPA-слой; загрузчик данных DataLoader не запускается,
 * поэтому база перед каждым тестом пуста, а изменения откатываются после теста.
 */
@DataJpaTest
@ActiveProfiles("test")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static Movie newMovie(String title, int year, double rating, String genres, String poster) {
        return new Movie(title, "Описание " + title, year, rating, 1000, "США", 120, "16+",
                genres, poster, "Режиссёр", "Актёр 1, Актёр 2");
    }

    // ---------- 1. Сохранение фильма ----------

    @Test
    @DisplayName("save() сохраняет фильм и присваивает ID")
    void save_persistsMovieAndGeneratesId() {
        Movie movie = newMovie("Интерстеллар", 2014, 8.6, "Фантастика, Драма", "interstellar.jpg");

        Movie saved = movieRepository.save(movie);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull().isPositive();
        Movie fromDb = entityManager.find(Movie.class, saved.getId());
        assertThat(fromDb.getTitle()).isEqualTo("Интерстеллар");
        assertThat(fromDb.getPoster()).isEqualTo("interstellar.jpg");
        assertThat(fromDb.getRating()).isEqualTo(8.6);
    }

    // ---------- 2. Поиск по ID ----------

    @Test
    @DisplayName("findById() находит сохранённый фильм")
    void findById_returnsSavedMovie() {
        Movie saved = entityManager.persistFlushFind(
                newMovie("Джокер", 2019, 8.0, "Триллер, Драма", "joker.webp"));

        Optional<Movie> found = movieRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Джокер");
        assertThat(found.get().getYear()).isEqualTo(2019);
    }

    @Test
    @DisplayName("findById() для несуществующего ID возвращает пустой Optional")
    void findById_missing_returnsEmpty() {
        assertThat(movieRepository.findById(12345L)).isEmpty();
    }

    // ---------- 3. Получение всех записей ----------

    @Test
    @DisplayName("findAll() возвращает все сохранённые фильмы")
    void findAll_returnsAllSavedMovies() {
        assertThat(movieRepository.findAll()).isEmpty();

        entityManager.persist(newMovie("Бэтмен", 2022, 7.9, "Экшен", "batman.jpg"));
        entityManager.persist(newMovie("Оппенгеймер", 2023, 8.5, "Драма", "oppenheimer.webp"));
        entityManager.persist(newMovie("Дюна: Часть вторая", 2024, 8.2, "Фантастика", "dune2.jpg"));
        entityManager.flush();

        List<Movie> all = movieRepository.findAll();

        assertThat(all).hasSize(3)
                .extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("Бэтмен", "Оппенгеймер", "Дюна: Часть вторая");
    }

    // ---------- Дополнительно: методы, сгенерированные по имени ----------

    @Test
    @DisplayName("Методы выборки по секциям (hero, popular, newRelease, topRank) работают")
    void sectionQueries_work() {
        Movie hero = newMovie("Герой", 2025, 7.8, "Экшен", "spiderman.jpg");
        hero.setHero(true);
        Movie popular = newMovie("Популярный", 2024, 8.0, "Драма", "dune2.jpg");
        popular.setPopular(true);
        Movie fresh = newMovie("Новинка", 2024, 6.5, "Триллер", "deadheaven.jpg");
        fresh.setNewRelease(true);
        Movie top2 = newMovie("Топ 2", 1999, 9.1, "Драма", "greenmile.webp");
        top2.setTopRank(2);
        Movie top1 = newMovie("Топ 1", 1994, 9.2, "Драма", "shawshenk.webp");
        top1.setTopRank(1);

        entityManager.persist(hero);
        entityManager.persist(popular);
        entityManager.persist(fresh);
        entityManager.persist(top2);
        entityManager.persist(top1);
        entityManager.flush();

        assertThat(movieRepository.findByHeroTrueOrderByIdAsc()).extracting(Movie::getTitle).containsExactly("Герой");
        assertThat(movieRepository.findByPopularTrueOrderByIdAsc()).extracting(Movie::getTitle).containsExactly("Популярный");
        assertThat(movieRepository.findByNewReleaseTrueOrderByIdAsc()).extracting(Movie::getTitle).containsExactly("Новинка");
        assertThat(movieRepository.findByTopRankNotNullOrderByTopRankAsc())
                .extracting(Movie::getTitle).containsExactly("Топ 1", "Топ 2");
    }
}
