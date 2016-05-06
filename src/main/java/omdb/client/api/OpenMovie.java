package omdb.client.api;

/**
 * Espone i valori presenti nella risposta json delle API
 */
public interface OpenMovie {
    public String title();
    public String year();
    public String rated();
    public String released();
    public String runtime();
    public String genre();
    public String director();
    public String writer();
    public String actors();
    public String plot();
    public String language();
    public String country();
    public String awards();
    public String poster();
    public String type();
}
