package omdb.client.api;

/**
 * <h2>Obiettivo 1.</h2>
 * Implementare un client che accede alle API ReST disponibili al sito
 * {@link http://www.omdbapi.com/}
 */
public interface OpenMovieClient {
    /**
     * Utilizza l'endpoint di ricerca generico (cfr. § By Search) per trovare i film
     * che contengono il termine di ricerca nel titolo.</br>
     * Per ciascun risultato viene caricato il dettaglio tramite l'endpoint specifico
     * per ID e il tutto viene restituito in un array
     *
     * @param title il termine da cercare
     * @return l'elenco di oggetti con il dettaglio dei film trovati
     */
    public OpenMovie[] listMoviesWithTitleLike(String title);
}
