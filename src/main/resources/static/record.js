let mediaRecorder;
let audioChunks = [];

$(function () {
    $("#startRecord").on('click', async function () {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({audio: true});
            mediaRecorder = new MediaRecorder(stream);
            mediaRecorder.start();

            audioChunks = [];
            mediaRecorder.ondataavailable = event => {
                audioChunks.push(event.data);
            };

            mediaRecorder.onstop = async () => {
                const audioBlob = new Blob(audioChunks);
                await uploadAudio(audioBlob);
            };

            $("#stopRecord").prop("disabled", false);
        } catch (error) {
            console.error("오디오 장치를 찾을 수 없습니다: ", error);
            alert("오디오 장치에 접근할 수 없습니다. 마이크가 연결되어 있는지 확인해주세요.");
        }
    });

    $("#stopRecord").click(function () {
        mediaRecorder.stop();
        $(this).prop("disabled", true);
    });

    const uploadAudio = async (audioBlob) => {
        const audio = new FormData();
        audio.append("audio", audioBlob, "voice.mp3");

        $.ajax({
            url: "/user/record",
            type: "POST",
            data: audio,
            headers: {
                [csrfHeader]: csrfToken
            },
            processData: false,
            contentType: false,
            success: function(response) {
                alert(response);
            },
            error: function(error) {
                alert(error);
            }
        });
    };
});