/*******************************************************************************
 * Copyright (c) 2002, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructureDeclaration;

/**
 * StructureDeclaration
 */
public class StructureDeclaration extends SourceManipulation implements IStructureDeclaration {

	public StructureDeclaration(ICElement parent, String name, int kind) {
		super(parent, name, kind);
	}

	@Override
	public String getTypeName() throws CModelException {
		return getStructureInfo().getTypeName();
	}

	public void setTypeName(String type) throws CModelException {
		getStructureInfo().setTypeName(type);
	}

	@Override
	public boolean isUnion() throws CModelException {
		return getStructureInfo().isUnion();
	}

	@Override
	public boolean isClass() throws CModelException {
		return getStructureInfo().isClass();
	}

	@Override
	public boolean isStruct() throws CModelException {
		return getStructureInfo().isStruct();
	}

	public StructureInfo getStructureInfo() throws CModelException {
		return (StructureInfo) getElementInfo();
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new StructureInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#isStatic()
	 */
	@Override
	public boolean isStatic() throws CModelException {
		return getStructureInfo().isStatic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	@Override
	public boolean isConst() throws CModelException {
		return getStructureInfo().isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#isVolatile()
	 */
	@Override
	public boolean isVolatile() throws CModelException {
		return getStructureInfo().isVolatile();
	}

}
