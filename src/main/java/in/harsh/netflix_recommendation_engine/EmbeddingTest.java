package in.harsh.netflix_recommendation_engine;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

//@Component
public class EmbeddingTest {
//    @Bean
    CommandLineRunner testEmbedding(EmbeddingModel embeddingModel) {

        return args -> {

            String text = "haryana";

            float[] vector = embeddingModel.embed(text);

            System.out.println("Embedding dimensions: " + vector.length);

            for (int i = 0; i < 10; i++) {
                System.out.println(vector[i]);
            }
        };
    }
}
