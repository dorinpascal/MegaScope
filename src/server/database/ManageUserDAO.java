package server.database;

import shared.*;

import java.sql.*;
import java.util.ArrayList;

public class ManageUserDAO implements UserDAO {

    private Controller controller;
    private static ManageUserDAO instance;

    /**
     * Private zero-argument constructor (Singleton class).
     */
    private ManageUserDAO() {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        controller = Controller.getInstance();
    }

    /**
     * Receive the singleton instance of {@link ManageUserDAO}.
     *
     * @return
     */
    public static synchronized ManageUserDAO getInstance() {
        if (instance == null) {
            instance = new ManageUserDAO();
        }
        return instance;
    }

    /**
     * Receive all {@link Show} objects from database and add to the {@link ShowsList} object from parameter.
     *
     * @param showList
     * @param connection
     * @throws SQLException
     */
    private void getMovieList(ShowsList showList, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select movies.id, name, dateofrelease, mainactors," +
                " description, time_show, date_show, show.id from public.movies join public.show on show.movie_id = movies.id order by movies.id");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Show temp = new Show(resultSet.getInt(1), resultSet.getString(2),
                    resultSet.getString(3), resultSet.getString(4),
                    resultSet.getString(5), resultSet.getString(6),
                    resultSet.getString(7), resultSet.getInt(8));
            showList.addShow(temp);
        }
        statement.close();
    }

    /**
     * Return {@link ShowsList} object from retrieved {@link Show} objects received from database.
     *
     * @return
     */
    @Override
    public ShowsList getAllMovies() {
        ShowsList showList = new ShowsList();
        try (Connection connection = controller.getConnection()) {
            getMovieList(showList, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showList;
    }


    @Override
    public ShowsList getAllMoviesUnique() {
        ShowsList showList = new ShowsList();
        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select distinct movies.id, name, dateofrelease, mainactors," +
                    " description from public.movies join public.show on show.movie_id = movies.id order by movies.id");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Show temp = new Show(resultSet.getInt(1), resultSet.getString(2),
                        resultSet.getString(3), resultSet.getString(4),
                        resultSet.getString(5), null,
                        null, 0);
                showList.addShow(temp);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showList;
    }

    /**
     * Insert {@link Show} object from parameter into database and return all {@link Show} objects from database thereafter.
     *
     * @param show
     * @return
     */
    @Override
    public ShowsList addMovie(Show show) {

        ShowsList showList = new ShowsList();
        PreparedStatement statement;
        try (Connection connection = controller.getConnection()) {
            statement = connection.prepareStatement(
                    "INSERT INTO public.movies (id, name, dateofrelease, mainactors, description)" +
                            "VALUES (" + "DEFAULT" + ",'"
                            + show.getName() + "','" + show.getDateOfRelease() + "','"
                            + show.getMainActors() + "','" + show.getDescription() + "') ON CONFLICT DO NOTHING");
            statement.executeUpdate();

            int id = 0;
            statement = connection.prepareStatement(
                    "select movies.id from movies where name='" + show.getName() + "'");

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                id = resultSet.getInt(1);
            }

            statement = connection.prepareStatement(
                    "INSERT INTO public.show (id, movie_id, time_show, date_show)" +
                            "VALUES (" + "DEFAULT" + ",'"
                            + id + "','" + show.getTimeOfShow() + "','"
                            + show.getDateOfShow() + "')");

            statement.executeUpdate();
            statement.close();
            getMovieList(showList, connection);
            return showList;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Alter {@link Show} object inside database and return all {@link Show} objects from database in a {@link ShowsList} object.
     *
     * @param show
     * @return
     */
    @Override
    public ShowsList editMovie(Show show) {
        ShowsList showList = new ShowsList();
        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE public.movies SET name='" + show.getName() + "',dateofrelease='" +
                            show.getDateOfRelease() + "',mainactors='" + show.getMainActors() +
                            "',description='" + show.getDescription() +
                            " 'where id='" + show.getMovie_id() + "'");

            statement.executeUpdate();

            statement = connection.prepareStatement(
                    "UPDATE public.show SET time_show='" + show.getTimeOfShow() + "',date_show='" + show.getDateOfShow() +
                            "' where id='" + show.getShow_id() + "'");
            statement.executeUpdate();
            statement.close();

            getMovieList(showList, connection);
            return showList;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Remove {@link Show} from database which is equal to one in the database. Then return a {@link ShowsList} object of all {@link Show} objects from database.
     *
     * @param show
     * @return
     */
    @Override
    public ShowsList removeMovie(Show show) {
        ShowsList showList = new ShowsList();
        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "Delete from show where id='" + show.getShow_id() + "'");
            statement.executeUpdate();
            System.out.println("Hi");
            statement = connection.prepareStatement(
                    " DELETE FROM movies WHERE NOT EXISTS(SELECT FROM show WHERE show.movie_id = movies.id)");
            System.out.println("Ho");
            statement.executeUpdate();
            statement.close();

            getMovieList(showList, connection);
            return showList;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    /**
     * Return reservations which contained the {@link Show} object from the parameter.
     *
     * @param show
     * @return
     */
    @Override
    public ArrayList<String> getReservations(Show show) {
        ArrayList<String> strings = new ArrayList<>();

        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT seat_id FROM public.seats WHERE blocked=true");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                strings.add(String.valueOf(resultSet.getInt(1)));
            }
            System.out.println(strings.size());

            if (show != null) {
                statement = connection.prepareStatement("SELECT * FROM public.reservations WHERE show_id='" +
                        show.getShow_id() + "'");

                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    strings.add(resultSet.getString(4));
                }
            } else {
                System.out.println("Null show");
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return strings;
    }

    @Override
    public ReservationList reserveMovie(ReservationList list) {
        ReservationList reservations = new ReservationList();
        PreparedStatement statement = null;
        Reservation temp;

        try (Connection connection = controller.getConnection()) {

            for (int i = 0; i < list.size(); i++) {

                statement = connection.prepareStatement(
                        "INSERT INTO public.reservations (reservation_id,seat_id,show_id ,user_id)"
                                + "VALUES (" + "DEFAULT" + ",'" + list.get(i).getSeat_no() + "','" + list.get(i).getShow_id()
                                + "','" + list.get(i).getUser_id() + "')");

                statement.executeUpdate();
            }
            statement = connection.prepareStatement(
                    "SELECT * FROM public.reservations WHERE show_id='" + list.get(0).getShow_id() + "'");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                temp = new Reservation(resultSet.getInt(1),
                        resultSet.getInt(4), resultSet.getInt(3),
                        resultSet.getInt(2));
                reservations.add(temp);
            }
            statement.close();
            return reservations;
        } catch (SQLException throwable) {
            if (throwable.toString().contains("duplicate key")) {
                reservations.setFailed(true);
                try (Connection connection = controller.getConnection()) {

                    statement = connection.prepareStatement(
                            "SELECT * FROM public.reservations WHERE show_id='" + list.get(0).getShow_id() + "'");
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        temp = new Reservation(resultSet.getInt(1),
                                resultSet.getInt(4), resultSet.getInt(3),
                                resultSet.getInt(2));
                        System.out.println(temp);
                        reservations.add(temp);
                    }
                    statement.close();
                    return reservations;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
            throwable.printStackTrace();
            System.out.println(reservations.isFailed());
            return reservations;
        }


    }

    @Override
    public ArrayList<UserReservationInfo> cancelReservation(UserReservationInfo userReservationInfo) {
        User user = null;
        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT user_id FROM reservations WHERE reservation_id = " + userReservationInfo.getReservation_id());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                statement = connection.prepareStatement(
                        "SELECT * FROM users WHERE id = " + resultSet.getInt(1));
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    user = new User(resultSet.getInt(1), resultSet.getString(2),
                            resultSet.getString(3), resultSet.getString(4),
                            resultSet.getString(5), resultSet.getString(6),
                            resultSet.getString(7), resultSet.getBoolean(8));
                }
            }
            statement.close();

            statement = connection.prepareStatement(
                    "DELETE FROM reservations WHERE reservation_id=" + userReservationInfo.getReservation_id());
            statement.executeUpdate();
            statement.close();

            return getUserReservation(user);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public SeatList adminConfirmSeats(SeatList seatList) {
        SeatList list = new SeatList();
        PreparedStatement statement;
        try (Connection connection = controller.getConnection()) {
            for (int i = 0; i < seatList.size(); i++) {
                statement = connection.prepareStatement("UPDATE public.seats SET blocked =" +
                        seatList.get(i).isDisabled() + "  WHERE seat_id = " + seatList.get(i).getId());
                statement.executeUpdate();
            }

            statement = connection.prepareStatement("SELECT * FROM public.seats ORDER by seat_id");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Seat temp = new Seat(resultSet.getInt(1), resultSet.getBoolean(2));
                list.add(temp);
            }
            statement.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SeatList getAdminSeats() {
        SeatList seatList = new SeatList();
        PreparedStatement statement;
        try (Connection connection = controller.getConnection()) {
            statement = connection.prepareStatement("SELECT * FROM public.seats ORDER by seat_id");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Seat temp = new Seat(resultSet.getInt(1), resultSet.getBoolean(2));
                seatList.add(temp);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seatList;
    }


    @Override
    public ArrayList<UserReservationInfo> getUserReservation(User user) {
        PreparedStatement statement;
        ArrayList<UserReservationInfo> userReservations = new ArrayList<>();
        UserReservationInfo temp;

        try (Connection connection = controller.getConnection()) {
            statement = connection.prepareStatement(
                    "SELECT reservations.reservation_id, movies.name, show.time_show, show.date_show, reservations.seat_id " +
                            "FROM ((reservations " +
                            "INNER JOIN show ON reservations.show_id = show.id) " +
                            "INNER JOIN movies ON show.movie_id = movies.id) " +
                            "WHERE user_id = " + user.getId() + ";");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                temp = new UserReservationInfo(resultSet.getInt(1), resultSet.getString(2),
                        resultSet.getString(3), resultSet.getString(4),
                        resultSet.getInt(5));
                userReservations.add(temp);
            }
            statement.close();
            System.out.println(userReservations);
            return userReservations;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public UserList getAllUsers() {
        UserList users = new UserList();
        PreparedStatement statement = null;
        try (Connection connection = controller.getConnection()) {
            statement = connection.prepareStatement("SELECT * FROM public.users WHERE type ='USER' or type='VIP' Order by id");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User temp = new User(resultSet.getInt(1),
                        resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5),
                        resultSet.getString(6), resultSet.getString(7),
                        resultSet.getBoolean(8));
                users.add(temp);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public UserList changeUserStatus(User user) {
        UserList users = new UserList();
        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE public.users SET firstname='" + user.getFirstName() + "',lastname='"
                            + user.getLastName() + "',username='" + user.getUsername() + "',password='" + user.getPassword()
                            + "',phonenumber='" + user.getPhoneNumber() + "',type='" + user.getUserType() + "',banned='" + user.getBanned()
                            + "' where id=" + user.getId() + "");

            statement.executeUpdate();
            statement.close();
            statement = connection.prepareStatement(
                    "SELECT * FROM public.users WHERE type ='USER' or type='VIP' Order by id");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User temp = new User(resultSet.getInt(1),
                        resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5),
                        resultSet.getString(6), resultSet.getString(7),
                        resultSet.getBoolean(8));
                users.add(temp);
            }
        } catch (SQLException throwable) {
            if (throwable.toString().contains("duplicate key")) {
                System.out.println("same username");
            }
        }
        return users;
    }


    @Override
    public User registerUser(User user) {
        try (Connection connection = controller.getConnection()) {

            User temp;
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM public.users WHERE username='" + user.getUsername() + "'");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                temp = new User(resultSet.getInt(1),
                        resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5),
                        resultSet.getString(6), resultSet.getString(7),
                        resultSet.getBoolean(8));
                user = temp;

            }
            statement = connection.prepareStatement(

                    "INSERT INTO public.users(firstname,lastname,username,password,phonenumber,type) VALUES ('" + user.getFirstName() + "','" + user.getLastName()
                            + "','" + user.getUsername() + "','" + user.getPassword() + "','" + user.getPhoneNumber() + "','" + "USER" + "')");

            statement.executeUpdate();

            statement.close();
        } catch (SQLException throwable) {
            if (throwable.toString().contains("duplicate key")) {
                System.out.println("Register Fail");
            }
            throwable.printStackTrace();
            return null;
        }
        return user;


    }


    @Override
    public User validateUser(int id, String username, String password) {
        User user = null;
        PreparedStatement statement;
        try (Connection connection = controller.getConnection()) {
            statement = connection.prepareStatement("SELECT password FROM public.users WHERE username='" + username + "'");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getString(1).equals(password)) {
                    statement = connection.prepareStatement(
                            "SELECT * FROM public.users WHERE username='" + username + "'");
                    resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        User temp = new User(resultSet.getInt(1),
                                resultSet.getString(2), resultSet.getString(3),
                                resultSet.getString(4), resultSet.getString(5),
                                resultSet.getString(6), resultSet.getString(7),
                                resultSet.getBoolean(8));
                        user = temp;
                    }
                    return user;
                }
            }
            statement.close();
        } catch (SQLException throwable) {
            if (throwable.toString().contains("duplicate key")) {
                System.out.println("Login fail");
            }
            throwable.printStackTrace();
            return null;
        }
        return user;
    }

    @Override
    public User saveNewInfo(User user) {
        User temp = null;
        try (Connection connection = controller.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE public.users SET firstname='" + user.getFirstName() + "',lastname='"
                            + user.getLastName() + "',username='" + user.getUsername() + "',password='" + user.getPassword()
                            + "',phonenumber='" + user.getPhoneNumber() + "',type='" + user.getUserType() + "',banned='" + user.getBanned()
                            + "' where id=" + user.getId() + "");

            statement.executeUpdate();
            statement = connection.prepareStatement(
                    "SELECT * FROM public.users WHERE id='" + user.getId() + "'");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                temp = new User(resultSet.getInt(1),
                        resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5),
                        resultSet.getString(6), resultSet.getString(7),
                        resultSet.getBoolean(8));
                System.out.println(temp);
            }
            statement.close();
            return temp;
        } catch (SQLException throwable) {
            if (throwable.toString().contains("duplicate key")) {
                System.out.println("same username");
            }
            return null;
        }
    }
}
