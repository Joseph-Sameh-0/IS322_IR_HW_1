package invertedIndex;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 * @author ehab
 */
public class SourceRecord {
    public int fid;
    public String URL;
    public String title;
    public String text;
    public Double norm;
    public int length;

    public String getURL() {
        return URL;
    }

    public SourceRecord(int fid, String URL, String title, int length, Double norm, String text) {
        this.fid = fid;
        this.URL = URL;
        this.title = title;
        this.text = text;
        this.norm = norm;
        this.length = length;
    }

    public SourceRecord(int fid, String URL, String title, String text) {
        this.fid = fid;
        this.URL = URL;
        this.title = title;
        this.text = text;
        this.norm = 0.0;
        this.length = 0;
    }
}
