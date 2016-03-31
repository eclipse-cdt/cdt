/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFieldTemplate extends PDOMCPPVariableTemplate implements ICPPFieldTemplate {
	protected static final int FIELD_POSITION_OFFSET = PDOMCPPVariableTemplate.RECORD_SIZE; // byte
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FIELD_POSITION_OFFSET + 1;

	public PDOMCPPFieldTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFieldTemplate template)
			throws CoreException, DOMException {
		super(linkage, parent, template);
		setFieldPosition(template);
	}

	public PDOMCPPFieldTemplate(PDOMLinkage pdomLinkage, long record) {
		super(pdomLinkage, record);
	}
	
	private void setFieldPosition(ICPPField field) throws CoreException {
		final Database db = getDB();
		db.putByte(record + FIELD_POSITION_OFFSET, field.getFieldPosition());
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_TEMPLATE;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getByte(record + PDOMCPPVariableTemplate.ANNOTATIONS));
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public byte getFieldPosition() {
		return getByte(record + FIELD_POSITION_OFFSET);
	}
}
