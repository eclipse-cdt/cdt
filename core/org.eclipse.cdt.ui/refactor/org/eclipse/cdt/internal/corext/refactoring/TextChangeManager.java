/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.refactoring.changes.TranslationUnitChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange;

/**
 * A <code>TextChangeManager</code> manages associations between <code>ITranslationUnit</code>
 * or <code>IFile</code> and <code>TextChange</code> objects.
 */
public class TextChangeManager {
	
	private Map fMap= new HashMap(10); // ITranslationUnit -> TextChange
	
	private final boolean fKeepExecutedTextEdits;
	
	public TextChangeManager() {
		this(false);
	}

	/**
	 * @see TextChange.setKeepExecutedTextEdits
	 */
	public TextChangeManager(boolean keepExecutedTextEdits) {
		fKeepExecutedTextEdits= keepExecutedTextEdits;
	}
	
	/**
	 * Adds an association between the given Translation unit and the passed
	 * change to this manager.
	 * 
	 * @param cu the Translation unit (key)
	 * @param change the change associated with the Translation unit
	 */
	public void manage(ITranslationUnit cu, TextChange change) {
		fMap.put(cu, change);
	}
	
	/**
	 * Returns the <code>TextChange</code> associated with the given Translation unit.
	 * If the manager does not already manage an association it creates a one.
	 * 
	 * @param cu the Translation unit for which the text buffer change is requested
	 * @return the text change associated with the given Translation unit. 
	 */
	public TextChange get(ITranslationUnit cu) throws CoreException {
		TextChange result= (TextChange)fMap.get(cu);
		if (result == null) {
			result= new TranslationUnitChange(cu.getElementName(), cu);
			result.setKeepExecutedTextEdits(fKeepExecutedTextEdits);
			fMap.put(cu, result);
		}
		return result;
	}
	
	/**
	 * Removes the <tt>TextChange</tt> managed under the given key
	 * <code>unit<code>.
	 * 
	 * @param unit the key determining the <tt>TextChange</tt> to be removed.
	 * @return the removed <tt>TextChange</tt>.
	 */
	public TextChange remove(ITranslationUnit unit) {
		return (TextChange)fMap.remove(unit);
	}
	
	/**
	 * Returns all text changes managed by this instance.
	 * 
	 * @return all text changes managed by this instance
	 */
	public TextChange[] getAllChanges(){
		return (TextChange[])fMap.values().toArray(new TextChange[fMap.values().size()]);
	}

	/**
	 * Returns all Translation units managed by this instance.
	 * 
	 * @return all Translation units managed by this instance
	 */	
	public ITranslationUnit[] getAllTranslationUnits(){
		return (ITranslationUnit[]) fMap.keySet().toArray(new ITranslationUnit[fMap.keySet().size()]);
	}
	
	/**
	 * Clears all associations between resources and text changes.
	 */
	public void clear() {
		fMap.clear();
	}

	/**
	 * Returns if any text changes are managed for the specified Translation unit.
	 * 
	 * @return <code>true</code> if any text changes are managed for the specified Translation unit and <code>false</code> otherwise.
	 */		
	public boolean containsChangesIn(ITranslationUnit cu){
		return fMap.containsKey(cu);
	}
}

