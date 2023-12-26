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

    $(".difDate").each(function() {
        $(this).before("<div class='card date'>" + $(this).data("date") + "</div>");
    });

    stompClient.connect({}, function(frame) {
        $.ajax({
            url: "/chat/confirm",
            type: "post",
            contentType: "application/json",
            headers: {
                [csrfHeader]: csrfToken
            },
            data: JSON.stringify({
                target: target,
                content: roomCode
            }),
            error: function(err) {
                console.log(err);
            }
        });

        stompClient.send("/app/online", {}, JSON.stringify({
            type: "online",
            target: $("#hiddenUserName").val()
        }));

        stompClient.subscribe("/topic/chat/" + roomCode, function(message) {
            var data = JSON.parse(message.body);
            if (data.code == "confirm") {
                if (data.target == user) $(".confirm").remove();
            }
            else {
                var element = "";
                var createDate = new Date(data.createDate);
                if (data.difDate) {
                    // 날짜
                }
                var date = createDate.getHours().toString().padStart(2, '0')+":"+createDate.getMinutes().toString().padStart(2, '0');

                if (data.writer == user) {
                    element =
                    '<div class="chatBox text-end">'+
                        '<div>'+
                            '<div>'+
                                '<small> '+ date +' </small>'+
                                '<div class="card ourColor">'+
                                    '<span class="confirm"></span>'+
                                    '<p class="card-body text-start">' + data.content + '</p>'+
                                '</div>'+
                            '</div>'+
                        '</div>'+
                    '</div>';
                } else {
                    element =
                    '<div class="chatBox">'+
                        '<img src="' + data.writerImage + '" alt="">'+
                        '<div>'+
                            '<p>' + data.writerNick + '</p>'+
                            '<div>'+
                                '<div class="card">'+
                                    '<p class="card-body text-start">' + data.content + '</p>'+
                                '</div>'+
                                '<small> ' + date + ' </small>'+
                            '</div>'+
                        '</div>'+
                    '</div>';
                }

                $("#chatBoard").append(element);
                chatBoard.scrollTop = chatBoard.scrollHeight;

                $.ajax({
                    url: "/chat/confirm",
                    type: "post",
                    contentType: "application/json",
                    headers: {
                        [csrfHeader]: csrfToken
                    },
                    data: JSON.stringify({
                        target: target,
                        content: roomCode
                    }),
                    error: function(err) {
                        console.log(err);
                    }
                });
            }
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