$(function () {
    var socket = new SockJS("/ws");
    var stompClient = Stomp.over(socket);
    stompClient.debug = null;
    var userStream = null;
    var pc = null;
    var iceServers = {
        iceServers: [
            {
                urls: "stun:stun.l.google.com:19302"
            },
        ],
    };
    var iceQueue = [];
    var targetPeer = null;
    var rtcConnect = false;

    stompClient.connect({}, function (frame) {

        stompClient.send("/app/online", {}, JSON.stringify({
            type: "online",
            target: $("#hiddenUserName").val()
        }));

        stompClient.subscribe("/topic/all/" + $("#hiddenUserName").val(), function (message) {
            var data = JSON.parse(message.body);
            var chatToast = new bootstrap.Toast($('#chatToast'), {
                autohide: false
            });

            if (data.type === "chatRequest") {
                $("#requestUsername").val(data.username);
                $("#chatRequestModal .modal-title").text(data.content);
                $("#chatRequestModal .nickName").text(data.nickname);
                $("#chatRequestModal .userAge").text(data.age);
                $("#chatRequestModal .introduce").text(data.introduce);
                $("#chatRequestModal .profileImage").attr("src", data.image);
                $("#chatRequestModal").modal("show");
            } else if (data.type === "refuse")
                alert(data.content);

            else if (data.type === "acceptChat") {
                alert(data.nickname + "님이 수락했습니다. 채팅방으로 이동합니다.");
                window.location.href = "/chat/join/" + data.content;
            } else if (data.type === "sendChat" && !window.location.pathname.includes(data.target)) {
                $("#chatToast .profile").attr("src", data.image);
                $("#chatToast .nickName").text(data.nickname);
                $("#chatToast .toast-body > a").text(data.content);
                $("#chatToast .toast-body > a").attr("href", "/chat/join/" + data.target);
                chatToast.show();
            } else if (data.type === "requestVoice") {
                if (confirm(data.senderNick + "님이 보이스 채팅을 요청했습니다. 수락하시겠습니까?")) {
                    stompClient.send("/app/all/" + data.sender, {}, JSON.stringify({
                        type: "acceptVoiceRequest",
                        sender: $("#hiddenUserName").val()
                    }));

                    targetPeer = data.sender;

                    pc = new RTCPeerConnection(iceServers);

                    pc.addEventListener('icecandidate', function (event) {
                        if (event.candidate && rtcConnect) {
                            console.log(event.candidate)
                            stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(event.candidate));
                            if (iceQueue.length > 0) {
                                for (var i = 0; i < iceQueue.length; i++) {
                                    stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(iceQueue.pop()));
                                }
                            }
                        } else if (event.candidate) {
                            iceQueue.push(event.candidate);
                        }
                    });




                } else {
                    stompClient.send("/app/all/" + data.sender, {}, JSON.stringify({
                        type: "refuse",
                        content: data.senderNick + "님이 보이스 채팅 요청을 거절하였습니다."
                    }));
                }
            } else if (data.type === "acceptVoiceRequest") {
                targetPeer = data.sender;

                pc = new RTCPeerConnection(iceServers);

                navigator.mediaDevices.getUserMedia({audio: true, video: false})
                    .then(function (stream) {
                        stream.getTracks().forEach(function (track) {
                            pc.addTrack(track, stream);
                        });
                    }).then(function () {
                    createOffer();
                })
                    .catch(function (err) {
                        alert("스트림 연결 실패\n" + err);
                    });

                pc.addEventListener('icecandidate', function (event) {
                    if (event.candidate && rtcConnect) {
                        console.log(event.candidate)
                        stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(event.candidate));
                        if (iceQueue.length > 0) {
                            for (var i = 0; i < iceQueue.length; i++) {
                                stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(iceQueue.pop()));
                            }
                        }
                    } else if (event.candidate) {
                        iceQueue.push(event.candidate);
                    }
                });

            }
        });

        stompClient.subscribe("/topic/peer/offer/" + $("#hiddenUserName").val(), function (message) {
            var data = JSON.parse(message.body);

            navigator.mediaDevices.getUserMedia({audio: true, video: false})
                .then(function (stream) {
                    stream.getTracks().forEach(function (track) {
                        pc.addTrack(track, stream);
                    });
                }).then(function () {
                createAnswer(data);
            })
                .catch(function (err) {
                    alert("스트림 연결 실패\n" + err);
                });
        });

        stompClient.subscribe("/topic/peer/answer/" + $("#hiddenUserName").val(), function (message) {
            var data = JSON.parse(message.body);

            pc.setRemoteDescription(new RTCSessionDescription(data));
            rtcConnect = true;
        });

        stompClient.subscribe("/topic/peer/candidate/" + $("#hiddenUserName").val(), function (message) {
            var data = JSON.parse(message.body);
            pc.addIceCandidate(new RTCIceCandidate(data));
        });
    });

    $("#chatRequestModal .refuse").on('click', function () {
        stompClient.send("/app/alert", {}, JSON.stringify({
            type: "refuse",
            target: $("#requestUsername").val()
        }));
    });

    $("#chatRequestModal .accept").on('click', function () {

        $.ajax({
            url: "/chat/create",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType: "text/plain",
            data: $("#requestUsername").val(),
            success: function (data) {
                window.location.href = "/chat/join/" + data;
            },
            error: function (err) {
                alert("채팅방 생성에 실패했습니다.");
            }
        });
    });

    $(window).on('beforeunload', function () {
        $.ajax({
            url: "/offline",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType: "text/plain",
            data: $("#hiddenUserName").val()
        });
    });

    $("#test").on('click', function () {
        console.log(pc);
    });

    function createOffer() {
        pc.createOffer()
            .then(function (offer) {
                return pc.setLocalDescription(offer);
            })
            .then(function () {
                stompClient.send("/app/peer/offer/" + targetPeer, {}, JSON.stringify(pc.localDescription));
            })
            .catch(function (err) {
                console.log("Offer Error:" + err);
            });
    }

    function createAnswer(data) {
        pc.setRemoteDescription(new RTCSessionDescription(data))
            .then(function () {
                pc.createAnswer()
                    .then(function (answer) {
                        return pc.setLocalDescription(answer);
                    })
                    .then(function () {
                        stompClient.send("/app/peer/answer/" + targetPeer, {}, JSON.stringify(pc.localDescription));
                        rtcConnect = true;
                    })
                    .catch(function (err) {
                        console.log("Answer Error:" + err);
                    });
            });
    }
});