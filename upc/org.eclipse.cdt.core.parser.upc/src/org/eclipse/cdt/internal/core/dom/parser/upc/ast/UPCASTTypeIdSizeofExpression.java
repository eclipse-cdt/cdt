/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTTypeIdSizeofExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdExpression;

@SuppressWarnings("restriction")
public class UPCASTTypeIdSizeofExpression extends CASTTypeIdExpression implements IUPCASTTypeIdSizeofExpression {

	private int upcSizeofOperator;

	public UPCASTTypeIdSizeofExpression() {
		this(null);
	}

	public UPCASTTypeIdSizeofExpression(IASTTypeId typeId) {
		super(IASTTypeIdExpression.op_sizeof, typeId);
	}

	public UPCASTTypeIdSizeofExpression(int upcSizeofOperator, IASTTypeId typeId) {
		super(IASTTypeIdExpression.op_sizeof, typeId);
		this.upcSizeofOperator = upcSizeofOperator;
	}

	@Override
	public UPCASTTypeIdSizeofExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTTypeIdSizeofExpression copy(CopyStyle style) {
		UPCASTTypeIdSizeofExpression copy = new UPCASTTypeIdSizeofExpression();
		copy.setUPCSizeofOperator(upcSizeofOperator);
		IASTTypeId typeId = getTypeId();
		copy.setTypeId(typeId == null ? null : typeId.copy(style));
		copy.setOffsetAndLength(this);
		if(style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public int getUPCSizeofOperator() {
		return upcSizeofOperator;
	}

	@Override
	public void setUPCSizeofOperator(int upcSizeofOperator) {
		this.upcSizeofOperator = upcSizeofOperator;
	}

}
