function scrollUp() {
    var container = document.getElementById('talkContainer');
    container.scrollBy({top: -500, behavior: 'smooth'});
}

function scrollDown() {
    var container = document.getElementById('talkContainer');
    container.scrollBy({top: 500, behavior: 'smooth'});
}

function scrollToTop() {
    var container = document.getElementById('talkContainer');
    container.scrollTo({top: 0, behavior: 'smooth'});
}

function scrollToBottom() {
    var container = document.getElementById('talkContainer');
    container.scrollTo({top: container.scrollHeight, behavior: 'smooth'});
}

$(function () {
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

    if ($('#postList').val() != null) {
    scrollToBottom();
    }

    $(".voice").on('click', function () {
        var audio = $('.audio');
        if (audio.paused) {
            audio.play();
        } else {
            audio.pause();
        }
    });

    $('.likedModal').on('shown.bs.modal', function () {
        $('.modal-backdrop').remove();
    });

    $('.acceptBtn').on('click', function () {
        if (confirm("아라큐 요청을 수락하시겠습니까?"))
            location.href = $(this).data("uri");
    });

    $('.refuseBtn').on('click', function () {
        if (confirm("아라큐 요청을 거절하시겠습니까?"))
            location.href = $(this).data("uri");
    });

    $('.araQBtn').on('click', function () {
        var nickname = $(this).data("nick");
        var username = $(this).data("user");
        if (confirm(nickname + "님에게 아라큐 요청을 보냅니다!")) {
            $.ajax({
                url: "/like/request",
                type: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: "text/plain",
                data: username,
                success: function(response) {
                    alert(response);
                    location.reload();
                },
                error: function(error) {
                    console.error(error);
                }
            });
        }
    });

    $(".chatRequest").on('click', function () {
        var nick = $(this).data("nick");
        var dataValue = $(this).data("value");

        if (confirm(nick + "님에게 채팅 신청을 하시겠습니까?")) {

            $.ajax({
                url: "/chat/request",
                type: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: "text/plain",
                data: dataValue,
                success: function () {
                    alert("성공적으로 요청이 완료되었습니다.");
                },
                error: function (err) {
                    alert("요청이 실패하였습니다.");
                }
            });
        }
    });

    $("#onlineUsers .viewProfile").on('click', function () {
        $.ajax({
            url: "/user/getInfo",
            type: "post",
            contentType: "text/plain",
            dataType: "json",
            data: $(this).data("value"),
            headers: {
                [csrfHeader]: csrfToken
            },
            success: function (data) {
                $("#profileModal .modal-title").text("프로필");
                $("#profileModal .profileImage").attr("src", data.image);
                $("#profileModal .card-title").text(data.nickName);
                $("#profileModal .age").text(data.age);
                $("#profileModal .introduce").text(data.introduce);
                $("#profileModal .audio").attr("src", data.audio);
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
    });

    $(".profileBtn").on('click', function () {
        $.ajax({
            url: "/user/getInfo",
            type: "post",
            contentType: "text/plain",
            dataType: "json",
            data: $(this).data("user"),
            headers: {
                [csrfHeader]: csrfToken
            },
            success: function (data) {
                $("#profileModal .modal-title").text("프로필");
                $("#profileModal .profileImage").attr("src", data.image);
                $("#profileModal .card-title").text(data.nickName);
                $("#profileModal .age").text(data.age);
                $("#profileModal .introduce").text(data.introduce);
                $("#profileModal .audio").attr("src", data.audio);
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
    });

    $('.reportBtn').on('click', function () {
        var reportedUsername = $('#reportedUsername');
        reportedUsername.val($(this).data("value"));
    });

    $('.submitBtn').on('click', function () {
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
});