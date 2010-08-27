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
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.index.IIndexFileSet;

/**
 * Scopes can be used to look-up names. With the exception of template-scopes the scopes
 * can be arranged in a hierarchy.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IScope {
	
	/**
	 * Classifies the scope.
	 * @since 5.1
	 */
	EScopeKind getKind();

	/**
     * Get the IName for this scope, may be null 
     * @return The name of this scope.
     */
    public IName getScopeName();
    
	/**
	 * The method returns the first enclosing non-template scope, or <code>null</code> if this
	 * is the global scope.
	 * <p>
	 * For scopes obtained from an index, <code>null</code> is returned to indicate that the
	 * scope is only enclosed by the global scope.
	 */
	public IScope getParent() throws DOMException;

	/**
	 * This is the general lookup entry point. It returns the list of
	 * valid bindings for a given name.  The lookup proceeds as an unqualified
	 * lookup.  Constructors are not considered during this lookup and won't be returned.
	 * No attempt is made to resolve potential ambiguities or perform access checking.
	 * 
	 * @param name
	 * @return An array of bindings.
	 */
	public IBinding[] find(String name);
	
	/**
	 * Get the binding in this scope that the given name would resolve to. Could
	 * return null if there is no matching binding in this scope, if the binding has not
	 * yet been cached in this scope, or if resolve == false and the appropriate binding 
	 * has not yet been resolved.
	 * 
	 * @param name
	 * @param resolve :
	 *            whether or not to resolve the matching binding if it has not
	 *            been so already.
	 * @return : the binding in this scope that matches the name, or null
	 */
	public IBinding getBinding(IASTName name, boolean resolve);
	
	/**
	 * Get the binding in this scope that the given name would resolve to. Could
	 * return null if there is no matching binding in this scope, if the binding has not
	 * yet been cached in this scope, or if resolve == false and the appropriate binding 
	 * has not yet been resolved. Accepts file local bindings from the index for the files
	 * int the given set, only.
	 * 
	 * @param name
	 * @param resolve :
	 *            whether or not to resolve the matching binding if it has not
	 *            been so already.
	 * @param acceptLocalBindings a set of files for which to accept local bindings.
	 * @return : the binding in this scope that matches the name, or null
	 */
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings);

	/**
	 * Get the bindings in this scope that the given name or prefix could resolve to. Could
	 * return null if there is no matching bindings in this scope, if the bindings have not
	 * yet been cached in this scope, or if resolve == false and the appropriate bindings 
	 * have not yet been resolved.
	 * 
	 * @param name
	 * @param resolve :
	 *            whether or not to resolve the matching bindings if they have not
	 *            been so already.
	 * @param prefixLookup whether the lookup is for a full name or a prefix
	 * @return : the bindings in this scope that match the name or prefix, or null
	 */
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup);

	/**
	 * Get the bindings in this scope that the given name or prefix could resolve to. Could
	 * return null if there is no matching bindings in this scope, if the bindings have not
	 * yet been cached in this scope, or if resolve == false and the appropriate bindings 
	 * have not yet been resolved.
	 * 
	 * @param name
	 * @param resolve :
	 *            whether or not to resolve the matching bindings if they have not
	 *            been so already.
	 * @param prefixLookup whether the lookup is for a full name or a prefix
	 * @param acceptLocalBindings a set of files for which to accept local bindings.
	 * @return : the bindings in this scope that match the name or prefix, or null
	 */
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet acceptLocalBindings);

}
