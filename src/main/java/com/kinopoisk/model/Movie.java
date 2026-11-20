package com.kinopoisk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Сущность «Фильм». Хранится в таблице MOVIES базы данных H2.
 */
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    /** Год выпуска (колонка release_year, т.к. YEAR — зарезервированное слово в SQL). */
    @Column(name = "release_year")
    private Integer year;

    /** Рейтинг по 10-балльной шкале, например 8.6. */
    private Double rating;

    /** Количество оценок (демонстрационное значение). */
    private Integer ratingCount;

    private String country;

    /** Продолжительность в минутах. */
    private Integer duration;

    /** Возрастное ограничение, например "16+". */
    private String ageRating;

    /** Жанры через запятую, например "Драма, Криминал". */
    private String genres;

    /** Имя файла постера в папке /images, например "dune2.jpg". */
    private String poster;

    private String director;

    /** Актёрский состав через запятую (колонка actors, т.к. CAST — зарезервированное слово в SQL). */
    @Column(name = "actors", length = 1000)
    private String cast;

    // ---- Поля, определяющие, в каких секциях главной страницы показывается фильм ----

    /** Фильм показывается в большом баннере (герой) на главной странице. */
    private boolean hero;

    /** Фильм входит в секцию «Популярное сейчас». */
    private boolean popular;

    /** Фильм входит в секцию «Новинки кино». */
    private boolean newRelease;

    /** Позиция в «Топ-250» (null — не входит в топ). */
    private Integer topRank;

    public Movie() {
    }

    public Movie(String title, String description, Integer year, Double rating, Integer ratingCount,
                 String country, Integer duration, String ageRating, String genres, String poster,
                 String director, String cast) {
        this.title = title;
        this.description = description;
        this.year = year;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.country = country;
        this.duration = duration;
        this.ageRating = ageRating;
        this.genres = genres;
        this.poster = poster;
        this.director = director;
        this.cast = cast;
    }

    // ---- Вспомогательные методы (не хранятся в базе) ----

    /** Список жанров, разобранный из строки genres. */
    public List<String> getGenreList() {
        if (genres == null || genres.isBlank()) {
            return List.of();
        }
        return Arrays.stream(genres.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** Первый жанр — используется в подписи карточки: «2024 • Фантастика». */
    public String getMainGenre() {
        List<String> list = getGenreList();
        return list.isEmpty() ? "" : list.get(0);
    }

    /** Подпись для списка «Топ-250» в формате исходной вёрстки: «Драма, криминал». */
    public String getTopGenreText() {
        List<String> list = getGenreList();
        if (list.isEmpty()) {
            return "";
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return list.get(0) + ", " + list.get(1).toLowerCase(Locale.ROOT);
    }

    /** Список актёров, разобранный из строки cast. */
    public List<String> getCastList() {
        if (cast == null || cast.isBlank()) {
            return List.of();
        }
        return Arrays.stream(cast.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** Продолжительность в формате «2ч 17мин». */
    public String getDurationText() {
        if (duration == null) {
            return "";
        }
        int hours = duration / 60;
        int minutes = duration % 60;
        if (hours == 0) {
            return minutes + "мин";
        }
        return hours + "ч " + minutes + "мин";
    }

    /** Рейтинг в виде строки с точкой: «8.2» (не зависит от локали). */
    public String getRatingText() {
        return rating == null ? "—" : String.format(Locale.ROOT, "%.1f", rating);
    }

    /** Количество оценок с разделителем тысяч: «42 318». */
    public String getRatingCountText() {
        if (ratingCount == null) {
            return "0";
        }
        return String.format(Locale.ROOT, "%,d", ratingCount).replace(',', ' ');
    }

    /** Путь к постеру для тега img: /images/dune2.jpg */
    public String getPosterUrl() {
        return "/images/" + poster;
    }

    /** Высокий рейтинг (8.6 и выше) — в исходной вёрстке такие оценки помечались классом rating--high. */
    public boolean isHighRating() {
        return rating != null && rating >= 8.6;
    }

    // ---- Геттеры и сеттеры ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public boolean isHero() { return hero; }
    public void setHero(boolean hero) { this.hero = hero; }

    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }

    public boolean isNewRelease() { return newRelease; }
    public void setNewRelease(boolean newRelease) { this.newRelease = newRelease; }

    public Integer getTopRank() { return topRank; }
    public void setTopRank(Integer topRank) { this.topRank = topRank; }

    @Override
    public String toString() {
        return "Movie{id=" + id + ", title='" + title + "', year=" + year + ", rating=" + rating + "}";
    }
}
