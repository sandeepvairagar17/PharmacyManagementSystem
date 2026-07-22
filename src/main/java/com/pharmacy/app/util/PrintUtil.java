package com.pharmacy.app.util;

import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles printing plain-text reports/documents through the system print dialog.
 * On Windows, choosing "Microsoft Print to PDF" in that dialog saves as a PDF file -
 * this is how the app supports "print or save as PDF" without needing a PDF library.
 * Paginates long content properly across multiple pages instead of clipping to one page.
 */
public class PrintUtil {

    private static final int LINES_PER_PAGE = 50;

    public static void printText(String title, String content, Window ownerWindow) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No printer available");
            alert.setContentText("No printer was found. On Windows, you can install 'Microsoft Print to PDF' as a printer to save reports as PDF files.");
            alert.showAndWait();
            return;
        }

        boolean proceed = job.showPrintDialog(ownerWindow);
        if (!proceed) return; // user cancelled

        PageLayout pageLayout = job.getJobSettings().getPageLayout();
        if (pageLayout == null) {
            pageLayout = job.getPrinter().createPageLayout(Paper.A4,
                    javafx.print.PageOrientation.PORTRAIT, javafx.print.Printer.MarginType.DEFAULT);
        }

        List<String> lines = List.of(content.split("\n", -1));
        List<List<String>> pages = paginate(lines, LINES_PER_PAGE);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        for (int i = 0; i < pages.size(); i++) {
            VBox page = buildPageNode(title, pages.get(i), i + 1, pages.size(), timestamp, pageLayout.getPrintableWidth());
            boolean success = job.printPage(pageLayout, page);
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Print failed");
                alert.setContentText("Printing was interrupted on page " + (i + 1) + ".");
                alert.showAndWait();
                job.endJob();
                return;
            }
        }

        job.endJob();
    }

    private static List<List<String>> paginate(List<String> lines, int linesPerPage) {
        List<List<String>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += linesPerPage) {
            pages.add(lines.subList(i, Math.min(i + linesPerPage, lines.size())));
        }
        if (pages.isEmpty()) pages.add(List.of("(no content)"));
        return pages;
    }

    private static VBox buildPageNode(String title, List<String> pageLines, int pageNum, int totalPages, String timestamp, double width) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(20));
        box.setPrefWidth(width);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", 16));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label metaLabel = new Label("Printed: " + timestamp + "        Page " + pageNum + " of " + totalPages);
        metaLabel.setFont(Font.font("System", 9));
        metaLabel.setStyle("-fx-text-fill: #666;");

        box.getChildren().addAll(titleLabel, metaLabel);

        for (String line : pageLines) {
            Label lineLabel = new Label(line);
            lineLabel.setFont(Font.font("Consolas", 11));
            box.getChildren().add(lineLabel);
        }

        return box;
    }
}