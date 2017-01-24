package com.asix.pixeldailieswidget;

import java.io.Serializable;

public class PDItem implements Serializable {

    private String pdTheme = "";
    private String pdDate = "";

    public PDItem(String theme, String date){
        pdDate = date;
        pdTheme = theme;
    }

    public String getPdTheme() {
        return pdTheme;
    }

    public void setPdTheme(String pdTheme) {
        this.pdTheme = pdTheme;
    }

    public String getPdDate() {
        return pdDate;
    }

    public void setPdDate(String pdDate) {
        this.pdDate = pdDate;
    }
}
