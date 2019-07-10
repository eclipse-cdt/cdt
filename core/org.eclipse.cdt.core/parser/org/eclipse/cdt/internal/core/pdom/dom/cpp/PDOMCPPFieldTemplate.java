/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFieldTemplate extends PDOMCPPVariableTemplate implements ICPPFieldTemplate {
	protected static final int FIELD_POSITION_OFFSET = PDOMCPPVariableTemplate.RECORD_SIZE; // 2 bytes 1-based

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FIELD_POSITION_OFFSET + 2;

	public PDOMCPPFieldTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFieldTemplate template)
			throws CoreException, DOMException {
		super(linkage, parent, template);
		setFieldPosition(template);
	}

	public PDOMCPPFieldTemplate(PDOMLinkage pdomLinkage, long record) {
		super(pdomLinkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_TEMPLATE;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotations.getVisibility(getAnnotations());
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getFieldPosition() {
		return Short.toUnsignedInt(getShort(record + FIELD_POSITION_OFFSET)) - 1;
	}

	private void setFieldPosition(ICPPField field) throws CoreException {
		int shiftedPos = field.getFieldPosition() + 1;
		if ((shiftedPos & 0xFFFF0000) != 0) {
			CCorePlugin.log(new IllegalArgumentException("Invalid field position " + field.getFieldPosition())); //$NON-NLS-1$
			shiftedPos = 0;
		}
		getDB().putShort(record + FIELD_POSITION_OFFSET, (short) shiftedPos);
	}
}
