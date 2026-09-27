package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;

import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.entities.enums.Period;

@JsonTypeName("MAGAZINE")
public class Magazine extends PhysicalItem {
    private String issn;
    private Period period;

    public Magazine() {
        //this is for jackson
    }
    public Magazine(String title, String author, String category, ItemType type,
                    int totalCopies, String publishYear, String issn, Period period) {
        super(title, author, category, type, totalCopies, publishYear);
        this.issn = issn;
        this.period = period;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return super.toString() + " [issn=" + issn + ", period=" + period + "]";
    }
}
