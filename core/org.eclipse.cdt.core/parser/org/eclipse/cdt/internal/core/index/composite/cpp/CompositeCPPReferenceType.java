/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.CPPReferenceTypeClone;
import org.eclipse.cdt.internal.core.index.composite.CompositeTypeContainer;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPReferenceType extends CompositeTypeContainer implements ICPPReferenceType {
	public CompositeCPPReferenceType(ICPPReferenceType referenceType, ICompositesFactory cf) {
		super((ITypeContainer) referenceType, cf);
	}

   	@Override
	public Object clone() {
   		return new CPPReferenceTypeClone(this);
   	}
}
