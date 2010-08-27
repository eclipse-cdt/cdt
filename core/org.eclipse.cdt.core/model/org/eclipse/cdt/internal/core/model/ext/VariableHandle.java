/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;

public class VariableHandle extends CElementHandle implements org.eclipse.cdt.core.model.IVariable {
	private boolean fIsStatic;

	public VariableHandle(ICElement parent, IVariable var) {
		super(parent, ICElement.C_VARIABLE, var.getName());
		fIsStatic= var.isStatic();
	}

	public boolean isStatic() throws CModelException {
		return fIsStatic;
	}
}
