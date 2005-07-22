/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.model.IMethod;

public class Method extends MethodDeclaration implements IMethod {
	
	public Method(ICElement parent, String name){
		this(parent, name, ICElement.C_METHOD);
	}

	public Method(ICElement parent, String name, int kind) {
		super(parent, name, kind);
	}
}
