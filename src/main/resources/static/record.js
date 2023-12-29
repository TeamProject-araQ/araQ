$(document).ready(function () {
    let mediaRecorder;
    let audioChunks = [];
    let isRecording = false;
    let recordingTimeout;
    let audioBlob;
    let circumference;

    const maxRecordingTime = 30000;

    const $recordButton = $("#recordButton");
    const $saveButton = $("#saveButton");
    const $circle = $('.progress-ring__circle');

    if ($circle.length > 0) {
        const radius = $circle.attr('r');
        circumference = 2 * Math.PI * radius;

        $circle.css({
            'stroke-dasharray': circumference,
            'stroke-dashoffset': circumference
        });
    }
    if ($circle.length > 0 && $circle.is(':visible')) {
        circumference = $circle[0].getTotalLength();
        $circle.css('stroke-dasharray', circumference);
        $circle.css('stroke-dashoffset', circumference);
    }

    $("#recordButton").click(async function () {
        if (!isRecording) {
            // 녹음 시작
            try {
                const stream = await navigator.mediaDevices.getUserMedia({audio: true});
                mediaRecorder = new MediaRecorder(stream);
                audioChunks = [];

                mediaRecorder.ondataavailable = event => {
                    audioChunks.push(event.data);
                };

                mediaRecorder.start();
                isRecording = true;
                $(this).text('■');
                $("#audio").removeAttr("src");
                recordingTimeout = setTimeout(stopRecording, maxRecordingTime);

                // 시간 테두리
                let elapsed = 0;
                recordingTimer = setInterval(() => {
                    elapsed += 1000;
                    const offset = circumference - (elapsed / maxRecordingTime) * circumference;
                    $circle.css('stroke-dashoffset', offset);

                    if (elapsed >= maxRecordingTime) {
                        stopRecording();
                    }
                }, 1000);
            } catch (error) {
                console.error("오디오 장치를 찾을 수 없습니다: ", error);
                alert("오디오 장치에 접근할 수 없습니다. 마이크가 연결되어 있는지 확인해주세요.");
            }
        } else {
            stopRecording();
        }
    });

    function stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            clearTimeout(recordingTimeout);
            $("#recordButton").text('▶');
            isRecording = false;

            // 시간 테두리
            clearInterval(recordingTimer);
            $circle.css('stroke-dashoffset', circumference);

            mediaRecorder.onstop = () => {
                audioBlob = new Blob(audioChunks);
                $("#audio").attr("src", URL.createObjectURL(audioBlob));
                $saveButton.prop("disabled", false);
            };
        }
    }

    $saveButton.click(function () {
        if (audioBlob) {
            uploadAudio(audioBlob);
            $(this).prop("disabled", true);
            var myModalEl = document.getElementById('voiceRecord'); // 모달의 ID를 'modalId'로 가정
            var modalInstance = bootstrap.Modal.getInstance(myModalEl);
            modalInstance.hide();
        }
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
                console.log(error);
            }
        });
    };
});



