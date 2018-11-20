/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTUnarySizeofExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;

@SuppressWarnings("restriction")
public class UPCASTUnarySizeofExpression extends CASTUnaryExpression implements IUPCASTUnarySizeofExpression {

	private int upcSizeofOperator;

	public UPCASTUnarySizeofExpression() {
		this(null);
	}

	public UPCASTUnarySizeofExpression(IASTExpression operand) {
		super(IASTUnaryExpression.op_sizeof, operand);
	}

	public UPCASTUnarySizeofExpression(int upcSizeofOperator, IASTExpression operand) {
		super(IASTUnaryExpression.op_sizeof, operand);
		this.upcSizeofOperator = upcSizeofOperator;
	}

	@Override
	public UPCASTUnarySizeofExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTUnarySizeofExpression copy(CopyStyle style) {
		UPCASTUnarySizeofExpression copy = new UPCASTUnarySizeofExpression();
		copy.setUPCSizeofOperator(upcSizeofOperator);
		IASTExpression operand = getOperand();
		copy.setOperand(operand == null ? null : operand.copy(style));
		return copy(copy, style);
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
