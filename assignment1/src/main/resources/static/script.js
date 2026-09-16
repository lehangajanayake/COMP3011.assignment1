const recordButton = document.getElementById('recordButton');
const statusElement = document.getElementById('status');
const errorElement = document.getElementById('errorMessage');
const resultElement = document.getElementById('result');

let mediaRecorder;
let audioChunks = [];

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

function clearError() {
    errorElement.textContent = '';
    errorElement.hidden = true;
}

function showError(message) {
    errorElement.textContent = message;
    errorElement.hidden = false;
}

recordButton.addEventListener('click', async () => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
        return;
    }

    try {
        clearError();
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
            stream.getTracks().forEach(track => track.stop());
            const audioBlob = new Blob(audioChunks, { type: mediaRecorder.mimeType });
            await submitRecording(audioBlob, recordingFilename(audioBlob.type));
        });

        mediaRecorder.start();
        recordButton.textContent = 'Stop recording';
        statusElement.textContent = 'Recording...';
        resultElement.textContent = '';
    } catch (error) {
        statusElement.textContent = 'Ready';
        showError('Microphone access was unavailable. Check your browser permissions and try again.');
    }
});

async function submitRecording(audioBlob, filename) {
    recordButton.disabled = true;
    statusElement.textContent = 'Transcribing...';
    clearError();

    const formData = new FormData();
    formData.append('audio', audioBlob, filename);

    try {
        const response = await fetch('/api/transcribe', {
            method: 'POST',
            body: formData
        });
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