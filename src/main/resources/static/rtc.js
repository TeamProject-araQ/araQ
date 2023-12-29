$(function () {
    var socket = null;
    var stompClient = null;
    var userStream = null;
    var pc = null;
    var iceServers = {
        iceServers: [
            {
                urls: "stun:stun.l.google.com:19302"
            },
        ],
    };

    $("#socketConnectBtn").on('click', function () {
        socket = new SockJS("/ws");
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, function () {
            pc = new RTCPeerConnection(iceServers);

            mediaStreaming();

            stompClient.subscribe("/topic/peer/offer/" + username, function (message) {
                var data = JSON.parse(message.body);
                peerConnection(data, "offer");
            });

            stompClient.subscribe("/topic/peer/answer/" + username, function (message) {
                var data = JSON.parse(message.body);
                peerConnection(data, "answer");
            });

            stompClient.subscribe("/topic/peer/candidate/" + username, function (message) {
                var data = JSON.parse(message.body);
                pc.addIceCandidate(new RTCIceCandidate(data));
            });

            pc.addEventListener('icecandidate', function (event) {
                if (event.candidate) {
                    stompClient.send("/app/peer/candidate", {}, JSON.stringify(event.candidate));
                }
            });

            pc.ontrack = function (event) {
                if (event.track.kind === 'audio') {
                    var remoteAudio = document.getElementById('remoteAudio');
                    remoteAudio.srcObject = event.streams[0];
                }
            };
        });
    });

    $("#offerSendBtn").on('click', peerConnection);

    function peerConnection(data, type) {
        if (data.type === "click") {
            pc.createOffer()
                .then(function (offer) {
                    return pc.setLocalDescription(offer);
                })
                .then(function () {
                    stompClient.send("/app/peer/offer", {}, JSON.stringify(pc.localDescription));
                })
                .catch(function (err) {
                    console.log("Offer Error:" + err);
                });
        } else if (data.type === "offer") {
            pc.setRemoteDescription(new RTCSessionDescription(data))
                .then(function () {
                    pc.createAnswer()
                        .then(function (answer) {
                            return pc.setLocalDescription(answer);
                        })
                        .then(function () {
                            stompClient.send("/app/peer/answer", {}, JSON.stringify(pc.localDescription));
                        })
                        .catch(function (err) {
                            console.log("Answer Error:" + err);
                        });
                });
        } else if (data.type === "answer") {
            pc.setRemoteDescription(new RTCSessionDescription(data))
                .then(function () {
                });
        }
    }

    function mediaStreaming() {
        navigator.mediaDevices.getUserMedia({audio: true, video: false})
            .then(function (stream) {
                stream.getTracks().forEach(function (track) {
                    pc.addTrack(track, stream);
                });
            })
            .catch(function (err) {
                alert("스트림 연결 실패\n" + err);
            });
    }

    $("#checkConnectBtn").on('click', function () {
        console.log(pc);
    });

});