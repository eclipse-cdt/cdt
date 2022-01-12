/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.osgi.util.NLS;

/**
 * Performs string substitution for context and value variables.
 * A clone of {@link org.eclipse.core.internal.variables.StringSubstitutionEngine}.
 */
public class StringSubstitutionEngine {
	// Delimiters.
	private static final String VARIABLE_START = "${"; //$NON-NLS-1$
	private static final char VARIABLE_END = '}';
	private static final char VARIABLE_ARG = ':';
	// Parsing states.
	private static final int SCAN_FOR_START = 0;
	private static final int SCAN_FOR_END = 1;

	/**
	 * Resulting string
	 */
	private StringBuilder fResult;

	/**
	 * Whether substitutions were performed
	 */
	private boolean fSubs;

	/**
	 * Stack of variables to resolve
	 */
	private Stack<VariableReference> fStack;

	class VariableReference {
		// The text inside the variable reference.
		private StringBuilder fText;

		public VariableReference() {
			fText = new StringBuilder();
		}

		public void append(String text) {
			fText.append(text);
		}

		public String getText() {
			return fText.toString();
		}
	}

	/**
	 * Performs recursive string substitution and returns the resulting string.
	 *
	 * @param expression expression to resolve
	 * @param reportUndefinedVariables whether to report undefined variables as an error
	 * @param resolveVariables if the variables should be resolved during the substitution
	 * @param manager registry of variables
	 * @return the resulting string with all variables recursively substituted
	 * @exception CoreException if unable to resolve a referenced variable or if a cycle exists
	 *     in referenced variables
	 */
	public String performStringSubstitution(String expression, boolean reportUndefinedVariables,
			boolean resolveVariables, IStringVariableManager manager) throws CoreException {
		substitute(expression, reportUndefinedVariables, resolveVariables, manager);
		List<HashSet<String>> resolvedVariableSets = new ArrayList<>();
		while (fSubs) {
			HashSet<String> resolved = substitute(fResult.toString(), reportUndefinedVariables, true, manager);

			for (int i = resolvedVariableSets.size(); --i >= 0;) {
				HashSet<String> prevSet = resolvedVariableSets.get(i);

				if (prevSet.equals(resolved)) {
					HashSet<String> conflictingSet = new HashSet<>();
					for (HashSet<String> set : resolvedVariableSets) {
						conflictingSet.addAll(set);
					}

					StringBuilder problemVariableList = new StringBuilder();
					for (String var : conflictingSet) {
						problemVariableList.append(var.toString());
						problemVariableList.append(", "); //$NON-NLS-1$
					}
					problemVariableList.setLength(problemVariableList.length() - 2); // Truncate the last ", "
					throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(),
							CDebugCorePlugin.INTERNAL_ERROR,
							NLS.bind(InternalDebugCoreMessages.StringSubstitutionEngine_undefined_variable,
									problemVariableList.toString()),
							null));
				}
			}

			resolvedVariableSets.add(resolved);
		}
		return fResult.toString();
	}

	/**
	 * Performs recursive string validation to ensure that all of the variables
	 * contained in the expression exist.
	 *
	 * @param expression expression to validate
	 * @param manager registry of variables
	 * @exception CoreException if a referenced variable does not exist or if a cycle exists
	 *     in referenced variables
	 */
	public void validateStringVariables(String expression, IStringVariableManager manager) throws CoreException {
		performStringSubstitution(expression, true, false, manager);
	}

	/**
	 * Makes a substitution pass of the given expression returns a Set of the variables that were
	 * resolved in this pass.
	 *
	 * @param expression source expression
	 * @param reportUndefinedVariables whether to report undefined variables as an error
	 * @param resolveVariables whether to resolve the value of any variables
	 * @param manager the {@link IStringVariableManager} to use for the substitution
	 * @return the set of {@link String}s resolved from the given expression
	 * @exception CoreException if unable to resolve a variable
	 */
	private HashSet<String> substitute(String expression, boolean reportUndefinedVariables, boolean resolveVariables,
			IStringVariableManager manager) throws CoreException {
		fResult = new StringBuilder(expression.length());
		fStack = new Stack<>();
		fSubs = false;

		HashSet<String> resolvedVariables = new HashSet<>();

		int pos = 0;
		int state = SCAN_FOR_START;
		while (pos < expression.length()) {
			switch (state) {
			case SCAN_FOR_START:
				int start = expression.indexOf(VARIABLE_START, pos);
				if (start >= 0) {
					int length = start - pos;
					// Copy non-variable text to the result.
					if (length > 0) {
						fResult.append(expression.substring(pos, start));
					}
					pos = start + 2;
					state = SCAN_FOR_END;

					fStack.push(new VariableReference());
				} else {
					// Done - no more variables.
					fResult.append(expression.substring(pos));
					pos = expression.length();
				}
				break;
			case SCAN_FOR_END:
				// Be careful of nested variables.
				start = expression.indexOf(VARIABLE_START, pos);
				int end = expression.indexOf(VARIABLE_END, pos);
				if (end < 0) {
					// Variables are not completed.
					VariableReference tos = fStack.peek();
					tos.append(expression.substring(pos));
					pos = expression.length();
				} else {
					if (start >= 0 && start < end) {
						// Start of a nested variable.
						int length = start - pos;
						if (length > 0) {
							VariableReference tos = fStack.peek();
							tos.append(expression.substring(pos, start));
						}
						pos = start + 2;
						fStack.push(new VariableReference());
					} else {
						// End of variable reference.
						VariableReference tos = fStack.pop();
						String substring = expression.substring(pos, end);
						tos.append(substring);
						resolvedVariables.add(substring);

						pos = end + 1;
						String value = resolve(tos, reportUndefinedVariables, resolveVariables, manager);
						if (value == null) {
							value = ""; //$NON-NLS-1$
						}
						if (fStack.isEmpty()) {
							// Append to result.
							fResult.append(value);
							state = SCAN_FOR_START;
						} else {
							// Append to previous variable.
							tos = fStack.peek();
							tos.append(value);
						}
					}
				}
				break;
			}
		}
		// Process incomplete variable references.
		while (!fStack.isEmpty()) {
			VariableReference tos = fStack.pop();
			if (fStack.isEmpty()) {
				fResult.append(VARIABLE_START);
				fResult.append(tos.getText());
			} else {
				VariableReference var = fStack.peek();
				var.append(VARIABLE_START);
				var.append(tos.getText());
			}
		}

		return resolvedVariables;
	}

	/**
	 * Resolve and return the value of the given variable reference, possibly {@code null}.
	 *
	 * @param var the {@link VariableReference} to try and resolve
	 * @param reportUndefinedVariables whether to report undefined variables as an error
	 * @param resolveVariables whether to resolve the variables value or just to validate that
	 *     this variable is valid
	 * @param manager variable registry
	 * @return variable value, possibly {@code null}
	 * @exception CoreException if unable to resolve a value
	 */
	private String resolve(VariableReference var, boolean reportUndefinedVariables, boolean resolveVariables,
			IStringVariableManager manager) throws CoreException {
		String text = var.getText();
		int pos = text.indexOf(VARIABLE_ARG);
		String name = null;
		String arg = null;
		if (pos > 0) {
			name = text.substring(0, pos);
			pos++;
			if (pos < text.length()) {
				arg = text.substring(pos);
			}
		} else {
			name = text;
		}
		IValueVariable valueVariable = manager.getValueVariable(name);
		if (valueVariable == null) {
			IDynamicVariable dynamicVariable = manager.getDynamicVariable(name);
			if (dynamicVariable == null) {
				// No variables with the given name.
				if (reportUndefinedVariables) {
					throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(),
							CDebugCorePlugin.INTERNAL_ERROR,
							NLS.bind(InternalDebugCoreMessages.StringSubstitutionEngine_undefined_variable, name),
							null));
				}
				// Leave as is.
				return getOriginalVarText(var);
			}

			if (resolveVariables) {
				fSubs = true;
				return dynamicVariable.getValue(arg);
			}
			// Leave as is.
			return getOriginalVarText(var);
		}

		if (arg == null) {
			if (resolveVariables) {
				fSubs = true;
				return valueVariable.getValue();
			}
			// Leave as is.
			return getOriginalVarText(var);
		}
		// Error - an argument specified for a value variable.
		throw new CoreException(
				new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR,
						NLS.bind(InternalDebugCoreMessages.StringSubstitutionEngine_unexpected_argument,
								valueVariable.getName()),
						null));
	}

	private String getOriginalVarText(VariableReference var) {
		StringBuilder res = new StringBuilder(var.getText());
		res.insert(0, VARIABLE_START);
		res.append(VARIABLE_END);
		return res.toString();
	}
}
