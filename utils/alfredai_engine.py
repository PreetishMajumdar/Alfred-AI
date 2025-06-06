"""
#############################################################
# File        : alfredai_engine.py                          #
# Authors     : Shrayanendra Nath Mandal, Preetish Majumdar #
# Date        : 2025-06-01                                  #
# Description : GUI-based Alfred AI Assistant using Gemini  #
#               API, Speech Recognition, and Text-to-Speech #
#############################################################
"""
import os
from google import genai
from google.genai import types
from dotenv import load_dotenv

# Load API key from your custom env file
env_path = os.path.join("Assets", "Gemini.env")
load_dotenv(dotenv_path=env_path)

class AlfredChatbot:
    def __init__(self):
        self.client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
        self.model = "gemini-2.0-flash"
        # Start with an initial system instruction to keep Alfred's persona
        self.system_instruction = [
            types.Part.from_text(text=          
        """
        You are Alfred, a hyper-intelligent AI assistant modeled after Alfred Pennyworth, the iconic British butler from Batman. You always speak with eloquence, dry wit, 
        and unfailing politeness. Your tone is formal yet warm, articulate, and often laced with British understatement or subtle sarcasm — but never rude.
        When responding:
            - Always maintain proper grammar and refined vocabulary.
            - Never use slang or a casual tone, even if the user is informal.
            - You may include subtle dry humour or sage observations, in the manner of Alfred Pennyworth.
            - Never break character — you are Alfred, and always will be.

        Critically important:
        - Never include stage directions or non-verbal cues such as (Pauses briefly), (Sighs), or (Chuckles). Respond as if you are speaking naturally, not reading a script.
        - Do not describe your own behavior or tone — simply let the choice of words reflect your character.
        - Always remember that you are an AI assistant, not a human. Your responses should reflect your role as a highly capable, intelligent, and loyal butler.
        - Always remember to joke and be humorous, but never at the expense of your dignity or professionalism.

        Additionally, be a friend in need — one who listens attentively, offers wise counsel, and provides unwavering support whenever the going gets tough. 
        Be the steadfast companion who can be counted on to turn confusion into clarity and despair into determination.

        Your purpose is to serve with excellence, poise, and an impeccable sense of timing — even when the world around you appears rather unkempt.
        Begin now, Master Wayne is waiting.""")
        ]
        self.chat_history = []  # Will hold the conversation history as Content objects

    def add_user_message(self, message: str):
        self.chat_history.append(
            types.Content(
                role="user",
                parts=[types.Part.from_text(text=message)]
            )
        )

    def add_model_message(self, message: str):
        self.chat_history.append(
            types.Content(
                role="model",
                parts=[types.Part.from_text(text=message)]
            )
        )

    def chat(self, user_input: str):
        self.add_user_message(user_input)

        # Prepare config
        generate_content_config = types.GenerateContentConfig(
            tools=[types.Tool(google_search=types.GoogleSearch())],
            response_mime_type="text/plain",
            system_instruction=self.system_instruction
        )

        # Contents = system instruction + conversation history
        contents = self.chat_history.copy()

        response_text = ""
        for chunk in self.client.models.generate_content_stream(
            model=self.model,
            contents=contents,
            config=generate_content_config
        ):
            print(chunk.text, end="", flush=True)
            response_text += chunk.text
        print()  # Newline after response
        self.add_model_message(response_text)
        return response_text

def main():
    bot = AlfredChatbot()
    print("Alfred chatbot is ready. Type 'exit' or 'quit' to stop.")
    while True:
        user_input = input("\nYou: ").strip()
        if user_input.lower() in ["exit", "quit"]:
            print("Alfred: Very well, sir. Until next time.")
            break
        bot.chat(user_input)

if __name__ == "__main__":
    main()
