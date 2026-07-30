package com.apexon.railEasy.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeatUtilsTest {

    @Test
    void allSeatsContainsSixtyFourUniqueLabels() {
        List<String> seats = SeatUtils.allSeats();
        assertThat(seats).hasSize(64);
        assertThat(seats).doesNotHaveDuplicates();
        assertThat(seats).startsWith("1A").endsWith("8H");
    }

    @Test
    void validatesSeatLabels() {
        assertThat(SeatUtils.isValid("1A")).isTrue();
        assertThat(SeatUtils.isValid("8H")).isTrue();
        assertThat(SeatUtils.isValid("9A")).isFalse();  // row out of range
        assertThat(SeatUtils.isValid("1I")).isFalse();  // column out of range
        assertThat(SeatUtils.isValid("A1")).isFalse();  // wrong order
        assertThat(SeatUtils.isValid("1")).isFalse();
        assertThat(SeatUtils.isValid(null)).isFalse();
    }
}

