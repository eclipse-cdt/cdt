/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCStructure extends CompositeCBinding implements ICompositeType, IIndexType {

	public CompositeCStructure(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IField findField(String name) {
		IField preresult = ((ICompositeType) rbinding).findField(name);
		return (IField) cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}

	@Override
	public IScope getCompositeScope() {
		return new CompositeCCompositeScope(cf, rbinding);
	}

	@Override
	public IField[] getFields() {
		IField[] result = ((ICompositeType) rbinding).getFields();
		for (int i = 0; i < result.length; i++)
			result[i] = (IField) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		return result;
	}

	@Override
	public int getKey() {
		return ((ICompositeType) rbinding).getKey();
	}

	@Override
	public boolean isSameType(IType type) {
		return ((ICompositeType) rbinding).isSameType(type);
	}

	@Override
	public Object clone() {
		fail();
		return null;
	}

	@Override
	public boolean isAnonymous() {
		return ((ICompositeType) rbinding).isAnonymous();
	}
}
