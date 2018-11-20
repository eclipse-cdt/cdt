/*******************************************************************************
 * Copyright (c) 2005, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Andrew Gvozdev (Quoin Inc.)
 *******************************************************************************/
package org.eclipse.cdt.utils.cdtvariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;
import org.eclipse.cdt.internal.core.cdtvariables.CdtMacroSupplier;

/**
 * Utility class to resolve macro and variable references. Provides fixture to parse ${macro}
 * expressions and replace macros with actual values using {@link IVariableSubstitutor}.
 *
 * @since 3.0
 */
public class CdtVariableResolver {
	/** @since 5.5 */
	public static final String VAR_CONFIG_NAME = CdtMacroSupplier.VAR_CONFIG_NAME;
	/** @since 5.5 */
	public static final String VAR_CONFIG_DESCRIPTION = CdtMacroSupplier.VAR_CONFIG_DESCRIPTION;
	/** @since 5.5 */
	public static final String VAR_PROJ_NAME = CdtMacroSupplier.VAR_PROJ_NAME;
	/** @since 5.5 */
	public static final String VAR_PROJ_DIR_PATH = CdtMacroSupplier.VAR_PROJ_DIR_PATH;
	/** @since 5.5 */
	public static final String VAR_WORKSPACE_DIR_PATH = CdtMacroSupplier.VAR_WORKSPACE_DIR_PATH;
	/** @since 5.5 */
	public static final String VAR_DIRECTORY_DELIMITER = CdtMacroSupplier.VAR_DIRECTORY_DELIMITER;
	/** @since 5.5 */
	public static final String VAR_PATH_DELIMITER = CdtMacroSupplier.VAR_PATH_DELIMITER;
	/** @since 5.5 */
	public static final String VAR_ECLIPSE_VERSION = CdtMacroSupplier.VAR_ECLIPSE_VERSION;
	/** @since 5.5 */
	public static final String VAR_CDT_VERSION = CdtMacroSupplier.VAR_CDT_VERSION;
	/** @since 5.5 */
	public static final String VAR_HOST_OS_NAME = CdtMacroSupplier.VAR_HOST_OS_NAME;
	/** @since 5.5 */
	public static final String VAR_HOST_ARCH_NAME = CdtMacroSupplier.VAR_HOST_ARCH_NAME;
	/** @since 5.5 */
	public static final String VAR_OS_TYPE = CdtMacroSupplier.VAR_OS_TYPE;
	/** @since 5.5 */
	public static final String VAR_ARCH_TYPE = CdtMacroSupplier.VAR_ARCH_TYPE;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public static final String VARIABLE_PREFIX = "${"; //$NON-NLS-1$
	private static final String VARIABLE_PREFIX_MASKED = "$\1"; //$NON-NLS-1$
	public static final char VARIABLE_SUFFIX = '}';
	private static final char VARIABLE_SUFFIX_MASKED = '\2';
	public static final char VARIABLE_ESCAPE_CHAR = '\\';
	private static final char VARIABLE_ESCAPE_CHAR_MASKED = '\3';

	// Regular expression fragments
	private static final String RE_VPREFIX = "\\$\\{"; //$NON-NLS-1$
	private static final String RE_VSUFFIX = "\\}"; //$NON-NLS-1$
	private static final String RE_VNAME = "[^${}]*"; //$NON-NLS-1$
	private static final String RE_BSLASH = "[\\\\]"; // *one* backslash //$NON-NLS-1$

	/**
	 * Converts list of strings to one string using given string as delimiter,
	 * i.e -> "string1:string2:string3"
	 *
	 * @param value - list of strings to convert.
	 * @param listDelimiter - delimiter.
	 * @return all strings from the list separated with given delimiter.
	 */
	static public String convertStringListToString(String value[], String listDelimiter) {

		if (value == null || value.length == 0)
			return EMPTY_STRING;

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < value.length; i++) {
			buffer.append(value[i]);
			if (listDelimiter != null && !EMPTY_STRING.equals(listDelimiter) && i < value.length - 1)
				buffer.append(listDelimiter);
		}
		return buffer.toString();
	}

	/**
	 * Resolves macros of kind ${Macro} in the given string by calling the macro substitutor
	 * for each macro reference found. Macros can be inside one another like
	 * ${workspace_loc:/${ProjName}/} but resolved just once. No recursive or concatenated
	 * macro names are allowed. It is possible to prevent macro from expanding using backslash \$.
	 *
	 * @param string - macro expression.
	 * @param substitutor - macro resolution provider to retrieve macro values.
	 * @return resolved string
	 *
	 * @throws CdtVariableException if substitutor can't handle the macro and returns null or throws.
	 */
	static public String resolveToString(String string, IVariableSubstitutor substitutor) throws CdtVariableException {
		if (string == null) {
			return EMPTY_STRING;
		}

		final Pattern pattern = Pattern
				.compile(".*?(" + RE_BSLASH + "*)(" + RE_VPREFIX + "(" + RE_VNAME + ")" + RE_VSUFFIX + ").*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		StringBuilder buffer = new StringBuilder(string);
		int limit = string.length();
		for (Matcher matcher = pattern.matcher(buffer); matcher.matches(); matcher = pattern.matcher(buffer)) {
			String bSlashes = matcher.group(1);
			String macro = matcher.group(2);
			String name = matcher.group(3);
			String resolved = name.length() > 0 ? substitutor.resolveToString(name) : EMPTY_STRING;
			if (resolved == null) {
				throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_UNDEFINED, null, string, name);
			}
			if (limit-- < 0) {
				// to prevent incidental looping
				throw new CdtVariableException(ICdtVariableStatus.TYPE_ERROR, name, matcher.group(0), resolved);
			}

			int nBSlashes = bSlashes.length();
			if ((nBSlashes & 1) == 1) {
				// if odd number of backslashes in front of "${...}" do not expand macro
				resolved = macro;
			}
			// Only one expansion is allowed, so hide any text interfering with macro syntax
			resolved = resolved.replace(VARIABLE_PREFIX, VARIABLE_PREFIX_MASKED);
			resolved = resolved.replace(VARIABLE_SUFFIX, VARIABLE_SUFFIX_MASKED);
			buffer.replace(matcher.start(2), matcher.end(2), resolved);
			// collapse and hide backslashes  \\\\${Macro} -> \\MacroValue or \\\\\${Macro} -> \\${Macro}
			buffer.replace(matcher.start(1), matcher.end(1),
					bSlashes.substring(0, nBSlashes / 2).replace(VARIABLE_ESCAPE_CHAR, VARIABLE_ESCAPE_CHAR_MASKED));
		}
		String result = buffer.toString();

		// take hidden data back
		result = result.replace(VARIABLE_PREFIX_MASKED, VARIABLE_PREFIX);
		result = result.replace(VARIABLE_SUFFIX_MASKED, VARIABLE_SUFFIX);
		result = result.replace(VARIABLE_ESCAPE_CHAR_MASKED, VARIABLE_ESCAPE_CHAR);

		return result;
	}

	/**
	 * finds the macro references in the given string and calls the macro substitutor for each macro found
	 * this could be used for obtaining the list of macros referenced in the given string, etc.
	 *
	 * @param string
	 * @param substitutor
	 * @throws CdtVariableException
	 *
	 * @deprecated Use {@link #resolveToString} which would do full nested expansion.
	 */
	@Deprecated
	static public void checkVariables(String string, IVariableSubstitutor substitutor) throws CdtVariableException {
		resolveToString(string, substitutor);
	}

	/**
	 * Resolves array of macros using {@code substitutor} to pull macro's list of values.
	 * Note that each macro of input array can in turn provide list of values and
	 * the resulting array combines all of them.
	 *
	 * @param values - input array of macros.
	 * @param substitutor - macro resolution provider to retrieve macro values.
	 * @param ignoreErrors - if {@code true} then exceptions are caught and ignored.
	 * @return array of resolved values.
	 * @throws CdtVariableException if substitutor throws {@link CdtVariableException}
	 *         and {@code ignoreErrors}={@code null}.
	 */
	static public String[] resolveStringListValues(String values[], IVariableSubstitutor substitutor,
			boolean ignoreErrors) throws CdtVariableException {
		String result[] = null;
		if (values == null || values.length == 0)
			result = values;
		else if (values.length == 1)
			try {
				result = CdtVariableResolver.resolveToStringList(values[0], substitutor);
			} catch (CdtVariableException e) {
				if (!ignoreErrors)
					throw e;
			}
		else {
			List<String> list = new ArrayList<>();
			for (String value : values) {
				String resolved[];
				try {
					resolved = CdtVariableResolver.resolveToStringList(value, substitutor);
					if (resolved != null && resolved.length > 0)
						list.addAll(Arrays.asList(resolved));
				} catch (CdtVariableException e) {
					if (!ignoreErrors)
						throw e;
				}
			}

			result = list.toArray(new String[list.size()]);
		}
		return result;
	}

	/**
	 * Resolves macro ${ListMacro} in the given String to the String-list using substitutor
	 * to pull macro's list of values. If the provided string is not exactly a single macro
	 * it is treated as macro expression and result is put into the first element of resulting array.
	 *
	 * @param string - input string.
	 * @param substitutor - macro resolution provider to retrieve macro values.
	 * @return array of resolved values.
	 * @throws CdtVariableException if substitutor can't handle the macro and returns null or throws.
	 */
	static public String[] resolveToStringList(String string, IVariableSubstitutor substitutor)
			throws CdtVariableException {

		StringBuilder buffer = new StringBuilder(string);
		final Pattern pattern = Pattern.compile("^" + RE_VPREFIX + "(" + RE_VNAME + ")" + RE_VSUFFIX + "$"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Matcher matcher = pattern.matcher(buffer);
		if (matcher.matches()) {
			String name = matcher.group(1);
			if (name.equals(EMPTY_STRING)) {
				return new String[0];
			}
			String[] result = substitutor.resolveToStringList(name);
			if (result == null) {
				throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_UNDEFINED, null, string, name);
			}
			return result;
		}
		return new String[] { resolveToString(string, substitutor) };
	}

	/**
	 * Test for String-list type of macro.
	 *
	 * @param macroType - type of tested macro.
	 * @return {@code true} if the given macro is a String-list macro.
	 */
	public static boolean isStringListVariable(int macroType) {
		switch (macroType) {
		case ICdtVariable.VALUE_TEXT_LIST:
		case ICdtVariable.VALUE_PATH_FILE_LIST:
		case ICdtVariable.VALUE_PATH_DIR_LIST:
		case ICdtVariable.VALUE_PATH_ANY_LIST:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks the macros integrity for the given context. If test fails {@link CdtVariableException}
	 * is thrown.
	 *
	 * @param info - context information to acquire list of available macros.
	 * @param substitutor - macro resolution provider to retrieve macro values.
	 * @throws CdtVariableException propagated up if {@code substitutor} throws.
	 */
	public static void checkIntegrity(IVariableContextInfo info, IVariableSubstitutor substitutor)
			throws CdtVariableException {

		if (info != null) {
			ICdtVariable macros[] = SupplierBasedCdtVariableManager.getVariables(info, true);
			if (macros != null) {
				for (ICdtVariable macro : macros) {
					if (isStringListVariable(macro.getValueType()))
						substitutor.resolveToStringList(macro.getName());
					else
						substitutor.resolveToString(macro.getName());
				}
			}
		}
	}

	/**
	 * Constructs a macro reference given the macro name
	 * e.g. if the "macro1" name is passed, returns "${macro1}"
	 *
	 * @param name - macro name.
	 * @return macro variable in form "${macro}"
	 */
	public static String createVariableReference(String name) {
		return VARIABLE_PREFIX + name + VARIABLE_SUFFIX;
	}

}
