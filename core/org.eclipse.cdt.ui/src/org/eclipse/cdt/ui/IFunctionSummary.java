/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;


public interface IFunctionSummary {
	
	public interface IFunctionPrototypeSummary {
		/**
		 * Get the name of the function.  This should be the
		 * same as for IFunctionSummary.
		 * ie "int main(int argc, char **argv)" --> "main"
		 * @return The name of the function without any additional
		 * information.
		 */
		public String getName();
				
		/**
		 * Get the return type of the function.
		 * ie "int main(int argc, char **argv)" --> "int"
		 * @return A string containing the return type of the 
		 * function.
		 */		
		public String getReturnType();
		
		/**
		 * Get the arguments of the function.
 		 * ie "int main(int argc, char **argv)" --> "int argc, char **argv"
		 * @return A string containing the arguments of the 
		 * function, or null if the function has no arguments.
		 */
		public String getArguments();

		/**
		 * Get a nice user defined string.  The format of
		 * which depends on the variable namefirst
		 * namefirst == true: main(int argc, char **argv) int
		 * namefirst == false: int main(int argc, char **argv);
		 * @return
		 */
		public String getPrototypeString(boolean namefirst);
	}
	
	/**
	 * Gets the name of the function.  This is the simple
	 * name without any additional return or argument information.
	 * The function "int main(int argc, char **argv)" would 
	 * return "main"
	 * @return The name of the function without any additional
	 * information
	 */
	public String getName();

	/**
	 * Get the full namespace qualifier for this function 
	 * (generally C++ only)
	 * @return The string of the fully qualified namespace for
	 * this function, or null if the namespace is not known.
	 */
	public String getNamespace();

	/**
	 * Gets the description of the function.  This string can be
	 * either text or HTML coded and is displayed as part of the
	 * hover help and as the context proposal information.
	 * @return A description for this function, or null if no 
	 * description is available.
	 */
	public String getDescription();
	
	/**
	 * Gets the prototype description for this function. 
	 * @return The IFunctionPrototypeSummary describing the 
	 * prototype for this function 
	 */
	public IFunctionPrototypeSummary getPrototype();
		
	/**
	 * Get headers required by this function
	 * @return A list of IRequiredInclude definitions, or null if no
	 * include definitions are available.
	 */
	public IRequiredInclude[] getIncludes();
}

