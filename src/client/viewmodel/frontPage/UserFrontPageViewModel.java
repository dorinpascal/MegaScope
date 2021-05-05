package client.viewmodel.frontPage;

import client.model.UserModel;
import com.sun.javafx.scene.control.TabObservableList;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import shared.Movie;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserFrontPageViewModel
{

  private UserModel model;
  private PropertyChangeSupport support;

  private StringProperty username, button;
  private StringProperty searchPhrase;
  private Property<ObservableList<Movie>> observableItems;
  private ObservableList<Movie> items;

  public UserFrontPageViewModel(UserModel model)
  {
    this.model = model;
    support = new PropertyChangeSupport(this);
    username = new SimpleStringProperty();
    button = new SimpleStringProperty();
    searchPhrase = new SimpleStringProperty();
    items = new SimpleListProperty<>();
    observableItems = new SimpleListProperty<>();

    System.out.println("start");
    model.addPropertyChangeListener("Movie Result", this::onGetMovies);
    System.out.println("Koniec");

  }

  public void onGetMovies(PropertyChangeEvent event)
  {
    System.out.println(6);
    List<Movie> list = (ArrayList<Movie>) event.getNewValue();

    ObservableList<Movie> observableList = FXCollections.observableArrayList();
    observableList.addAll(list);
    System.out.println("Kappa " + observableList.size());
    observableItems.setValue(observableList);
    System.out.println("LOLO " + observableItems.getValue().toString());
    items = FXCollections.observableArrayList(list);
    //support.firePropertyChange("Update", null, null);
  }

  public StringProperty usernameProperty()
  {
    return username;
  }

  public StringProperty buttonProperty()
  {
    return button;
  }

  public void getMovies()
  {
    model.getMovies();
  }

  public ObservableList<Movie> getItems()
  {
    return items;
  }

  public void addPropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(name, listener);
  }

  public void close()
  {
    model.deactivateClient();
  }

  public Property<ObservableList<Movie>> observableItemsProperty()
  {
    return observableItems;
  }

  public StringProperty searchPhraseProperty()
  {
    return searchPhrase;
  }

  public void search()
  {
    if (searchPhrase.getValue() == null || searchPhrase.getValue().equals(""))
    {
      System.out.println("Please");
      getMovies();
    }
    else
    {
      ObservableList<Movie> observableList = FXCollections
          .observableArrayList();
      for (int i = 0; i < observableItems.getValue().size(); i++)
      {
        System.out.println(observableItems.getValue().get(i).getName());
        System.out.println(searchPhrase.toString());
        if (observableItems.getValue().get(i).getName()
            .contains(searchPhrase.getValue()))
        {
          observableList.add(observableItems.getValue().get(i));
        }
      }
      observableItems.setValue(observableList);
    }
  }
}
