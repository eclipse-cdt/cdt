/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Doug Schaefer
 */
public interface IASTMacroExpansion extends IASTNodeLocation {

	/**
	 * The macro definition used for the expansion
	 * 
	 * @return
	 */
	public IASTMacroDefinition getMacroDefinition();

	/**
	 * The source locations for for the macro expansion. These are the locations
	 * where the expansion in question occured and was replaced.
	 * 
	 * @return
	 */
	public IASTNodeLocation[] getExpansionLocations();
	
}
