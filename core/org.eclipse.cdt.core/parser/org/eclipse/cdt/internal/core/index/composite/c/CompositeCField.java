/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCField extends CompositeCVariable implements IField {
	public CompositeCField(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}
	
	public ICompositeType getCompositeTypeOwner() throws DOMException {
		IBinding preresult = ((IField)rbinding).getCompositeTypeOwner();
		return (ICompositeType) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
}
