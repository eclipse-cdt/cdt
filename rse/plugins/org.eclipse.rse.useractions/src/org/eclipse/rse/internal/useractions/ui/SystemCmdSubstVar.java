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
/**
 * @author coulthar
 *
 * This class encapsulates a single substitution variable.
 * Such a variable has the following information:
 * <ol>
 *   <li>The actual variable, as in "&L"
 *   <li>A translated description of the variable, as in "Library name"
 *   <li>A display string, which is typically a concatenation of the above two attributes
 * </ol>
 * Currently this class is not used at runtime to do the actual substitution,
 *  although that would be a natural next step.
 */
public class SystemCmdSubstVar implements Comparable {
	private String var, desc;

	// public constants
	/**
	 * Constructor 
	 */
	public SystemCmdSubstVar(String variable, String description) {
		super();
		this.var = variable;
		this.desc = description;
	}

	/**
	 * Return the substitution variable. Eg "&x" or "${xxxx}"
	 */
	public String getVariable() {
		return var;
	}

	/**
	 * Return the description. Eg "File name"
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * Return the display string. Eg var " - " description
	 */
	public String getDisplayString() {
		return var + " - " + desc; //$NON-NLS-1$
	}

	// comparable interface method, to enable sorting
	/**
	 * Compare ourself to another instance of this class
	 * @return -1 we are less than given object, 0 we are equal, 1 we are greater than
	 */
	public int compareTo(Object o) {
		/* only re-use this when we want to bubble longer names to top...
		 SystemUDASubstVar other = (SystemUDASubstVar)o;
		 if (var.equals(other.getVariable()))
		 return 0;
		 else if (var.length() > other.getVariable().length())
		 return -1; // we want longer names at the beginning of an ascending list!
		 else
		 return 1;
		 */
		return var.compareTo(((SystemCmdSubstVar) o).getVariable());
	}
}
