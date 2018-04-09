//ECE 309  March 26th, 2018  Team Project: Josh Hofmann, Iason Katsaros, Randy Paluszkiewicz, Brian Cuthrell

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ExpressionCalculator implements Calculator, ActionListener {
	 JFrame window = new JFrame(
			"EXPRESSION CALCULATOR    Operators are + - * / ^ r ( )     Operands are numbers, x, e, and pi ");
	private JButton clearButton = new JButton("Clear");
	private JButton recallButton = new JButton("Recall");
	private JTextField expressionTextField = new JTextField(30);
	private JTextArea displayTextArea = new JTextArea();
	private JScrollPane displayScrollPane = new JScrollPane(displayTextArea);
	private JTextField errorTextField = new JTextField();
	private JLabel forXLabel = new JLabel("for x =", 4);
	private JTextField forXTextField = new JTextField(8);

	private String newLine = System.getProperty("line.separator");
	private String expressionInstructs = "Enter an algebraic expression. e.g. x^2 [x squared] or Xr2 [square root of x] or (x + pi + e - 7) / 3";
	private String previousExpression;
	private String previousForXString;

	public static void main(String[] args) {
		try {
			new ExpressionCalculator();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public ExpressionCalculator() throws Exception {
		System.out.println("Josh Hofmann, Iason Katsaros, Randy Paluszkiewicz, Brian Cuthrell");

		JPanel topPanel = new JPanel();
		topPanel.add(clearButton);
		topPanel.add(recallButton);
		topPanel.add(expressionTextField);
		topPanel.add(forXLabel);
		topPanel.add(forXTextField);
		window.getContentPane().add(topPanel, "North");
		window.getContentPane().add(displayScrollPane, "Center");
		window.getContentPane().add(errorTextField, "South");

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(800, 300);
		displayTextArea.setEditable(false);
		displayTextArea.setFont(new Font("default", 1, 15));
		errorTextField.setEditable(false);
		expressionTextField.setFont(new Font("default", 1, 15));

		expressionTextField.addActionListener(this);
		clearButton.addActionListener(this);
		recallButton.addActionListener(this);
		forXTextField.addActionListener(this);

		displayTextArea.setText(expressionInstructs + newLine);
		window.setVisible(true);
		expressionTextField.requestFocus();
	}

	public void actionPerformed(ActionEvent ae) {
		errorTextField.setText("");
		errorTextField.setBackground(Color.white);

		if (ae.getSource() == clearButton) {
			forXTextField.setText("");
			expressionTextField.setText("");
			expressionTextField.requestFocus();
			return;
		}

		if (ae.getSource() == recallButton) {
			expressionTextField.setText(previousExpression);
			forXTextField.setText(previousForXString);
			expressionTextField.requestFocus();
			return;
		}

		if ((ae.getSource() == expressionTextField) || (ae.getSource() == forXTextField)) {
			try {
				String enteredExpression = expressionTextField.getText();
				String forXString = forXTextField.getText();
				double result = calculate(enteredExpression, forXString);

				previousExpression = enteredExpression;
				previousForXString = forXString;

				Double d = new Double(result);
				String resultString;

				if ((d.isInfinite()) || (d.isNaN())) {
					resultString = d.toString();
				} else {
					BigDecimal resultBD = new BigDecimal(result, MathContext.DECIMAL32);
					resultBD = resultBD.setScale(7, 1);
					resultString = resultBD.toPlainString();
					resultString = stripTrailingZeros(resultString);
				}

				displayTextArea.append(newLine + enteredExpression + " = " + resultString);
				if ((enteredExpression.contains("x")) || (enteredExpression.contains("X"))) {
					displayTextArea.append(" for x = " + forXString);
				}

				displayTextArea.setCaretPosition(displayTextArea.getDocument().getLength());
				expressionTextField.setText("");
				forXTextField.setText("");
				expressionTextField.requestFocus();
				return;
			} catch (Exception e) {
				errorTextField.setText(e.getMessage());
				errorTextField.setBackground(Color.yellow);
			}
		}
	}

	public double calculate(String expression, String xString) throws Exception {
		boolean expressionContainsX = false;
		boolean xValueWasSpecified = false;
		boolean expressionContainsParentheses = false;

		expression = expression.trim();
		if ((expression == null) || (expression.trim().length() == 0)) {
			throw new IllegalArgumentException("Expression is null or length 0.");
		}

		expression = expression.trim();
		if ((expression.contains("x")) || (expression.contains("X"))) {
			expressionContainsX = true;
		}
		if ((xString != null) && (xString.trim().length() > 0))
			xValueWasSpecified = true;
		xString = xString.trim();

		if (xValueWasSpecified) {
			try {
				double d1 = Double.parseDouble(xString);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("The x value specified is not numeric.");
			}
		}

		if ((expressionContainsX) && (!xValueWasSpecified))
			throw new IllegalArgumentException("The entered expression contains x but an x value is not specified.");
		if ((xValueWasSpecified) && (!expressionContainsX)) {
			throw new IllegalArgumentException("An x value is specified but the expression does not contain x.");
		}
		if ((expression.contains("(")) || (expression.contains(")"))) {
			expressionContainsParentheses = true;
		}

		expression = symbolSubstitution(expression, xString);

		if ((expression.contains("(")) || (expression.contains(")")))
			expression = removeParentheses(expression);
		double result = evaluateComplexExpression(expression);
		return result;
	}

	private String symbolSubstitution(String expression, String xString) throws Exception {
		expression = expression.replace("X", "x");
		expression = expression.replace("E", "e");
		expression = expression.replace("PI", "pi");
		expression = expression.replace("Pi", "pi");
		expression = expression.replace("R", "r");

		String[] symbols = { "x", "e", "pi" };
		for (int i = 0; i < symbols.length; i++) {
			int start = 0;
			int offset;
			while ((offset = expression.indexOf(symbols[i], start)) != -1) {
				start = offset + 1;
				if (offset != 0) {

					if ((expression.charAt(offset - 1) != ' ') && (expression.charAt(offset - 1) != '+')
							&& (expression.charAt(offset - 1) != '-') && (expression.charAt(offset - 1) != '*')
							&& (expression.charAt(offset - 1) != '/') && (expression.charAt(offset - 1) != '^')
							&& (expression.charAt(offset - 1) != 'r') && (expression.charAt(offset - 1) != '('))
						throw new IllegalArgumentException(
								"Operator missing before " + symbols[i] + " at offset " + (offset + 1));
				}
				if (offset < expression.length() - symbols[i].length()) {

					if ((expression.charAt(offset + symbols[i].length()) != ' ')
							&& (expression.charAt(offset + symbols[i].length()) != '+')
							&& (expression.charAt(offset + symbols[i].length()) != '-')
							&& (expression.charAt(offset + symbols[i].length()) != '*')
							&& (expression.charAt(offset + symbols[i].length()) != '/')
							&& (expression.charAt(offset + symbols[i].length()) != '^')
							&& (expression.charAt(offset + symbols[i].length()) != 'r')
							&& (expression.charAt(offset + symbols[i].length()) != ')')) {
						throw new IllegalArgumentException(
								"Operator missing after " + symbols[i] + " at offset " + (offset + 1));
					}
				}
			}
		}

		xString = xString.replace("-", "n");
		expression = expression.replace("x", xString);
		expression = expression.replace("pi", String.valueOf(3.141592653589793D));
		expression = expression.replace("e", String.valueOf(2.718281828459045D));

		expression = replaceUnary(expression);
		return expression;
	}

	private String replaceUnary(String expression) throws IllegalArgumentException {
		expression = expression.replace("-n", "+");

		int lastOperatorOffset = -1;
		char lastOperator = ' ';

		if (expression.startsWith("-"))
			expression.replaceFirst("-", "n");
		for (int i = 1; i < expression.length(); i++) {

			if ((expression.charAt(i) == '+') || (expression.charAt(i) == '-') || (expression.charAt(i) == '*')
					|| (expression.charAt(i) == '/') || (expression.charAt(i) == '^')
					|| (expression.charAt(i) == 'r')) {

				String operand = expression.substring(lastOperatorOffset + 1, i).trim();

				if (operand.length() == 0) {

					if (expression.charAt(i) != '-') {
						if (lastOperator == ' ') {
							throw new IllegalArgumentException("Missing operand between beginning of expression and " +

									expression.charAt(i));
						}
						throw new IllegalArgumentException(
								"Missing operand between " + lastOperator + " and " + expression.charAt(i));
					}

					if (i + 1 == expression.length()) {
						throw new IllegalArgumentException("Missing operand following " + expression.charAt(i));
					}
					if (expression.charAt(i + 1) == ' ') {
						throw new IllegalArgumentException(
								"Missing operand between " + lastOperator + " and " + expression.charAt(i));
					}
					if (expression.charAt(i - 1) == 'n') {
						throw new IllegalArgumentException(
								"Missing operand between " + lastOperator + " and " + expression.charAt(i));
					}

					expression =

							expression.substring(0, i) + "n" + expression.substring(i + 1);
				}

				lastOperatorOffset = i;
				lastOperator = expression.charAt(i);
			}
		}
		if (lastOperatorOffset == expression.length() - 1) {
			throw new IllegalArgumentException("Expression cannot end with an operator.");
		}

		return expression;
	}

	private String stripTrailingZeros(String number) {
		int decimalPointOffset = number.indexOf(".");
		if (decimalPointOffset < 0)
			return number;
		while (number.endsWith("0"))
			number = number.substring(0, number.length() - 1);
		if (number.endsWith(".")) {
			number = number.substring(0, number.length() - 1);
		}
		return number;
	}

	private double evaluateSimpleExpression(String expression) throws IllegalArgumentException {
		char operator = 'o';
		
		int i;
		for (i = 1; i < expression.length(); i++) {
			if ((expression.charAt(i) == '+') || (expression.charAt(i) == '-') || (expression.charAt(i) == '*')
					|| (expression.charAt(i) == '/') || (expression.charAt(i) == '^')
					|| (expression.charAt(i) == 'r')) {
				operator = expression.charAt(i);
				break;
			}
		}
		if ((i == expression.length()) || (i == expression.length() - 1)) {
			throw new IllegalArgumentException(
					"'Simple Expression' " + expression + " is not an operator surrounded by operators.");
		}

		String leftPart = expression.substring(0, i).trim();

		if (leftPart.startsWith("n")) {
			leftPart = "-" + leftPart.substring(1);
		}
		String rightPart = expression.substring(i + 1).trim();

		if (rightPart.startsWith("n"))
			rightPart = "-" + rightPart.substring(1);
		
		double leftNumber;
		double rightNumber;
		try {
			leftNumber = Double.parseDouble(leftPart);
			rightNumber = Double.parseDouble(rightPart);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(
					"Left or right operand of simple expression " + expression + " is not numeric.");
		}

		switch (operator) {
		case '+':
			return leftNumber + rightNumber;
		case '-':
			return leftNumber - rightNumber;
		case '*':
			return leftNumber * rightNumber;
		case '/':
			return leftNumber / rightNumber;
		case '^':
			return Math.pow(leftNumber, rightNumber);
		case 'r':
			return Math.pow(leftNumber, 1.0D / rightNumber);
		}
		throw new IllegalArgumentException("INTERNAL ERROR: Operator " + operator + " not recognized in switch.");
	}

	private double evaluateComplexExpression(String expression) throws IllegalArgumentException {
		int operatorOffset = 0;
		char[] operators = { '^', 'r', '*', '/', '+', '-' };

		for (int i = 0; i < operators.length; i += 2) {

			for (;;) {

				if (expression.startsWith("-")) {
					expression = 'n' + expression.substring(1);
				} else {
					int operator1Offset = expression.indexOf(operators[i]);
					if (operator1Offset == 0)
						throw new IllegalArgumentException(
								"Expression (or inner expression) starts with operator " + operators[i]);
					if (operator1Offset == expression.length() - 1)
						throw new IllegalArgumentException(
								"Expression (or inner expression) ends with operator " + operators[i]);
					int operator2Offset = expression.indexOf(operators[(i + 1)]);
					if (operator2Offset == 0)
						throw new IllegalArgumentException(
								"Expression (or inner expression) starts with operator " + operators[(i + 1)]);
					if (operator2Offset == expression.length() - 1) {
						throw new IllegalArgumentException(
								"Expression (or inner expression) ends with operator " + operators[(i + 1)]);
					}
					if ((operator1Offset < 0) && (operator2Offset < 0)) {
						break;
					}

					if ((operator1Offset > 0) && (operator2Offset > 0)) {
						if (operator1Offset < operator2Offset) {
							operatorOffset = operator1Offset;
						} else {
							operatorOffset = operator2Offset;
						}

					} else if (operator1Offset > 0) {
						operatorOffset = operator1Offset;
					} else {
						operatorOffset = operator2Offset;
					}
					expression = replaceInnerExpression(expression, operatorOffset);
				}
			}
		}

		try {
			if (expression.startsWith("n"))
				expression = "-" + expression.substring(1);
			return Double.parseDouble(expression);

		} catch (NumberFormatException nfe) {

			throw new IllegalArgumentException("Invalid expression. Missing operator before or after parenthesis.");
		}
	}

	private String replaceInnerExpression(String expression, int operatorOffset) throws IllegalArgumentException {
		boolean noNextOperator = false;
		boolean noPreviousOperator = false;
		
		int i;
		for (i = operatorOffset + 1; i < expression.length(); i++) {
			if ((expression.charAt(i) == '+') || (expression.charAt(i) == '-') || (expression.charAt(i) == '*')
					|| (expression.charAt(i) == '/') || (expression.charAt(i) == '^') || (expression.charAt(i) == 'r'))
				break;
		}
		if (i == expression.length() - 1)
			throw new IllegalArgumentException(
					"Expression (or an inner-expression) ends with operator " + expression.charAt(i));
		int nextOperatorOffset = i;
		if (i == expression.length()) {
			noNextOperator = true;
		}
		for (i = operatorOffset - 1;

				i >= 0; i--) {
			if ((expression.charAt(i) == '+') || (expression.charAt(i) == '-') || (expression.charAt(i) == '*')
					|| (expression.charAt(i) == '/') || (expression.charAt(i) == '^') || (expression.charAt(i) == 'r'))
				break;
		}
		if (i == 0)
			throw new IllegalArgumentException("Expression (or sub-expression) starts with operator " + expression.charAt(0));
		int previousOperatorOffset = i;
		if (i < 0) {
			noPreviousOperator = true;
		}
		String innerExpression = expression.substring(previousOperatorOffset + 1, nextOperatorOffset);

		double innerExpressionValue = evaluateSimpleExpression(innerExpression);
		String resultString = String.valueOf(innerExpressionValue);
		if (resultString.contains("E-")) {
			System.out.println("detected negative exponential format in replaceInnerExpression()");
			System.out.println("Value returned from evaluateSimpleExpression is " + resultString);

			BigDecimal resultBD = new BigDecimal(innerExpressionValue, MathContext.DECIMAL32);
			resultBD = resultBD.setScale(7, 1);
			resultString = resultBD.toPlainString();
			resultString = stripTrailingZeros(resultString);
		}

		if ((noNextOperator) && (noPreviousOperator)) {
			return resultString;
		}

		expression =

				expression.substring(0, previousOperatorOffset + 1) + resultString.replaceFirst("-", "n")
						+ expression.substring(nextOperatorOffset);

		return expression;
	}

	private String removeParentheses(String expression) {

		int leftCount = 0;
		int rightCount = 0;
		for (int i = 0; i < expression.length(); i++) {
			if (expression.charAt(i) == '(')
				leftCount++;
			if (expression.charAt(i) == ')')
				rightCount++;
		}
		if (leftCount != rightCount) {
			throw new IllegalArgumentException("Unequal number of left and right parentheses.");
		}

		while ((expression.contains("(")) || (expression.contains(")"))) {
			int leftParenOffset = -1;
			int rightParenOffset = -1;
			for (int i = 0; i < expression.length(); i++) {
				if (expression.charAt(i) == '(')
					leftParenOffset = i;
				if (expression.charAt(i) == ')') {
					rightParenOffset = i;
					break;
				}
			}
			if (leftParenOffset < 0) {
				throw new IllegalArgumentException("Unbalanced parentheses.");
			}

			double result;
			try {
				String innerExpression = expression.substring(leftParenOffset + 1, rightParenOffset).trim();
				if (innerExpression.length() == 0)
					throw new IllegalArgumentException("Empty parentheses");
				innerExpression = replaceUnary(innerExpression);
				result = evaluateComplexExpression(innerExpression);
			} catch (IllegalArgumentException iae) {
				throw iae;
			}

			expression = expression.substring(0, leftParenOffset) + " " + String.valueOf(result).replaceFirst("-", "n")
							+ expression.substring(rightParenOffset + 1);
		}

		return expression;
	}
  	
public double CalculateYscale(double yMin, double yMax) {
		double dPlotRange;
		int plotRange, initialIncrement, upperIncrement, lowerIncrement, selectedIncrement, numberOfYscaleValues,
				lowestYscaleValue, highestYscaleValue;
		String zeros = "0000000000";
		try {
			if (yMin > yMax) {
				double temp = yMax;
				yMax = yMin;
				yMin = temp;
			}
			System.out.println("Entered values are: Ymin = " + yMin + " Ymax = " + yMax);
		} catch (NumberFormatException nfe) {
			System.out.println("Both input parms must be numeric.");
			return -1;
		}

		// 1) Determine the RANGE to be plotted.
		dPlotRange = yMax - yMin;
		System.out.println("Plot range (Ymax-Ymin) = " + dPlotRange);

		// 2) Determine an initial increment value.
		if (dPlotRange > 10) {
			plotRange = (int) dPlotRange;
			System.out.println("Rounded plot range = " + plotRange);
		} else {
			System.out.println("Add handling of small plot range!");
			return -1;
		}
		/* ASSUME */ // 10 scale values as a starting assumption.
		initialIncrement = plotRange / 10;
		System.out.println("Initial increment value = " + initialIncrement);
		// Please excuse this clumsy "math"!
		String initialIncrementString = String.valueOf(initialIncrement);
		// System.out.println("InitialIncrementString = " + initialIncrementString + "
		// (length = " + initialIncrementString.length() + ")");

		// 3) Find even numbers above and below the initial increment.
		String leadingDigit = initialIncrementString.substring(0, 1);
		int leadingNumber = Integer.parseInt(leadingDigit);
		int bumpedLeadingNumber = leadingNumber + 1;
		String bumpedLeadingDigit = String.valueOf(bumpedLeadingNumber);
		String upperIncrementString = bumpedLeadingDigit + zeros.substring(0, initialIncrementString.length() - 1);
		String lowerIncrementString = leadingDigit + zeros.substring(0, initialIncrementString.length() - 1);
		upperIncrement = Integer.parseInt(upperIncrementString);
		lowerIncrement = Integer.parseInt(lowerIncrementString);
		System.out.println("Upper increment alternative = " + upperIncrement);
		System.out.println("Lower increment alternative = " + lowerIncrement);

		// 4) Pick the upper or lower even increment depending on which is closest.
		int distanceToUpper = upperIncrement - initialIncrement;
		int distanceToLower = initialIncrement - lowerIncrement;
		if (distanceToUpper > distanceToLower)
			selectedIncrement = lowerIncrement;
		else
			selectedIncrement = upperIncrement;
		System.out.println("The closest even increment (and therefore the one chosen) = " + selectedIncrement);

		// 5) Determine lowest Y scale value
		numberOfYscaleValues = 0;
		lowestYscaleValue = 0;
		if (yMin < 0) {
			for (; lowestYscaleValue > yMin; lowestYscaleValue -= selectedIncrement)
				numberOfYscaleValues++;
		}
		if (yMin > 0) {
			for (; lowestYscaleValue < yMin; lowestYscaleValue += selectedIncrement)
				numberOfYscaleValues++;
			numberOfYscaleValues--;
			lowestYscaleValue -= selectedIncrement;
		}
		System.out.println("The lowest Y scale value will be " + lowestYscaleValue + ")");

		// 6) Determine upper Y scale value
		numberOfYscaleValues = 1;
		for (highestYscaleValue = lowestYscaleValue; highestYscaleValue < yMax; highestYscaleValue += selectedIncrement)
			numberOfYscaleValues++;
		System.out.println("The highest Y scale value will be " + highestYscaleValue);
		System.out.println("The number of Y scale click marks will be " + numberOfYscaleValues);
		if ((numberOfYscaleValues < 5) || (numberOfYscaleValues > 20)) {
			System.out.println("Number of Y scale click marks is too few or too many!");
			return -1;
		}

		// 7) Determine if Y scale will be extended to include the 0 point.
		if ((lowestYscaleValue < 0) && (highestYscaleValue > 0))
			System.out.println("The Y scale includes the 0 point.");
		else // Y scale does not include 0.
		{ // Should it be extended to include the 0 point?
			if ((lowestYscaleValue > 0) && (lowestYscaleValue / selectedIncrement <= 3)) {
				lowestYscaleValue = 0;
				System.out.println(
						"Lower Y scale value adjusted down to 0 to include 0 point. (Additional click marks added.)");
			}
			if ((highestYscaleValue < 0) && (highestYscaleValue / selectedIncrement <= 3)) {
				highestYscaleValue = 0;
				System.out.println(
						"Upper Y scale value adjusted up to 0 to include 0 point. (Additional click marks added.)");
			}
		}
		int yScaleValue = lowestYscaleValue;
		while (yScaleValue < highestYscaleValue) {
			System.out.print(yScaleValue + ",");
			yScaleValue += selectedIncrement;
		}
		return yScaleValue;
	}
}