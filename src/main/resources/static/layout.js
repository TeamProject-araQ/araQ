$(function () {
    var loginUser = $('#hiddenUserName').val();
    var loginUserNick = $('#loginUserNick').val();

    $('.friendRequest').on('click', function () {
        var userNick = $(this).data("nick");
        var username = $(this).data("value");
        if (confirm(userNick + "님에게 친구 신청을 보냅니다.")) {
            if (stompClient) {
                stompClient.send("/app/friend/request", {}, JSON.stringify({
                    sender: loginUser,
                    receiver: username,
                    senderNick: loginUserNick
                }));
            }
        }
    });

    $('.idealTypeMatchLink').on('click', function () {
        if ($('#userIdealType').val() === "") {
            if (confirm("이상형 선택이 완료되지 않았습니다. 선택 화면으로 이동합니다."))
                location.href = "/idealType/create";
        } else location.href = $(this).data("uri");
    });

    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    // stompClient.debug = null;
    let pc = null;
    let notifyCheck = false;
    const iceServers = {
        iceServers: [
            {
                urls: "stun:stun.l.google.com:19302"
            },
        ],
    };
    let targetPeer = null;

    stompClient.connect({}, function (frame) {
        if (Notification.permission !== "granted") {
            Notification.requestPermission().then(r => {
                if (r === "granted") alert("푸시 알람이 설정되었습니다.");
            });
        }

        stompClient.subscribe('/topic/friend/request/impossible/' + loginUser, function (notification) {
            alert(notification.body);
        });

        stompClient.subscribe('/topic/friend/request/possible/' + loginUser, function (notification) {
            const data = JSON.parse(notification.body);
            if (confirm(data.senderNick + "님이 친구 요청을 보냈습니다. 수락하시겠습니까?")) {
                $.ajax({
                    url: "/friend/accept",
                    type: "POST",
                    headers: {
                        [csrfHeader]: csrfToken
                    },
                    contentType: "application/json",
                    data: JSON.stringify({sender: data.sender, receiver: data.receiver}),
                    success: function (response) {
                        alert(response);
                    },
                    error: function (error) {
                        console.error(error);
                    }
                });
            }
        });

        stompClient.send("/app/pong", {}, $("#hiddenUserName").val());

        stompClient.subscribe("/topic/ping", function () {
            stompClient.send("/app/pong", {}, $("#hiddenUserName").val());
        });

        stompClient.subscribe("/topic/all/" + $("#hiddenUserName").val(), function (message) {
            const data = JSON.parse(message.body);
            const chatToast = new bootstrap.Toast($('#chatToast'), {
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
                            stompClient.send("/app/peer/candidate/" + targetPeer, {}, JSON.stringify(event.candidate));
                        }
                    });

                    pc.ontrack = function (event) {
                        if (event.track.kind === 'audio') {
                            const remoteAudio = document.getElementById('voiceChatPlayer');
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
                        const remoteAudio = document.getElementById('voiceChatPlayer');
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
                window.location.reload();
            }
        });

        stompClient.subscribe("/topic/peer/offer/" + $("#hiddenUserName").val(), function (message) {
            createAnswer(JSON.parse(message.body));
        });

        stompClient.subscribe("/topic/peer/answer/" + $("#hiddenUserName").val(), function (message) {
            var data = JSON.parse(message.body);

            pc.setRemoteDescription(new RTCSessionDescription(data));
            $("#voiceChatModal").modal("show");
            let width = 0;
            let count = 60;
            const interval = setInterval(function () {
                width += 1.67;
                count -= 1;
                $("#voiceChatModal .progress-bar").css("width", width + "%");
                $("#voiceChatModal .progress-bar").text(count);
                if (width >= 100) {
                    clearInterval(interval);
                    $("#voiceChatModal .closeBtn").click();
                }
            }, 1000);
        });

        stompClient.subscribe("/topic/peer/candidate/" + $("#hiddenUserName").val(), function (message) {
            let data = JSON.parse(message.body);
            pc.addIceCandidate(new RTCIceCandidate(data));
        });

        stompClient.subscribe("/topic/notification/" + $("#hiddenUserName").val(), function (message) {
            let data = JSON.parse(message.body);
            let permission = Notification.permission;
            if (permission === "granted" && document.hidden && !notifyCheck) {
                showNotification(data);
                notifyCheck = true;
                setTimeout(function () {
                    notifyCheck = false;
                }, 5000);
            } else Notification.requestPermission().then(r => {
                if (document.hidden && !notifyCheck) {
                    showNotification(data);
                    notifyCheck = true;
                    setTimeout(function () {
                        notifyCheck = false;
                    }, 5000);
                }
            });
        })
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

    $("#voiceChatModal .closeBtn").on('click', function () {
        pc.close();
        pc = null;
        stompClient.send("/app/all/" + targetPeer, {}, JSON.stringify({type: "RtcClose"}));
        $("#voiceChatModal").modal("hide");
        alert("보이스 채팅이 종료되었습니다.");
        window.location.reload();
    });

    $('#reportForm .submitBtn').on('click', function () {
        if ($('#selectReason').val() == '')
            alert('신고 사유를 선택해주세요.');
        else if ($('#selectReason').val() == 4) {
            if ($('#detailReason').val() == '')
                alert('신고 내용을 입력해주세요.');
            else {
                $('#reportForm').submit();
                alert("신고가 접수되었습니다.");
            }
        } else {
            $('#reportForm').submit();
            alert("신고가 접수되었습니다.");
        }
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
                        let width = 0;
                        let count = 60;
                        const interval = setInterval(function () {
                            width += 1.67;
                            count -= 1;
                            $("#voiceChatModal .progress-bar").css("width", width + "%");
                            $("#voiceChatModal .progress-bar").text(count);
                            if (width >= 100) {
                                clearInterval(interval);
                                $("#voiceChatModal .closeBtn").click();
                            }
                        }, 1000);
                    })
                    .catch(function (err) {
                        console.log("Answer Error:" + err);
                    });
            });
    }

    function showNotification(data) {
        let notification = new Notification(data.title, {
            body: data.content,
            icon: "/image/_logo.png"
        });

        notification.onclick = function () {
            window.focus();
            window.location.href = data.url;
        };
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
                } else if (confirm("500 버블을 사용하여 음성을 들으시겠습니까?"))
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

function showProfile(username) {
    $.ajax({
        url: "/user/getInfo",
        type: "post",
        contentType: "text/plain",
        dataType: "json",
        data: username,
        headers: {
            [csrfHeader]: csrfToken
        },
        success: function (data) {
            $("#profileModal .modal-title").text("프로필");
            $("#profileModal .profileImage").attr("src", data.image);
            $("#profileModal .card-title").text(data.nickName);
            $("#profileModal .age").text(data.age);
            $("#profileModal .introduce").text(data.introduce);
            $("#profileModal .username").val(data.username);
            if (data.audio) {
                var audioElement = $("#profileModal .audio").attr("src", data.audio)[0];
                var durationElement = $("#profileModal #audioDuration");

                audioElement.ontimeupdate = function () {
                    var currentMinutes = Math.floor(audioElement.currentTime / 60);
                    var currentSeconds = Math.floor(audioElement.currentTime - currentMinutes * 60);
                    durationElement.text(pad(currentMinutes) + ":" + pad(currentSeconds));
                };

                function pad(value) {
                    return value > 9 ? value : "0" + value;
                }

                $("#profileModal .listen, #profileModal #audioDuration").show();
            } else {
                $("#profileModal .listen, #profileModal #audioDuration").hide();
            }
            $("#profileModal").modal("show");

            $("#moreInfoForm > table > tbody > tr:nth-child(1) > td").text(data.height);
            $("#moreInfoForm > table > tbody > tr:nth-child(2) > td").text(data.drinking);
            $("#moreInfoForm > table > tbody > tr:nth-child(3) > td").text(data.smoking);
            $("#moreInfoForm > table > tbody > tr:nth-child(4) > td").text(data.personality);
            $("#moreInfoForm > table > tbody > tr:nth-child(5) > td").text(data.hobby);
            $("#moreInfoForm > table > tbody > tr:nth-child(6) > td").text(data.mbti);
            $("#moreInfoForm > table > tbody > tr:nth-child(7) > td").text(data.religion);
        },
        error: function (err) {
            alert("요청이 실패하였습니다.");
            console.log(err);
        }
    });
}