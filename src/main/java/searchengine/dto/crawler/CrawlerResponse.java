package searchengine.dto.crawler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlerResponse {

    @JsonIgnore
    private final String[] errors = {
            "Индексация уже запущена",
            "Индексация ещё не запущена",
            ""
    };

    private boolean result;

    private String error;

    public void setError(int index){
        error = errors[index];
    }


}
