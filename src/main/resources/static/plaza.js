$(function () {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;
    let keydown = false;
    let interval = null;
    let timer = new Map();

    init();

    stompClient.connect({}, function (frame) {

        stompClient.send("/app/plaza/join/" + code, {}, user);

        stompClient.subscribe("/topic/plaza/join/" + code, function (message) {
            const data = JSON.parse(message.body);
            if (user !== data.username) {
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
            $("#plazaChatBoard").append("<p class='noticeMessage'>" + message.body + "</p>");

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
            $("#plazaBoard").css("background-image", `url('${message.body}')`);
        });
    });

    $(window).on("beforeunload", function () {
        stompClient.send("/app/plaza/exit/" + code, {}, user);
    });

    $("#plazaChatForm").submit(function (e) {
        e.preventDefault();
        const text = $("#plazaChatParam").val();

        $("#plazaChatBoard").css("height", "80px");
        const chatBoard = document.getElementById("plazaChatBoard");
        chatBoard.scrollTop = chatBoard.scrollHeight;

        $("#plazaChatParam").hide();

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
            stompClient.send("/app/plaza/delegate/" + code, {}, $(this).data("value"));
            window.location.reload();
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
        const bgImg = $(".selectedImg > *").attr("src");
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
                contentType: "application/json",
                data: JSON.stringify({
                    title: title,
                    people: people,
                    password: password,
                    code: code,
                    img: bgImg
                }),
                success: function () {
                    alert("변경이 완료되었습니다.");
                },
                error: function (err) {
                    alert("변경 실패\n사유 : " + err);
                }
            });
        }
    });

    $("#bgImgList > a").click(function () {
        $("#bgImgList > a").removeClass("selectedImg");
        $(this).addClass("selectedImg");
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
        const data = {
            sender: user,
            left: element.css("left"),
            top: element.css("top")
        };

        stompClient.send("/app/plaza/location/" + code, {}, JSON.stringify(data));
    }

    function showMessage(data) {
        const element = $("." + data.sender + " > .talkBox");

        element.text(data.content);
        element.show();

        if (timer.has(data.sender)) clearTimeout(timer.get(data.sender));
        timer.set(data.sender, setTimeout(function () {
            element.hide();
        }, 3000));

        $("#plazaChatBoard").append("<p class='normalMessage'>" + data.nick + ": " + data.content + "</p>");

        const chatBoard = document.getElementById("plazaChatBoard");
        chatBoard.scrollTop = chatBoard.scrollHeight;
    }
});


function createAvatar(data) {
    const element =
        "<div class='avatar " + data.username + "'>" +
        "<div class='talkBox card'></div>" +
        "<img class='userImage' src='" + data.image + "'>" +
        "<div class='nickname'>" + data.nickname + "</div>" +
        "<div class='status focus'></div>" +
        "</div>";

    $("#plazaBoard").append(element);
    $(".njk7740 > .talkBox").addClass("talkBoxBlack");
}

function moveLocation(data) {
    const element = $("." + data.sender);
    element.css({
        top: data.top,
        left: data.left
    });
}

function init() {
    $(".avatar").each(function (index, element) {
        $(element).css({
            top: $(element).find("input:first").val(),
            left: $(element).find("input:last").val()
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