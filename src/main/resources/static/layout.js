$(function() {

    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    if($("#hiddenUserName").val() != null && !window.location.pathname.includes("/chat/join")) {
        var socket = new SockJS("/ws");
        var stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {

            stompClient.send("/app/online", {}, JSON.stringify({
                type: "online",
                target: $("#hiddenUserName").val()
            }));

            stompClient.subscribe("/topic/all/" + $("#hiddenUserName").val(), function(message) {
                var data = JSON.parse(message.body);
                var chatToast = new bootstrap.Toast($('#chatToast'), {
                    autohide: false
                });

                if (data.type == "chatRequest") {
                    $("#chatRequestModal .modal-title").text(data.content);
                    $("#chatRequestModal .nickName").text(data.nickname);
                    $("#chatRequestModal .userAge").text(data.age);
                    $("#chatRequestModal .introduce").text(data.introduce);
                    $("#chatRequestModal .profileImage").attr("src", data.image);
                    $("#chatRequestModal").modal("show");
                }

                else if (data.type == "refuse")
                    alert(data.content);

                else if (data.type == "acceptChat") {
                    alert(data.nickname + "님이 수락했습니다. 채팅방으로 이동합니다.");
                    window.location.href = "/chat/join/" + data.content;
                }

                else if (data.type == "sendChat") {
                    $("#chatToast .profile").attr("src", data.image);
                    $("#chatToast .nickName").text(data.nickname);
                    $("#chatToast .toast-body > a").text(data.content);
                    $("#chatToast .toast-body > a").attr("href", "/chat/join/" + data.target);
                    chatToast.show();
                }
            });
        });
    }

    $("#chatRequestModal .refuse").on('click', function() {
        stompClient.send("/app/alert", {}, JSON.stringify({
            type: "refuse",
            target: $("#chatRequestModal .nickName").text()
        }));
    });

    $("#chatRequestModal .accept").on('click', function() {

        $.ajax({
            url: "/chat/create",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType:"text/plain",
            data: $("#chatRequestModal .nickName").text(),
            success: function(data) {
                window.location.href = "/chat/join/" + data;
            },
            error: function(err) {
                alert("채팅방 생성에 실패했습니다.");
            }
        });
    });

    $(window).on('beforeunload', function() {
        $.ajax({
            url: "/offline",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType:"text/plain",
            data: $("#hiddenUserName").val()
        });
    });
});