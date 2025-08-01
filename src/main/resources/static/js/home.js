document.addEventListener("DOMContentLoaded", function () {
    // New Arrivals Swiper
    const swiperNewArrivals = new Swiper(".mySwiper", {
        slidesPerView: 5,
        spaceBetween: 20,
        loop: true,
        loopAdditionalSlides: 1,
        autoplay: {
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: false,
        },
        pagination: {
            el: ".mySwiper .new-arrivals-swiper-pagination",
            type: "progressbar",
            clickable: false,
        },
        breakpoints: {
            320: {slidesPerView: 2, spaceBetween: 10},
            576: {slidesPerView: 3, spaceBetween: 15},
            768: {slidesPerView: 4, spaceBetween: 20},
            992: {slidesPerView: 5, spaceBetween: 20}
        }
    });

    // Promo Banners Swiper
    const swiperPromo = new Swiper(".promoSwiper", {
        slidesPerView: 3,
        spaceBetween: 20,
        loop: true,
        loopAdditionalSlides: 1,
        autoplay: {
            delay: 4000,
            disableOnInteraction: false,
            pauseOnMouseEnter: false,
        },
        pagination: {
            el: ".promoSwiper .promo-swiper-pagination",
            type: "progressbar",
            clickable: false,
        },
        breakpoints: {
            320: {slidesPerView: 1, spaceBetween: 10},
            576: {slidesPerView: 2, spaceBetween: 15},
            768: {slidesPerView: 3, spaceBetween: 20}
        }
    });

    // Club Kits Swiper
    const swiperClub = new Swiper(".clubSwiper", {
        slidesPerView: 4,
        spaceBetween: 20,
        loop: true,
        loopAdditionalSlides: 1,
        autoplay: {
            delay: 3500,
            disableOnInteraction: false,
            pauseOnMouseEnter: false,
        },
        pagination: {
            el: ".clubSwiper .club-swiper-pagination",
            type: "progressbar",
            clickable: false,
        },
        breakpoints: {
            320: {slidesPerView: 2, spaceBetween: 1},
            576: {slidesPerView: 3, spaceBetween: 15},
            768: {slidesPerView: 4, spaceBetween: 20}
        }
    });


    swiperPromo.on('autoplayStop', function () {
        console.log('Promo Swiper stopped');
    });

    swiperClub.on('autoplayStop', function () {
        console.log('Club Swiper stopped');
    });
});