/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;

/**
 * Represents a member of a class. Adds in the visibility attribute.
 * 
 * @author Doug Schaefer
 */
public interface ICPPMember extends ICPPBinding {

	/**
	 * The visibility.
	 */ 
	public int getVisibility() throws DOMException;

	public static final int v_private = ICPPASTVisibilityLabel.v_private;

	public static final int v_protected = ICPPASTVisibilityLabel.v_protected;

	public static final int v_public = ICPPASTVisibilityLabel.v_public;

	public ICPPClassType getClassOwner() throws DOMException;
}
