/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCEnumerator extends CompositeCBinding implements IIndexBinding, IEnumerator {
	public CompositeCEnumerator(ICompositesFactory cf, IBinding rbinding) {
		super(cf, rbinding);
	}

	public IType getType() throws DOMException {
		return cf.getCompositeType(((IEnumerator)rbinding).getType());
	}
}
