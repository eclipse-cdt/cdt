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
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSizeofExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;

public class UPCASTSizeofExpression extends CASTUnaryExpression implements IUPCASTSizeofExpression {

	// TODO: don't really know if extending CASTUnaryExpression is the right thing to do
	private int upcSizeofOperator;
	
	
	public int getOperator() {
		return IASTUnaryExpression.op_sizeof;
	}
	
	
	public void setUPCSizeofOperator(int upcSizeofOperator) {
		this.upcSizeofOperator = upcSizeofOperator;
	}
	
	
	public int getUPCSizeofOperator() {
		return upcSizeofOperator;
	}

	
}
