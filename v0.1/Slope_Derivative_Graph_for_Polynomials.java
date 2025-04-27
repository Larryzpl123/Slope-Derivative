import java.awt.*;
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
    private List<Term> originalTerms;
    private List<Term> derivativeTerms;

    public Slope_Derivative_Graph_for_Polynomials() {
        setTitle("Polynomial and Derivative");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

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
        originalEquation = buildEquation(originalTerms);
        derivativeEquation = buildEquation(derivativeTerms);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel equationLabel = new JLabel();
        equationLabel.setText("<html><div style='text-align: center;'>" +
                "Original: <font color='blue'>" + originalEquation + "</font><br/>" +
                "Derivative: <font color='red'>" + derivativeEquation + "</font></div></html>");
        mainPanel.add(equationLabel, BorderLayout.NORTH);

        GraphPanel graphPanel = new GraphPanel(originalTerms, derivativeTerms);
        mainPanel.add(graphPanel, BorderLayout.CENTER);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Slope_Derivative_Graph_for_Polynomials());
    }
}

class GraphPanel extends JPanel {
    private final List<Term> originalTerms;
    private final List<Term> derivativeTerms;
    private static final double X_MIN = -10;
    private static final double X_MAX = 10;
    private static final double Y_MIN = -10;
    private static final double Y_MAX = 10;

    public GraphPanel(List<Term> originalTerms, List<Term> derivativeTerms) {
        this.originalTerms = originalTerms;
        this.derivativeTerms = derivativeTerms;
        setPreferredSize(new Dimension(800, 500));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawAxes(g);
        drawFunction(g, originalTerms, Color.BLUE);
        drawFunction(g, derivativeTerms, Color.RED);
    }

    private void drawAxes(Graphics g) {
        g.setColor(Color.BLACK);
        int width = getWidth();
        int height = getHeight();

        // X-axis
        int yAxisPos = (int) ((Y_MAX - 0) * height / (Y_MAX - Y_MIN));
        g.drawLine(0, yAxisPos, width, yAxisPos);

        // Y-axis
        int xAxisPos = (int) ((0 - X_MIN) * width / (X_MAX - X_MIN));
        g.drawLine(xAxisPos, 0, xAxisPos, height);
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        int width = getWidth();
        int height = getHeight();

        // Vertical lines
        for (int x = (int) X_MIN; x <= X_MAX; x++) {
            int xPos = (int) ((x - X_MIN) * width / (X_MAX - X_MIN));
            g.drawLine(xPos, 0, xPos, height);
        }

        // Horizontal lines
        for (int y = (int) Y_MIN; y <= Y_MAX; y++) {
            int yPos = (int) ((Y_MAX - y) * height / (Y_MAX - Y_MIN));
            g.drawLine(0, yPos, width, yPos);
        }
    }

    private void drawFunction(Graphics g, List<Term> terms, Color color) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(color);

        Polygon polygon = new Polygon();
        for (double x = X_MIN; x <= X_MAX; x += 0.1) {
            double y = evaluate(terms, x);
            int xPixel = (int) ((x - X_MIN) * width / (X_MAX - X_MIN));
            int yPixel = (int) ((Y_MAX - y) * height / (Y_MAX - Y_MIN));
            polygon.addPoint(xPixel, yPixel);
        }
        g.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    private double evaluate(List<Term> terms, double x) {
        double y = 0;
        for (Term term : terms) {
            y += term.coefficient * Math.pow(x, term.exponent);
        }
        return y;
    }
}