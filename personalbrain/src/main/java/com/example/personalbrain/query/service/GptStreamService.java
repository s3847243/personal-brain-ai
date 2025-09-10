package com.example.personalbrain.query.service;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.personalbrain.user.repository.ChatMessageRepository;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GptStreamService {

    @Value("${openai.api-key}")
    private String apiKey;

    private final PromptBuilder promptBuilder;
    private final ChatMessageRepository chatMessageRepo;
    // public void streamResponse(List<String> chunks, String question, SseEmitter emitter) {
    //     OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
    //             .apiKey(apiKey)
    //             .modelName("gpt-3.5-turbo")
    //             .temperature(0.2)
    //             .build();

    //     List<ChatMessage> messages = promptBuilder.buildRagPrompt(chunks, question);

    //     model.generate(messages, new StreamingResponseHandler<AiMessage>() {
    //         @Override
    //         public void onNext(String token) {
    //             try {
    //                 emitter.send(SseEmitter.event().data(token));
    //             } catch (IOException e) {
    //                 emitter.completeWithError(e);
    //             }
    //         }

    //         @Override
    //         public void onError(Throwable t) {
    //             System.err.println("Streaming error: " + t.getMessage());
    //             emitter.completeWithError(t);
    //         }
    //     });
    // }
    public void streamWithMemory(List<com.example.personalbrain.user.model.ChatMessage> history,
                                List<String> contextChunks,
                                String userQuestion,
                                SseEmitter emitter,
                                UUID sessionId) {

        List<ChatMessage> chatPrompt = promptBuilder.buildRagPrompt(history, contextChunks, userQuestion);


        // // System + notes
        // fullPrompt.add(new SystemMessage("You are a helpful assistant based on personal notes."));
        // fullPrompt.add(new UserMessage("Here are some notes:\n" + String.join("\n\n", contextChunks)));

         // Add memory from chat history
        // Add memory from chat history - fixed the conversion
        // for (com.example.personalbrain.user.model.ChatMessage h : history) {
        //     if (h.getRole().equals("user")) {
        //         chatPrompt.add(new UserMessage(h.getContent()));
        //     } else {
        //         chatPrompt.add(new AiMessage(h.getContent()));
        //     }
        // }

        // // New question
        // fullPrompt.add(new UserMessage(userQuestion));
        StringBuilder responseBuilder = new StringBuilder();

        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.1)
                .build();

        model.generate(chatPrompt, new StreamingResponseHandler<AiMessage>(){
            @Override
            public void onNext(String token) {
                responseBuilder.append(token);
                try {
                    emitter.send(SseEmitter.event().name("token").data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            // @Override
            // public void onComplete(Response<AiMessage> response) {
            //     emitter.complete();

            //     // Save assistant reply
            //     chatMessageRepo.save(com.example.personalbrain.user.model.ChatMessage.builder()
            //             .sessionId(sessionId)
            //             .role("assistant")
            //             .content(responseBuilder.toString())
            //             .createdAt(LocalDateTime.now())
            //             .build());
            // }
            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    // 1) Persist assistant message
                    var saved = chatMessageRepo.save(
                        com.example.personalbrain.user.model.ChatMessage.builder()
                            .sessionId(sessionId)
                            .role("assistant")
                            .content(responseBuilder.toString())
                            .createdAt(LocalDateTime.now())
                            .build()
                    );

                    // 2) Tell the client we saved it (frontend replaces temp-assistant with this)
                    emitter.send(SseEmitter.event()
                        .name("assistant-saved")
                        .data(Map.of(
                            "id", saved.getId().toString(),
                            "role", "assistant",
                            "content", saved.getContent(),
                            "timestamp", saved.getCreatedAt().toString()
                        ))
                    );

                    // 3) Final marker so client can close the stream
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));

                } catch (IOException io) {
                    emitter.completeWithError(io);
                    return; // don’t fall through
                } catch (Exception e) {
                    // If saving failed, surface it to the client before closing
                    try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); } catch (IOException ignored) {}
                    emitter.completeWithError(e);
                    return;
                }

                // 4) Close the SSE
                emitter.complete();
            }

            // @Override
            // public void onError(Throwable t) {
            //     emitter.completeWithError(t);
            // }
            @Override
            public void onError(Throwable t) {
                try { emitter.send(SseEmitter.event().name("error").data(t.getMessage())); } catch (IOException ignored) {}
                emitter.completeWithError(t);
            }
        });
    }
}