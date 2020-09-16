import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

public class CovidData {

    @JsonProperty(value = "date")
    private Date date;

    @JsonProperty(value = "newCasesByPublishDate")
    private int newCasesByPublishDate;

    public CovidData(Date date, int newCasesByPublishDate) {
        this.date = date;
        this.newCasesByPublishDate = newCasesByPublishDate;
    }

    public CovidData() {

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNewCasesByPublishDate() {
        return newCasesByPublishDate;
    }

    public void setNewCasesByPublishDate(int newCasesByPublishDate) {
        this.newCasesByPublishDate = newCasesByPublishDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CovidData covidData = (CovidData) o;
        return newCasesByPublishDate == covidData.newCasesByPublishDate &&
                Objects.equals(date, covidData.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, newCasesByPublishDate);
    }
}
