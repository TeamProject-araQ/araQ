$(function () {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;


    $('#rateBtn').on('click', function () {
        $('#rateModal').modal('show');
    });

    $('#mannerRate input').on('change', function () {
        $('#mannerValue').val($(this).val() / 2);
    });

    $('#appearanceRate input').on('change', function () {
        $('#appearanceValue').val($(this).val() / 2);
    });

    $('#appealRate input').on('change', function () {
        $('#appealValue').val($(this).val() / 2);
    });

    $('#saveRate').on('click', function () {
        if ($('#appealValue').val() === "" || $('#mannerValue').val() === "" || $('#appearanceValue').val() === "")
            alert("모든 평가를 완료해주세요.");
        else {
            if (confirm("평가가 완료되면 재평가가 불가능합니다. 평가를 저장하시겠습니까?")) {
                $.ajax({
                    url: "/rate/save",
                    type: "POST",
                    headers: {
                        [csrfHeader]: csrfToken
                    },
                    contentType: "application/json",
                    data: JSON.stringify({
                        target: target,
                        room: roomCode,
                        manner: $('#mannerValue').val(),
                        appeal: $('#appealValue').val(),
                        appearance: $('#appearanceValue').val()
                    }),
                    success: function (data) {
                        alert(data);
                        $('#rateModal').modal('hide');
                        window.location.reload();
                    },
                    error: function (data) {
                        console.log(data);
                    }
                });
            }
        }
    });

    $("#sendChatForm > textarea").on('keydown', function (e) {
        if (e.keyCode === 13 && !e.shiftKey) {
            e.preventDefault();
            $('#emojiCollapse').collapse('hide');
            $("#sendChatForm").submit();
        }
    });

    $(".difDate").each(function () {
        $(this).before("<div class='card date'>" + $(this).data("date") + "</div>");
    });

    $("#msgContent").focus();
    var chatBoard = document.getElementById("chatBoard");
    chatBoard.scrollTop = chatBoard.scrollHeight;

    $("#chatImageBtn").on('click', function () {
        $("#chatImageInput").click();
    });

    $("#chatImageInput").on('change', function () {
        var files = this.files;
        var preview = $("#imagePreview");

        if (files.length > 5) {
            alert("이미지는 최대 5개만 선택 가능합니다.");
            $(this).val("");
            preview.empty();
            $("#imagePreviewForm").hide();
            return;
        }

        for (var i = 0; i < files.length; i++) {
            if (!files[i].type.startsWith('image/')) {
                alert("이미지 파일만 선택해주세요.");
                $(this).val("");
                preview.empty();
                $("#imagePreviewForm").hide();
                return;
            }
        }

        preview.empty();

        $.each(files, function (index, file) {
            var reader = new FileReader();

            reader.onload = function (e) {
                var img = $("<img>").attr("src", e.target.result).css({width: '100px', height: '100px'});
                preview.append(img);
            };

            reader.readAsDataURL(file);
        });

        if (files.length > 0) $("#imagePreviewForm").show();
        else $("#imagePreviewForm").hide();
    });

    stompClient.connect({}, function (frame) {
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
            error: function (err) {
                console.log(err);
            }
        });

        stompClient.subscribe("/topic/chat/" + roomCode, function (message) {
            const data = JSON.parse(message.body);
            if (data.code === "confirm") {
                if (data.target === user) $(".confirm").remove();
            } else {
                var element = "";
                var createDate = new Date(data.createDate);
                if (data.difDate) {
                    // 날짜
                }
                var date = createDate.getHours().toString().padStart(2, '0') + ":" + createDate.getMinutes().toString().padStart(2, '0');
                var imageElement = ""
                if (data.images != null) {
                    imageElement += '<div class="chatImage">';
                    for (var i = 0; i < data.images.length; i++) {
                        imageElement +=
                            '<a href="' + data.images[i] + '">' +
                            '<img src="' + data.images[i] + '" alt="" style="width:100px; height:100px;">' +
                            '</a>';
                    }
                    imageElement += '</div>';
                }

                if (data.writer === user) {
                    element = $(
                        '<div class="chatBox text-end">' +
                        '<div>' +
                        '<div>' +
                        '<small> ' + date + ' </small>' +
                        '<div class="card ourColor">' +
                        '<span class="confirm"></span>' +
                        imageElement +
                        '<p class="card-body text-start"></p>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                } else {
                    element = $(
                        '<div class="chatBox">' +
                        '<img src="' + data.writerImage + '" alt="">' +
                        '<div>' +
                        '<a href="javascript:void(0)" class="chatWriterBtn">' + data.writerNick + '</a>' +
                        '<div>' +
                        '<div class="card">' +
                        imageElement +
                        '<p class="card-body text-start"></p>' +
                        '</div>' +
                        '<small> ' + date + ' </small>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                }

                element.find("p").text(data.content);

                $("#chatBoard").append(element);
                chatBoard.scrollTop = chatBoard.scrollHeight;

                if (!document.hidden) {
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
                        error: function (err) {
                            console.log(err);
                        }
                    });
                }
            }
        });
    });

    $("#sendChatForm").submit(function (event) {
        event.preventDefault();
        $('#emojiCollapse').collapse('hide');

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
                success: function (result) {
                    $("#imagePreview").empty();
                    $("#imagePreviewForm").hide();
                    $("#chatImageInput").val("");
                },
                error: function (err) {
                    alert("사진 업로드 실패");
                    console.log(err);
                }
            });
        } else {
            if ($("#msgContent").val().trim() !== "") {
                stompClient.send("/app/send", {}, JSON.stringify(chatContent));
            }
        }

        $("#msgContent").val("");
        $("#msgContent").focus();
    });

    $("#voiceChatBtn").on('click', function () {
        if (confirm(targetNick + "님에게 보이스 채팅을 요청하시겠습니까?")) {
            stompClient.send("/app/all/" + target, {}, JSON.stringify({
                type: "requestVoice",
                sender: user,
                senderNick: nickName
            }));
        }
    });

    $(".chatBox").on("click", ".chatWriterBtn", function () {
        showProfile(target);
    });

    $(window).on("focus", function () {
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
            error: function (err) {
                console.log(err);
            }
        });
    });

    $("#chatReportBtn").on('click', function () {
        $("#reportedUsername").val(target);
        $("#reportedRoomCode").val(roomCode);
        $("#reportedLocation").val("chat");
        $("#report").modal("show");
    });

    $('#emojiModal').modal({
        backdrop: false
    });

    $.ajax({
        url: '/emoji.json',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            var modalBody = $('#emoji');
            Object.keys(data).forEach(function(key) {
                var emoji = key;
                var span = $('<span>').text(emoji);
                modalBody.append(span);
            });
        },
        error: function(error) {
            console.error('Error:', error);
        }
    });

    $(document).on('click', '#emojiCollapse #emoji span', function() {
        var emoji = $(this).text();
        var chatInput = $('#msgContent');
        chatInput.val(chatInput.val() + emoji);
        $("#msgContent").focus();
    });
});