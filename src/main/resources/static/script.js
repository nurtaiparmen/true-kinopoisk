/* ============================================
   КИНОПОИСК — Интерактивность
   (исходный script.js + исправления и переходы на страницы фильмов)
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {

    // ===== Бургер-меню =====
    const burgerBtn = document.getElementById('burgerBtn');
    const mobileMenu = document.getElementById('mobileMenu');

    if (burgerBtn && mobileMenu) {
        burgerBtn.addEventListener('click', () => {
            burgerBtn.classList.toggle('active');
            mobileMenu.classList.toggle('active');
            document.body.style.overflow = mobileMenu.classList.contains('active') ? 'hidden' : '';
        });

        // Закрытие меню при клике на ссылку
        mobileMenu.querySelectorAll('.mobile-link').forEach(link => {
            link.addEventListener('click', () => {
                burgerBtn.classList.remove('active');
                mobileMenu.classList.remove('active');
                document.body.style.overflow = '';
            });
        });
    }


    // ===== Жанры (переключение активного) =====
    const genresScroll = document.getElementById('genresScroll');
    if (genresScroll) {
        const pills = genresScroll.querySelectorAll('.genre-pill');
        pills.forEach(pill => {
            pill.addEventListener('click', () => {
                pills.forEach(p => p.classList.remove('genre-pill--active'));
                pill.classList.add('genre-pill--active');
            });
        });
    }


    // ===== Тост-уведомление =====
    const toast = document.getElementById('toast');
    const toastText = document.getElementById('toastText');
    let toastTimeout = null;

    function showToast(message) {
        if (!toast || !toastText) return;
        if (toastTimeout) {
            clearTimeout(toastTimeout);
        }
        toastText.textContent = message;
        toast.classList.add('show');
        toastTimeout = setTimeout(() => {
            toast.classList.remove('show');
        }, 2500);
    }


    // ===== «Смотреть позже» / закладки — хранятся в localStorage по ID фильма =====
    const WATCH_LATER_KEY = 'kinopoisk.watchLater';

    function loadWatchLater() {
        try {
            const raw = localStorage.getItem(WATCH_LATER_KEY);
            const list = raw ? JSON.parse(raw) : [];
            return Array.isArray(list) ? list.map(String) : [];
        } catch (e) {
            return [];
        }
    }

    function saveWatchLater(list) {
        try {
            localStorage.setItem(WATCH_LATER_KEY, JSON.stringify(list));
        } catch (e) {
            // localStorage может быть недоступен (приватный режим) — просто не сохраняем
        }
    }

    function isSaved(id) {
        return loadWatchLater().includes(String(id));
    }

    // Возвращает true, если фильм добавлен; false — если удалён
    function toggleWatchLater(id) {
        const list = loadWatchLater();
        const key = String(id);
        const index = list.indexOf(key);
        if (index === -1) {
            list.push(key);
            saveWatchLater(list);
            return true;
        }
        list.splice(index, 1);
        saveWatchLater(list);
        return false;
    }


    // ===== Кнопка «Смотреть позже» (на герое и на странице фильма) =====
    document.querySelectorAll('.btn-watch-later').forEach(btn => {
        const id = btn.dataset.id;
        if (id && isSaved(id)) {
            btn.classList.add('saved');
        }
        btn.addEventListener('click', () => {
            if (!id) return;
            const added = toggleWatchLater(id);
            btn.classList.toggle('saved', added);
            showToast(added ? 'Добавлено в «Смотреть позже»' : 'Удалено из «Смотреть позже»');
        });
    });


    // ===== Кнопка «Смотреть» (если это кнопка, а не ссылка) =====
    const btnWatch = document.querySelector('button.btn-watch');
    if (btnWatch) {
        btnWatch.addEventListener('click', () => {
            showToast('Воспроизведение...');
        });
    }


    // ===== Закладки на карточках =====
    document.querySelectorAll('.movie-card__bookmark').forEach(btn => {
        const id = btn.dataset.id;
        if (id && isSaved(id)) {
            btn.classList.add('saved');
        }
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            e.preventDefault();
            if (id) {
                const added = toggleWatchLater(id);
                btn.classList.toggle('saved', added);
                showToast(added ? 'Добавлено в закладки' : 'Удалено из закладок');
            } else {
                btn.classList.toggle('saved');
                showToast(btn.classList.contains('saved') ? 'Добавлено в закладки' : 'Удалено из закладок');
            }
        });
    });


    // ===== Клик по карточке фильма — переход на страницу фильма =====
    document.querySelectorAll('.movie-card[data-href]').forEach(card => {
        card.addEventListener('click', (e) => {
            // Клик по ссылке внутри карточки браузер обработает сам
            if (e.target.closest('a')) return;
            window.location.href = card.dataset.href;
        });
    });


    // ===== Хедер — фон при скролле =====
    const header = document.querySelector('.header');

    if (header) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 100) {
                header.style.background = 'rgba(13, 13, 13, 0.95)';
            } else {
                header.style.background = 'rgba(13, 13, 13, 0.85)';
            }
        }, { passive: true });
    }


    // ===== Горизонтальный скролл мышью для movies-scroll =====
    document.querySelectorAll('.movies-scroll, .genres-scroll').forEach(container => {
        let isDown = false;
        let startX = 0;
        let scrollLeft = 0;
        let moved = false;

        container.addEventListener('mousedown', (e) => {
            isDown = true;
            moved = false;
            container.style.cursor = 'grabbing';
            startX = e.pageX - container.offsetLeft;
            scrollLeft = container.scrollLeft;
        });

        container.addEventListener('mouseleave', () => {
            isDown = false;
            container.style.cursor = 'grab';
        });

        container.addEventListener('mouseup', () => {
            isDown = false;
            container.style.cursor = 'grab';
        });

        container.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - container.offsetLeft;
            const walk = (x - startX) * 1.5;
            if (Math.abs(walk) > 5) moved = true;
            container.scrollLeft = scrollLeft - walk;
        });

        // Если пользователь тянул ленту, клик не должен открывать карточку
        container.addEventListener('click', (e) => {
            if (moved) {
                e.stopPropagation();
                e.preventDefault();
                moved = false;
            }
        }, true);

        container.style.cursor = 'grab';
    });


    // ===== Intersection Observer — анимация появления =====
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -40px 0px'
    };

    const fadeObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                fadeObserver.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Наблюдаем за карточками и элементами топ-списка
    document.querySelectorAll('.movie-card, .top-item').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 500ms ease, transform 500ms ease';
        fadeObserver.observe(el);
    });

    // Небольшая задержка для каскадного эффекта
    document.querySelectorAll('.movies-grid .movie-card').forEach((card, i) => {
        card.style.transitionDelay = `${i * 60}ms`;
    });

    document.querySelectorAll('.top-item').forEach((item, i) => {
        item.style.transitionDelay = `${i * 80}ms`;
    });


    // ===== Поиск (Enter — показать тост) =====
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && searchInput.value.trim()) {
                showToast(`Поиск: «${searchInput.value.trim()}»`);
                searchInput.value = '';
                searchInput.blur();
            }
        });
    }

});
