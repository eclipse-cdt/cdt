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
package org.eclipse.cdt.core.parser.ast2.c;

import java.util.List;

/**
 * @author Doug Schaefer
 */
public interface ICASTTemplateDeclaration extends ICASTDeclaration {

	/**
	 * Is this template explicity exported
	 * TODO or something like that
	 * @return
	 */
	public boolean isExport();

	/**
	 * The parameters may be a regular ICASTParameterDeclaration or
	 * an ICASTTypeParameter.
	 * 
	 * @return
	 */
	public List getParameters();
	
	/**
	 * @return the declaration being templated
	 */
	public ICASTDeclaration getDeclaration();

}
