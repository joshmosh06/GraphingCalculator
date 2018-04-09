//ECE 309  April 3rd, 2018  Team Project: Josh Hofmann, Iason Katsaros, Randy Paluszkiewicz, Brian Cuthrell

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class GraphingCalculator implements Calculator, ActionListener {
	// GUI Globals
	JFrame calculatorWindow = new JFrame();
	JPanel topPanel = new JPanel();
	JPanel midPanel = new JPanel();
	JPanel botPanel = new JPanel();

	JLabel enterExpressionLabel = new JLabel();
	JLabel enterXLabel = new JLabel();
	JLabel IncrementxLabel = new JLabel("with x Increments of =", 4);
	JTextField enterExpressionTextField = new JTextField();
	JTextField enterXTextField = new JTextField();
	JTextArea displayTextArea = new JTextArea();
	JTextField errorTextField = new JTextField();
	JButton clearButton = new JButton("Clear Text Fields");
	JButton recallButton = new JButton("Get Last Entered Values");
	JTextField XIncrementTextField = new JTextField(8);
	JScrollPane displayScrollPane = new JScrollPane(displayTextArea);
	// Global Variables
	String expressionInstructs = "Enter an algebraic expression. e.g. x^2 [x squared] with a x value to be solved for OR a starting x value for the graph.";
	String previousExpression;
	String previousXString;
	String previousXIncrement;
	ArrayList<Integer> operatorIndexList = new ArrayList<Integer>();
	Vector<String> operators = new Vector<String>();
	private Object instanceObj;
	private Class<?> classObj;
	private String previousIncrement;

	public static void main(String[] args) {
		try {
			new GraphingCalculator();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public GraphingCalculator() throws Exception {
		// Create the GUI
		// ===================================================================================
		UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		calculatorWindow.setTitle("Expression Calculator");
		calculatorWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		calculatorWindow.getContentPane().add(topPanel, "North");
		calculatorWindow.getContentPane().add(botPanel, "South");
		calculatorWindow.add(midPanel);
		// Top Panel Items
		enterExpressionLabel = new JLabel("Enter Expression:");
		enterExpressionLabel.setHorizontalAlignment(JLabel.CENTER);
		enterExpressionTextField.setText("");
		enterExpressionTextField.addActionListener(this); // activate!
		enterXLabel = new JLabel("For X = ");
		enterXLabel.setHorizontalAlignment(JLabel.CENTER);
		enterXTextField.setText("");
		enterXTextField.addActionListener(this); // activate!
		XIncrementTextField.setText("");
		XIncrementTextField.addActionListener(this);
		// Add to Top Panel
		topPanel.setLayout(new GridLayout(1, 6));
		topPanel.add(enterExpressionLabel);
		topPanel.add(enterExpressionTextField);
		topPanel.add(enterXLabel);
		topPanel.add(enterXTextField);
		topPanel.add(IncrementxLabel);
		topPanel.add(XIncrementTextField);
		// Center Panel
		displayTextArea.setFont(new Font("default", Font.BOLD, 14));
		displayTextArea.setEditable(false); // keep cursor out
		displayTextArea.setLineWrap(true);
		displayTextArea.setWrapStyleWord(true);
		// Add to Center Panel
		midPanel.setLayout(new GridLayout(1, 1));
		midPanel.add(displayScrollPane);
		// Bottom Panel
		errorTextField.setText("Errors Will Appear Here");
		errorTextField.setEditable(false); // keep cursor out
		errorTextField.setBackground(Color.pink);
		errorTextField.setForeground(Color.black);
		clearButton.addActionListener(this); // activate!
		clearButton.setBackground(Color.green);
		recallButton.addActionListener(this); // activate!
		recallButton.setBackground(Color.cyan);
		// Add to Bottom Panel
		botPanel.setLayout(new GridLayout(1, 3));
		botPanel.add(clearButton);
		botPanel.add(recallButton);
		botPanel.add(errorTextField);
		// Pretext to guide user
		displayTextArea.setText(expressionInstructs);
		// Enable the GUI
		calculatorWindow.setSize(800, 500); // width, height (in "pixels"!)
		calculatorWindow.setVisible(true); // show it!
		// ===================================================================================
		// ===================================================================================
		operators.add("^");
		operators.add("r");
		operators.add("*");
		operators.add("/");
		operators.add("+");
		operators.add("-");
		// Create class connection using java reflection
		classObj = Class.forName("ExpressionCalculator");
		instanceObj = classObj.newInstance();
		JFrame windowBowman = (JFrame) instanceObj.getClass().getDeclaredField("window").get(instanceObj);
		windowBowman.setVisible(false);
		windowBowman.hide();
		windowBowman.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		errorTextField.setText("");
		errorTextField.setBackground(Color.pink);
		if (ae.getSource() == enterExpressionTextField || ae.getSource() == enterXTextField
				|| ae.getSource() == XIncrementTextField) {
			try {
				String expression = enterExpressionTextField.getText().trim();
				String XString = enterXTextField.getText().trim();
				String increments = XIncrementTextField.getText().trim();
				double incrementsVal = -1;
				try {
					incrementsVal = Double.parseDouble(increments);
				} catch (Exception e) {
					if (incrementsVal <= 0 && !increments.equals("")) {
						throw new IllegalArgumentException("Invalid Increment entered," + increments);
					}
				}
				if (incrementsVal <= 0 && !increments.equals("")) {
					throw new IllegalArgumentException("Invalid Increment entered must be postive," + incrementsVal);
				}
				previousExpression = expression;
				previousXString = XString;
				previousIncrement = increments;
				double result = calculate(expression, XString);
				Double temp = new Double(result);
				String resultString;

				enterExpressionTextField.setText("");
				enterXTextField.setText("");
				XIncrementTextField.setText("");
				if (temp.isInfinite() || temp.isNaN()) {
					resultString = temp.toString();
				} else {
					resultString = temp.toString();
				}

				displayTextArea.append("\n" + expression + " = " + resultString);
				if (previousExpression.contains("x") || previousExpression.contains("X")) {
					displayTextArea.append(" for X = " + XString);
				}
				displayTextArea.setCaretPosition(displayTextArea.getDocument().getLength());
				// Graphing Window
				if ((incrementsVal > 0) && (XString.length() > 0)) {
					// if you make it in here graph
					double[] xValues = calculateXValues(expression, XString, incrementsVal);
					double[] yValues = calculateYValues(expression, XString, incrementsVal);
					RefreshGraphPanel graphPanel = new RefreshGraphPanel(this, expression, xValues, yValues);
				}

			} catch (Exception e) {
				if (errorTextField.getText().equals("")) {
					errorTextField.setText(e.getMessage());
				}

			}
		}
		if (ae.getSource() == clearButton) {
			enterExpressionTextField.setText("");
			enterXTextField.setText("");
			errorTextField.setText("");
			XIncrementTextField.setText("");
			return;
		}
		if (ae.getSource() == recallButton) {
			enterExpressionTextField.setText(previousExpression);
			enterXTextField.setText(previousXString);
			XIncrementTextField.setText(previousIncrement);
			return;
		}
	}

	private double[] calculateYValues(String expression, String xString, double increments) throws Exception {
		Double x = Double.parseDouble(xString);
		ArrayList<Double> yValues = new ArrayList<Double>();
		for (int i = 0; i <= 10; i++) {
			yValues.add(calculate(expression, Double.toString((x + (i * increments)))));
		}
		double[] arry = new double[yValues.size()];
		int i = 0;
		for (double d : yValues) {
			arry[i] = d;
			i++;
		}
		return arry;
	}

	// creates an array of x points to be graphed from the user defined increments
	// and x value
	public double[] calculateXValues(String expression, String xString, double increments) throws Exception {
		Double x = Double.parseDouble(xString);
		ArrayList<Double> xValues = new ArrayList<Double>();
		for (int i = 0; i <= 10; i++) {
			xValues.add(x + (i * increments));
		}
		double[] arry = new double[xValues.size()];
		int i = 0;
		for (double d : xValues) {
			arry[i] = d;
			i++;
		}
		return arry;

	}

	@Override
	public double calculate(String expression, String x) throws Exception {
		Method[] methods = classObj.getDeclaredMethods();
		Method meth = null;
		for (Method method : methods) {
			if (method.getName().equals("calculate")) {
				meth = method;
			}
		}
		double result = -1;
		try {
			result = (double) meth.invoke(instanceObj, expression, x);
		} catch (Exception e) {
			errorTextField.setText(e.getCause().getMessage());
			throw e;
		}
		return result;
	}
}