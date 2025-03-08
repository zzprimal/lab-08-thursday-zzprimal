package com.example.androidcicd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.androidcicd.movie.Movie;
import com.example.androidcicd.movie.MovieProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MovieProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockMovieCollection;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private DocumentReference mockDocRef2;

    private MovieProvider movieProvider;

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("movies")).thenReturn(mockMovieCollection);
        when(mockMovieCollection.document()).thenReturn(mockDocRef);
        when(mockMovieCollection.document(anyString())).thenReturn(mockDocRef);
        // Setup the movie provider
        MovieProvider.setInstanceForTesting(mockFirestore);
        movieProvider = MovieProvider.getInstance(mockFirestore);
    }

    @Test
    public void testAddMovieSetsId() {
        // Movie to add
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);

        // Define the ID we want to set for the movie
        when(mockDocRef.getId()).thenReturn("123");


        // Add movie and check that we update our movie with the generated id
        movieProvider.addMovie(movie);
        assertEquals("Movie was not updated with correct id.", "123", movie.getId());

    /*
        Verify that we called the set method. Normally, this would call the database, but due to what we did in our setup method, this will not actually interact with the database, but the mock does track that the method was called.
    */
        verify(mockDocRef).set(movie);
    }

    @Test
    public void testDeleteMovie() {
        // Create movie and set our id
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");

        // Call the delete movie and verify the firebase delete method was called.
        movieProvider.deleteMovie(movie);
        verify(mockDocRef).delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForDifferentIds() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        // Set our ID to 1
        movie.setId("1");

        // Make sure the doc ref has a different ID
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error
        movieProvider.updateMovie(movie, "Another Title", "Another Genre", 2026);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForEmptyName() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error due to having an empty name
        movieProvider.updateMovie(movie, "", "Another Genre", 2026);
    }

    @Test
    public void testAddDuplicateTitles() {
        // Create movie and set our id
        Movie movie1 = new Movie("Batman", "Action", 2022);
        when(mockDocRef.getId()).thenReturn("123");
        movieProvider.addMovie(movie1);

        Movie movie2 = new Movie("Batman", "Action", 1966);
        when(mockDocRef.getId()).thenReturn("124");
        movieProvider.addMovie(movie2);
        for (Movie movie: movieProvider.getMovies()){
            assertNotEquals("Duplicate movie was added", "124", movie.getId());
        }
    }
}
