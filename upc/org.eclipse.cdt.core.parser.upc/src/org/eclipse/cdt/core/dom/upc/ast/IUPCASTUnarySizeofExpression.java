/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

public interface IUPCASTUnarySizeofExpression extends IASTUnaryExpression {

	public final int upc_localsizeof = 1;

	public final int upc_blocksizeof = 2;

	public final int upc_elemsizeof = 3;

	public int getUPCSizeofOperator();

	public void setUPCSizeofOperator(int upcSizeofOperator);

	@Override
	public IUPCASTUnarySizeofExpression copy();
}