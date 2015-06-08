/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.index.IIndex;
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
     * Returns the IName for this scope, may be {@code null} 
     * @return The name of this scope.
     */
    public IName getScopeName();
    
	/**
	 * Returns the first enclosing non-template scope, or {@code null} if this is the global scope.
	 */
	public IScope getParent() throws DOMException;

	/**
	 * This is the general lookup entry point. It returns the list of valid bindings for a given name.
	 * The lookup proceeds as an unqualified lookup.  Constructors are not considered during this lookup
	 * and won't be returned. No attempt is made to resolve potential ambiguities or perform access checking.
	 * 
	 * @param name the name of the bindings
	 * @param tu the translation unit determining the global scope for the lookup
	 * @return An array of bindings
	 * @since 5.11
	 */
	public IBinding[] find(String name, IASTTranslationUnit tu);

	/**
	 * @deprecated Use {{@link #find(String, IASTTranslationUnit)}
	 */
	@Deprecated
	public IBinding[] find(String name);
	
	/**
	 * Returns the binding in this scope that the given name would resolve to. Could
	 * return null if there is no matching binding in this scope, if the binding has not
	 * yet been cached in this scope, or if resolve is {@code false} and the appropriate binding 
	 * has not yet been resolved.
	 * 
	 * @param name the name of the binding
	 * @param resolve whether or not to resolve the matching binding if it has not been so already
	 * @return the binding in this scope that matches the name, or {@code null}
	 */
	public IBinding getBinding(IASTName name, boolean resolve);
	
	/**
	 * Returns the binding in this scope that the given name would resolve to. Could
	 * return null if there is no matching binding in this scope, if the binding has not
	 * yet been cached in this scope, or if resolve is {@code false} and the appropriate binding 
	 * has not yet been resolved. Accepts file local bindings from the index for the files
	 * in the given set, only.
	 * 
	 * @param name the name of the binding
	 * @param resolve whether or not to resolve the matching binding if it has not been so already
	 * @param acceptLocalBindings a set of files for which to accept local bindings
	 * @return the binding in this scope that matches the name, or @code null}
	 */
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings);

	/**
	 * @deprecated Use {@link #getBindings(ScopeLookupData)} instead
	 */
	@Deprecated
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup);

	/**
	 * @deprecated Use {@link #getBindings(ScopeLookupData)} instead
	 */
	@Deprecated
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet acceptLocalBindings);

	
	/**
	 * @since 5.5
	 * @noextend This class is not intended to be subclassed by clients.
	 */
	public static class ScopeLookupData {
		private char[] fLookupKey;
		private final IASTNode fLookupPoint;
		private final IASTTranslationUnit fTu;
		private final boolean fLookupPointIsName;
		private boolean fResolve= true;
		private boolean fPrefixLookup;
		private boolean fIgnorePointOfDeclaration;
		
		public ScopeLookupData(IASTName name, boolean resolve, boolean prefixLookup) {
			if (name == null)
				throw new IllegalArgumentException();
			fLookupPoint = name;
			fLookupPointIsName= true;
			fLookupKey= name.getLookupKey();
			fResolve = resolve;
			fPrefixLookup = prefixLookup;
			fTu= name.getTranslationUnit();
		}

		public ScopeLookupData(char[] name, IASTNode point) {
			// To support IScope.find(...) the lookup point may be null.
			fLookupPoint= point;
			fLookupPointIsName= false;
			fLookupKey= name;
			fIgnorePointOfDeclaration= false;
			if (fLookupPoint == null) {
				fTu= null;
				fIgnorePointOfDeclaration= true;
			} else {
				fTu= fLookupPoint.getTranslationUnit();
			}
		}

		/**
		 * @since 5.11
		 */
		public ScopeLookupData(char[] name, IASTTranslationUnit tu) {
			fLookupPoint= null;
			fLookupPointIsName= false;
			fLookupKey= name;
			fIgnorePointOfDeclaration= true;
			fTu= tu;
		}

		public final void setPrefixLookup(boolean prefixLookup) {
			fPrefixLookup = prefixLookup;
		}

		public final void setResolve(boolean resolve) {
			fResolve = resolve;
		}

		public final void setIgnorePointOfDeclaration(boolean ignorePointOfDeclaration) {
			fIgnorePointOfDeclaration = ignorePointOfDeclaration;
		}

		public final void setLookupKey(char[] key) {
			fLookupKey= key;
		}

		public final char[] getLookupKey() {
			return fLookupKey;
		}

		public final IASTNode getLookupPoint() {
			return fLookupPoint;
		}

		public final boolean isResolve() {
			return fResolve;
		}

		public final boolean isPrefixLookup() {
			return fPrefixLookup;
		}

		public final boolean isIgnorePointOfDeclaration() {
			return fIgnorePointOfDeclaration;
		}

		public final IIndexFileSet getIncludedFiles() {
			return fTu == null ? IIndexFileSet.EMPTY : fTu.getIndexFileSet();
		}

		public final IIndex getIndex() {
			return fTu == null ? null : fTu.getIndex();
		}

		public final IASTName getLookupName() {
			return fLookupPointIsName ? (IASTName) fLookupPoint : null;
		}

		public IASTTranslationUnit getTranslationUnit() {
			return fTu;
		}
	}

	/**
	 * Returns the bindings in this scope that the given name or prefix could resolve to. Could
	 * return null if there is no matching bindings in this scope, if the bindings have not
	 * yet been cached in this scope, or if resolve == false and the appropriate bindings 
	 * have not yet been resolved.
	 * 
	 * @return the bindings in this scope that match the name or prefix, or {@code null}
	 * @since 5.5
	 */
	public IBinding[] getBindings(ScopeLookupData lookup);
}
