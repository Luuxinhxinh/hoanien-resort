const username = document.querySelector('meta[name="username"]')?.getAttribute('content');

if (username) {
    const socket = new SockJS('/ws-endpoint');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket for notifications: ' + frame);
        stompClient.subscribe('/topic/notifications/' + username, function (messageOutput) {
            const notification = JSON.parse(messageOutput.body);
            showRealtimeNotification(notification);
        });
    });

    function showRealtimeNotification(n) {
        // Increment badge
        let badge = document.querySelector('.dropdown-notification button .bg-red-500');
        if (!badge) {
            badge = document.createElement('span');
            badge.className = 'absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full';
            document.querySelector('.dropdown-notification button').appendChild(badge);
        }

        // Add to list
        const list = document.getElementById('notifications-list');
        if (list) {
            const item = document.createElement('div');
            item.className = 'p-3 bg-primary/10 rounded-xl border-l-4 border-primary space-y-1';
            
            let icon = n.icon || 'info';
            if (n.type === 'TOUR_CANCELLED') icon = 'cancel';
            if (n.type === 'TOUR_ASSIGNED') icon = 'assignment';

            item.innerHTML = `
                <div class="flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[14px] text-primary">${icon}</span>
                    <p class="text-xs font-semibold text-on-surface">${n.title}</p>
                </div>
                <p class="text-[11px] text-on-surface-variant leading-relaxed line-clamp-2">${n.message}</p>
                <div class="flex items-center justify-between pt-1">
                    <span class="text-[9px] font-medium opacity-50 uppercase tracking-wider">Vừa xong</span>
                </div>
            `;
            list.insertBefore(item, list.firstChild);
        }

        // Show toast
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                toast: true,
                position: 'top-end',
                icon: 'info',
                title: n.title,
                text: n.message,
                showConfirmButton: false,
                timer: 4000,
                timerProgressBar: true
            });
        }
        
        // Play sound
        const audio = new Audio('/employee/sounds/ting.mp3'); // Optional sound
        audio.play().catch(e => console.log('Audio play blocked', e));
    }
}
