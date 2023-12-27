var socket = new SockJS("/ws");
var stompClient = Stomp.over(socket);

$(function() {

    $("#sendChatForm > textarea").on('keydown', function(e) {
        if (e.keyCode == 13 && !e.shiftKey) {
            e.preventDefault();
            $("#sendChatForm").submit();
        }
    });

    $(".difDate").each(function() {
        $(this).before("<div class='card date'>" + $(this).data("date") + "</div>");
    });

    $("#msgContent").focus();
    var chatBoard = document.getElementById("chatBoard");
    chatBoard.scrollTop = chatBoard.scrollHeight;

    $("#chatImageBtn").on('click', function() {
        $("#chatImageInput").click();
    });

    $("#chatImageInput").on('change', function() {
        var files = this.files;
        console.log(files);
        var preview = $("#imagePreview");

        if (files.length > 5) {
            alert("이미지는 최대 5개만 선택 가능합니다.");
            $(this).val("");
            preview.empty();
            $("#imagePreviewForm").hide();
            return;
        }

        for (var i = 0; i < files.length; i++) {
            if(!files[i].type.startsWith('image/')) {
                alert("이미지 파일만 선택해주세요.");
                $(this).val("");
                preview.empty();
                $("#imagePreviewForm").hide();
                return;
            }
        }

        preview.empty();

        $.each(files, function(index, file){
            var reader = new FileReader();

            reader.onload = function(e) {
                var img = $("<img>").attr("src", e.target.result).css({width: '100px', height: '100px'});
                preview.append(img);
            };

            reader.readAsDataURL(file);
        });

        if (files.length > 0) $("#imagePreviewForm").show();
        else $("#imagePreviewForm").hide();
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
                var imageElement = ""
                if (data.images != null) {
                    for (var i = 0; i < data.images.length; i++) {
                        imageElement +=
                        '<a href="' + data.images[i] + '">'+
                            '<img src="' + data.images[i] + '" alt="" style="width:100px; height:100px;">'+
                        '</a>';
                    }
                }

                if (data.writer == user) {
                    element =
                    '<div class="chatBox text-end">'+
                        '<div>'+
                            '<div>'+
                                '<small> '+ date +' </small>'+
                                '<div class="card ourColor">'+
                                    '<span class="confirm"></span>'+
                                    imageElement+
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
                                    imageElement+
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

        var files = $("#chatImageInput")[0].files;
        var chatContent = {
            content: $("#msgContent").val(),
            writer: user,
            code: roomCode,
            target: target
        };

        if (files.length > 0) {
            var formData = new FormData();

            for (var i = 0; i < files.length; i++) {
                formData.append('files', files[i]);
            }

            formData.append('chatContainer', JSON.stringify(chatContent));

            $.ajax({
                url: "/chat/uploadImage",
                type: "post",
                data: formData,
                headers: {
                    [csrfHeader]: csrfToken
                },
                processData: false,
                contentType: false,
                success: function(result) {
                    $("#imagePreview").empty();
                    $("#imagePreviewForm").hide();
                    $("#chatImageInput").val("");
                },
                error: function(err) {
                    alert("사진 업로드 실패");
                    console.log(err);
                }
            });
        } else {
            if($("#msgContent").val().trim() != "") {
                stompClient.send("/app/send", {}, JSON.stringify(chatContent));
            }
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

    $("#voiceChatBtn").on('click', function() {
        navigator.mediaDevices.getUserMedia({audio: true, video: false})
        .then(stream => {
            alert("스트림 가져오기 성공");
        })
        .catch(error => alert("스트림 가져오기 실패\n" + error));
    });

    $(".chatWriterBtn").on('click', function() {
        $("#profileModal").modal("show");
    });
});