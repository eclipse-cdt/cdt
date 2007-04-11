package org.eclipse.rse.internal.useractions.ui.compile;

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
import java.util.StringTokenizer;

/**
 * This class encapsulates, for a particular compile command, the 
 *  important information for that command including:
 * <ul>
 *  <li>The label of the command... which is what the user sees in the compile menu
 *  <li>The name of the command... without parameters.
 *  <li>The default command string with the minimum parameters we need to compile with this command, using substitution variables.
 * </ul>
 */
public class SystemDefaultCompileCommand {
	// instance variables
	protected String name, label, jobEnv;
	protected String addlParms;
	protected String[] srcTypes;

	/**
	 * Constructor that takes a command name and label.
	 * You must call setAdditionalCommandParameters after this.
	 */
	public SystemDefaultCompileCommand(String commandLabel, String commandName) {
		super();
		this.name = commandName;
		this.label = commandLabel;
	}

	/**
	 * Constructor that just takes a command name and defaults the label to it.
	 * You must call setAdditionalCommandParameters after this.
	 */
	public SystemDefaultCompileCommand(String commandName) {
		this(commandName, commandName);
	}

	/**
	 * Constructor that takes a command name and label and the parameters.
	 * This avoids you having to call setAdditionalCommandParameters.
	 */
	public SystemDefaultCompileCommand(String commandLabel, String commandName, String parameters) {
		this(commandLabel, commandName);
		setAdditionalParameters(parameters);
	}

	/**
	 * Set additional minimum parameters not specified via the constructor. Will be appended to the
	 *  command string in the methods getMinimumCommandWithParameters and getFullCommandWithParameters.
	 * <p>
	 * Don't worry about a leading blank.
	 */
	public void setAdditionalParameters(String parms) {
		this.addlParms = " " + parms; //$NON-NLS-1$
	}

	/**
	 * Set the source type this applies to, when there is only one
	 */
	public void setSourceType(String type) {
		setSourceTypes(new String[] { type });
	}

	/**
	 * Set the source types this applies to
	 */
	public void setSourceTypes(String[] types) {
		this.srcTypes = types;
	}

	/**
	 * Get the source types this applies to
	 */
	public String[] getSourceTypes() {
		return srcTypes;
	}

	/**
	 * Return true if this command applies to the given source type
	 */
	public boolean appliesToSourceType(String type) {
		if (srcTypes == null)
			return false;
		else {
			boolean match = false;
			for (int idx = 0; !match && (idx < srcTypes.length); idx++) {
				if (type.equals(srcTypes[idx])) match = true;
			}
			return match;
		}
	}

	/**
	 * Return the command label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return the command name, without parameters
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return command fully populated with default parameters and substitution variables
	 */
	public String getCommandWithParameters() {
		return getCommandWithParameters(null);
	}

	/**
	 * Set the job environment. Some systems support multiple command systems, and this attribute
	 *  is needed to identify which system this should run in.
	 */
	public void setJobEnvironment(String jobEnv) {
		this.jobEnv = jobEnv;
	}

	/**
	 * Return the job environment. This is not often used, but sometimes needed for systems 
	 *  that support multiple command systems.
	 */
	public String getJobEnvironment() {
		return jobEnv;
	}

	/**
	 * Given user-specified command paramaters (minus the cmd name), 
	 *   verify it has all the minimum parameters we defined for this command.
	 * For any that are missing, add them...
	 */
	public String fillWithRequiredParams(String commandParams) {
		if ((commandParams == null) || (commandParams.length() == 0))
			return getCommandWithParameters();
		else
			return getCommandWithParameters(commandParams);
	}

	/**
	 * Print the command lable to standard out, for debugging purposes
	 */
	public void printCommandLabel() {
		System.out.println(label);
	}

	/**
	 * Print the command name to standard out, for debugging purposes
	 */
	public void printCommandName() {
		System.out.println(name);
	}

	/**
	 * Print the full command string to standard out, for debugging purposes
	 */
	public void printCommand() {
		System.out.println(getCommandWithParameters());
	}

	// -------------------
	// PRIVATE METHODS...
	// -------------------
	/**
	 * Private implementation that supports two modes:
	 *  - append all required parameters
	 *  - append only those required parameters that do not already exist
	 * <p>
	 * Typically not overridden. Rather populateWithParameters is overridden, which this calls.
	 */
	protected String getCommandWithParameters(String existingParameters) {
		StringBuffer buffer = null;
		if (existingParameters == null)
			buffer = new StringBuffer(name);
		else
			buffer = new StringBuffer(name + " " + existingParameters); //$NON-NLS-1$
		populateWithParameters(buffer);
		if (addlParms != null) {
			if (existingParameters == null)
				buffer.append(" " + addlParms); //$NON-NLS-1$
			else {
				// we have to look at each additional parameter and determine if it has been specified in
				//  given parameter string or not. If not, we add that additional parameter.
				StringTokenizer tokens = new StringTokenizer(addlParms);
				while (tokens.hasMoreTokens()) {
					String parm = tokens.nextToken();
					String parmName = parm.substring(0, parm.indexOf('(') + 1); // "PARM("
					if (existingParameters.indexOf(parmName) == -1) buffer.append(" " + parm); //$NON-NLS-1$
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * Overridable method that will append required parameters to the command string.
	 * These are any not already specified via additional parameters
	 */
	protected void populateWithParameters(StringBuffer bufferSoFar) {
		return;
	}
}
