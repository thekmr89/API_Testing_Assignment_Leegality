package com.automation.utils;

import com.automation.models.Booking;
import com.automation.models.BookingDates;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class TestDataFactory {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private TestDataFactory() {
    }

    public static Booking validBooking() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        LocalDate checkin = LocalDate.now(ZoneOffset.UTC).plusDays(30);
        return new Booking(
                "QA" + suffix,
                "Automation" + suffix,
                250,
                true,
                new BookingDates(DATE_FORMAT.format(checkin), DATE_FORMAT.format(checkin.plusDays(3))),
                "Breakfast");
    }
}
