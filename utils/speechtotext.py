"""
#############################################################
# File        : speechtotext.py                             #
# Authors     : Shrayanendra Nath Mandal, Preetish Majumdar #
# Date        : 2025-06-01                                  #
# Description : Speech to Text Converter                    #
#               API, Speech Recognition, and Speech to Text #
#############################################################
"""
import threading
from RealtimeSTT import AudioToTextRecorder

class SpeechToText:
    def __init__(self):
        self.recorder = AudioToTextRecorder()
        self.last_text = None
        self.listening = False

    def _internal_callback(self, text):
        self.last_text = text  # Store the recognized text
        # print(f"Recognized: {text}")  # Optional: useful for debugging/logging

    def start(self):
        if not self.listening:
            self.listening = True
            threading.Thread(target=self.recorder.text, args=(self._internal_callback,), daemon=True).start()

    def stop(self):
        if self.listening:
            self.recorder.stop()
            self.listening = False

    def get_last_text(self):
        return self.last_text
    

