package in.harsh.netflix_recommendation_engine;

import in.harsh.netflix_recommendation_engine.Model.Movie;
import in.harsh.netflix_recommendation_engine.Model.MovieData;
import in.harsh.netflix_recommendation_engine.Model.MovieMatch;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class MovieService {
    private final EmbeddingModel embeddingModel;
    private final JsonMapper jsonMapper;

    private final List<Movie> moviesEmbedding=new ArrayList<>();

    public MovieService(EmbeddingModel embeddingModel, JsonMapper jsonMapper) {
        this.embeddingModel = embeddingModel;
        this.jsonMapper=jsonMapper;
    }

    public List<MovieMatch> search(String query){
        float[] userQueryEmbeddings = embeddingModel.embed(query);

        List<MovieMatch> matches=new ArrayList<>();

        for(Movie movie:moviesEmbedding)
        {
            double similarity=cosineSimilarity(userQueryEmbeddings,movie.getEmbedding());
            MovieMatch match=new MovieMatch(movie.getTitle(), movie.getDescription(),similarity);

            matches.add(match);
        }
        sortBySimilarity(matches);

        return topKMatches(matches,3);
        
    }
    public List<MovieMatch> similarMovies(String title){
        Movie selectedMovies=findMovie(title);
        List<MovieMatch>matches=new ArrayList<>();

        for(Movie movie:moviesEmbedding){
            if(movie.getTitle().equalsIgnoreCase(title)){
                continue;
            }

            double similarity=cosineSimilarity(selectedMovies.getEmbedding(),movie.getEmbedding());

            MovieMatch match=new MovieMatch(movie.getTitle(),movie.getDescription(),similarity);
            matches.add(match);
        }
        sortBySimilarity(matches);
        return topKMatches(matches,3);
    }

    public Movie findMovie(String title){
        for(Movie movie:moviesEmbedding){
            if(movie.getTitle().equalsIgnoreCase(title)){
                return movie;
            }
        }
        throw new IllegalArgumentException("Movie not found"+title);
    }

    @PostConstruct
    public void initializeMovies() throws IOException{
        ClassPathResource resource=new ClassPathResource("movies.json");

        InputStream inputStream=resource.getInputStream();

        //json file converted to Movied Data
        List<MovieData> movieDataList= jsonMapper.readValue(inputStream,new TypeReference<>(){});

        for(MovieData movieData:movieDataList){
            float[] embedding=embeddingModel.embed(movieData.getDescription());
            Movie movie =new Movie(movieData.getTitle(),movieData.getDescription(),embedding);
            moviesEmbedding.add(movie);
        }
        inputStream.close();

        System.out.println(moviesEmbedding.size()+"movies loaded with embedding ");

    }

    public double cosineSimilarity(float[] a,float[] b){
        double dotProduct=0.0;
        double normA=0.0;
        double normB=0.0;

        for(int i=0;i<a.length;i++)
        {
            dotProduct+=(a[i]*b[i]);
            normA+=(a[i]*a[i]);
            normB+=(b[i]*b[i]);
        }

        if(normB==0 || normA==0)
        {
            return 0.0;
        }
        return dotProduct/(Math.sqrt(normB))*(Math.sqrt(normA));
    }

    public void sortBySimilarity(List<MovieMatch> matches){
        Collections.sort(matches,(first, second)->Double.compare(second.getMatch(),first.getMatch()));
    }

    public List<MovieMatch> topKMatches(List<MovieMatch> matches,int limit) {
        List<MovieMatch> topMatches=new ArrayList<>();

        int numberOfMatches=Math.min(limit,matches.size());

        for(int i=0;i<numberOfMatches;i++)
        {
            topMatches.add(matches.get(i));
        }
        return topMatches;
    }
}
