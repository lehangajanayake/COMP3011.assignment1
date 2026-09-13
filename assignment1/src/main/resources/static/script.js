const recordButton = document.getElementById('recordButton');
const statusElement = document.getElementById('status');
const resultElement = document.getElementById('result');

let mediaRecorder;
let audioChunks = [];

recordButton.addEventListener('click', async () => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
        return;
    }

    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        audioChunks = [];
        mediaRecorder = new MediaRecorder(stream);

        mediaRecorder.addEventListener('dataavailable', event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

        mediaRecorder.addEventListener('stop', async () => {
            stream.getTracks().forEach(track => track.stop());
            await submitRecording(new Blob(audioChunks, { type: mediaRecorder.mimeType }));
        });

        mediaRecorder.start();
        recordButton.textContent = 'Stop recording';
        statusElement.textContent = 'Recording...';
        resultElement.textContent = '';
    } catch (error) {
        statusElement.textContent = 'Microphone access was unavailable.';
    }
});

async function submitRecording(audioBlob) {
    recordButton.disabled = true;
    statusElement.textContent = 'Transcribing...';

    const formData = new FormData();
    formData.append('audio', audioBlob, 'recording.webm');

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