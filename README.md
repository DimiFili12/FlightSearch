
Project description:    
In this project, you'll build the Flight Search app in which users enter an airport and can view a list of destinations using that airport as a departure. This project gives you the opportunity to practice your skills with SQL, Room, and DataStore by offering you a set of app requirements that you must fulfill. In particular, you need the Flight Search app to meet the following requirements:

1. Provide a text field for the user to enter an airport name or International Air Transport Association (IATA) airport identifier.
2. Query the database to provide autocomplete suggestions as the user types.
3. When the user chooses a suggestion, generate a list of available flights from that airport, including the IATA identifier and airport name to other airports in the database.
4. Let the user save favorite individual routes.
5. When no search query is entered, display all the user-selected favorite routes in a list.
6. Save the search text with Preferences DataStore. When the user reopens the app, the search text, if any, needs to prepopulate the text field with appropriate results from the database.
    
The data for this app comes from the flights database.     
(github.com/google-developer-training/android-basics-kotlin-sql-basics-app/blob/project/flight_search.db)    
The flights database contains two tables, airport and favorite.
You can use the airport table to search for airports and build a list of potential flights. You use the favorite table, which is initially empty, to save pairs of departure and arrival destinations selected by the user.
    
Plan your UI:    
When the user first opens the app, they see an empty screen with a text field, prompting for an airport.    
When the user starts typing, the app displays a list of autocomplete suggestions that match either the airport name or identifier.    
When the user selects a suggestion, the app displays a list of all possible flights from that airport. Each item includes the identifier and names for both airports, and a button to save the destination as a favorite.     
When the user clears the search box or does not enter a search query, the app displays a list of saved favorite destinations, if any exist.    

Use Room to integrate the flights database:    
In order to implement the features above, you need to leverage your knowledge of SQL and Room. The database already consists of two tables, airport and favorite, and you need entities for each one. Select the appropriate Kotlin data types so that you can access the values in each table.
Additionally, you need to consider the following requirements when querying the flights database and persisting data:
1. Search for autocomplete suggestions in the airport table. Keep in mind that the user might already know the airport code, so you need to check their input against the iata_code column, in addition to the name column, when searching for text. Remember that you can use the LIKE keyword to perform text searches.
2. Show more frequently visited airports in descending order by sorting on the passengers column.
3. Assume that every airport has flights to every other airport in the database (except for itself).
4. When no text is in the search box, display a list of favorite flights, showing the departure and destination. As the favorite table only includes columns for the airport codes, you're not expected to show the airport names in this list.
(Comment: The app shows both airport names and airport codes in the favorite list)
    
Persist user state with Preferences DataStore:    
In addition to SQL and Room, you also know how to persist individual values like user settings. For the Flight Search app, you need to store the user's search string in Preferences DataStore so that it populates when the user relaunches the app. If the text field is empty when the user exits the app, then the list of favorite flights needs to display instead.
    
