"""
At the command line, only need to run once to install the package via pip:

$ pip install google-generativeai
"""

import os
import google.generativeai as genai

GOOGLE_API_KEY = os.getenv('GOOGLE_API_KEY')
genai.configure(api_key=os.getenv('GOOGLE_API_KEY'))

# Set up the model
generation_config = {
  "temperature": 0.9,
  "top_p": 1,
  "top_k": 1,
  "max_output_tokens": 2048,
}

safety_settings = [
  {
    "category": "HARM_CATEGORY_HARASSMENT",
    "threshold": "BLOCK_MEDIUM_AND_ABOVE"
  },
  {
    "category": "HARM_CATEGORY_HATE_SPEECH",
    "threshold": "BLOCK_MEDIUM_AND_ABOVE"
  },
  {
    "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
    "threshold": "BLOCK_MEDIUM_AND_ABOVE"
  },
  {
    "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
    "threshold": "BLOCK_MEDIUM_AND_ABOVE"
  },
]

model = genai.GenerativeModel(model_name="gemini-1.0-pro-001",
                              generation_config=generation_config,
                              safety_settings=safety_settings)

convo = model.start_chat(history=[
  {
    "role": "user",
    "parts": ["Write a story about a magic backpack"]
  },
  {
    "role": "model",
    "parts": ["In the quaint town of Willow Creek, where secrets whispered through the wind, there existed an extraordinary backpack that held unimaginable power. Its worn leather exterior concealed a hidden realm, a magical dimension where anything was possible.\n\nElena, a curious and spirited 12-year-old, stumbled upon the backpack while exploring her grandmother's attic. As she reached out to touch it, a surge of energy coursed through her body. Suddenly, the backpack transformed before her eyes, its buckles gleaming and its straps shimmering with an ethereal glow.\n\nIntrigued, Elena cautiously opened the backpack. Within its velvety depths lay an endless expanse of enchanted space. There were shelves filled with books that contained the knowledge of the universe, a shimmering pool that could transport her to distant lands, and a celestial hourglass that could manipulate time.\n\nOverwhelmed with wonder, Elena embarked on countless adventures with her magic backpack. She journeyed through enchanted forests, soared through the skies on the back of a winged unicorn, and witnessed the birth of stars. Each experience filled her with a profound sense of awe and wonder.\n\nHowever, with great power came great responsibility. Elena soon realized that the backpack's magic could also be dangerous. One careless mistake could unleash its unpredictable forces. Determined to use its powers wisely, Elena sought guidance from her wise and enigmatic grandmother.\n\nTogether, they explored the backpack's secrets and developed a deep understanding of its potential. They learned that the books contained not only knowledge but also ancient spells that could alter the course of events. The pool, while alluring, could be treacherous if not used with caution. And the hourglass, though tempting to control time, could have dire consequences.\n\nAs Elena grew older, she became a guardian of the backpack's secrets. She used its magic to help those in need, to right wrongs, and to inspire hope in others. And so, the legend of the magic backpack was passed down through generations, reminding all who heard it that even in the most ordinary of objects, extraordinary possibilities could be found."]
  },
])

convo.send_message("YOUR_USER_INPUT")
print(convo.last.text)
