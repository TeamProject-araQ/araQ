$(function () {
    $("#createPlazaBtn").on('click', function () {
        $("#createPlazaForm").modal("show");
    });

    $("#peopleRange").on('change', function () {
        $(".peopleRangeNumber").text($(this).val());
    });

    $("#plazaPublic").on('change', function () {
        if (this.checked) {
            const passwordElement = $("#plazaPassword");
            passwordElement.val("");
            passwordElement.prop("disabled", true);
        }
    });

    $("#plazaPrivate").on('change', function () {
        if (this.checked) {
            const passwordElement = $("#plazaPassword");
            passwordElement.prop("disabled", false);
            passwordElement.focus();
        }
    });

    $("#plazaCreateBtn").click(function () {
        const title = $("#plazaTitle").val();
        const people = $("#peopleRange").val();
        const password = $("#plazaPassword").val();
        const bgImg = $(".selectedImg > *").attr("src");
        if (title.trim() === "") alert("광장 이름을 입력해주세요");
        else if ($("#plazaPrivate").is(":checked") && password  === "")
            alert("비밀번호를 입력해주세요");
        else {
            $("#plazaCreateForm > input[name='title']").val(title);
            $("#plazaCreateForm > input[name='people']").val(people);
            $("#plazaCreateForm > input[name='password']").val(password);
            $("#plazaCreateForm > input[name='img']").val(bgImg);
            $("#plazaCreateForm").submit();
        }
    });

    $(".plazaDeleteBtn").click(function () {
        if (confirm("정말 삭제하시겠습니까?")) {
            $("#plazaDeleteForm > input[name='code']").val($(this).data("value"));
            $("#plazaDeleteForm").submit();
        }
    });

    $(".plazaJoinBtn").click(function () {
        if (!$(this).data("pw")) {
            $("#plazaJoinForm > input[name='code']").val($(this).data("value"));
            $("#plazaJoinForm").submit();
        } else {
            $("#plazaCode").val($(this).data("value"));
            $("#plazaPwModal").modal("show");
        }
    });

    $("#bgImgList > a").click(function () {
        $("#bgImgList > a").removeClass("selectedImg");
        $(this).addClass("selectedImg");
    });

    $("#plazaPwCheckBtn").click(function () {
        $.ajax({
            url: "/plaza/check",
            type: "post",
            contentType: "application/json",
            headers: {
                [csrfHeader]: csrfToken
            },
            data: JSON.stringify({
                input: $("#plazaPwParam").val(),
                code: $("#plazaCode").val()
            }),
            success: function (message) {
                if (message === "access") {
                    $("#plazaJoinForm > input[name='code']").val($("#plazaCode").val());
                    $("#plazaJoinForm").submit();
                } else {
                    $("#plazaPwParam").addClass("is-invalid");
                    $("#plazaPwFeedback").text("비밀번호가 맞지 않습니다.");
                }
            },
            error: function (error) {
                alert("접근 실패");
            }
        });
    });

    $(".customImage").click(function () {
        $("#customImageInput").click();
    });

    $("#customImageInput").change(function () {
        const element = $(".customImage");
        let file = this.files[0];

        element.text("");

        if (!file.type.startsWith("image/")) {

        }
    });
});