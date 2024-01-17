$(function () {
    const loginUser = $('#hiddenUserName').val();
    const loginUserNick = $('#loginUserNick').val();

    var preferenceDay = $('#preferenceDay').val();
    var purchaseTime = new Date($('#matchHourLeft').text());
    var dayLater = new Date(purchaseTime.getTime() + preferenceDay * 24 * 60 * 60 * 1000);
    var currentTime = new Date();
    currentTime = new Date(Date.UTC(
        currentTime.getUTCFullYear(),
        currentTime.getUTCMonth(),
        currentTime.getUTCDate(),
        currentTime.getUTCHours(),
        currentTime.getUTCMinutes(),
        currentTime.getUTCSeconds()));
    var totalHoursLeft = (dayLater - currentTime) / (1000 * 60 * 60);
    var daysLeft = Math.floor(totalHoursLeft / 24);
    var hoursLeft = Math.floor(totalHoursLeft % 24);
    $('#matchHourLeft').text(daysLeft + "일 " + hoursLeft + "시간");

    var chatPurchaseTime = new Date($('#chatHourLeft').text());
    var chatDayLater = new Date(chatPurchaseTime.getTime() + 7 * 24 * 60 * 60 * 1000);
    var chatTotalHoursLeft = (chatDayLater - currentTime) / (1000 * 60 * 60);
    $('#chatHourLeft').text(Math.floor(chatTotalHoursLeft / 24) + "일 " + Math.floor(chatTotalHoursLeft % 24) + "시간");

    var voiceDay = $('#voiceDay').val();
    var voicePurchaseTime = new Date($('#voiceHourLeft').text());
    var voiceDayLater = new Date(voicePurchaseTime.getTime() + voiceDay * 24 * 60 * 60 * 1000);
    var voiceTotalHoursLeft = (voiceDayLater - currentTime) / (1000 * 60 * 60);
    var voiceDaysLeft = Math.floor(voiceTotalHoursLeft / 24);
    var voiceHoursLeft = Math.floor(voiceTotalHoursLeft % 24);
    $('#voiceHourLeft').text(voiceDaysLeft + "일 " + voiceHoursLeft + "시간");

    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;
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
    let location = (typeof code === "undefined") ? window.location.pathname : code;

    stompClient.connect({}, function (frame) {
        if (Notification.permission !== "granted") {
            Notification.requestPermission().then(r => {
                if (r === "granted") alert("푸시 알림이 설정되었습니다.");
            });
        } else alert("푸시 알림이 설정되어 있지 않습니다.");

        stompClient.subscribe('/topic/friend/request/impossible/' + loginUser, function (notification) {
            alert(notification.body);
        });

        stompClient.subscribe('/topic/send/message/' + loginUser, function (notification) {
            const data = JSON.parse(notification.body);
            var image = data.image;
            var senderNick = data.senderNick;
            var sender = data.sender;
            var content = data.content;
            var datetime = data.datetime;
            var messageId = data.messageId;
            const messageToast = new bootstrap.Toast($('#messageToast'), {
                autohide: false
            });
            $('#messageToast .image').attr('src', image);
            $('#messageToast .nickname').text(senderNick);
            $('#messageToast .datetime').text(datetime);
            $('#messageToast .message').data("value", messageId);
            $('#messageToast .message').text(senderNick + "님으로부터 쪽지가 도착했습니다.");
            $('#messageModal #image').attr('src', image);
            $('#messageModal #messageModalLabel').text(senderNick);
            $('#messageModal .messageContent').text(content);
            $('#messageModal .datetime').text(datetime);
            $('#messageModal .receiver').val(sender);
            messageToast.show();
            setTimeout(function () {
                $('#messageToast').fadeOut();
            }, 5000);
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

        stompClient.send("/app/pong", {}, JSON.stringify({
            user: $("#hiddenUserName").val(),
            location: location
        }));

        stompClient.subscribe("/topic/ping", function () {
            stompClient.send("/app/pong", {}, JSON.stringify({
                user: $("#hiddenUserName").val(),
                location: location
            }));
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

    if ($('#phone').val() == null || $('#phone').val() === "") {
        if (!window.location.pathname.startsWith('/user/resetPw/')) {
            alert("휴대폰 인증이 완료되지 않은 회원입니다. 휴대폰 인증을 진행합니다.");
            $('#verModal').modal('show');
        }
    }

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
        if ($('#selectReason').val() === '')
            alert('신고 사유를 선택해주세요.');
        else if ($('#selectReason').val() === 4) {
            if ($('#detailReason').val() === '')
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
        $.ajax({
            url: "/user/checkAccess",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            success: function (data) {
                if (data === true) {
                    if (myAudio.paused) myAudio.play();
                    else myAudio.pause();
                } else alert("음성 이용권이 필요합니다.");
            },
            error: function (err) {
                console.log(err);
            }
        });
    });

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

    $('.sendBtn').on('click', function () {
        var sender = $('#hiddenUserName').val();
        var receiver = $('.receiver').val();
        var content = $('#content').val();
        if (stompClient) {
            stompClient.send("/app/send/message", {}, JSON.stringify({
                sender: sender,
                receiver: receiver,
                content: content
            }));
            window.location.reload();
        }
    });

    $('#content').keypress(function (e) {
        if (e.keyCode === 13) {
            $('.sendBtn').click();
        }
    });

    $('.answerBtn').on('click', function () {
        var sender = $('#hiddenUserName').val();
        var receiver = $('#messageModal .receiver').val();
        var content = $('#messageModal .answerContent').val();
        if (stompClient) {
            stompClient.send("/app/send/message", {}, JSON.stringify({
                sender: sender,
                receiver: receiver,
                content: content
            }));
            window.location.reload();
        }
    });

    $('.answerContent').keypress(function (e) {
        if (e.keyCode === 13) {
            $('.answerBtn').click();
        }
    });

    $('.idealTypeMatchLink').on('click', function (event) {
        event.preventDefault();
        if ($('#userIdealType').val() === "") {
            if (confirm("이상형 선택이 완료되지 않았습니다. 선택 화면으로 이동합니다."))
                window.location.assign("/idealType/create");
        } else window.location.assign($(this).data("uri"));
    });

    $('#messageToast .message').on('click', function () {
        var messageId = $(this).data("value");
        $.ajax({
            url: "/message/read",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType: "text/plain",
            data: messageId,
            success: function (data) {
            },
            error: function (err) {
                console.log(err);
            }
        });
    });

    function displayRateData(data) {
        var rate = JSON.parse(data);
        $("#collapseRate").collapse('toggle');
        $('#profileModal #manner').text(rate.manner);
        $('#profileModal #appeal').text(rate.appeal);
        $('#profileModal #appearance').text(rate.appearance);
    }

    $('#profileModal .viewRate').on('click', function () {
        const nick = $(this).data("nick");
        const target = $(this).data("value")
        $.ajax({
            url: "/user/getRate",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            contentType: "text/plain",
            data: target,
            success: function (data) {
                if (data !== "") {
                    $.ajax({
                        url: "/rate/check",
                        type: "POST",
                        headers: {
                            [csrfHeader]: csrfToken
                        },
                        contentType: "text/plain",
                        data: target,
                        success: function (value) {
                            if (value === true) displayRateData(data);
                            else {
                                if (confirm(nick + "님에 대한 평가를 열람하시겠습니까?")) {
                                    $.ajax({
                                        url: "/rate/view",
                                        type: "POST",
                                        headers: {
                                            [csrfHeader]: csrfToken
                                        },
                                        contentType: "text/plain",
                                        data: target,
                                        success: function (response) {
                                            if (response === false)
                                                alert("평가 열람권이 필요합니다.");
                                            else displayRateData(data);
                                        },
                                        error: function (err) {
                                            console.log(err)
                                        }
                                    });
                                }
                            }
                        }, error: function (err) {
                            console.log(err);
                        }
                    })
                } else {
                    alert("아직 " + nick + "님에 대한 평가 내역이 존재하지 않습니다.");
                }
            },
            error: function (err) {
                console.log(err);
            }
        });
    });

    $('.saveNum').on('click', function () {
        var phoneNum = $('#phoneNumber').val();
        phoneNum = phoneNum.replace(/-/g, '');
        if (verKeyConfirmed === false)
            alert("인증번호 확인이 완료되지 않았습니다.");
        else {
            $.ajax({
                url: "/user/save/phone",
                type: "POST",
                contentType: "text/plain",
                data: phoneNum,
                headers: {
                    [csrfHeader]: csrfToken
                },
                success: function (data) {
                    alert(data);
                    $('#verModal').modal('hide');

                },
                error: function (err) {
                    console.log(err);
                }
            })
        }
    });
});

var verKeyConfirmed = false;

function sendVerKey() {
    var phoneNum = $('#phoneNumber').val();

    phoneNum = phoneNum.replace(/-/g, '');

    $.ajax({
        type: 'POST',
        url: '/user/signupAuth',
        contentType: 'application/json',
        data: JSON.stringify({phoneNum: phoneNum}),
        headers: {
            'X-CSRF-TOKEN': csrfToken
        },
        success: function (response) {
            if (response === "success") {
                alert('인증번호을 발송했습니다. 핸드폰 번호가 정확한지 확인해주세요.');
                $('#verKeySection').show();
            } else {
                alert('번호가 정확하지 않습니다.');
            }
        },
        error: function () {
            alert('서버 오류');
        }
    });
}

function confirmVerKey() {
    var phoneNum = $('#phoneNumber').val();
    var verKey = $('#verKey').val();

    $.ajax({
        type: 'POST',
        url: '/user/confirmPhoneNum',
        contentType: 'application/json',
        data: JSON.stringify({phoneNum: phoneNum, verKey: verKey}),
        headers: {
            [csrfHeader]: csrfToken
        },
        success: function (response) {
            if (response === "success") {
                alert('인증번호가 확인되었습니다.');
                verKeyConfirmed = true;
            } else {
                alert('인증번호가 일치하지 않습니다.');
                verKeyConfirmed = false;
            }
        },
        error: function () {
            alert('서버 오류');
        }
    });
}

function showProfile(username) {
    $.ajax({
        url: "/user/getInfo",
        type: "post",
        contentType: "text/plain",
        dataType: "json",
        data: username.toString(),
        headers: {
            [csrfHeader]: csrfToken
        },
        success: function (data) {
            $("#profileModal .modal-title").text("프로필");
            $("#profileModal .profileImage").attr("src", data.image);
            $("#profileModal .card-title").text(data.nickName);
            $("#profileModal .age").text(data.age);
            $("#profileModal .introduce").text(data.introduce);
            $('#profileModal .viewRate').data("value", data.username);
            $('#profileModal .viewRate').data("nick", data.nickName);
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