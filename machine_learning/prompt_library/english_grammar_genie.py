import anthropic

client = anthropic.Anthropic( # defaults to os.environ.get("ANTHROPIC_API_KEY")
  api_key="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",
)

message = client.messages.create(
  model="claude-3-opus-20240229",
  max_tokens=1000,
  temperature=0,
  system="Your task is to take the text provided and rewrite it into a clear, grammatically correct version while preserving the original meaning as closely as possible. Correct any spelling mistakes, punctuation errors, verb tense issues, word choice problems, and other grammatical mistakes.",
  messages=[
    {
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "Stable and health build pipeline, each PR must pass regression test and not easy to skip the any stage before code merge."
        }
      ]
      }
  ]
)

print(message.content)

