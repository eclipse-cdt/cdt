/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.pro.parser;

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Allows for the manipulation of information stored in a Qt Project File. At the moment the only modifiable information is that
 * which is contained within variables such as the following:
 *
 * <pre>
 * <code>SOURCES += file.cpp \ # This is the first line with value "file.cpp"
 *     file2.cpp # This is the second line with value "file2.cpp"</code>
 * </pre>
 *
 * This class supports the following modifications to variables:
 * <ul>
 * <li><b>Add Value</b>: If the specified String does not exist in the given variable then it is added as a new line at the end of
 * the variable declaration. A line escape (\) is also inserted into the preceding line.</li>
 * <li><b>Remove Value</b>: If the specified String exists in the given variable then it is removed. The line escape character (\)
 * is also removed from the preceding line if necessary.</li>
 * <li><b>Replace Value</b>: If the specified String exists as a line in the given variable, then it is replaced with another
 * String. All spacing is preserved as only the value itself is modified.</li>
 * </ul>
 * <p>
 * Comments may appear after the line escape character (\) in a variable Declaration. For this case, replace and addition operations
 * will preserve these comments. However, a comment will not be preserved if its line is deleted during a remove operation.
 * </p>
 */
public class QtProjectFileModifier {

	private QtProjectFileParser parser;
	private IDocument document;

	public QtProjectFileModifier(IDocument doc) {
		if (doc == null) {
			throw new IllegalArgumentException("document cannot be null"); //$NON-NLS-1$
		}

		this.document = doc;
		this.parser = new QtProjectFileParser(doc);
	}

	public QtProjectFileModifier(QtProjectFileParser parser) {
		if (parser == null) {
			throw new IllegalArgumentException("parser cannot be null"); //$NON-NLS-1$
		}

		this.document = parser.getDocument();
		this.parser = parser;
	}

	/**
	 * Attempts to replace the given value with a new value if it is found within the given variable name. This is a convenience
	 * method equivalent to <code>replaceVariableValue(variable,oldValue,newValue,true)</code> and will only match values that
	 * occupy an entire line within the variable declaration.
	 * <p>
	 * This method does <b>not</b> create a new value if the specified <code>oldValue</code> was not found. If this behavior is
	 * desired, then check for a return of <code>false</code> from this method and then call the <code>addVariableValue</code>
	 * method.
	 * </p>
	 * <p>
	 * <b>Note:</b> The "entire line" refers to only the value as it appears in the variable declaration. That is, any whitespace
	 * before or after will not be included when matching a value to the "entire line".
	 * </p>
	 *
	 * @param variable
	 *            the name of the variable
	 * @param oldValue
	 *            the value that will be replaced
	 * @param newValue
	 *            the value to replace with
	 * @return whether or not the value was able to be replaced
	 */
	public boolean replaceVariableValue(String variable, String oldValue, String newValue) {
		return replaceVariableValue(variable, oldValue, newValue, true);
	}

	/**
	 * Attempts to replace the first instance of <code>oldValue</code> with <code>newValue</code> if it is found within the given
	 * variable name. If <code>matchWholeLine</code> is false, this method will try to match sections of each line with the value of
	 * <code>oldValue</code>. If a match is found, only that portion of the line will be replaced. If <code>matchWholeLine</code> is
	 * true, this method will try to match the entire line with the value of <code>oldValue</code> and will replace that. All other
	 * line spacing and comments are preserved as only the value itself is replaced.
	 * <p>
	 * This method does <b>not</b> create a new value if <code>oldValue</code> was not found. If this behavior is desired, then
	 * check for a return of <code>false</code> from this method and then call the <code>addVariableValue</code> method.
	 * </p>
	 * <p>
	 * <b>Note:</b> The "entire line" refers to only the value as it appears in the variable declaration. That is, any whitespace
	 * before or after will not be included when matching a value to the "entire line".
	 * </p>
	 *
	 * @param variable
	 *            the name of the variable
	 * @param oldValue
	 *            the value that will be replaced
	 * @param newValue
	 *            the value to replace with
	 * @param matchWholeLine
	 *            whether or not the value should match the entire line
	 * @return whether or not the value was able to be replaced
	 */
	public boolean replaceVariableValue(String variable, String oldValue, String newValue, boolean matchWholeLine) {
		QtProjectVariable var = parser.getVariable(variable);

		if (var != null) {
			if (matchWholeLine) {
				int line = var.getValueIndex(oldValue);
				if (line >= 0) {
					return replaceVariableValue(var, line, newValue);
				}
			} else {
				int line = 0;
				for (String value : var.getValues()) {
					int offset = value.indexOf(oldValue);
					if (offset >= 0) {
						return replaceVariableValue(var, line, var.getValueOffsetForLine(line) + offset,
								oldValue.length(), newValue);
					}
					line++;
				}
			}
		}
		return false;
	}

	private boolean replaceVariableValue(QtProjectVariable var, int lineNo, String newValue) {
		int offset = var.getValueOffsetForLine(lineNo);
		String value = var.getValueForLine(lineNo);
		int length = value.length();

		return replaceVariableValue(var, lineNo, offset, length, newValue);
	}

	private boolean replaceVariableValue(QtProjectVariable var, int lineNo, int offset, int length, String newValue) {
		try {
			document.replace(offset, length, newValue);
			return true;
		} catch (BadLocationException e) {
			Activator.log(e);
		}
		return false;
	}

	/**
	 * Adds <code>value</code> to the specified variable as a new line and escapes the previous line with a backslash. The escaping
	 * is done in such a way that comments and spacing are preserved on the previous line. If this variable does not exist, a new
	 * one is created at the bottom-most position of the document with the initial value specified by <code>value</code>.
	 *
	 * @param variable
	 *            the name of the variable to add to
	 * @param value
	 *            the value to add to the variable
	 */
	public void addVariableValue(String variable, String value) {
		QtProjectVariable var = parser.getVariable(variable);

		if (var != null) {
			if (var.getValueIndex(value) < 0) {
				int line = var.getNumberOfLines() - 1;
				String indent = var.getIndentString(line);

				int offset = var.getEndOffset();
				if (var.getLine(line).endsWith("\n")) { //$NON-NLS-1$
					offset--;
				}

				try {
					document.replace(offset, 0, "\n" + indent + value); //$NON-NLS-1$
				} catch (BadLocationException e) {
					Activator.log(e);
				}

				try {
					offset = var.getLineEscapeReplacementOffset(line);
					String lineEscape = var.getLineEscapeReplacementString(line);

					document.replace(offset, 0, lineEscape);
				} catch (BadLocationException e) {
					Activator.log(e);
				}
			}
		} else {
			// Variable does not exist, create it
			String baseVariable = variable + " += " + value + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

			// Check the contents of the document and re-format accordingly
			if (document.get().trim().isEmpty()) {
				try {
					document.replace(0, document.getLength(), baseVariable);
				} catch (BadLocationException e) {
					Activator.log(e);
				}
			} else if (document.get().endsWith("\n")) { //$NON-NLS-1$
				try {
					document.replace(document.getLength(), 0, "\n" + baseVariable); //$NON-NLS-1$
				} catch (BadLocationException e) {
					Activator.log(e);
				}
			} else {
				try {
					document.replace(document.getLength(), 0, "\n\n" + baseVariable); //$NON-NLS-1$
				} catch (BadLocationException e) {
					Activator.log(e);
				}
			}
		}
	}

	/**
	 * Removes <code>value</code> from the specified variable and removes the previous line escape if necessary. The entire line is
	 * removed including any comments. If the value is not found, nothing happens.
	 *
	 * @param variable
	 *            the name of the variable to remove from
	 * @param value
	 *            the value to remove from the variable
	 */
	public void removeVariableValue(String variable, String value) {
		QtProjectVariable var = parser.getVariable(variable);

		if (var != null) {
			int line = var.getValueIndex(value);
			if (line == 0 && var.getNumberOfLines() > 1) {
				// Entering this block means we're removing the first line where more lines exist.
				int offset = var.getValueOffsetForLine(line);
				int end = var.getValueOffsetForLine(line + 1);

				try {
					document.replace(offset, end - offset, ""); //$NON-NLS-1$
				} catch (BadLocationException e) {
					Activator.log(e);
				}
			} else if (line >= 0) {
				int offset = var.getLineOffset(line);
				int length = var.getLine(line).length();
				if (line > 0) {
					// Remove the previous line feed character
					offset--;
					length++;
				}

				try {
					document.replace(offset, length, ""); //$NON-NLS-1$
				} catch (BadLocationException e) {
					Activator.log(e);
				}

				// Remove the previous line's line escape character if necessary
				if (line > 0 && line == var.getNumberOfLines() - 1) {
					try {
						offset = var.getLineEscapeOffset(line - 1);
						length = var.getLineEscapeEnd(line - 1) - offset;

						document.replace(offset, length, ""); //$NON-NLS-1$
					} catch (BadLocationException e) {
						Activator.log(e);
					}
				}
			}
		}
	}

	/**
	 * Get the <code>IDocument</code> currently being modified by this class.
	 *
	 * @return the document being modified
	 */
	public IDocument getDocument() {
		return document;
	}
}
