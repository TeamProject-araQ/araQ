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
    var targetPeer = null;

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
                        if (event.candidate) {
                            console.log(event.candidate)
                            stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(event.candidate));
                        }
                    });

                    pc.ontrack = function (event) {
                        if (event.track.kind === 'audio') {
                            var remoteAudio = document.getElementById('voiceChatPlayer');
                            remoteAudio.srcObject = event.streams[0];
                        }
                    };
                } else {
                    stompClient.send("/app/all/" + data.sender, {}, JSON.stringify({
                        type: "refuse",
                        content: data.senderNick + "님이 보이스 채팅 요청을 거절하였습니다."
                    }));
                }
            } else if (data.type === "acceptVoiceRequest") {
                targetPeer = data.sender;

                pc = new RTCPeerConnection(iceServers);

                pc.ontrack = function (event) {
                    if (event.track.kind === 'audio') {
                        var remoteAudio = document.getElementById('voiceChatPlayer');
                        remoteAudio.srcObject = event.streams[0];
                    }
                };

                navigator.mediaDevices.getUserMedia({audio: true, video: false})
                    .then(function (stream) {
                        stream.getTracks().forEach(function (track) {
                            pc.addTrack(track, stream);
                        });
                    }).then(function () {
                    stompClient.send("/topic/all/" + targetPeer, {}, JSON.stringify({type: "mediaOK"}));
                })
                    .catch(function (err) {
                        stompClient.send("/topic/all/" + targetPeer, {}, JSON.stringify({
                            type: "refuse",
                            content: "연결이 실패했습니다."
                        }));
                        alert("스트림 연결 실패\n" + err);
                    });

                pc.addEventListener('icecandidate', function (event) {
                    if (event.candidate) {
                        console.log(event.candidate)
                        stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(event.candidate));
                    }
                });
            } else if (data.type === "mediaOK") {
                navigator.mediaDevices.getUserMedia({audio: true, video: false})
                    .then(function (stream) {
                        stream.getTracks().forEach(function (track) {
                            pc.addTrack(track, stream);
                        });
                    }).then(function () {
                    stompClient.send("/topic/all/" + targetPeer, {}, JSON.stringify({type: "mediaConnect"}));
                })
                    .catch(function (err) {
                        stompClient.send("/topic/all/" + targetPeer, {}, JSON.stringify({
                            type: "refuse",
                            content: "연결이 실패했습니다."
                        }));
                        alert("스트림 연결 실패\n" + err);
                    });
            } else if (data.type === "mediaConnect") {
                createOffer();
            } else if (data.type === "RtcClose") {
                pc.close();
                pc = null;
                $("#voiceChatModal").modal("hide");
                alert("보이스 채팅이 종료되었습니다.");
            }
        });

        stompClient.subscribe("/topic/peer/offer/" + $("#hiddenUserName").val(), function (message) {
            createAnswer(JSON.parse(message.body));
        });

        stompClient.subscribe("/topic/peer/answer/" + $("#hiddenUserName").val(), function (message) {
            var data = JSON.parse(message.body);

            pc.setRemoteDescription(new RTCSessionDescription(data));
            $("#voiceChatModal").modal("show");
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

    $("#voiceChatModal .closeBtn").on('click', function () {
        pc.close();
        pc = null;
        stompClient.send("/app/all/" + targetPeer, {}, JSON.stringify({type: "RtcClose"}));
        $("#voiceChatModal").modal("hide");
        alert("보이스 채팅이 종료되었습니다.");
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
                        $("#voiceChatModal").modal("show");
                    })
                    .catch(function (err) {
                        console.log("Answer Error:" + err);
                    });
            });
    }


    $('.listen').on('click', function () {
        var myAudio = $('.audio')[0];
        var username = $('.username').val();
        $.ajax({
            url: "/user/checkAccess",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType: "text/plain",
            data: username,
            success: function (data) {
                if (data === true) {
                    if (myAudio.paused) myAudio.play();
                    else myAudio.pause();
                } else
                    if (confirm("500 버블을 사용하여 음성을 들으시겠습니까?"))
                        initiatePayment();
            },
            error: function (err) {
                console.log(err);
            }
        });
    });

    function initiatePayment(audioOwnerId) {
        $.ajax({
            url: "/pay",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType: "text/plain",
            data: $(".username").val(),
            success: function (data) {
                if (data) alert("결제가 완료되었습니다.");
                else {
                    if (confirm("보유하신 버블이 부족합니다. 결제 페이지로 이동하시겠습니까?"))
                        location.href = "/payment";
                }
            },
            error: function (err) {
                alert(err.responseText);
            }
        });
    }
});