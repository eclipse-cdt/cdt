/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.corext.util;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

public class CModelUtil {
	/**
	 * Returns the working copy CU of the given CU. If the CU is already a
	 * working copy or the CU has no working copy the input CU is returned.
	 */	
	public static ITranslationUnit toWorkingCopy(ITranslationUnit unit) {
		if (!unit.isWorkingCopy()) {
			ITranslationUnit workingCopy= EditorUtility.getWorkingCopy(unit);
			if (workingCopy != null) {
				return workingCopy;
			}
		}
		return unit;
	}
	
	public static ITranslationUnit toOriginal(ITranslationUnit unit){
		if (unit.isWorkingCopy()) {
			return (((IWorkingCopy)unit).getOriginalElement());
		}
		return unit;
	}
}
