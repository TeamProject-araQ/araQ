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
    $('#recommendedMatches').popover({
        html: true,
        content: "<strong><추천 매칭></strong> 에는 매칭 우선권을 구매한 회원만 표시됩니다."
    });

    var postListVal = $('#postList').val();
    if (!postListVal === "") {
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
                success: function (response) {
                    alert(response);
                    location.reload();
                },
                error: function (error) {
                    console.error(error);
                }
            });
        }
    });

    $(".chatRequest").on('click', function () {
        var nick = $(this).data("nick");
        var dataValue = $(this).data("value");

        $.ajax({
            url: "/user/check/chatPass",
            type: "POST",
            headers: {
                [csrfHeader]: csrfToken
            },
            success: function (data) {
                if (data) {
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
                } else alert("채팅 신청권이 필요합니다.");
            },
            error: function (err) {
                alert("요청이 실패하였습니다.");
            }
        });
    });

    $(".viewProfile").on('click', function () {
        showProfile($(this).data("value"));
    });

    $('.reportBtn').on('click', function () {
        var reportedUsername = $('#reportedUsername');
        reportedUsername.val($(this).data("value"));
    });

    $('.testLink').on('click', function () {
        if ($(this).data("value") === "" || $(this).data("value") == null)
            alert("취향 조사가 진행되지 않은 회원입니다.")
        else
            location.href = $(this).data("uri");
    });
});