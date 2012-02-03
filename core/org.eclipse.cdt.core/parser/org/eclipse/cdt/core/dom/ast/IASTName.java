/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;

/**
 * This class represents a name in the program that represents a semantic object
 * in the program.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTName extends IASTNode, IName {
	/**
	 * Constant sentinel.
	 */
	public static final IASTName[] EMPTY_NAME_ARRAY = {};

	/**
	 * Returns the name including qualification and template arguments. 
	 */
	@Override
	public char[] toCharArray();
	
	/**
	 * Same as {@link #toCharArray()}.
	 * @since 5.1
	 */
	@Override
	public String toString();

	/**
	 * Get the semantic object attached to this name.  May be null if this name
	 * has not yet been semantically resolved (@see resolveBinding)
	 * @return <code>IBinding</code> if it has been resolved, otherwise null 
	 */
	public IBinding getBinding();
		
	/**
	 * Resolve the semantic object this name is referring to.
	 * 
	 * @return <code>IBinding</code> binding
	 */
	public IBinding resolveBinding();

	/**
	 * Get the role of this name. If the name needs to be resolved to determine that and 
	 * <code>allowResolution</code> is set to <code>false</code>, then {@link IASTNameOwner#r_unclear}
	 * is returned.  
	 * 
	 * @param allowResolution whether or not resolving the name is allowed.
	 * @return {@link IASTNameOwner#r_definition}, {@link IASTNameOwner#r_declaration}, 
	 * 		   {@link IASTNameOwner#r_reference},  {@link IASTNameOwner#r_unclear}.
	 * @since 5.0
	 */
	public int getRoleOfName(boolean allowResolution);
	
	/**
	 * Return the completion context for this name.
	 * 
	 * @return <code>IASTCompletionContext</code> the context for completion
	 */
	public IASTCompletionContext getCompletionContext();
	
	/**
	 * Determines the current linkage in which the name has to be resolved.
	 */
	public ILinkage getLinkage();
	
	/**
	 * Returns the image location for this name or <code>null</code> if the information is not available.
	 * <p>
	 * An image location can be computed when the name is either found directly in the code, is (part of) 
	 * an argument to a macro expansion or is (part of) a macro definition found in the source code.
	 * <p>
	 * The image location is <code>null</code>, when the name consists of multiple tokens (qualified names)
	 * and the tokens are not found side by side in the code, or if the name is the result of
	 * a token-paste operation or the name is found in the definition of a built-in macro.
	 * @since 5.0
	 */
	public IASTImageLocation getImageLocation();
	
	/**
	 * For convenience this method returns the last name of a qualified name or this if this is not a
	 * qualified name.
	 * @since 5.1
	 */
	public IASTName getLastName();
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTName copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	public IASTName copy(CopyStyle style);

	/** 
	 * Set the semantic object for this name to be the given binding
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setBinding(IBinding binding);
	
	/** 
	 * Get the key for looking up this name in a scope.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public char[] getLookupKey();

	/**
	 * Gets the intermediate representation of the binding, if already available.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IBinding getPreBinding();

	/**
	 * Resolves to an intermediate representation of the binding.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IBinding resolvePreBinding();

	/**
	 * Returns whether this name is qualified, i.e. whether it is preceded by a scope operator.
	 * @since 5.4
	 */
	public boolean isQualified();
}
