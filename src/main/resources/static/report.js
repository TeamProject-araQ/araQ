$(function () {

    $(".page-link").on('click', function () {
        $("#page").val(this.dataset.page);
        $("#searchForm").submit();
    });

    $("#checkAll").on('click', function () {
        $('.check').prop('checked', this.checked);
    });

    $('.actionSelect').change(function () {
        var selectedAction = $(this).val();
        var $checkbox = $(this).closest('tr').find('.check');
        var reason = $(this).closest('tr').find('.reason').text();
        console.log(reason);
        if (selectedAction === "계정 정지") {
            $('#suspensionModal').modal('show');

            $('#saveSuspensionDays').click(function () {
                var suspensionDays = $('#suspensionDays').val();
                if (suspensionDays == null || suspensionDays === "")
                    alert("일수 선택은 필수 입력 사항입니다.");
                else {
                    $checkbox.attr('data-action', selectedAction);
                    $checkbox.attr('data-days', suspensionDays);
                    $checkbox.attr('data-reason', reason);
                    $('#suspensionModal').modal('hide');
                }
            });
        } else if (selectedAction === "영구 정지") {
            $checkbox.attr('data-action', selectedAction);
            $checkbox.attr('data-reason', reason);
        } else $checkbox.attr('data-action', selectedAction);
    });

    $('#actionBtn').on('click', function () {
        var dataObject = {};
        var isEmptyAction = false;
        $(".check:checked").each(function () {
            var id = $(this).val();
            var action = $(this).data("action");
            var days = $(this).data("days");
            var reason = $(this).data("reason");
            if (action === "" || action == null) {
                isEmptyAction = true;
            }
            dataObject[id] = {
                action: action,
                days: days,
                reason: reason
            };
        });

        var jsonData = JSON.stringify(dataObject);
        var keys = Object.keys(dataObject);
        if (keys.length === 0) {
            alert('선택된 신고 내역이 없습니다.');
        } else if (isEmptyAction) {
            alert("조치 옵션을 선택해주세요.");
        } else {
            $.ajax({
                url: "/admin/report/action",
                type: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: "application/json",
                data: jsonData,
                success: function (response) {
                    alert(response);
                },
                error: function (err) {
                    console.log(err);
                }
            });
        }
    });

    $("#deleteBtn").on('click', function () {
        var checkedValues = $(".check:checked").map(function () {
            return this.value;
        }).get();

        if (checkedValues.length === 0) {
            alert('선택된 신고 내역이 없습니다.');
            return;
        }

        if (confirm('신고 내역을 삭제하시겠습니까?')) {
            $.ajax({
                url: '/admin/report/delete',
                type: 'post',
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: 'application/json',
                data: JSON.stringify(checkedValues),
                success: function (data) {
                    alert(data);
                    location.reload();
                },
                error: function (error) {
                    console.log(error);
                }
            });
        }
    });

    $(".historyBtn").click(function () {
        const code = $(this).data("value");
        const element = $("#historyModal .modal-body");
        element.empty();
        if ($(this).data("location") === "chat") {
            $.ajax({
                url: "/report/getChatHistory",
                type: "post",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: "text/plain",
                data: code,
                success: (data) => {
                    $(data).each((i, e) => {
                        const appendedElement = $("<p></p>");
                        appendedElement.text(e.writer.username + ": " + e.content + " (" + e.createDate + ")");
                        element.append(appendedElement);
                        if (e.images !== null) {
                            $(e.images).each((ii, ie) => {
                                const imgElement = $("<img alt=''>");
                                imgElement.attr("src", ie).css({
                                    width: 100,
                                    height: 100
                                });
                                element.append(imgElement);
                            });
                        }
                    });
                    $("#historyModal").modal("show");
                },
                error: (err) => console.log(err)
            });
        } else if ($(this).data("location") === "plaza") {
            $.ajax({
                url: "/report/getPlazaHistory",
                type: "post",
                headers: {
                    [csrfHeader]: csrfToken
                },
                contentType: "text/plain",
                data: code,
                success: (data) => {
                    const e = $("<div>" +
                        "<h3>이름</h3>" +
                        "<p></p>" +
                        "<h3>배경이미지</h3>" +
                        "<img src='' alt=''>" +
                        "</div>");

                    e.find("p").text(data.title);
                    e.find("img").attr("src", data.background).css({width:"100px", height:"100px", border:"solid"});
                    element.append(e);

                    $("#historyModal").modal("show");
                },
                error: (err) => console.log(err)
            });
        } else {
            $.ajax({
                url: "/user/getInfo",
                type: "post",
                contentType: "text/plain",
                dataType: "json",
                data: $(this).data("target"),
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
                        $("#profileModal .audio").attr("src", data.audio)[0];
                        $("#profileModal .audio").show();
                    } else {
                        $("#profileModal .audio").hide();
                    }
                    $("#profileModal").modal("show");

                    $("#moreInfoForm > table > tbody > tr:nth-child(1) > td").text(data.height);
                    $("#moreInfoForm > table > tbody > tr:nth-child(2) > td").text(data.drinking);
                    $("#moreInfoForm > table > tbody > tr:nth-child(3) > td").text(data.smoking);
                    $("#moreInfoForm > table > tbody > tr:nth-child(4) > td").text(data.personality);
                    $("#moreInfoForm > table > tbody > tr:nth-child(5) > td").text(data.hobby);
                    $("#moreInfoForm > table > tbody > tr:nth-child(6) > td").text(data.mbti);
                    $("#moreInfoForm > table > tbody > tr:nth-child(7) > td").text(data.religion);

                    var carouselInner = $("#profileModal #carouselExampleControls .carousel-inner");
                    carouselInner.empty();  // 기존 캐러셀 내용 비우기

                    // 각 이미지를 캐러셀에 동적으로 추가
                    for (var i = 0; i < data.images.length; i++) {
                        var imageUrl = data.images[i];

                        // 이미지에 대한 HTML 생성 및 추가
                        var carouselItem = $("<div>").addClass("carousel-item");
                        var imageElement = $("<img>").addClass("d-block w-100").attr("src", imageUrl).css("height", "500px");
                        carouselItem.append(imageElement);

                        // 첫 번째 이미지는 활성화 상태로 설정
                        if (i === 0) {
                            carouselItem.addClass("active");
                        }

                        // 캐러셀에 이미지 추가
                        carouselInner.append(carouselItem);
                    }
                },
                error: function (err) {
                    alert("요청이 실패하였습니다.");
                    console.log(err);
                }
            });
        }
    });
});