/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.model.ICElement;

public class FunctionHandle extends FunctionDeclarationHandle implements org.eclipse.cdt.core.model.IFunction {

	public FunctionHandle(ICElement parent, IFunction func) {
		super(parent, ICElement.C_FUNCTION, func);
	}
}
