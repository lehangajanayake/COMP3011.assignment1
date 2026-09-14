const recordButton = document.getElementById('recordButton');
const statusElement = document.getElementById('status');
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

recordButton.addEventListener('click', async () => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
        return;
    }

    try {
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
        statusElement.textContent = 'Microphone access was unavailable.';
    }
});

async function submitRecording(audioBlob, filename) {
    recordButton.disabled = true;
    statusElement.textContent = 'Transcribing...';

    const formData = new FormData();
    formData.append('audio', audioBlob, filename);

    try {
        const response = await fetch('/api/transcribe', {
            method: 'POST',
            body: formData
        });
        const payload = await response.json();

        if (!response.ok) {
            throw new Error('Transcription request failed');
        }

        resultElement.textContent = payload.text || '';
        statusElement.textContent = 'Ready';
    } catch (error) {
        statusElement.textContent = 'Transcription failed. Please try again.';
    } finally {
        recordButton.disabled = false;
        recordButton.textContent = 'Start recording';
        mediaRecorder = undefined;
        audioChunks = [];
    }
}