import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(value = {"length", "maxPageLimit", "pagination"})
public class CovidResponse {

    @JsonProperty(value = "data")
    private List<CovidData> data;

    public CovidResponse() {

    }

    public CovidResponse(List<CovidData> data) {
        this.data = data;
    }

    public List<CovidData> getData() {
        return data;
    }

    public void setData(List<CovidData> data) {
        this.data = data;
    }
}
