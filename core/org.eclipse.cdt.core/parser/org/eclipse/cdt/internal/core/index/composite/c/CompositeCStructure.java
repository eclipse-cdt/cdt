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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCStructure extends CompositeCBinding implements IIndexBinding, ICompositeType, IIndexType {

	public CompositeCStructure(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	public IField findField(String name) throws DOMException {
		IField preresult = ((ICompositeType)rbinding).findField(name);
		return (IField) cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}

	public IScope getCompositeScope() throws DOMException {
		return new CompositeCCompositeScope(cf, rbinding); 
	}

	public IField[] getFields() throws DOMException {
		IField[] result = ((ICompositeType)rbinding).getFields();
		for(int i=0; i<result.length; i++)
			result[i] = (IField) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		return result;
	}

	public int getKey() throws DOMException {
		return ((ICompositeType)rbinding).getKey();
	}

	public boolean isSameType(IType type) {
		return ((ICompositeType)rbinding).isSameType(type);
	}

	public Object clone() {fail(); return null;}
}
