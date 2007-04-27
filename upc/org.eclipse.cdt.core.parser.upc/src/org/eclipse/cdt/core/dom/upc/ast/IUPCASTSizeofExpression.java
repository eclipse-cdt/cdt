/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

public interface IUPCASTSizeofExpression extends IASTUnaryExpression {

	public final int op_sizeof = 0;
	
	public final int op_upc_localsizeof = 1;
	
	public final int op_upc_blocksizeof = 2;
	
	public final int op_upc_elemsizeof  = 3;
	
	public void setUPCSizeofOperator(int upcSizeofOperator);
	
	public int getUPCSizeofOperator();
	
}
