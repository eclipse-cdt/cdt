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
 * Represents the mapping between goto statements and the label statements
 * the go to.
 * 
 * @author Doug Schaefer
 */
public interface ILabel extends IBinding {

	/**
	 * Returns the label statement for this label.
	 * 
	 * @return
	 */
	public IASTLabelStatement getLabelStatement();
	
}
