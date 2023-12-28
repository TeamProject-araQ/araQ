var stompClient = null;
var peerConnection = null;
var localStream = null;
var iceCandidatesQueue = [];

function connect() {
    var socket = new SockJS('/ws'); // 서버의 STOMP 엔드포인트 연결
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/signaling', function(message) {
            var signal = JSON.parse(message.body);
            handleSignalingData(signal);
        });

        // 오디오 스트림 요청
        navigator.mediaDevices.getUserMedia({ audio: true, video: false })
            .then(stream => {
                localStream = stream;
                document.getElementById('localAudio').srcObject = localStream;
                createPeerConnection();
            })
            .catch(error => console.error('MediaStream error: ', error));
    });
}

function createPeerConnection() {
    peerConnection = new RTCPeerConnection();


    // 로컬 스트림을 PeerConnection에 추가
    localStream.getTracks().forEach(track => {
        peerConnection.addTrack(track, localStream);
    });

    // ICE 후보 수집
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
            console.log(event.candidate);
            stompClient.send("/app/signaling", {}, JSON.stringify({"ice": event.candidate}));
        }
    };

    // 원격 스트림 수신 대기
    peerConnection.ontrack = function(event) {
        document.getElementById('remoteAudio').srcObject = event.streams[0];
    };

    peerConnection.oniceconnectionstatechange = function(event) {
        console.log("ICE connection 상태 변경:", peerConnection.iceConnectionState);
        if (peerConnection.iceConnectionState === "connected" ||
            peerConnection.iceConnectionState === "completed") {
            console.log("ICE 연결 성공");
        }
    };

    peerConnection.onconnectionstatechange = function(event) {
        console.log("PeerConnection 상태 변경:", peerConnection.connectionState);
        if (peerConnection.connectionState === "connected") {
            console.log("Peer-to-Peer 연결 성공");
        }
    };
}

function handleSignalingData(data) {
    if (data.ice) {
        if (peerConnection.remoteDescription) {
            // 원격 SDP가 설정된 경우, ICE 후보 추가
            peerConnection.addIceCandidate(new RTCIceCandidate(data.ice));
        } else {
            // 원격 SDP가 아직 설정되지 않았다면, 큐에 저장
            iceCandidatesQueue.push(data.ice);
        }
    } else if (data.sdp) {
        if(peerConnection.signalingState !== 'stable') {
            peerConnection.setRemoteDescription(new RTCSessionDescription(data.sdp))
                .then(() => {
                    // 원격 SDP가 설정된 후, 큐에 저장된 모든 ICE 후보 처리
                    iceCandidatesQueue.forEach(iceCandidate => {
                        peerConnection.addIceCandidate(new RTCIceCandidate(iceCandidate));
                    });
                    iceCandidatesQueue = [];

                    if (data.sdp.type === 'offer') {
                        return peerConnection.createAnswer();
                    }
                })
                .then(answer => {
                    if (answer) {
                        return peerConnection.setLocalDescription(answer);
                    }
                })
                .then(() => {
                    if (peerConnection.localDescription) {
                        stompClient.send("/app/signaling", {}, JSON.stringify({"sdp": peerConnection.localDescription}));
                    }
                })
                .catch(e => console.error(e));
        }
    }
}

function startCall() {
    peerConnection.createOffer()
        .then(offer => peerConnection.setLocalDescription(offer))
        .then(() => {
            stompClient.send("/app/signaling", {}, JSON.stringify({"sdp": peerConnection.localDescription}));
        });
}

function endCall() {
    peerConnection.close();
    peerConnection = null;
    // 추가적인 종료 로직
}
