/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariable;

public class Variable extends VariableDeclaration implements IVariable {
	
	public Variable(ICElement parent, String name) {
		super(parent, name, ICElement.C_VARIABLE);
	}

	public Variable(ICElement parent, String name, int kind) {
		super(parent, name, kind);
	}

	public String getInitializer() {
		return ""; //$NON-NLS-1$
	}

}
