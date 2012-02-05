/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Googel)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.core.model.ITranslationUnit;

public class StubTypeContext {
	private final ITranslationUnit tu;

	public StubTypeContext(ITranslationUnit tu) {
		this.tu = tu;
	}

	public ITranslationUnit getTranslationUnit() {
		return tu;
	}
}
