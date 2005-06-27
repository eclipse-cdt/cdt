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
package org.eclipse.cdt.internal.corext.refactoring.changes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.Assert;

public class TranslationUnitChange extends TextFileChange {

	private ITranslationUnit fCUnit;

	/**
	 * Creates a new <code>TranslationUnitChange</code>.
	 * 
	 * @param name the change's name mainly used to render the change in the UI
	 * @param cunit the Translation unit this text change works on
	 */
	public TranslationUnitChange(String name, ITranslationUnit cunit) throws CoreException {
		super(name, getFile(cunit));
		Assert.isNotNull(cunit);
		fCUnit= cunit;
		setTextType("java"); //$NON-NLS-1$
	}
	
	private static IFile getFile(ITranslationUnit cunit) throws CoreException {
		return (IFile) cunit.getResource();
	}
	
	/* non java-doc
	 * Method declared in IChange.
	 */
	public Object getModifiedLanguageElement(){
		return fCUnit;
	}
	
	/**
	 * Returns the Translation unit this change works on.
	 * 
	 * @return the Translation unit this change works on
	 */
	public ITranslationUnit getTranslationUnit() {
		return fCUnit;
	}	
}

