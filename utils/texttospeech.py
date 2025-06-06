"""
#############################################################
# File        : texttospeech.py                             #
# Authors     : Shrayanendra Nath Mandal, Preetish Majumdar #
# Date        : 2025-06-01                                  #
# Description : GUI-based Alfred AI Assistant using Gemini  #
#               API, Speech Recognition, and Text to Speech #
#############################################################
"""
import asyncio
import edge_tts

class TextToSpeech:
    def __init__(self, voice="en-GB-RyanNeural"):
        self.voice = voice

    async def speak(self, text: str, output_path: str):
        """Convert text to speech and save it as an audio file."""
        communicate = edge_tts.Communicate(text=text, voice=self.voice)
        await communicate.save(output_path)
        print(f"Speech saved to {output_path}")

    def generate(self, text: str, output_path: str):
        """Wrapper to run the async speak method."""
        asyncio.run(self.speak(text, output_path))
