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

import java.util.List;

/**
 * The translation unit represents a compilable unit of source.
 *
 * @author Doug Schaefer
 */
public interface IASTTranslationUnit extends IASTNode {

	/**
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public List getDeclarations();
	
}
