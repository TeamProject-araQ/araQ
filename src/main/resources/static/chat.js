var socket = new SockJS("/ws");
var stompClient = Stomp.over(socket);

$(function() {
    $("#msgContent").focus();
    var chatBoard = document.getElementById("chatBoard");
    chatBoard.scrollTop = chatBoard.scrollHeight;

    stompClient.connect({}, function(frame) {

        stompClient.send("/app/online", {}, JSON.stringify({
            type: "online",
            target: $("#hiddenUserName").val()
        }));

        stompClient.subscribe("/topic/chat/" + roomCode, function(message) {
            var m = JSON.parse(message.body);

            $.ajax({
                url: "/user/getInfo",
                type: "post",
                contentType: "text/plain",
                dataType: "json",
                data: m.writer,
                headers: {
                    [csrfHeader]: csrfToken
                },
                success: function(data) {
                    var element = "";

                    if (data.username == user) {
                        element =
                        '<div class="chatBox text-end">' +
                            '<div>' +
                                '<div class="card ourColor">' +
                                    '<p class="card-body text-start">' + m.content + '</p>' +
                                '</div>' +
                            '</div>' +
                        '</div>';
                    } else {
                        element =
                        '<div class="chatBox">' +
                            '<img src="' + data.image + '" alt="">' +
                            '<div>' +
                                '<p>' + data.nickName + '</p>' +
                                '<div class="card">' +
                                    '<p class="card-body">' + m.content + '</p>' +
                                '</div>' +
                            '</div>' +
                        '</div>';
                    }

                    $("#chatBoard").append(element);
                    chatBoard.scrollTop = chatBoard.scrollHeight;
                },
                error: function(err) {
                    alert("요청이 실패하였습니다.");
                }
            });
        });
    });

    $("#sendChatForm").submit(function(event) {
        event.preventDefault();

        if($("#msgContent").val() != "") {
            stompClient.send("/app/send", {}, JSON.stringify({
                content: $("#msgContent").val(),
                writer: user,
                code: roomCode,
                target: target
            }));
        }

        $("#msgContent").val("");
        $("#msgContent").focus();
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