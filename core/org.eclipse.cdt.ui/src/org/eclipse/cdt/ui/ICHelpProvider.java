/**********************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

/**
 * Represents the C/C++ help provider
 * 
 * @since 2.1
 */
public interface ICHelpProvider {

	/**
	 * Initialize the completion contributor class
	 */
	void initialize();

	/**
	 * Get available help books
	 * @return The <code>ICHelpBook[]</code> array of available help books
	 */
	ICHelpBook[] getCHelpBooks();
	
	/**
	 * get the matching function of a given name
	 * 
	 * @param helpBooks the array of help books to be searched for help
	 * @param name the function name for which help is needed
	 * @return the IFunctionSummary interface
	 */
	IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name);
	
	/**
	 * Get array of matching functions starting with this prefix
	 * 
	 * @param helpBooks the array of help books to be searched for help
	 * @param prefix the function name prefix
	 * @return the IFunctionSummary[] array
	 */
	IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String prefix);

	/**
	 * 
	 * @param helpBooks the array of help books to be searched for help
	 * @param name the C/C++ element name for which help is needed
	 * @return the ICHelpResourceDescriptor[] array representing found help resources
	 */
	ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name);
}
