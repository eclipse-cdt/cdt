/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Represents the relationship between a class and one of its base classes.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPBase extends Cloneable {
	public static final ICPPBase[] EMPTY_BASE_ARRAY = new ICPPBase[0];

	public static final int v_private = ICPPASTBaseSpecifier.v_private;
	public static final int v_protected = ICPPASTBaseSpecifier.v_protected;
	public static final int v_public = ICPPASTBaseSpecifier.v_public;

	/**
	 * The base class.  Generally a ICPPClassType, but may be a ICPPTemplateParameter.
	 * In the case of typedefs, the target type will be returned instead of the typedef itself.
	 */
	public IBinding getBaseClass();
	
	/**
	 * Returns the name that specifies the base class.
	 * @since 4.0
	 */
	public IName getBaseClassSpecifierName();

	/**
	 * The visibility qualifier applied to the base class.
	 * 
	 */
	public int getVisibility();


	/**
	 * Whether this is a virtual base class.
	 */
	public boolean isVirtual();
	
	/**
	 * @since 5.1
	 */
	public ICPPBase clone();
	
	/** 
	 * Used internally to change cloned bases.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setBaseClass(IBinding baseClass);
}
