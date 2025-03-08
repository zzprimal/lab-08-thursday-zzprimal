package com.example.androidcicd.movie;

import android.widget.EditText;

import com.example.androidcicd.MainActivity;
import com.example.androidcicd.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                System.out.println("HAHAHHAHAHAHHA2");
                //throw new IllegalArgumentException("HAHAHHAHAHAHHA2");
                return;
            }
            movies.clear();
            if (snapshot != null) {
                for (QueryDocumentSnapshot item : snapshot) {
                    item.getData();
                    Map<String, Object> map = item.getData();
                    Movie movie = new Movie((String) map.get("Title"), (String) map.get("Genre"), ((Long) map.get("Year")).intValue());
                    movie.setId((String) map.get("Id"));
                    movies.add(movie);
                }
                dataStatus.onDataUpdated();
            }
            //throw new IllegalArgumentException("HAHAHHAHAHAHHA");
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

    public void updateMovie(Movie movie, String title, String genre, int year) {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("Title", title);
        data1.put("Genre", genre);
        data1.put("Year", year);
        data1.put("Id", movie.getId());
        DocumentReference docRef = movieCollection.document(movie.getId());
        Query queryByTitle = movieCollection.whereEqualTo("Title", movie.getTitle());
        if (queryByTitle == null){
            if (validMovie(movie, docRef)){
                docRef.set(data1);
            }
            else{
                throw new IllegalArgumentException("Invalid Movie!");
            }
        }
        else{
            queryByTitle.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                if (!task.getResult().isEmpty() && !title.equals(movie.getTitle())){
                    EditText titletext = MovieDialogFragment.dialog.findViewById(R.id.edit_title);
                    titletext.setError("Error: movie title already exists");
                }
                else{
                    movie.setTitle(title);
                    movie.setGenre(genre);
                    movie.setYear(year);
                    if (validMovie(movie, docRef)) {
                        docRef.set(data1);
                    } else {
                        throw new IllegalArgumentException("Invalid Movie!");
                    }
                    MovieDialogFragment.dialog.dismiss();
                }
            }
            });
        }

    }

    public void addMovie(Movie movie) {
        Map<String, Object> data1 = new HashMap<>();
        DocumentReference docRef = movieCollection.document();
        movie.setId(docRef.getId());
        data1.put("Title", movie.getTitle());
        data1.put("Genre", movie.getGenre());
        data1.put("Year", movie.getYear());
        data1.put("Id", movie.getId());
        Query queryByTitle = movieCollection.whereEqualTo("Title", movie.getTitle());
        if (queryByTitle == null){
            if (validMovie(movie, docRef)){
                docRef.set(data1);
            }
            else{
                throw new IllegalArgumentException("Invalid Movie!");
            }
        }
        else{
            queryByTitle.get().addOnCompleteListener(task -> {
                System.out.println("HAHAHHAHAHAHHA2");
            if (task.isSuccessful()){
                if (!task.getResult().isEmpty()){
                    EditText titletext = MovieDialogFragment.dialog.findViewById(R.id.edit_title);
                    titletext.setError("Error: movie title already exists");
                }
                else{
                    if (validMovie(movie, docRef)) {
                        docRef.set(data1);
                    } else {
                        throw new IllegalArgumentException("Invalid Movie!");
                    }
                    MovieDialogFragment.dialog.dismiss();
                }
            }
            else{
                System.out.println("HAHAHHAHAHAHHA4");
            }
            });
            System.out.println("HAHAHHAHAHAHHA3");
        }
        /*
        if (validMovie(movie, docRef)) {
            docRef.set(movie);
        } else {
            throw new IllegalArgumentException("Invalid Movie!");
        }

         */
    }

    public void deleteMovie(Movie movie) {
        DocumentReference docRef = movieCollection.document(movie.getId());
        docRef.delete();
    }

    public boolean validMovie(Movie movie, DocumentReference docRef) {
        return movie.getId().equals(docRef.getId()) && !movie.getTitle().isEmpty() && !movie.getGenre().isEmpty() && movie.getYear() > 0;
    }
}