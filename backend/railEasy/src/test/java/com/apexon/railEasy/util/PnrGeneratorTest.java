package com.apexon.railEasy.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PnrGeneratorTest {

    @Test
    void generatesEightCharUppercaseAlphanumericPnr() {
        String pnr = PnrGenerator.generate();
        assertThat(pnr).hasSize(8);
        assertThat(pnr).matches("[0-9A-F]{8}");
    }

    @Test
    void generatesDistinctValues() {
        assertThat(PnrGenerator.generate()).isNotEqualTo(PnrGenerator.generate());
    }
}

