$(function () {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    let keydown = false;
    let interval = null;

    stompClient.connect({}, function (frame) {

        stompClient.send("/app/plaza/join", {}, user);

        stompClient.subscribe("/topic/plaza/join", function (message) {
            createAvatar(JSON.parse(message.body));
        });

        stompClient.subscribe("/topic/plaza/exit", function (message) {
            $("." + message.body).remove();
        });

        stompClient.subscribe("/topic/plaza/message", function (message) {
            showMessage(JSON.parse(message.body));
        });

        stompClient.subscribe("/topic/plaza/location", function (message) {
            moveLocation(JSON.parse(message.body));
        });
    });

    $(window).on("beforeunload", function () {
        stompClient.send("/app/plaza/exit", {}, user);
    });

    $("#plazaChatForm").submit(function (e) {
        e.preventDefault();
        const text = $("#plazaChatParam").val();

        if (text !== "") {
            const message = {
                content: text,
                sender: user
            };

            stompClient.send("/app/plaza/message", {}, JSON.stringify(message));
        }

        $("#plazaChatParam").val("");
        $("#plazaChatParam").blur();
    });

    $(document).keydown(function (e) {
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

        if (["ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight"].includes(e.key)) {
            sendLocation();
        }
    });

    $(document).keypress(function (e) {
        if (e.key === "Enter")
            $("#plazaChatParam").focus();
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

        stompClient.send("/app/plaza/location", {}, JSON.stringify(data));
    }
});

function showMessage(data) {
    const element = $("." + data.sender + " > .talkBox");

    element.text(data.content);
    element.show();

    const timer = setTimeout(function () {
        element.hide();
    }, 3000);
}

function createAvatar(data) {
    const element =
        "<div class='avatar " + data.username + "'>" +
        "<div class='talkBox card'></div>" +
        "<img class='userImage' src='" + data.image + "'>" +
        "<div class='nickname'>" + data.nickname + "</div>" +
        "</div>";

    $("#plazaBoard").append(element);
}

function moveLocation(data) {
    const element = $("." + data.sender);
    element.css({
        top: data.top,
        left: data.left
    });
}