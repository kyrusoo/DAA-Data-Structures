package report;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PlotGenerator {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 1120;
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final Color ARRAY_COLOR = new Color(38, 99, 174);
    private static final Color LIST_COLOR = new Color(34, 139, 87);
    private static final Color HEAP_COLOR = new Color(191, 76, 52);
    private static final Color GRID_COLOR = new Color(220, 224, 229);
    private static final Color TEXT_COLOR = new Color(40, 47, 55);
    private static final Font NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    private static final Font SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 24);

    private PlotGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path csv = args.length > 0 ? Path.of(args[0]) : Path.of("results", "results.csv");
        Path outputDirectory = args.length > 1 ? Path.of(args[1]) : Path.of("results", "plots");
        Files.createDirectories(outputDirectory);
        List<Row> rows = readRows(csv);
        for (String workload : new String[]{"W1", "W2", "W3", "W4"}) {
            BufferedImage image = drawWorkload(workload, rows);
            ImageIO.write(image, "png", outputDirectory.resolve(workload + ".png").toFile());
        }
        System.out.println("Wrote workload charts to " + outputDirectory.toAbsolutePath());
    }

    private static List<Row> readRows(Path csv) throws IOException {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] fields = lines.get(i).split(",");
            rows.add(new Row(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]),
                    Double.parseDouble(fields[4]), Long.parseLong(fields[5]),
                    Long.parseLong(fields[6]), Long.parseLong(fields[7])));
        }
        return rows;
    }

    private static BufferedImage drawWorkload(String workload, List<Row> allRows) {
        List<Row> rows = allRows.stream().filter(row -> row.workload.equals(workload)).toList();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setColor(TEXT_COLOR);
        graphics.setFont(TITLE);
        graphics.drawString(workloadTitle(workload), 70, 42);

        drawTimePanel(graphics, workload, rows);
        drawMetricPanel(graphics, workload, rows, "steps", 100, 660, 340, 320);
        drawMetricPanel(graphics, workload, rows, "moves", 550, 660, 340, 320);
        drawMetricPanel(graphics, workload, rows, "comparisons", 1_000, 660, 340, 320);
        graphics.setColor(TEXT_COLOR);
        graphics.setFont(SMALL);
        graphics.drawString("Input size n", WIDTH / 2 - 30, 1_020);
        drawLegend(graphics, series(rows));
        graphics.dispose();
        return image;
    }

    private static void drawTimePanel(Graphics2D g, String workload, List<Row> rows) {
        int left = 105;
        int top = 105;
        int width = 1190;
        int height = 410;
        drawLogAxes(g, left, top, width, height, "Median time (ms)");
        List<String> keys = series(rows);
        for (String key : keys) {
            List<Row> values = rowsForSeries(rows, key);
            g.setColor(color(key));
            g.setStroke(stroke(key));
            int previousX = -1;
            int previousY = -1;
            for (Row row : values) {
                int x = xPosition(row.n, left, width);
                int y = logYPosition(row.timeMs, top, height, 0.01, 1_000.0);
                if (previousX >= 0) {
                    g.drawLine(previousX, previousY, x, y);
                }
                g.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
                previousX = x;
                previousY = y;
            }
        }
        g.setColor(TEXT_COLOR);
        g.setFont(NORMAL);
        g.drawString("Input size n", left + width / 2 - 40, top + height + 44);
        g.setFont(SMALL);
        g.drawString("Lower panels show physical operation counts for the same cases.", left, 585);
    }

    private static void drawLogAxes(Graphics2D g, int left, int top, int width, int height,
                                    String yLabel) {
        g.setFont(NORMAL);
        g.setColor(TEXT_COLOR);
        g.drawString("Median time", left, top - 20);
        int[] powers = {-2, -1, 0, 1, 2, 3};
        for (int power : powers) {
            double value = Math.pow(10, power);
            int y = logYPosition(value, top, height, 0.01, 1_000.0);
            g.setColor(GRID_COLOR);
            g.drawLine(left, y, left + width, y);
            g.setColor(TEXT_COLOR);
            g.setFont(SMALL);
            g.drawString(formatTimeTick(value), left - 55, y + 4);
        }
        drawXTicks(g, left, top, width, height);
        g.setColor(TEXT_COLOR);
        g.setFont(SMALL);
        g.rotate(-Math.PI / 2, left - 78, top + height / 2);
        g.drawString(yLabel, left - 78, top + height / 2);
        g.rotate(Math.PI / 2, left - 78, top + height / 2);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(left, top, left, top + height);
        g.drawLine(left, top + height, left + width, top + height);
    }

    private static void drawMetricPanel(Graphics2D g, String workload, List<Row> rows,
                                        String metric, int left, int top, int width, int height) {
        long max = 0;
        for (Row row : rows) {
            max = Math.max(max, row.metric(metric));
        }
        long axisMax = niceMaximum(max);
        g.setFont(NORMAL);
        g.setColor(TEXT_COLOR);
        g.drawString(metric, left, top - 16);
        for (int tick = 0; tick <= 4; tick++) {
            double value = axisMax * tick / 4.0;
            int y = top + height - tick * height / 4;
            g.setColor(GRID_COLOR);
            g.drawLine(left, y, left + width, y);
            g.setColor(TEXT_COLOR);
            g.setFont(SMALL);
            g.drawString(formatCount(value), left - 55, y + 4);
        }
        drawXTicks(g, left, top, width, height);
        g.setColor(TEXT_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(left, top, left, top + height);
        g.drawLine(left, top + height, left + width, top + height);

        for (String key : series(rows)) {
            List<Row> values = rowsForSeries(rows, key);
            g.setColor(color(key));
            g.setStroke(stroke(key));
            int previousX = -1;
            int previousY = -1;
            for (Row row : values) {
                int x = xPosition(row.n, left, width);
                int y = top + height - (int) Math.round((double) row.metric(metric) / axisMax * height);
                if (previousX >= 0) {
                    g.drawLine(previousX, previousY, x, y);
                }
                g.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
                previousX = x;
                previousY = y;
            }
        }
    }

    private static void drawXTicks(Graphics2D g, int left, int top, int width, int height) {
        g.setFont(SMALL);
        for (int n : SIZES) {
            int x = xPosition(n, left, width);
            g.setColor(GRID_COLOR);
            g.drawLine(x, top, x, top + height);
            g.setColor(TEXT_COLOR);
            String label = Integer.toString(n);
            int labelWidth = g.getFontMetrics().stringWidth(label);
            int labelX = n == SIZES[SIZES.length - 1] ? x - labelWidth : x - labelWidth / 2;
            g.drawString(label, labelX, top + height + 20);
        }
    }

    private static void drawLegend(Graphics2D g, List<String> keys) {
        int left = 100;
        int y = 1_070;
        g.setFont(SMALL);
        for (String key : keys) {
            g.setColor(color(key));
            g.setStroke(stroke(key));
            g.drawLine(left, y, left + 32, y);
            g.fill(new Ellipse2D.Double(left + 12, y - 4, 8, 8));
            g.setColor(TEXT_COLOR);
            g.drawString(key, left + 40, y + 4);
            left += 40 + g.getFontMetrics().stringWidth(key) + 35;
        }
    }

    private static List<String> series(List<Row> rows) {
        List<String> keys = new ArrayList<>();
        for (Row row : rows) {
            String key = seriesKey(row);
            if (!keys.contains(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static List<Row> rowsForSeries(List<Row> rows, String key) {
        return rows.stream().filter(row -> seriesKey(row).equals(key))
                .sorted((first, second) -> Integer.compare(first.n, second.n)).toList();
    }

    private static String seriesKey(Row row) {
        if (row.workload.equals("W3")) {
            return row.structure.equals("DynamicArray") ? "Array " + row.variant : "List " + row.variant;
        }
        return switch (row.structure) {
            case "DynamicArray" -> "DynamicArray";
            case "MyLinkedList" -> "MyLinkedList";
            default -> "MinHeap";
        };
    }

    private static Color color(String key) {
        if (key.startsWith("Array") || key.equals("DynamicArray")) {
            return ARRAY_COLOR;
        }
        if (key.startsWith("List") || key.equals("MyLinkedList")) {
            return LIST_COLOR;
        }
        return HEAP_COLOR;
    }

    private static BasicStroke stroke(String key) {
        if (key.endsWith("middle")) {
            return new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10, new float[]{8, 6}, 0);
        }
        return new BasicStroke(2.2f);
    }

    private static int xPosition(int n, int left, int width) {
        double fraction = (Math.log10(n) - 2.0) / 3.0;
        return left + (int) Math.round(fraction * width);
    }

    private static int logYPosition(double value, int top, int height, double min, double max) {
        double fraction = (Math.log10(max) - Math.log10(value))
                / (Math.log10(max) - Math.log10(min));
        return top + (int) Math.round(fraction * height);
    }

    private static long niceMaximum(long value) {
        if (value <= 0) {
            return 1;
        }
        double magnitude = Math.pow(10, Math.floor(Math.log10(value)));
        double scaled = value / magnitude;
        double ceiling = scaled <= 1 ? 1 : scaled <= 2 ? 2 : scaled <= 5 ? 5 : 10;
        return (long) (ceiling * magnitude);
    }

    private static String formatTimeTick(double value) {
        return value < 1 ? String.format(Locale.ROOT, "%.2f", value) : String.format(Locale.ROOT, "%.0f", value);
    }

    private static String formatCount(double value) {
        if (value >= 1_000_000_000) {
            return String.format(Locale.ROOT, "%.1fB", value / 1_000_000_000.0);
        }
        if (value >= 1_000_000) {
            return String.format(Locale.ROOT, "%.0fM", value / 1_000_000.0);
        }
        if (value >= 1_000) {
            return String.format(Locale.ROOT, "%.0fk", value / 1_000.0);
        }
        if (value != Math.rint(value)) {
            return String.format(Locale.ROOT, "%.2f", value);
        }
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static String workloadTitle(String workload) {
        return switch (workload) {
            case "W1" -> "W1 Random Access";
            case "W2" -> "W2 Search";
            case "W3" -> "W3 Insert and Remove";
            default -> "W4 Priority Processing";
        };
    }

    private record Row(String workload, String variant, String structure, int n, double timeMs,
                       long steps, long moves, long comparisons) {
        long metric(String name) {
            return switch (name) {
                case "steps" -> steps;
                case "moves" -> moves;
                default -> comparisons;
            };
        }
    }
}
