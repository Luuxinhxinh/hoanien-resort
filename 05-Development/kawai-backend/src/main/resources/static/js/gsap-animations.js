document.addEventListener("DOMContentLoaded", () => {
    gsap.registerPlugin(ScrollTrigger);

    // Fade up sections on scroll
    const sections = gsap.utils.toArray('main section, .feature-row, .results, .site-footer');
    sections.forEach(section => {
        gsap.fromTo(section, 
            { opacity: 0, y: 40 },
            { 
                opacity: 1, 
                y: 0, 
                duration: 1, 
                ease: "power2.out",
                scrollTrigger: {
                    trigger: section,
                    start: "top 85%",
                    toggleActions: "play none none none"
                }
            }
        );
    });

    // Stagger reveal cards inside grids
    const grids = gsap.utils.toArray('.room-grid');
    grids.forEach(grid => {
        const cards = grid.querySelectorAll('.room-card');
        if (cards.length > 0) {
            gsap.fromTo(cards, 
                { opacity: 0, y: 50 },
                {
                    opacity: 1,
                    y: 0,
                    duration: 0.8,
                    stagger: 0.15,
                    ease: "power3.out",
                    scrollTrigger: {
                        trigger: grid,
                        start: "top 85%",
                        toggleActions: "play none none none"
                    }
                }
            );
        }
    });

    // Stagger reveal feature chips
    const chipLists = gsap.utils.toArray('.chip-list');
    chipLists.forEach(list => {
        const chips = list.querySelectorAll('.chip');
        if (chips.length > 0) {
            gsap.fromTo(chips,
                { opacity: 0, x: -20 },
                {
                    opacity: 1,
                    x: 0,
                    duration: 0.6,
                    stagger: 0.1,
                    ease: "power2.out",
                    scrollTrigger: {
                        trigger: list,
                        start: "top 90%",
                        toggleActions: "play none none none"
                    }
                }
            );
        }
    });

    // Header reveal
    gsap.fromTo('.site-header', 
        { opacity: 0, y: -20 }, 
        { opacity: 1, y: 0, duration: 1.2, ease: "power2.out" }
    );
});
