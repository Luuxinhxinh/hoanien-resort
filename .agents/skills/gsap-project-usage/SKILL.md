---
name: gsap-project-usage
description: Cú pháp chuẩn của GSAP dùng nội bộ trong project hiện tại. Kích hoạt khi thêm/sửa các hiệu ứng cuộn trang cơ bản bằng GSAP trong template Thymeleaf.
---

# Hướng dẫn sử dụng GSAP (Project-specific)

Trong project này, chúng ta chỉ dùng GSAP cho các hiệu ứng cơ bản (chủ yếu là fade in khi cuộn trang).
Để tiết kiệm token, hãy tuân thủ pattern ngắn gọn sau thay vì nạp toàn bộ API GSAP:

## 1. Cách import CDN
Bắt buộc sử dụng CDN (đặt ở cuối thẻ `<body>` hoặc trong block JS chung). Cần có fallback:
```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.2/gsap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.2/ScrollTrigger.min.js"></script>
<script>
  if (typeof gsap === 'undefined') {
    console.warn("GSAP CDN failed to load. Fallback to no animation.");
  }
</script>
```

## 2. Cú pháp ScrollTrigger cơ bản đang dùng
```javascript
if (typeof gsap !== 'undefined') {
    gsap.registerPlugin(ScrollTrigger);
    gsap.utils.toArray('.animate-on-scroll').forEach(element => {
        gsap.fromTo(element, 
            { opacity: 0, y: 50 }, 
            { 
                opacity: 1, 
                y: 0, 
                duration: 0.8, 
                ease: "power2.out",
                scrollTrigger: {
                    trigger: element,
                    start: "top 80%", // Kích hoạt khi top của element chạm mốc 80% viewport
                    toggleActions: "play none none none"
                }
            }
        );
    });
}
```
*Lưu ý:* Mọi sửa đổi nâng cao về timeline, matchMedia, stagger thì xem ở các file skill gốc (nhưng hiếm khi cần).
