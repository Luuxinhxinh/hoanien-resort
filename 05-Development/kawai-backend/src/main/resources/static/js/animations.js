document.addEventListener("DOMContentLoaded", () => {
    // Basic Intersection Observer for smooth reveal
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.15
    };

    const observer = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('opacity-100', 'translate-y-0');
                entry.target.classList.remove('opacity-0', 'translate-y-8');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Select all major sections to animate them
    const sections = document.querySelectorAll('main section, .feature-row, .room-grid, .room-card, .results, .site-footer');
    sections.forEach(section => {
        section.classList.add('transition-all', 'duration-[800ms]', 'ease-out', 'opacity-0', 'translate-y-8', 'will-change-transform');
        observer.observe(section);
    });

    // Sub-navigation smooth reveal
    const subNav = document.querySelector('.fixed.top-\\[70px\\]');
    if (subNav) {
        subNav.style.opacity = '0';
        subNav.style.transform = 'translateY(-10px)';
        subNav.style.transition = 'all 0.6s ease-out';
        setTimeout(() => {
            subNav.style.opacity = '1';
            subNav.style.transform = 'translateY(0)';
        }, 300);
    }
});
