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
 * This is the almighty identifier.
 * 
 * @author Doug Schaefer
 */
public interface IASTIdentifier {

	/**
	 * Returns a copy of the name for the identifier
	 * 
	 * @return
	 */
	public String getName();
	
}
