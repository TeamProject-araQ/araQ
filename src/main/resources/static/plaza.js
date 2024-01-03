$(function () {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {

        stompClient.send("/app/plaza/join", {}, user);

        stompClient.subscribe("/topic/plaza/join", function (message) {
            const data = JSON.parse(message.body);
            createAvatar(data);
        });

    });
});

function createAvatar(data) {
    const element =
        "<div class='avatar " + data.username + "'>" +
            "<img src='"+ data.image +"'>" +
            "<div class='card'>" + data.nickname + "</div>" +
        "</div>";

    $("#plazaBoard").prepend(element);
}