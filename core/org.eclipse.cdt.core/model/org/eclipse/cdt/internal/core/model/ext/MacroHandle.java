/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class MacroHandle extends CElementHandle implements IMacro {

	private final boolean fFunctionStyle;

	public MacroHandle(ITranslationUnit tu, IIndexMacro macro) {
		super(tu, ICElement.C_MACRO, new String(macro.getName()));
		fFunctionStyle= macro.isFunctionStyle();
	}

	public String getIdentifierList() {
		return ""; //$NON-NLS-1$
	}

	public String getTokenSequence() {
		return ""; //$NON-NLS-1$
	}

	public boolean isFunctionStyle() {
		return fFunctionStyle;
	}
}
