let mediaRecorder;
let audioChunks = [];

$(document).ready(function () {
    $("#startRecord").click(async function () {
        const stream = await navigator.mediaDevices.getUserMedia({audio: true});
        mediaRecorder = new MediaRecorder(stream);
        mediaRecorder.start();

        audioChunks = [];
        mediaRecorder.ondataavailable = event => {
            audioChunks.push(event.data);
        };

        mediaRecorder.onstop = async () => {
            const audioBlob = new Blob(audioChunks);
            $("#audio").attr("src", URL.createObjectURL(audioBlob));

            await uploadAudio(audioBlob);
        };

        $("#stopRecord").prop("disabled", false);
    });

    $("#stopRecord").click(function () {
        mediaRecorder.stop();
        $(this).prop("disabled", true);
    });
});

const uploadAudio = async (audioBlob) => {
    const formData = new FormData();
    formData.append("audio", audioBlob, "voice.mp3");

    $.ajax({
        url: "/user/record",
        type: "POST",
        data: formData,
        headers: {
            [csrfHeader]: csrfToken
        },
        processData: false,
        contentType: false,
        success: function (response) {
            alert(response);
        },
        error: function (error) {
            alert(error);
        }
    });
};
