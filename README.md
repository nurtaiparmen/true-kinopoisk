# Кинопоиск — учебный сайт о фильмах на Spring Boot

Модернизированная версия статического сайта «Кинопоиск»: исходная вёрстка (`ndex.html`, `style.css`,
`script.js`, папка `images/`) сохранена и перенесена на Java-backend **Spring Boot** с базой данных **H2**.

## Назначение проекта

- Главная страница с тем же дизайном, что и в оригинале (тёмная тема, оранжевые акценты, шрифт Inter,
  герой-баннер, жанры, «Популярное сейчас», «Новинки кино», «Топ-250»). Карточки теперь формируются из базы данных.
- Отдельная страница для каждого фильма: `/movies/1`, `/movies/2`, … (название, большой постер, рейтинг, год,
  страна, продолжительность, возраст, жанры, описание, режиссёр, актёры, кнопка «На главную», кнопка «Смотреть
  позже», блок «Похожие фильмы»).
- Корректная страница 404 для несуществующего фильма (`/movies/999`) и любого неизвестного адреса.
- REST API: `GET /api/movies` и `GET /api/movies/{id}`.
- Автоматизированные тесты (JUnit 5, Mockito, Spring Boot Test, MockMvc) — см. `TESTING.md`.

## Используемые технологии

| Технология            | Версия / назначение                            |
|-----------------------|------------------------------------------------|
| Java                  | 21 (подойдёт и 17)                             |
| Spring Boot           | 3.3.5                                          |
| Spring Web (MVC)      | контроллеры, REST API                          |
| Spring Data JPA       | доступ к базе через `MovieRepository`          |
| Hibernate             | ORM (входит в Spring Data JPA)                 |
| H2 Database           | встроенная БД в памяти, не требует установки   |
| Thymeleaf             | HTML-шаблоны                                   |
| Maven                 | сборка и запуск                                |
| JUnit 5, Mockito, MockMvc | тестирование                              |

## Структура проекта

```
kinopoisk/
├── pom.xml                                  — Maven-проект, зависимости
├── README.md                                — этот файл
├── TESTING.md                               — документация по тестированию
└── src/
    ├── main/
    │   ├── java/com/kinopoisk/
    │   │   ├── KinopoiskApplication.java    — точка входа
    │   │   ├── model/Movie.java             — сущность «Фильм» (JPA)
    │   │   ├── repository/MovieRepository.java — JpaRepository
    │   │   ├── service/MovieService.java    — бизнес-логика
    │   │   ├── controller/MovieController.java     — HTML-страницы (/ и /movies/{id})
    │   │   ├── controller/MovieRestController.java — REST API (/api/movies)
    │   │   ├── exception/MovieNotFoundException.java
    │   │   ├── exception/GlobalExceptionHandler.java — страница 404
    │   │   └── config/DataLoader.java       — заполнение базы фильмами при старте
    │   └── resources/
    │       ├── application.properties       — настройки H2, JPA, Thymeleaf
    │       ├── templates/
    │       │   ├── index.html               — главная (перенесённый ndex.html)
    │       │   ├── movie.html               — страница фильма
    │       │   ├── error/404.html           — страница «не найдено»
    │       │   └── fragments/layout.html    — шапка, меню, футер, тост, карточка фильма
    │       └── static/
    │           ├── style.css                — оригинальные стили + блок дополнений в конце
    │           ├── script.js                — оригинальный скрипт с исправлениями
    │           └── images/                  — все 18 оригинальных постеров (имена не менялись)
    └── test/
        ├── java/com/kinopoisk/
        │   ├── service/MovieServiceTest.java        — модульные тесты сервиса (Mockito)
        │   ├── controller/MovieControllerTest.java  — тесты контроллеров (MockMvc)
        │   ├── repository/MovieRepositoryTest.java  — тесты репозитория (@DataJpaTest)
        │   └── KinopoiskApplicationTests.java       — интеграционные тесты всего приложения
        └── resources/application-test.properties    — отдельная тестовая база H2
```

Архитектура MVC: **Controller → Service → Repository → H2**. Контроллеры не обращаются к репозиторию напрямую.

## Требования для запуска

- JDK 17 или 21 (`java -version`);
- Maven 3.8+ (`mvn -version`). Если Maven не установлен, можно использовать IDE (IntelliJ IDEA / Eclipse),
  которая откроет `pom.xml` как Maven-проект;
- доступ в интернет при первой сборке (Maven скачает зависимости).

Отдельный сервер базы данных не нужен — H2 работает в памяти внутри приложения.

## Запуск приложения через Maven

```bash
cd kinopoisk
mvn spring-boot:run
```

После строки `Started KinopoiskApplication` откройте в браузере:

- Главная: <http://localhost:8080/>
- Страница фильма: <http://localhost:8080/movies/1> (ID от 1 до 18)
- Страница 404: <http://localhost:8080/movies/999>
- REST API: <http://localhost:8080/api/movies>, <http://localhost:8080/api/movies/1>

Альтернативно можно собрать jar и запустить его:

```bash
mvn clean package
java -jar target/kinopoisk-1.0.0.jar
```

## Как открыть H2 Console

1. Запустите приложение.
2. Откройте <http://localhost:8080/h2-console>.
3. Укажите параметры подключения:
   - **JDBC URL:** `jdbc:h2:mem:kinopoisk`
   - **User Name:** `sa`
   - **Password:** (пусто)
4. Нажмите **Connect**. Таблица `MOVIES` содержит все 18 фильмов, например:

```sql
SELECT ID, TITLE, RELEASE_YEAR, RATING, POSTER FROM MOVIES ORDER BY ID;
```

База создаётся заново при каждом запуске (`spring.jpa.hibernate.ddl-auto=create-drop`) и заполняется
классом `DataLoader`.

## Как запустить автоматизированные тесты

```bash
mvn test
```

Тесты используют отдельную базу `jdbc:h2:mem:kinopoisk_test` (файл `src/test/resources/application-test.properties`,
профиль `test`), поэтому не зависят от основной базы. Подробное описание тестов и их результаты — в `TESTING.md`.

Запуск одного класса тестов:

```bash
mvn test -Dtest=MovieServiceTest
```

## Данные о фильмах

- Названия, постеры, годы, рейтинги и жанры взяты из исходной вёрстки сайта (как и были на главной странице).
- Режиссёры, актёры, страны и продолжительность указаны по общеизвестным сведениям о фильмах; описания —
  краткие аннотации для демонстрации.
- Количество оценок — демонстрационные числа (как «42 318 оценок» в исходнике).
- Для фильма «Мёртвый рай» в исходном проекте нет сведений, поэтому его режиссёр, актёры и страна помечены
  как «Нет данных», а описание — как демонстрационное.

## Что изменилось во frontend по сравнению с оригиналом

- `ndex.html` → `templates/index.html`: та же разметка, карточки выводятся через `th:each` из базы.
  Повторяющиеся блоки (шапка, мобильное меню, футер, тост, карточка) вынесены в `fragments/layout.html`.
- Карточки фильмов, элементы «Топ-250», заголовок и кнопка «Смотреть» героя ведут на страницы `/movies/{id}`.
- `script.js`: удалён обрывок кода в конце файла, вызывавший синтаксическую ошибку
  (`SyntaxError: Unexpected identifier 'on'`, переменная `cardBrrd`); добавлен переход по клику на карточку;
  «Смотреть позже» и закладки сохраняются в `localStorage` по ID фильма (раньше состояние терялось при перезагрузке).
- `style.css`: оригинальные стили не менялись; в конце добавлен отдельный блок для страницы фильма и 404,
  построенный на тех же CSS-переменных.
- Кнопки, которых не касалось задание (поиск, «Фильмы», «Сериалы», «Закладки» в шапке и т. п.), сохранили
  исходное поведение.
#   t r u e - k i n o p o i s k  
 