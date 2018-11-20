/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
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
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTExpression;

public interface IUPCASTKeywordExpression extends IASTExpression {

	public static final int kw_threads = 1;

	public static final int kw_mythread = 2;

	public static final int kw_upc_max_block_size = 3;

	public int getKeywordKind();

	public void setKeywordKind(int kind);

	@Override
	public IUPCASTKeywordExpression copy();
}
