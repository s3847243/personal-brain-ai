package com.example.personalbrain.query.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PromptBuilder {

    // public List<ChatMessage> buildRagPrompt(List<String> chunks, String question) {
    //     String system = "You are a helpful assistant answering questions from the user's personal notes.";
    //     String context = String.join("\n\n", chunks);
    //     String user = String.format("Here are some notes:\n%s\n\nAnswer the question: \"%s\"", context, question);

    //     return Arrays.asList(
    //             new SystemMessage(system),
    //             new UserMessage(user)
    //     );
    // }
    public List<ChatMessage> buildRagPrompt(
        List<com.example.personalbrain.user.model.ChatMessage> history,
        List<String> chunks,
        String question
    ) {
        String system = """
            You are a helpful assistant that MUST answer using the provided context chunks from the user's notes.
            - If the answer is not in the context, say you don't know.
            - Quote exact lines when possible and be concise.
            """;

        // (optional) trim long histories to last N messages
        int MAX_HISTORY = 10;
        var trimmed = history.size() > MAX_HISTORY
                ? history.subList(history.size() - MAX_HISTORY, history.size())
                : history;

        List<ChatMessage> prompt = new ArrayList<>();
        prompt.add(new SystemMessage(system));

        // Add prior conversation in order
        for (var h : trimmed) {
            if ("user".equals(h.getRole())) prompt.add(new UserMessage(h.getContent()));
            else                            prompt.add(new AiMessage(h.getContent()));
        }

        // Build RAG context
        StringBuilder ctx = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            ctx.append("### Chunk ").append(i + 1).append("\n").append(chunks.get(i)).append("\n\n");
        }

        // Latest user turn: context + question
        String user = "Use ONLY the following context to answer.\n\n"
                + ctx + "\nQuestion: " + question
                + "\n\nIf the answer is not in the context, say so explicitly.";
        prompt.add(new UserMessage(user));

        return prompt;
    }
}