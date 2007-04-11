package org.eclipse.rse.internal.useractions.ui;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @author coulthar
 *
 * This encapsulates a list of substitution variables.
 * The input for the list is the variable prefix (eg "&"), an 
 *   array of variable names (eg "A", "B", etc), plus the
 *   resource bundle and key-root for getting the variable 
 *   descriptions (will append variable names to the key-root
 *   to get the resource bundle key).
 * <p>
 * This class is also used to help with the actual substitutions
 * at runtime. The method doSubstitutions will walk the given 
 * string looking for matches on any of the variables in this
 * list, taking care to look for the longer-named variables first,
 * and when a match is found, will call back to the given 
 * implementor of ISystemSubstitutor, to get the substitution 
 * value. The substitutor will be given the variable name (eg, "N",
 * not "&N") and whatever context object was passed into 
 * doSubstitutions, presumably one of the currently selected
 * objects.
 */
public class SystemCmdSubstVarList {
	private SystemCmdSubstVar[] list, sortedList;
	private char prefix = ' ';
	private boolean usingDelimiters = false;
	// constants
	/**
	 * Typical substitution variable prefix when using single prefix char: '&'
	 */
	public static final char SUBST_PREFIX_AMP = '&';
	/**
	 * Typical substitution variable char prefix when using delimiters: "$"
	 */
	public static final char SUBST_PREFIX_DOLLAR = '$';
	/**
	 * Typical substitution variable char prefix when using delimiters: "{"
	 */
	public static final char SUBST_PREFIX_BRACE = '}';
	/**
	 * Typical substitution variable string prefix when using delimiters: "{"
	 */
	public static final String SUBST_PREFIX = "${"; //$NON-NLS-1$
	/**
	 * Typical substitution variable char suffix when using delimiters: "}"
	 */
	public static final char SUBST_SUFFIX_BRACE = '}';

	/**
	 * Constructor when using single prefix like '&'
	 */
	public SystemCmdSubstVarList(char prefix, String[] names, String[] descriptions) {
		this(null, prefix, names, descriptions);
	}

	/**
	 * Constructor when using single prefix like '&', and based on another list
	 * Sometimes a substitution variable list contains common variables, plus some unique variables.
	 * In this case, use this construction, and pass in the list object for the common variables.
	 */
	public SystemCmdSubstVarList(SystemCmdSubstVarList commonList, char prefix, String[] names, String[] descriptions) {
		super();
		this.prefix = prefix;
		init(commonList, names, descriptions);
	}

	/**
	 * Constructor when using ${xxx} delimiting, and not based on another list
	 */
	public SystemCmdSubstVarList(String[] names, String[] descriptions) {
		this(null, names, descriptions);
	}

	/**
	 * Constructor when using ${xxx} delimiting, and we are based on another list
	 */
	public SystemCmdSubstVarList(SystemCmdSubstVarList commonList, String[] names, String[] descriptions) {
		super();
		usingDelimiters = true;
		prefix = SUBST_PREFIX_DOLLAR;
		init(commonList, names, descriptions);
	}

	/**
	 * Abstraction of common stuff done by all constructors.
	 */
	public void init(SystemCmdSubstVarList commonList, String[] names, String[] descriptions) {
		SystemCmdSubstVar[] commonArray = null;
		int idx = 0;
		if (commonList == null)
			list = new SystemCmdSubstVar[names.length];
		else {
			commonArray = commonList.getListAsArray();
			list = new SystemCmdSubstVar[commonArray.length + names.length];
			for (; idx < commonArray.length; idx++)
				list[idx] = commonArray[idx];
		}
		String varName = null;
		String description = null;
		for (int jdx = 0; jdx < names.length; idx++, jdx++) {
			if (!usingDelimiters) {
				varName = prefix + names[jdx];
			} else {
				varName = SUBST_PREFIX + names[jdx] + SUBST_SUFFIX_BRACE;
			}
			description = descriptions[jdx];
			list[idx] = new SystemCmdSubstVar(varName, description);
		}
		// sort list alphabetically...
		Arrays.sort(list);
		// for testing...
		/*
		 System.out.println("Sorted list: ");
		 for (int jdx=0; jdx<list.length; jdx++)
		 System.out.println("..."+list[jdx].getVariable());
		 System.out.println();
		 */
	}

	/**
	 * Return the list of variables as an array of SystemCmdSubstVar objects
	 */
	public SystemCmdSubstVar[] getListAsArray() {
		return list;
	}

	/**
	 * Return the list as an array of display strings of the form xx - some text
	 */
	public String[] getDisplayStrings() {
		String[] strings = new String[list.length];
		for (int idx = 0; idx < strings.length; idx++)
			strings[idx] = list[idx].getDisplayString();
		return strings;
	}

	/**
	 * For debugging purposes, writes the list of variables to standard out
	 */
	public void printDisplayStrings() {
		System.out.println("Substitution Variables for " + getClass().getName()); //$NON-NLS-1$
		String[] strings = getDisplayStrings();
		for (int idx = 0; idx < strings.length; idx++)
			System.out.println(strings[idx]);
		System.out.println();
	}

	/**
	 * For whatever purpose, writes the list of variables to given stream
	 */
	public void printDisplayStrings(PrintWriter stream) {
		stream.println("Substitution Variables for " + getClass().getName()); //$NON-NLS-1$
		String[] strings = getDisplayStrings();
		for (int idx = 0; idx < strings.length; idx++)
			stream.println(strings[idx]);
		stream.println();
	}

	/**
	 * Given a command string potentially containing substitution variables,
	 * and a context object that represents something currently selected (say),
	 * this will scan the command string for matches on any of the substitution
	 * variables defined in this list. For each match it calls the given 
	 * implementor of {@link ISystemSubstitutor} to retrieve the value to 
	 * replace the substitution variable with. The substitutor is also given 
	 * the context object passed in here.
	 * <p>
	 * Currently this assume all variables use the prefix given in the constructor, 
	 * as it optimizes performance.
	 * Another flavour would be needed if arbitrary prefixes were to supported!
	 * <p>
	 * Further, this also currently assumes a doubled up prefix is used for escaping,
	 * meaning the first prefix is to be removed, the next is to be left unsubstituted.
	 * 
	 * @param commandString - the command from the user action, that contains vars to be substituted
	 * @param context - a selected object
	 * @param substitutor - an object that knows how to do substitutions. A callback.
	 */
	public String doSubstitutions(String commandString, Object context, ISystemSubstitutor substitutor) {
		//System.out.println("Command before substitution: " + commandString);
		// walk the command string, looking for variables...
		String part1, part2;
		int index = 0;
		int lastindex = 0;
		//int cmdLength = commandString.length();
		while ((index = commandString.indexOf(prefix, lastindex)) >= 0) {
			lastindex = index + 1; // start next search at char after this '&'
			// ampersand followed by at least one letter?
			if (commandString.length() >= (index + 1)) {
				char sc = commandString.charAt(index + 1); // char after this '&'
				if (sc == prefix) // next char is also an '&'?
				{
					++lastindex; // skip it. Note its ok to bump it past length of string
				} else {
					String var = findMatchingVar(commandString, index);
					if (var != null) {
						String replacement = substitutor.getSubstitutionValue(var, context);
						if (replacement != null) {
							if (index == 0) // substitution variable at front of command?
								commandString = commandString.substring(index + var.length());
							else {
								part1 = commandString.substring(0, index);
								part2 = commandString.substring(index + var.length());
								commandString = part1 + replacement + part2;
							}
							lastindex = index + replacement.length(); // assume replacement has no '&' chars in it!
						}
					}
				}
			}
		} // end while
		//System.out.println("Command after substitution : " + commandString);
		//System.out.println();
		return commandString;
	}

	/**
	 * For testing purposes.
	 * Given the selected object, this returns an array of strings, one for each substitution
	 *  variable, of the form "varname = substituted-value".
	 * @param context - a selected object
	 * @param substitutor - an object that knows how to do substitutions. A callback
	 */
	public String[] doAllSubstitutions(Object context, ISystemSubstitutor substitutor) {
		String[] substitutedVariables = new String[list.length];
		String currVar = null;
		for (int idx = 0; idx < list.length; idx++) {
			currVar = list[idx].getVariable();
			substitutedVariables[idx] = currVar + " = " + //$NON-NLS-1$
					doSubstitutions("\"" + currVar + "\"", context, substitutor); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return substitutedVariables;
	}

	/**
	 * Check our list of sub vars for a match on given string at given index
	 */
	private String findMatchingVar(String cmd, int indexOfPrefix) {
		if (sortedList == null) {
			sortedList = list;
			/*
			 * At this point we don't need to sort names as we are careful not
			 *  to define variables that are ambiguous. Eg, &A and &AB.
			 *
			 sortedList = new SystemUDASubstVar[list.length];
			 for (int i = 0; i < sortedList.length; i++)
			 sortedList[i] = list[i];
			 Arrays.sort(sortedList);
			 */
		}
		int cmdlen = cmd.length();
		for (int idx = 0; idx < sortedList.length; idx++) {
			String var = sortedList[idx].getVariable();
			int varlen = var.length();
			if (((indexOfPrefix + varlen) <= cmdlen) && var.equals(cmd.substring(indexOfPrefix, indexOfPrefix + varlen))) return var;
		}
		return null;
	}

	/**
	 * Helper method to test for duplicate variables
	 */
	public void testForDuplicates() {
		String currname = null;
		for (int idx = 0; idx < list.length; idx++) {
			currname = list[idx].getVariable();
			for (int i = 0; i < list.length; i++)
				if (i != idx) if (list[i].equals(currname)) System.out.println("duplicate subs var " + currname + " in list " + this.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * This writes out the class name.
	 */
	public String toString() {
		return getClass().getName();
	}
}
