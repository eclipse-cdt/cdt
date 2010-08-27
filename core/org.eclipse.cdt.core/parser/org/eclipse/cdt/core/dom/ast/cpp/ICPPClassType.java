/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;

/**
 * Represents a C++ class.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassType extends ICompositeType, ICPPBinding {
	public static final ICPPClassType[] EMPTY_CLASS_ARRAY = new ICPPClassType[0];
	public static final int k_class = ICPPASTCompositeTypeSpecifier.k_class;

	/**
	 * Returns a list of base class relationships. The list is empty if there
	 * are none.
	 * 
	 * @return List of ICPPBase
	 */
	public ICPPBase[] getBases();

	/**
	 * Get fields is restated here just to point out that this method returns a
	 * list of ICPPField objects representing all fields, declared or inherited.
	 */
	public IField[] getFields();

	/**
	 * findField is restated here to point out that this method looks through
	 * the inheritance tree of this class while looking for a field with the
	 * given name If no field is found, null is returned, if the name is found
	 * to be ambiguous a IProblemBinding is returned.
	 * 
	 * @param name
	 */
	public IField findField(String name);

	/**
	 * Returns a list of ICPPField objects representing fields declared in this
	 * class. It does not include fields inherited from base classes.
	 * 
	 * @return List of ICPPField
	 */
	public ICPPField[] getDeclaredFields();

	/**
	 * Returns a list of ICPPMethod objects representing all methods defined for
	 * this class including those declared, inherited, or generated (e.g.
	 * default constructors and the like).
	 * 
	 * @return List of ICPPMethod
	 */
	public ICPPMethod[] getMethods();

	/**
	 * Returns a list of ICPPMethod objects representing all method explicitly
	 * declared by this class and inherited from base classes. It does not
	 * include automatically generated methods.
	 * 
	 * @return List of ICPPMethod
	 */
	public ICPPMethod[] getAllDeclaredMethods();

	/**
	 * Returns a list of ICPPMethod objects representing all methods explicitly
	 * declared by this class. It does not include inherited methods or
	 * automatically generated methods.
	 * 
	 * @return List of ICPPMethod
	 */
	public ICPPMethod[] getDeclaredMethods();

	/**
	 * Returns an array of ICPPConstructor objects representing the constructors
	 * for this class. This list includes both declared and implicit
	 * constructors.
	 * 
	 */
	public ICPPConstructor[] getConstructors();

	/**
	 * return an array of bindings for those classes/functions declared as
	 * friends of this class.
	 */
	public IBinding[] getFriends();
	
	/**
	 * return an array of nested classes/structures
	 */
	public ICPPClassType[] getNestedClasses();
}
