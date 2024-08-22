package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChefService {
    private static final Logger log = LoggerFactory.getLogger(ChefService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/chef")
    private Resource chef;
    @Value("classpath:/prompts/chefCookBook")
    private Resource chefCookBook;

    public ChefService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }


    public String answerCustomerQuestion(String question) {
        log.info("Answering question from Chat");
        var advisorSearchRequest = SearchRequest.query(question);
        var advise = new PromptTemplate(chefCookBook).getTemplate();
        return chatClient.prompt()
                        .system(chef)
                        .user(question)
                        .advisors(new QuestionAnswerAdvisor(vectorStore, advisorSearchRequest, advise))
                        .call()
                        .content();
    }

}
