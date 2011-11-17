/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
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
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	private static final String KEY_SRC  = "isSource"; //$NON-NLS-1$
	private static final String KEY_PAGE = "pageEnabled"; //$NON-NLS-1$
	private static final String VAL_EXP  = "export"; //$NON-NLS-1$
	private static final String VAL_TOOL = "toolEdit"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (KEY_SRC.equals(property)) {
			if (receiver instanceof ITranslationUnit) {
				return ((ITranslationUnit)receiver).isSourceUnit();
			}
			else if (receiver instanceof IFile) {
				IFile file = (IFile)receiver;
				return CoreModel.isValidSourceUnitName(file.getProject(), file.getName());
			}
		} else if (KEY_PAGE.equals(property)
				&& expectedValue instanceof String) {
			String s = (String) expectedValue;
			if (VAL_EXP.equalsIgnoreCase(s))
				return CDTPrefUtil.getBool(CDTPrefUtil.KEY_EXPORT);
			if (VAL_TOOL.equalsIgnoreCase(s))
				return !CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOTOOLM);
		}
		return false;
	}

}
