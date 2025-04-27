import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

class Term {
    double coefficient;
    int exponent;

    Term(double coefficient, int exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
    }
}

public class Slope_Derivative_Graph_for_Polynomials extends JFrame {
    private String originalEquation;
    private String derivativeEquation;
    private String secondDerivativeEquation;
    private List<Term> originalTerms;
    private List<Term> derivativeTerms;
    private List<Term> secondDerivativeTerms;
    private GraphPanel graphPanel;
    private JLabel coordinatesLabel;
    private JCheckBox showLabelsCheckBox;
    private static final Color[] ADDITIONAL_COLORS = {Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};
    private int nextColorIndex = 0;
    private int additionalFunctionCount = 0;

    public Slope_Derivative_Graph_for_Polynomials() {
        setTitle("Polynomial and its 1st and 2nd Derivative (Slope and Change of Slope)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 775); // Adjusted to fit all components, graph is 800x800

        String input = JOptionPane.showInputDialog(this, "Enter polynomial (e.g., 3x^2 + 2x - 5):");
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No input provided.");
            System.exit(0);
        }

        originalTerms = parsePolynomial(input);
        if (originalTerms == null || originalTerms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid polynomial format.");
            System.exit(0);
        }

        derivativeTerms = computeDerivative(originalTerms);
        secondDerivativeTerms = computeDerivative(derivativeTerms);
        originalEquation = buildEquation(originalTerms);
        derivativeEquation = buildEquation(derivativeTerms);
        secondDerivativeEquation = buildEquation(secondDerivativeTerms);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel equationLabel = new JLabel();
        equationLabel.setText("<html><div style='text-align: left;'>" +
                ".        Original: <font color='blue'>" + originalEquation + "</font><br/>" +
                ".        Derivative: <font color='red'>" + derivativeEquation + "</font><br/>" +
                ".        Second Derivative: <font color='black'>" + secondDerivativeEquation + "</font></div></html>");
        mainPanel.add(equationLabel, BorderLayout.NORTH);

        graphPanel = new GraphPanel(originalTerms, derivativeTerms, secondDerivativeTerms);
        mainPanel.add(graphPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        showLabelsCheckBox = new JCheckBox("Show Labels", true);
        showLabelsCheckBox.addActionListener(e -> graphPanel.repaint());
        bottomPanel.add(showLabelsCheckBox);

        JPanel zoomControlPanel = new JPanel(new GridLayout(2, 2));
        zoomControlPanel.add(new JLabel("Center X:"));
        JTextField cxField = new JTextField("0", 5);
        zoomControlPanel.add(cxField);
        zoomControlPanel.add(new JLabel("Center Y:"));
        JTextField cyField = new JTextField("0", 5);
        zoomControlPanel.add(cyField);
        zoomControlPanel.add(new JLabel("Zoom Width:"));
        JTextField zoomLengthField = new JTextField("20", 5);
        zoomControlPanel.add(zoomLengthField);
        zoomControlPanel.add(new JLabel("Zoom Height:"));
        JTextField zoomWidthField = new JTextField("20", 5);
        zoomControlPanel.add(zoomWidthField);
        bottomPanel.add(zoomControlPanel);

        JButton applyZoomButton = new JButton("Apply Zoom");
        applyZoomButton.addActionListener(e -> {
            try {
                double cx = Double.parseDouble(cxField.getText());
                double cy = Double.parseDouble(cyField.getText());
                double zoomLength = Double.parseDouble(zoomLengthField.getText());
                double zoomWidth = Double.parseDouble(zoomWidthField.getText());
                graphPanel.setZoom(cx, cy, zoomLength, zoomWidth);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid zoom values.");
            }
        });
        bottomPanel.add(applyZoomButton);

        JButton addFunctionButton = new JButton("Add Function");
        addFunctionButton.addActionListener(e -> {
            String funcInput = JOptionPane.showInputDialog(this, "Enter function (e.g., y=3x^2 + 2x -5, y=5, x=3):");
            if (funcInput != null && !funcInput.trim().isEmpty()) {
                String[] parts = funcInput.split("=");
                if (parts.length == 2) {
                    String left = parts[0].trim().toLowerCase();
                    String right = parts[1].trim();
                    if (left.equals("y")) {
                        try {
                            double constant = Double.parseDouble(right);
                            additionalFunctionCount++;
                            graphPanel.addFunction(new ConstantFunction(constant, getNextColor(), "g" + additionalFunctionCount + "(x)"));
                        } catch (NumberFormatException ex) {
                            List<Term> terms = parsePolynomial(right);
                            if (terms != null) {
                                additionalFunctionCount++;
                                graphPanel.addFunction(new PolynomialFunction(terms, getNextColor(), "g" + additionalFunctionCount + "(x)"));
                            } else {
                                JOptionPane.showMessageDialog(this, "Invalid function.");
                            }
                        }
                    } else if (left.equals("x")) {
                        try {
                            double xValue = Double.parseDouble(right);
                            graphPanel.addVerticalLine(new VerticalLine(xValue, getNextColor()));
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Invalid vertical line.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid function format.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid function format.");
                }
            }
        });
        bottomPanel.add(addFunctionButton);

        coordinatesLabel = new JLabel("Coordinates: ");
        bottomPanel.add(coordinatesLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);
    }

    private List<Term> parsePolynomial(String input) {
        List<Term> terms = new ArrayList<>();
        input = input.replaceAll("\\s+", "");

        if (input.isEmpty()) return null;

        String[] termStrings = input.split("(?=[+-])");
        for (String termStr : termStrings) {
            Term term = parseTerm(termStr);
            if (term == null) return null;
            terms.add(term);
        }
        return terms;
    }

    private Term parseTerm(String termStr) {
        termStr = termStr.replaceAll("\\s+", "");
        if (termStr.isEmpty()) return null;

        int sign = 1;
        int startIndex = 0;
        if (termStr.startsWith("+")) {
            sign = 1;
            startIndex = 1;
        } else if (termStr.startsWith("-")) {
            sign = -1;
            startIndex = 1;
        }

        String body = termStr.substring(startIndex);
        if (body.isEmpty()) return null;

        if (!body.contains("x")) {
            try {
                double coeff = sign * Double.parseDouble(body);
                return new Term(coeff, 0);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String[] parts = body.split("x", 2);
        String coeffPart = parts[0];
        String expPart = parts.length > 1 ? parts[1] : "";

        double coefficient;
        if (coeffPart.isEmpty()) {
            coefficient = sign * 1.0;
        } else {
            try {
                coefficient = sign * Double.parseDouble(coeffPart);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        int exponent;
        if (expPart.startsWith("^")) {
            try {
                exponent = Integer.parseInt(expPart.substring(1));
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (expPart.isEmpty()) {
            exponent = 1;
        } else {
            return null;
        }

        return new Term(coefficient, exponent);
    }

    private List<Term> computeDerivative(List<Term> terms) {
        Map<Integer, Double> derivativeMap = new HashMap<>();
        for (Term term : terms) {
            if (term.exponent == 0) continue;
            double newCoeff = term.coefficient * term.exponent;
            int newExp = term.exponent - 1;
            derivativeMap.put(newExp, derivativeMap.getOrDefault(newExp, 0.0) + newCoeff);
        }

        List<Term> derivativeTerms = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : derivativeMap.entrySet()) {
            derivativeTerms.add(new Term(entry.getValue(), entry.getKey()));
        }
        return derivativeTerms;
    }

    private String buildEquation(List<Term> terms) {
        if (terms.isEmpty()) return "0";

        terms.sort((a, b) -> Integer.compare(b.exponent, a.exponent));
        List<String> termStrings = new ArrayList<>();

        for (Term term : terms) {
            if (term.coefficient == 0) continue;
            String termStr = getTermWithSign(term.coefficient, term.exponent);
            termStrings.add(termStr);
        }

        if (termStrings.isEmpty()) return "0";

        String equation = String.join(" ", termStrings);
        if (equation.startsWith("+")) {
            equation = equation.substring(1).trim();
        }
        return equation.isEmpty() ? "0" : equation;
    }

    private String getTermWithSign(double coeff, int exp) {
        if (coeff == 0) return "";

        StringBuilder term = new StringBuilder();
        term.append(coeff > 0 ? "+" : "-");
        double absCoeff = Math.abs(coeff);

        if (exp == 0) {
            term.append(absCoeff);
            return term.toString();
        }

        if (absCoeff != 1 || exp == 0) {
            term.append(absCoeff);
        }

        term.append("x");

        if (exp != 1) {
            term.append("^").append(exp);
        }

        return term.toString();
    }

    private Color getNextColor() {
        Color color = ADDITIONAL_COLORS[nextColorIndex % ADDITIONAL_COLORS.length];
        nextColorIndex++;
        return color;
    }

    // Abstract class for functions y=f(x)
    abstract class GraphFunction {
        Color color;
        String name;

        GraphFunction(Color color, String name) {
            this.color = color;
            this.name = name;
        }

        abstract double evaluate(double x);
    }

    class PolynomialFunction extends GraphFunction {
        List<Term> terms;

        PolynomialFunction(List<Term> terms, Color color, String name) {
            super(color, name);
            this.terms = terms;
        }

        @Override
        double evaluate(double x) {
            double y = 0;
            for (Term term : terms) {
                y += term.coefficient * Math.pow(x, term.exponent);
            }
            return y;
        }
    }

    class ConstantFunction extends GraphFunction {
        double value;

        ConstantFunction(double value, Color color, String name) {
            super(color, name);
            this.value = value;
        }

        @Override
        double evaluate(double x) {
            return value;
        }
    }

    // Class for vertical lines x=constant
    class VerticalLine {
        double xValue;
        Color color;

        VerticalLine(double xValue, Color color) {
            this.xValue = xValue;
            this.color = color;
        }
    }

    class GraphPanel extends JPanel {
        private List<GraphFunction> functions = new ArrayList<>();
        private List<VerticalLine> verticalLines = new ArrayList<>();
        private double cx = 0;
        private double cy = 0;
        private double zoomLength = 20;
        private double zoomWidth = 20;
        private double xMin = -10;
        private double xMax = 10;
        private double yMin = -10;
        private double yMax = 10;
        private Double currentX;

        public GraphPanel(List<Term> originalTerms, List<Term> derivativeTerms, List<Term> secondDerivativeTerms) {
            functions.add(new PolynomialFunction(originalTerms, Color.BLUE, "f(x)"));
            functions.add(new PolynomialFunction(derivativeTerms, Color.RED, "f'(x)"));
            functions.add(new PolynomialFunction(secondDerivativeTerms, Color.BLACK, "f''(x)"));
            setPreferredSize(new Dimension(1440, 775));
            setBackground(Color.WHITE);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int mouseXPixels = e.getX();
                    int mouseYPixels = e.getY();
                    double x = xMin + (mouseXPixels * (xMax - xMin) / (double) getWidth());
                    double y = yMax - (mouseYPixels * (yMax - yMin) / (double) getHeight());
                    currentX = x;
                    StringBuilder sb = new StringBuilder("x = " + String.format("%.2f", x) + ", y = " + String.format("%.2f", y));
                    for (GraphFunction function : functions) {
                        double funcY = function.evaluate(x);
                        sb.append(", " + function.name + " = " + String.format("%.2f", funcY));
                    }
                    Slope_Derivative_Graph_for_Polynomials.this.coordinatesLabel.setText(sb.toString());
                    repaint();
                }
            });
        }

        public void setZoom(double cx, double cy, double zoomLength, double zoomWidth) {
            this.cx = cx;
            this.cy = cy;
            this.zoomLength = zoomLength;
            this.zoomWidth = zoomWidth;
            this.xMin = cx - zoomLength / 2;
            this.xMax = cx + zoomLength / 2;
            this.yMin = cy - zoomWidth / 2;
            this.yMax = cy + zoomWidth / 2;
            repaint();
        }

        public void addFunction(GraphFunction function) {
            functions.add(function);
            repaint();
        }

        public void addVerticalLine(VerticalLine verticalLine) {
            verticalLines.add(verticalLine);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();

            drawGrid(g);
            drawAxes(g);
            drawLabels(g);

            for (GraphFunction function : functions) {
                drawFunction(g, function, width, height);
            }

            for (VerticalLine vLine : verticalLines) {
                g.setColor(vLine.color);
                int xPixel = (int) ((vLine.xValue - xMin) * width / (xMax - xMin));
                g.drawLine(xPixel, 0, xPixel, height);
            }

            if (currentX != null) {
                for (GraphFunction function : functions) {
                    double y = function.evaluate(currentX);
                    int xPixel = (int) ((currentX - xMin) * width / (xMax - xMin));
                    int yPixel = (int) ((yMax - y) * height / (yMax - yMin));
                    g.setColor(function.color);
                    g.fillOval(xPixel - 3, yPixel - 3, 6, 6);
                }
            }
        }

        private void drawAxes(Graphics g) {
            g.setColor(Color.BLACK);
            int width = getWidth();
            int height = getHeight();

            int yAxisPos = (int) ((yMax - 0) * height / (yMax - yMin));
            if (yMin <= 0 && 0 <= yMax) {
                g.drawLine(0, yAxisPos, width, yAxisPos);
            }

            int xAxisPos = (int) ((0 - xMin) * width / (xMax - xMin));
            if (xMin <= 0 && 0 <= xMax) {
                g.drawLine(xAxisPos, 0, xAxisPos, height);
            }
        }

        private void drawGrid(Graphics g) {
            g.setColor(Color.LIGHT_GRAY);
            int width = getWidth();
            int height = getHeight();
            double stepX = (xMax - xMin) / 10.0;
            double stepY = (yMax - yMin) / 10.0;

            for (int i = 0; i <= 10; i++) {
                double x = xMin + i * stepX;
                int xPixel = (int) ((x - xMin) * width / (xMax - xMin));
                g.drawLine(xPixel, 0, xPixel, height);
            }

            for (int i = 0; i <= 10; i++) {
                double y = yMin + i * stepY;
                int yPixel = (int) ((yMax - y) * height / (yMax - yMin));
                g.drawLine(0, yPixel, width, yPixel);
            }
        }

        private void drawLabels(Graphics g) {
            if (!Slope_Derivative_Graph_for_Polynomials.this.showLabelsCheckBox.isSelected()) return;
            g.setColor(Color.BLACK);
            int width = getWidth();
            int height = getHeight();
            double stepX = (xMax - xMin) / 10.0;
            double stepY = (yMax - yMin) / 10.0;

            for (int i = 0; i <= 10; i++) {
                double x = xMin + i * stepX;
                int xPixel = (int) ((x - xMin) * width / (xMax - xMin));
                String label = String.format("%.2f", x);
                g.drawString(label, xPixel - 10, height - 5);
            }

            for (int i = 0; i <= 10; i++) {
                double y = yMin + i * stepY;
                int yPixel = (int) ((yMax - y) * height / (yMax - yMin));
                String label = String.format("%.2f", y);
                g.drawString(label, 5, yPixel + 5);
            }
        }

        private void drawFunction(Graphics g, GraphFunction function, int width, int height) {
            g.setColor(function.color);
            Polygon polygon = new Polygon();
            for (double x = xMin; x <= xMax; x += (xMax - xMin) / width) {
                double y = function.evaluate(x);
                int xPixel = (int) ((x - xMin) * width / (xMax - xMin));
                int yPixel = (int) ((yMax - y) * height / (yMax - yMin));
                polygon.addPoint(xPixel, yPixel);
            }
            g.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Slope_Derivative_Graph_for_Polynomials());
    }
}