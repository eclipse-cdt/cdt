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
package org.eclipse.cdt.core.dom.ast.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;

/**
 * Represents a C++ class.
 * 
 * @author Doug Schaefer
 */
public interface ICPPClassType extends ICompositeType {

	public static final int k_class = ICPPASTCompositeTypeSpecifier.k_class;
	/**
	 * Returns a list of base class relationships. The list is empty if
	 * there are none.
	 * 
	 * @return List of ICPPBase
	 */
	public ICPPBase [] getBases() throws DOMException;

	/**
	 * Get fields is restated here just to point out that this method returns
	 * a list of ICPPField objects representing all fields, declared or inherited.
	 */
	public IField[] getFields() throws DOMException;
	
	/**
	 * Returns a list of ICPPField objects representing fields declared in this
	 * class. It does not include fields inherited from base classes.
	 * 
	 * @return List of ICPPField
	 */
	public List getDeclaredFields() throws DOMException;
	
	/**
	 * Returns a list of ICPPMethod objects representing all methods defined for
	 * this class including those declared, inherited, or generated (e.g. default
	 * constructors and the like).
	 * 
	 * @return List of ICPPMethod
	 */
	public List getMethods() throws DOMException;

	/**
	 * Returns a list of ICPPMethod objects representing all method explicitly
	 * declared by this class and inherited from base classes. It does not include
	 * automatically generated methods.
	 * 
	 * @return List of ICPPMethod
	 */
	public List getAllDeclaredMethods() throws DOMException;
	
	/**
	 * Returns a list of ICPPMethod objects representing all methods explicitly
	 * declared by this class. It does not include inherited methods or automatically
	 * generated methods.
	 * 
	 * @return List of ICPPMethod
	 */
	public List getDeclaredMethods() throws DOMException;

    /**
     * @return
     */
    public ICPPConstructor[] getConstructors() throws DOMException;
	
}
