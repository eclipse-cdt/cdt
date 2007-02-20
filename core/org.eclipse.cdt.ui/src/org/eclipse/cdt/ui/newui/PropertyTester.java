/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Checks whether given object is a source file.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	private static final String KEY = "isSource"; //$NON-NLS-1$
	
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (!KEY.equals(property)) return false;
		
		if (receiver instanceof ITranslationUnit) {
			return ((ITranslationUnit)receiver).isSourceUnit();
		}
		else if (receiver instanceof IFile) {
			IFile file = (IFile)receiver;
			return CoreModel.isValidSourceUnitName(file.getProject(), file.getName());
		}
		else return false;
	}

}
