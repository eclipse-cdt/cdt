/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the root class of all declarations.
 * 
 * @author Doug Schaefer
 */
public interface IASTDeclaration extends IASTNode {
	public static final IASTDeclaration[] EMPTY_DECLARATION_ARRAY = new IASTDeclaration[0];

}
