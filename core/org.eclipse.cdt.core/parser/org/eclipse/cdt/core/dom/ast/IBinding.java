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
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Represents the semantics of a name found in the AST or the index.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBinding extends IAdaptable {
    public static final IBinding[] EMPTY_BINDING_ARRAY = new IBinding[0];
	/**
	 * Returns the unqualified name of the binding as a string.
	 */
	public String getName();
    
	/**
	 * Returns the unqualified name of the binding as an array of characters.
	 */
	public char[] getNameCharArray();
	
	/**
	 * Returns the linkage the binding belongs to. C++-declarations declared as
	 * extern "C" will still return c++-linkage.
	 */
	public ILinkage getLinkage() throws CoreException;
	
	/**
	 * Returns the binding that owns this binding, or <code>null</code> if there is no owner.
	 * <p>
	 * The owner is determined as follows:
	 * <br> {@link ICPPUsingDeclaration}: The owner depends on where the declaration is found, within a 
	 * function or method, a class-type, a namespace or on global scope. 
	 * <br> {@link ICPPTemplateParameter}: The owner is the {@link ICPPTemplateDefinition}.
	 * <br> {@link IEnumerator}: The owner is the {@link IEnumeration}, independent of whether they are scoped or not. 
	 * <br> For all other bindings: The owner depends on where the binding can be defined (it could be
	 * declared else where). 
	 * <p> Possible owners are:
	 * <br> {@link IFunction}: for parameters, local types, variables, enumerators, labels and using declarations;
	 * <br> {@link ICPPClassType}: for class-, struct- and union-members, even if the composite type is anonymous; 
	 * also for enumerators and using declarations;
	 * <br> {@link ICompositeType}: for struct- and union-members, even if the composite type is anonymous; 
	 * also for anonymous structs or unions found within another struct;
	 * <br> {@link ICPPNamespace}: for global types, functions, variables, enumerators, namespaces and using declarations;
	 * <br> {@link IEnumeration}: for enumerators.
	 * <br> <code>null</code>: for types, functions, variables, namespaces and using declarations;
	 * @since 5.1
	 */
	public IBinding getOwner();
	
	/**
	 * Returns the parent scope for this binding. A binding may have declarations in multiple scopes,
	 * this method returns the scope where the binding would potentially be defined. 
	 */
	public IScope getScope() throws DOMException;
}
