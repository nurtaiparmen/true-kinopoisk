package com.kinopoisk.repository;

import com.kinopoisk.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с таблицей фильмов.
 * Базовые операции (findAll, findById, save, delete...) предоставляет JpaRepository.
 * Остальные методы Spring Data генерирует автоматически по их названию.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /** Фильм для главного баннера (героя). */
    List<Movie> findByHeroTrueOrderByIdAsc();

    /** Секция «Популярное сейчас». */
    List<Movie> findByPopularTrueOrderByIdAsc();

    /** Секция «Новинки кино». */
    List<Movie> findByNewReleaseTrueOrderByIdAsc();

    /** Секция «Топ-250» — фильмы с позицией в топе, по возрастанию позиции. */
    List<Movie> findByTopRankNotNullOrderByTopRankAsc();
}
