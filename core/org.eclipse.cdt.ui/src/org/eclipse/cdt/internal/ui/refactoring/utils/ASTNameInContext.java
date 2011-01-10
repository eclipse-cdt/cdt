/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Encapsulates an IASTName and the ITranslationUnit it belongs to. 
 */
public class ASTNameInContext {
	private final IASTName name;
	private final ITranslationUnit tu;

	ASTNameInContext(IASTName name, ITranslationUnit tu) {
		this.name = name;
		this.tu = tu;
	}

	public IASTName getName() {
		return name;
	}

	public ITranslationUnit getTranslationUnit() {
		return tu;
	}
}
