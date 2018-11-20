/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTLayoutQualifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

@SuppressWarnings("restriction")
public class UPCASTLayoutQualifier extends ASTNode implements IUPCASTLayoutQualifier {

	private boolean isPure;
	private boolean isIndefinite;
	private IASTExpression blockSizeExpression;

	@Override
	public UPCASTLayoutQualifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTLayoutQualifier copy(CopyStyle style) {
		UPCASTLayoutQualifier copy = new UPCASTLayoutQualifier();
		copy.isPure = isPure;
		copy.isIndefinite = isIndefinite;
		copy.setBlockSizeExpression(blockSizeExpression == null ? null : blockSizeExpression.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getBlockSizeExpression() {
		return blockSizeExpression;
	}

	@Override
	public boolean isIndefiniteBlockAllocation() {
		return isIndefinite;
	}

	@Override
	public boolean isPureBlockAllocation() {
		return isPure;
	}

	@Override
	public void setBlockSizeExpression(IASTExpression expr) {
		this.blockSizeExpression = expr;
	}

	@Override
	public void setIndefiniteBlockAllocation(boolean allocation) {
		this.isIndefinite = allocation;
	}

	@Override
	public void setPureBlockAllocation(boolean allocation) {
		this.isPure = allocation;
	}
}
