/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
