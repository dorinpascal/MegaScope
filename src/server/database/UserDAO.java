package server.database;

import shared.Movie;
import shared.NewRegisteredUser;

import java.util.ArrayList;

public interface UserDAO {
    NewRegisteredUser validateUser(int id,String username,String password);
    NewRegisteredUser createUser( String firstName, String lastName, String username, String password,String phoneNumber);
    ArrayList<NewRegisteredUser> getAllUsers();
    ArrayList<Movie> getAllMovies();
    NewRegisteredUser saveNewInfo(int id,String firstName, String lastName, String username, String password,String phoneNumber);
    void addMovie(String name, String dateOfRelease, String mainActors, String description, String timeOfShow, String dateOfShow);
    void editMovie(String name, String dateOfRelease, String mainActors, String description, String timeOfShow, String dateOfShow);
    void removeMovie(Movie movie);
}
