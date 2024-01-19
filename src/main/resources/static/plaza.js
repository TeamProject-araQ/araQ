$(function () {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;
    let keydown = false;
    let interval = null;
    let timer = new Map();
    const mobile = /Mobi|Android|iPhone/.test(navigator.userAgent);

    init();

    stompClient.connect({}, function (frame) {

        stompClient.send("/app/plaza/join/" + code, {}, user);

        stompClient.subscribe("/topic/plaza/join/" + code, function (message) {
            const data = JSON.parse(message.body);
            if ($("." + data.username).length === 0) {
                createAvatar(data);
                createParticipantList(data);
            }
        });

        stompClient.subscribe("/topic/plaza/exit/" + code, function (message) {
            $("." + message.body).remove();
            $(".p_" + message.body).remove();
        });

        stompClient.subscribe("/topic/plaza/message/" + code, function (message) {
            showMessage(JSON.parse(message.body));
        });

        stompClient.subscribe("/topic/plaza/location/" + code, function (message) {
            moveLocation(JSON.parse(message.body));
        });

        stompClient.subscribe("/topic/plaza/focus/" + code, function (message) {
            const data = JSON.parse(message.body);
            const element = $("." + data.sender);
            if (data.status === "blur") {
                element.removeClass("statusFocus");
                element.addClass("statusBlur");
                element.find(".status").removeClass("focus");
                element.find(".status").addClass("blur");
            } else {
                element.removeClass("statusBlur");
                element.addClass("statusFocus");
                element.find(".status").removeClass("blur");
                element.find(".status").addClass("focus");
            }
        });

        stompClient.subscribe("/topic/plaza/notice/" + code, function (message) {
            $("#plazaChatBoard").append($.parseHTML("<p class='noticeMessage'>" + message.body + "</p>"));

            const chatBoard = document.getElementById("plazaChatBoard");
            chatBoard.scrollTop = chatBoard.scrollHeight;
        });

        stompClient.subscribe("/topic/plaza/fire/" + code + "/" + user, function () {
            window.location.href = "/plaza/list";
        });

        stompClient.subscribe("/topic/plaza/delegate/" + code + "/" + user, function () {
            alert("방장을 위임받으셨습니다.");
            window.location.reload();
        });

        stompClient.subscribe("/topic/plaza/modify/" + code, function (message) {
            window.location.reload();
        });
    });

    $(window).on("beforeunload", function () {
        stompClient.send("/app/plaza/exit/" + code, {}, user);
    });

    $("#plazaChatForm").submit(function (e) {
        e.preventDefault();
        const text = $("#plazaChatParam").val();

        if (!mobile) {
            $("#plazaChatBoard").css("height", "80px");
            const chatBoard = document.getElementById("plazaChatBoard");
            chatBoard.scrollTop = chatBoard.scrollHeight;

            $("#plazaChatParam").hide();
        }

        if (text !== "") {
            const message = {
                content: text,
                sender: user,
                nick: nick
            };

            stompClient.send("/app/plaza/message/" + code, {}, JSON.stringify(message));
        }

        $("#plazaChatParam").val("");
        $("#plazaChatParam").blur();
    });

    if (mobile) $("#plazaChatParam").show();

    $("#plazaBoard").click(function (e) {
        if (!($(e.target).is("#plazaBoard") || $(e.target).is("#plazaChatBoard") ||
            $(e.target).is(".noticeMessage"))) return;

        const offset = $(this).offset();
        const clickX = e.pageX - offset.left;
        const clickY = e.pageY - offset.top;

        sendLocationOnClick(clickX - 100, clickY - 25);
    });

    if (!mobile) {
        $(document).keydown(function (e) {
            $(window).focus();
            if (keydown === false) {
                keydown = true;
                changeLocation(e);
                interval = setInterval(function () {
                    changeLocation(e);
                }, 1000);
            }
        });

        $(document).keyup(function (e) {
            keydown = false;
            clearInterval(interval);

            correctLocation();

            if (["ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight"].includes(e.key)) {
                sendLocation();
            }
            $("." + user).css("transition", "1s linear");
        });

        $(document).keypress(function (e) {
            if (e.key === "Enter") {
                $("#plazaChatBoard").css("height", "200px");
                $("#plazaChatParam").show();
                $("#plazaChatParam").focus();
            }
        });
    }

    $(window).blur(function () {
        stompClient.send("/app/plaza/focus/" + code, {}, JSON.stringify({
            sender: user,
            status: "blur"
        }));
    });

    $(window).focus(function () {
        stompClient.send("/app/plaza/focus/" + code, {}, JSON.stringify({
            sender: user,
            status: "focus"
        }));
    });

    $("#plazaManageBtn").click(function () {
        $("#plazaManageModal").modal("show");
    });

    $("#participant-tab-pane").on("click", ".fire", function () {
        if (confirm($(this).data("nick") + "님을 강퇴하시겠습니까?")) {
            stompClient.send("/app/plaza/fire/" + code, {}, $(this).data("value"));
        }
    });

    $("#participant-tab-pane").on("click", ".delegate", function () {
        if (confirm($(this).data("nick") + "님에게 방장을 위임하시겠습니까?")) {
            $.ajax({
                url: "/plaza/delegate",
                type: "post",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: "application/json",
                data: JSON.stringify({
                    code: code,
                    target: $(this).data("value")
                }),
                success: function (data) {
                    if (data === "deny") alert("현재 해당 방을 변경할 수 없습니다.");
                    else window.location.reload();
                },
                error: function (error) {
                    alert("실패");
                    console.log(err.responseJSON.message);
                }
            });
        }
    });

    $("#peopleRange").on('change', function () {
        $(".peopleRangeNumber").text($(this).val());
    });

    $("#plazaPublic").on('change', function () {
        if (this.checked) {
            const passwordElement = $("#plazaPassword");
            passwordElement.prop("disabled", true);
        }
    });

    $("#plazaPrivate").on('change', function () {
        if (this.checked) {
            const passwordElement = $("#plazaPassword");
            passwordElement.prop("disabled", false);
            passwordElement.focus();
        }
    });

    $("#plazaModifyBtn").click(function () {
        const title = $("#plazaTitle").val();
        const people = $("#peopleRange").val();
        const password = ($("#plazaPrivate").is(":checked")) ? $("#plazaPassword").val() : "";
        let formData = new FormData();

        if ($(".selectedImg").hasClass("customImage")) {
            formData.append("customImg", $("#customImageInput")[0].files[0]);
        } else {
            formData.append("img", $(".selectedImg > *").attr("src"));
        }

        formData.append("title", title);
        formData.append("people", people);
        formData.append("password", password);
        formData.append("code", code);

        if ($("#plazaPrivate").is(":checked") && password === "")
            alert("비밀번호를 입력해주세요");
        else if (title.trim() === "") alert("광장 이름을 입력해주세요");
        else {
            $.ajax({
                url: "/plaza/modify",
                type: "post",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: false,
                processData: false,
                data: formData,
                success: function (data) {
                    if (data === "deny") alert("현재 해당 방을 변경할 수 없습니다.");
                    else alert("변경이 완료되었습니다.");
                },
                error: function (err) {
                    alert("변경 실패");
                }
            });
        }
    });

    $("#bgImgList > a").click(function () {
        $("#bgImgList > a").removeClass("selectedImg");
        $(this).addClass("selectedImg");
    });

    $(".customImage").click(function () {
        $("#customImageInput").click();
    });

    $("#customImageInput").change(function () {
        const preview = $(".customImage");
        const file = this.files[0];

        if ($(this).val() === "") {
            preview.empty();
            preview.text("커스텀이미지");
            return;
        }

        if (!file.type.startsWith("image/")) {
            alert("이미지 파일만 선택해주세요.");
            $(this).val("");
            preview.empty();
            preview.text("커스텀이미지");
            return;
        } else if (file.size > 10485760) {
            alert("10MB 이하의 이미지만 사용해주세요.");
            $(this).val("");
            preview.empty();
            preview.text("커스텀이미지");
            return;
        }

        preview.empty();

        const reader = new FileReader();
        reader.onload = function (e) {
            const img = $("<img>").attr("src", e.target.result).css({width: '100%', height: '100%'});
            preview.append(img);
        };

        reader.readAsDataURL(file);
    });

    $("#plazaReportBtn").click(function () {
        $("#reportedUsername").val(manager);
        $("#reportedRoomCode").val(code);
        $("#reportedLocation").val("plaza");
        $("#report").modal("show");
    });

    function changeLocation(e) {
        const element = $("." + user);
        const left = parseInt(element.css("left"));
        const top = parseInt(element.css("top"));

        switch (e.key) {
            case "ArrowUp":
                element.css("top", top - 100);
                break;
            case "ArrowDown":
                element.css("top", top + 100);
                break;
            case "ArrowLeft":
                element.css("left", left - 100);
                break;
            case "ArrowRight":
                element.css("left", left + 100);
                break;
        }
    }

    function sendLocation() {
        const element = $("." + user);
        const left = parseInt(element.css("left"));
        const top = parseInt(element.css("top"));
        const parentWidth = parseInt(element.parent().css("width"));
        const parentHeight = parseInt(element.parent().css("height"));
        const data = {
            sender: user,
            left: left / parentWidth * 100,
            top: top / parentHeight * 100
        };

        stompClient.send("/app/plaza/location/" + code, {}, JSON.stringify(data));
    }

    function sendLocationOnClick(x, y) {
        const element = $("." + user);
        const parentWidth = parseInt(element.parent().css("width"));
        const parentHeight = parseInt(element.parent().css("height"));
        const data = {
            sender: user,
            left: x / parentWidth * 100,
            top: y / parentHeight * 100
        };

        stompClient.send("/app/plaza/location/" + code, {}, JSON.stringify(data));
    }

    function showMessage(data) {
        const element = $("." + data.sender + " > .talkBox");
        const boardMessage = $("<p class='normalMessage'></p>");
        boardMessage.text(data.nick + ": " + data.content);

        element.text(data.content);
        element.show();

        if (timer.has(data.sender)) clearTimeout(timer.get(data.sender));
        timer.set(data.sender, setTimeout(function () {
            element.hide();
        }, 3000));

        $("#plazaChatBoard").append(boardMessage);

        const chatBoard = document.getElementById("plazaChatBoard");
        chatBoard.scrollTop = chatBoard.scrollHeight;
    }
});


function createAvatar(data) {
    const element = $(
        "<div class='avatar " + data.username + "'>" +
        "<div class='talkBox card' style='background: " + data.background + "; color: " + data.color + "'></div>" +
        "<img class='userImage' src='" + data.image + "' alt=''>" +
        "<div class='nickname'>" + data.nickname + "</div>" +
        "<div class='status focus'></div>" +
        "</div>"
    );
    $("#plazaBoard").append(element);
}

function moveLocation(data) {
    const element = $("." + data.sender);
    const parentWidth = parseInt(element.parent().css("width"));
    const parentHeight = parseInt(element.parent().css("height"));
    const top = parseInt(data.top) * parentHeight / 100 + 5;
    const left = parseInt(data.left) * parentWidth / 100 + 5;
    element.css({
        top: top,
        left: left
    });
}

function init() {
    $(".avatar").each(function (index, element) {
        const parentWidth = parseInt($(element).parent().css("width"));
        const parentHeight = parseInt($(element).parent().css("height"));
        const top = parseInt($(element).find("input:first").val()) * parentHeight / 100 + 5;
        const left = parseInt($(element).find("input:last").val()) * parentWidth / 100 + 5;
        $(element).css({
            top: top,
            left: left
        });
    });

    $("#plazaBoard").css("background-image", `url('${background}')`);
}

function correctLocation() {
    const element = $("." + user);
    const parentWidth = parseInt(element.parent().css("width"));
    const parentHeight = parseInt(element.parent().css("height"));
    const left = parseInt(element.css("left"));
    const top = parseInt(element.css("top"));

    if (left < -75) {
        element.css("transition", "none");
        element.css("left", -75);
    } else if (left > parentWidth - 125) {
        element.css("transition", "none");
        element.css("left", parentWidth - 125);
    }
    if (top < 0) {
        element.css("transition", "none");
        element.css("top", 0);
    } else if (top > parentHeight - 50) {
        element.css("transition", "none");
        element.css("top", parentHeight - 50);
    }
}

function createParticipantList(data) {
    const element =
        "<div class='dropdown dropend p_" + data.username + "'>" +
        "<a href='#' data-bs-toggle='dropdown' aria-expanded='false'>" + data.nickname + "</a>" +
        "<ul class='dropdown-menu'>" +
        "<li><a class='delegate dropdown-item' href='javascript:void(0)' " +
        "data-nick='" + data.nickname + "' " +
        "data-value='" + data.username + "'>방장위임</a></li>" +
        "<li>" +
        "<li><a class='fire dropdown-item text-danger' href='javascript:void(0)' " +
        "data-nick='" + data.nickname + "' " +
        "data-value='" + data.username + "'>강제퇴장</a></li>" +
        "</ul>" +
        "</div>";

    $("#participant-tab-pane > .card").append(element);
}
