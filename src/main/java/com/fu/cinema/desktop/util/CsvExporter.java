package com.fu.cinema.desktop.util;

import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Xuất báo cáo doanh thu ra file CSV với encoding UTF-8 BOM.
 * BOM (Byte Order Mark \uFEFF) giúp Microsoft Excel tự động nhận diện và hiển thị đúng tiếng Việt.
 */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static void write(Path path, ReportData d) {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Ghi UTF-8 BOM để Excel hiển thị đúng tiếng Việt
            w.write('\uFEFF');
            w.write("Báo cáo doanh thu," + d.start() + "," + d.end());
            w.newLine();
            w.write("Phim,Số vé,Doanh thu");
            w.newLine();
            for (RevenueRow r : d.revenue()) {
                w.write(String.join(",",
                        csv(r.title()),
                        String.valueOf(r.tickets()),
                        r.revenue() != null ? r.revenue().toPlainString() : "0"));
                w.newLine();
            }
            w.newLine();
            w.write("Mã đơn,Ngày đặt,Khách hàng,Phim,Tổng tiền");
            w.newLine();
            for (ReportBookingRow b : d.bookings()) {
                w.write(String.join(",",
                        b.id().toString(),
                        b.bookingDate() != null ? b.bookingDate().toString() : "",
                        csv(b.customerName()),
                        csv(b.movieTitle()),
                        b.total() != null ? b.total().toPlainString() : "0"));
                w.newLine();
            }
            w.write("TỔNG,%d đơn,%d vé,,%s".formatted(
                    d.totalBookings(),
                    d.totalTickets(),
                    d.totalRevenue() != null ? d.totalRevenue().toPlainString() : "0"));
            w.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Escape giá trị trường CSV theo chuẩn RFC 4180.
     */
    static String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
