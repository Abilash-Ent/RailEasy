package com.apexon.railEasy.util;

import java.util.UUID;

/**
 * Generates unique PNR (Passenger Name Record) identifiers using the first
 * 8 characters of a random UUID, as specified by the requirements.
 */
public final class PnrGenerator {

    private PnrGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

