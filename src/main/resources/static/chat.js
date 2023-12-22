var socket = new SockJS("/ws");
var stompClient = Stomp.over(socket);

$(function() {
    $("#msgContent").focus();
    var chatBoard = document.getElementById("chatBoard");
    chatBoard.scrollTop = chatBoard.scrollHeight;

    $("#sendChatForm > textarea").on('keydown', function(e) {
        if (e.keyCode == 13 && !e.shiftKey) {
            e.preventDefault();
            $("#sendChatForm").submit();
        }
    });

    stompClient.connect({}, function(frame) {

        stompClient.send("/app/online", {}, JSON.stringify({
            type: "online",
            target: $("#hiddenUserName").val()
        }));

        stompClient.subscribe("/topic/chat/" + roomCode, function(message) {
            var data = JSON.parse(message.body);
            var element = "";

            if (data.writer == user) {
                element =
                '<div class="chatBox text-end">' +
                    '<div>' +
                        '<div class="card ourColor">' +
                            '<p class="card-body text-start">' + data.content + '</p>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            } else {
                element =
                '<div class="chatBox">' +
                    '<img src="' + data.writerImage + '" alt="">' +
                    '<div>' +
                        '<p>' + data.writerNick + '</p>' +
                        '<div class="card">' +
                            '<p class="card-body">' + data.content + '</p>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            }

            $("#chatBoard").append(element);
            chatBoard.scrollTop = chatBoard.scrollHeight;
        });
    });

    $("#sendChatForm").submit(function(event) {
        event.preventDefault();

        if($("#msgContent").val().trim() != "") {
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