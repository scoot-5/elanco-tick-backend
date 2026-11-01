
package com.example.ticktracker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api")
public class TickController {

    private final TickService tickService;

    public TickController(TickService tickService) {
        this.tickService = tickService;
    }

    // search sightings by location and date, with pagination
    @GetMapping("/sightings")
    public Map<String, Object> search(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int pageSize) {

        if (page < 0 || pageSize < 1 || pageSize > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Page must be 0 or more. Size must be 1 to 100.");
        }

        List<TickSighting> matchingSightings = filter(location, from, to);
        long startIndex = (long) page * pageSize;
        List<TickSighting> pageResults = new ArrayList<>();

        if (startIndex < matchingSightings.size()) {
            int endIndex = (int) Math.min(startIndex + pageSize,
                    matchingSightings.size());

            pageResults = matchingSightings.subList((int) startIndex, endIndex);
        }

        return Map.of(
                "totalSightingsMatched", matchingSightings.size(),
                "page", page,
                "pageSize", pageSize,
                "sightings", pageResults
        );
    }


    // go through sightings, keep the ones that match the filters
    private List<TickSighting> filter(String location, String fromDate, String toDate) {

        LocalDate startDate = checkDate(fromDate);
        LocalDate endDate = checkDate(toDate);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start date cannot be after end date");
        }

        List<TickSighting> matchingSightings = new ArrayList<>();

        for (TickSighting sighting : tickService.getSightings()) {

            LocalDate sightingDate = LocalDate.parse(
                    sighting.getDate().substring(0, 10));

            if (startDate != null && sightingDate.isBefore(startDate)) continue;
            if (endDate != null && sightingDate.isAfter(endDate)) continue;

            if (location != null && !location.isBlank()) {
                String sightingCity = sighting.getLocation().toLowerCase(Locale.ROOT);

                if (!sightingCity.contains(location.trim().toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }

            matchingSightings.add(sighting);
        }

        return matchingSightings;
    }


    // Check if the dates entered are valid
    private LocalDate checkDate(String dateText) {

        if (dateText == null || dateText.isBlank()) return null;

        try {
            return LocalDate.parse(dateText);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use YYYY-MM-DD for dates");
        }
    }


    // Count sightings for each UK region
    @GetMapping("/stats/regions")
    public Map<String, Integer> regions() {

        Map<String, Integer> sightingsByRegion = new TreeMap<>();

        for (TickSighting sighting : tickService.getSightings()) {

            String regionName = findRegion(sighting.getLocation());

            sightingsByRegion.put(regionName,
                    sightingsByRegion.getOrDefault(regionName, 0) + 1);
        }

        return sightingsByRegion;
    }

    // The API gives cities, so I matched them to regions
    private String findRegion(String cityName) {

        return switch (cityName.toLowerCase(Locale.ROOT)) {
            case "london" -> "London";
            case "manchester", "liverpool" -> "North West England";
            case "birmingham" -> "West Midlands";
            case "bristol" -> "South West England";
            case "cardiff" -> "Wales";
            case "edinburgh", "glasgow" -> "Scotland";
            case "leeds", "sheffield" -> "Yorkshire and the Humber";
            case "leicester", "nottingham" -> "East Midlands";
            case "newcastle" -> "North East England";
            case "southampton" -> "South East England";
            default -> "Other / Unmapped";
        };
    }


    // Count how many sightings happened each month
    @GetMapping("/stats/monthly")
    public Map<String, Integer> monthly() {

        Map<String, Integer> sightingsByMonth = new TreeMap<>();

        for (TickSighting sighting : tickService.getSightings()) {

            String sightingMonth = sighting.getDate().substring(0, 7);

            sightingsByMonth.put(sightingMonth,
                    sightingsByMonth.getOrDefault(sightingMonth, 0) + 1);
        }

        return sightingsByMonth;
    }
}
