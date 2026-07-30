package com.apexon.railEasy.util;

import com.apexon.railEasy.dto.response.BookingResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Renders a {@link BookingResponse} into a printable A4 PDF e-ticket using OpenPDF.
 *
 * <p>PDF generation is CPU-bound and blocking; callers on reactive threads must
 * offload this work (see {@code BookingServiceImpl}).
 */
@Component
public class TicketPdfGenerator {

    private static final Color BRAND = new Color(0x0D, 0x47, 0xA1);
    private static final Color LIGHT = new Color(0xE8, 0xEF, 0xFB);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.WHITE);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
    private static final Font PNR_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND);
    private static final Font FOOTER_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

    /**
     * Builds the PDF bytes for the given booking.
     *
     * @param booking the booking to render (must not be {@code null})
     * @return the generated PDF document as a byte array
     */
    public byte[] generate(BookingResponse booking) {
        Objects.requireNonNull(booking, "booking must not be null");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(header());
            document.add(pnrBanner(booking));
            document.add(journeyDetails(booking));
            document.add(passengerDetails(booking));
            document.add(footer());

            document.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException ex) {
            throw new IllegalStateException("Failed to generate ticket PDF for PNR " + booking.getPnr(), ex);
        }
    }

    private PdfPTable header() {
        PdfPTable table = fullWidthTable(1);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BRAND);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(16);
        cell.addElement(new Paragraph("RailEasy E-Ticket", TITLE_FONT));
        cell.addElement(new Paragraph("Booking Confirmation / Travel Pass", SUBTITLE_FONT));
        table.addCell(cell);
        table.setSpacingAfter(14);
        return table;
    }

    private PdfPTable pnrBanner(BookingResponse booking) {
        PdfPTable table = fullWidthTable(2);

        PdfPCell pnr = new PdfPCell();
        pnr.setBackgroundColor(LIGHT);
        pnr.setBorder(Rectangle.NO_BORDER);
        pnr.setPadding(12);
        pnr.addElement(new Paragraph("PNR", LABEL_FONT));
        pnr.addElement(new Paragraph(safe(booking.getPnr()), PNR_FONT));
        table.addCell(pnr);

        PdfPCell status = new PdfPCell();
        status.setBackgroundColor(LIGHT);
        status.setBorder(Rectangle.NO_BORDER);
        status.setPadding(12);
        status.setHorizontalAlignment(Element.ALIGN_RIGHT);
        status.addElement(rightParagraph("STATUS", LABEL_FONT));
        Color statusColor = booking.getStatus() != null
                && "CANCELLED".equals(booking.getStatus().name())
                ? new Color(0xC6, 0x28, 0x28)
                : new Color(0x2E, 0x7D, 0x32);
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, statusColor);
        status.addElement(rightParagraph(booking.getStatus() != null ? booking.getStatus().name() : "-", statusFont));
        table.addCell(status);

        table.setSpacingAfter(14);
        return table;
    }

    private PdfPTable journeyDetails(BookingResponse booking) {
        PdfPTable table = fullWidthTable(2);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        addField(table, "Train", value(booking.getTrainNumber(), booking.getTrainName()));
        addField(table, "Travel Class", booking.getTravelClass() != null ? booking.getTravelClass().name() : "-");
        addField(table, "From", safe(booking.getFromStation()));
        addField(table, "To", safe(booking.getToStation()));
        addField(table, "Journey Date",
                booking.getJourneyDate() != null ? booking.getJourneyDate().format(DATE) : "-");
        addField(table, "Booked At",
                booking.getBookedAt() != null ? booking.getBookedAt().format(DATE_TIME) : "-");

        table.setSpacingAfter(14);
        return table;
    }

    private PdfPTable passengerDetails(BookingResponse booking) {
        PdfPTable table = fullWidthTable(2);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        String seats = booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()
                ? String.join(", ", booking.getSeatNumbers())
                : "-";
        int seatCount = booking.getSeatNumbers() != null ? booking.getSeatNumbers().size() : 0;

        addField(table, "Seat(s)", seats);
        addField(table, "No. of Seats", String.valueOf(seatCount));
        addField(table, "Total Fare",
                booking.getTotalFare() != null ? "INR " + booking.getTotalFare().toPlainString() : "-");
        addField(table, "Booking Ref", booking.getId() != null ? "#" + booking.getId() : "-");

        table.setSpacingAfter(18);
        return table;
    }

    private Paragraph footer() {
        Paragraph footer = new Paragraph(
                "This is a system-generated e-ticket. Please carry a valid photo ID during your journey. "
                        + "Happy journey with RailEasy!", FOOTER_FONT);
        footer.setSpacingBefore(10);
        return footer;
    }

    private void addField(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(10);
        cell.addElement(new Paragraph(label, LABEL_FONT));
        cell.addElement(new Paragraph(value, VALUE_FONT));
        table.addCell(cell);
    }

    private PdfPTable fullWidthTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        return table;
    }

    private Paragraph rightParagraph(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    private String value(String number, String name) {
        String composed = String.join(" - ",
                java.util.stream.Stream.of(number, name)
                        .filter(s -> s != null && !s.isBlank())
                        .toList());
        return composed.isBlank() ? "-" : composed;
    }

    private String safe(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }
}







