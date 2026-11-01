# Elanco Tick Sighting Backend

**Mahbobullah Wahidy**  
Computer Science student, Brunel University London

This is my backend project for the Elanco placement technical task. It gets tick-sighting records from Elanco's API, checks the data, and provides endpoints for searching sightings and viewing basic statistics.

I used **Java 17, Spring Boot and Maven**. I chose Java because it's the language I've used most at university. Spring Boot was new to me, but I wanted to use it to build the API endpoints in Java.

## What the backend does

- Downloads data from `https://dev-task.elancoapps.com/data/tick-sightings`.
- Skips records with missing information or invalid dates.
- Checks IDs so duplicate records aren't added twice.
- Lets users search by location and filter by a start and end date.
- Returns results in pages, so a request doesn't have to return every sighting at once.
- Counts sightings by UK region and by month.
- Handles invalid search inputs and problems connecting to Elanco's API.

## How I organised the code

- `TickTrackerApplication.java` starts the Spring Boot application.
- `TickSighting.java` stores the details of one sighting.
- `TickService.java` retrieves the JSON data and removes invalid or duplicate records.
- `TickController.java` handles the search requests and statistics endpoints.

## How to run it

1. Install **JDK 17 or newer** and open the project in **IntelliJ IDEA**.
2. Open the folder containing `pom.xml` as a Maven project, and let IntelliJ load the dependencies.
3. Open `TickTrackerApplication.java` in `src/main/java/com/example/ticktracker`.
4. Click the green **Run** button next to the `main()` method.
5. When the console shows that the application has started on port **8080**, open one of the URLs below in your browser.

You can also start the application with `mvn spring-boot:run` if Maven is installed on your computer.

## API endpoints

| Endpoint | What it returns |
| --- | --- |
| `GET /api/sightings` | Tick sightings, with 50 results per page by default |
| `GET /api/sightings?location=London` | Sightings matching London |
| `GET /api/sightings?from=2024-01-01&to=2024-12-31` | Sightings within the chosen dates |
| `GET /api/sightings?page=0&size=5` | The first five matching sightings |
| `GET /api/stats/regions` | Number of sightings in each UK region |
| `GET /api/stats/monthly` | Number of sightings in each month |

Add `http://localhost:8080` before the endpoint when testing it in a browser.

The search endpoint accepts these optional parameters:

- `location`: part or all of a city name (not case-sensitive).
- `from` and `to`: dates written as `YYYY-MM-DD`.
- `page`: starts at `0`.
- `size`: number of results per page, from `1` to `100` (default `50`).

The search response includes `totalSightingsMatched`, `page`, `pageSize`, and `sightings`.

## Data handling and errors

When the data is downloaded, `TickService` goes through each record. It checks the required fields and the date, then skips repeated IDs. Valid records are stored as `TickSighting` objects.

I added a five-minute cache so the backend doesn't download the same data for every request. If Elanco's API fails, it can return previously downloaded data. If no data is available, it returns HTTP **503**. Invalid dates and invalid page values return HTTP **400**.

## What I'd improve with more time

- Improve the city-to-region matching. At the moment, known cities are mapped manually and anything else is counted under `Other / Unmapped`.
- Add automated tests for more situations, including unusual API responses.
- Look into storing the records in a database instead of keeping them only in memory, especially for much larger datasets.

This project focuses on the backend, so there isn't a frontend or interactive map. The AI/ML extension wasn't included.
