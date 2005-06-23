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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Represents the relationship between a class and one of its base classes.
 * 
 * @author Doug Schaefer
 */
public interface ICPPBase {
	public static final ICPPBase[] EMPTY_BASE_ARRAY = new ICPPBase[0];

	/**
	 * The base class.
	 * 
	 * @return
	 */
	public ICPPClassType getBaseClass() throws DOMException;

	/**
	 * The visibility qualifier applied to the base class.
	 * 
	 * @return
	 */
	public int getVisibility() throws DOMException;

	public static final int v_private = ICPPASTBaseSpecifier.v_private;

	public static final int v_protected = ICPPASTBaseSpecifier.v_protected;

	public static final int v_public = ICPPASTBaseSpecifier.v_public;

	/**
	 * Whether this is a virtual base class.
	 * 
	 * @return
	 */
	public boolean isVirtual() throws DOMException;

}
