package com.kinopoisk.exception;

/**
 * Выбрасывается, когда фильм с указанным ID не найден в базе данных.
 */
public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long id) {
        super("Фильм с ID " + id + " не найден");
    }
}
