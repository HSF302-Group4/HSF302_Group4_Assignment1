package com.fu.cinema.desktop.util;

import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Xuất file CSV có UTF-8 BOM, đúng định dạng và escape đúng ký tự đặc biệt")
    void write_generatesCsvWithBomAndEscapes() throws IOException {
        Path csvFile = tempDir.resolve("test-report.csv");

        List<RevenueRow> revenue = List.of(
                new RevenueRow("Mai, đào \"và\" piano", 5, BigDecimal.valueOf(500000)),
                new RevenueRow("Lật Mặt 7", 2, BigDecimal.valueOf(180000))
        );

        List<ReportBookingRow> bookings = List.of(
                new ReportBookingRow(101L, LocalDateTime.of(2026, 11, 1, 10, 30),
                        "Nguyễn, Văn A", "Mai, đào \"và\" piano", BigDecimal.valueOf(500000)),
                new ReportBookingRow(102L, LocalDateTime.of(2026, 11, 2, 14, 0),
                        "Trần Thị B", "Lật Mặt 7", BigDecimal.valueOf(180000))
        );

        ReportData reportData = new ReportData(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 5),
                revenue,
                bookings,
                2,
                7,
                BigDecimal.valueOf(680000)
        );

        CsvExporter.write(csvFile, reportData);

        assertTrue(Files.exists(csvFile));

        byte[] bytes = Files.readAllBytes(csvFile);
        // Kiểm tra UTF-8 BOM (0xEF, 0xBB, 0xBF)
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);

        String content = Files.readString(csvFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("Báo cáo doanh thu,2026-11-01,2026-11-05"));
        assertTrue(content.contains("\"Mai, đào \"\"và\"\" piano\",5,500000"));
        assertTrue(content.contains("101,2026-11-01T10:30,\"Nguyễn, Văn A\",\"Mai, đào \"\"và\"\" piano\",500000"));
        assertTrue(content.contains("TỔNG,2 đơn,7 vé,,680000"));
    }

    @Test
    @DisplayName("csv() escape đúng dấu phẩy, dấu ngoặc kép, xuống dòng")
    void csvEscapeHelper() {
        assertEquals("", CsvExporter.csv(null));
        assertEquals("normal text", CsvExporter.csv("normal text"));
        assertEquals("\"text, with comma\"", CsvExporter.csv("text, with comma"));
        assertEquals("\"text with \"\"quotes\"\"\"", CsvExporter.csv("text with \"quotes\""));
        assertEquals("\"text with\nnewline\"", CsvExporter.csv("text with\nnewline"));
    }
}
