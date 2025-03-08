package com.example.androidcicd.movie;

import android.widget.EditText;

import com.example.androidcicd.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MovieProvider {
    private static MovieProvider movieProvider;
    private final ArrayList<Movie> movies;
    private final CollectionReference movieCollection;

    private MovieProvider(FirebaseFirestore firestore) {
        movies = new ArrayList<>();
        movieCollection = firestore.collection("movies");
    }

    public interface DataStatus {
        void onDataUpdated();
        void onError(String error);
    }

    public void listenForUpdates(final DataStatus dataStatus) {
        movieCollection.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                dataStatus.onError(error.getMessage());
                return;
            }
            movies.clear();
            if (snapshot != null) {
                List<String> title_saw = new ArrayList<>();
                for (QueryDocumentSnapshot item : snapshot) {
                    Movie movie = item.toObject(Movie.class);
                    title_saw.add(movie.getTitle());
                    movies.add(item.toObject(Movie.class));
                }
                dataStatus.onDataUpdated();
            }
        });
    }

    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        movieProvider = new MovieProvider(firestore);
    }

    public static MovieProvider getInstance(FirebaseFirestore firestore) {
        if (movieProvider == null)
            movieProvider = new MovieProvider(firestore);
        return movieProvider;
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public boolean updateMovie(Movie movie, String title, String genre, int year) {
        if (!title.equals(movie.getTitle())){
            for (Movie movie_element: movies){
                if (movie_element.getTitle().equals(title)){
                    return false;
                }
            }
        }
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setYear(year);
        DocumentReference docRef = movieCollection.document(movie.getId());
        if (validMovie(movie, docRef)) {
            docRef.set(movie);
        } else {
            throw new IllegalArgumentException("Invalid Movie!");
        }
        return true;
    }

    public boolean addMovie(Movie movie) {
        for (Movie movie_element: movies){
            if (movie_element.getTitle().equals(movie.getTitle())){
                return false;
            }
        }
        DocumentReference docRef = movieCollection.document();
        //Query queryByProductName = movieCollection.whereEqualTo("Title", movie.getTitle());
        movie.setId(docRef.getId());
        if (validMovie(movie, docRef)) {
            docRef.set(movie);
        } else {
            throw new IllegalArgumentException("Invalid Movie!");
        }
        return true;
    }

    public void deleteMovie(Movie movie) {
        DocumentReference docRef = movieCollection.document(movie.getId());
        docRef.delete();
    }

    public boolean validMovie(Movie movie, DocumentReference docRef) {
        return movie.getId().equals(docRef.getId()) && !movie.getTitle().isEmpty() && !movie.getGenre().isEmpty() && movie.getYear() > 0;
    }
}