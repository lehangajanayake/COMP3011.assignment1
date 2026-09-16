const recordButton = document.getElementById('recordButton');
const statusElement = document.getElementById('status');
const recordingTimerElement = document.getElementById('recordingTimer');
const errorElement = document.getElementById('errorMessage');
const resultElement = document.getElementById('result');

let mediaRecorder;
let audioChunks = [];
let recordingTimer;
let recordingStartedAt;

const recordingFormats = [
    'audio/webm;codecs=opus',
    'audio/mp4'
];

function recordingMimeType() {
    return recordingFormats.find(format => MediaRecorder.isTypeSupported(format)) || '';
}

function recordingFilename(mimeType) {
    const extensionByType = {
        'audio/mp4': 'm4a',
        'audio/webm': 'webm'
    };
    const mediaType = mimeType.split(';', 1)[0];
    return `recording.${extensionByType[mediaType] || 'webm'}`;
}

function formatDuration(milliseconds) {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
    const seconds = (totalSeconds % 60).toString().padStart(2, '0');
    return `${minutes}:${seconds}`;
}

function updateRecordingTimer() {
    recordingTimerElement.textContent = formatDuration(Date.now() - recordingStartedAt);
}

function startRecordingTimer() {
    recordingStartedAt = Date.now();
    recordingTimerElement.hidden = false;
    updateRecordingTimer();
    recordingTimer = setInterval(updateRecordingTimer, 1000);
}

function stopRecordingTimer() {
    clearInterval(recordingTimer);
    recordingTimer = undefined;
    recordingStartedAt = undefined;
    recordingTimerElement.textContent = '00:00';
    recordingTimerElement.hidden = true;
}

function clearError() {
    errorElement.textContent = '';
    errorElement.hidden = true;
}

function showError(message) {
    errorElement.textContent = message;
    errorElement.hidden = false;
}

function microphoneErrorMessage(error) {
    if (error?.name === 'NotAllowedError' || error?.name === 'PermissionDeniedError') {
        return 'Microphone permission was denied. Allow microphone access and try again.';
    }
    if (error?.name === 'NotFoundError') {
        return 'No microphone was found. Connect a microphone and try again.';
    }
    if (error?.name === 'NotReadableError') {
        return 'The microphone is already in use by another application.';
    }
    return 'Microphone access was unavailable. Check your browser permissions and try again.';
}

recordButton.addEventListener('click', async () => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
        return;
    }

    try {
        clearError();
        if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === 'undefined') {
            throw new Error('Audio recording is not supported by this browser.');
        }
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        audioChunks = [];
        const mimeType = recordingMimeType();
        mediaRecorder = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);

        mediaRecorder.addEventListener('dataavailable', event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

        mediaRecorder.addEventListener('stop', async () => {
            stopRecordingTimer();
            stream.getTracks().forEach(track => track.stop());
            const audioBlob = new Blob(audioChunks, { type: mediaRecorder.mimeType });
            await submitRecording(audioBlob, recordingFilename(audioBlob.type));
        });

        mediaRecorder.start();
        startRecordingTimer();
        recordButton.textContent = 'Stop recording';
        statusElement.textContent = 'Recording...';
        resultElement.textContent = '';
    } catch (error) {
        stopRecordingTimer();
        statusElement.textContent = 'Ready';
        showError(error instanceof Error && error.message.startsWith('Audio recording')
            ? error.message
            : microphoneErrorMessage(error));
    }
});

async function submitRecording(audioBlob, filename) {
    recordButton.disabled = true;
    statusElement.textContent = 'Uploading audio...';
    clearError();

    const formData = new FormData();
    formData.append('audio', audioBlob, filename);

    try {
        const response = await fetch('/api/transcribe', {
            method: 'POST',
            body: formData
        });
        statusElement.textContent = 'Transcribing...';
        const responseText = await response.text();
        let payload;

        try {
            payload = responseText ? JSON.parse(responseText) : {};
        } catch (error) {
            payload = {};
        }

        if (!response.ok) {
            throw new Error(payload.message || `Request failed (${response.status})`);
        }

        if (typeof payload.text !== 'string') {
            throw new Error('The transcription response was invalid.');
        }

        resultElement.textContent = payload.text || '';
        statusElement.textContent = 'Ready';
    } catch (error) {
        showError(error instanceof Error
            ? error.message
            : 'Transcription failed. Please try again.');
        statusElement.textContent = 'Ready';
    } finally {
        recordButton.disabled = false;
        recordButton.textContent = 'Start recording';
        mediaRecorder = undefined;
        audioChunks = [];
    }
}