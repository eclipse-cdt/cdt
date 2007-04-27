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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTLayoutQualifier;

public class UPCASTLayoutQualifier implements IUPCASTLayoutQualifier {

	
	private boolean isPure;
	private boolean isIndefinite;
	private IASTExpression blockSizeExpression;
	
	
	public IASTExpression getBlockSizeExpression() {
		return blockSizeExpression;
	}

	
	public boolean isIndefiniteBlockAllocation() {
		return isIndefinite;
	}

	
	public boolean isPureBlockAllocation() {
		return isPure;
	}

	
	public void setBlockSizeExpression(IASTExpression expr) {
		this.blockSizeExpression = expr;
	}

	
	public void setIndefiniteBlockAllocation(boolean allocation) {
		this.isIndefinite = allocation;
		
	}

	
	public void setPureBlockAllocation(boolean allocation) {
		this.isPure = allocation;
	}

	

}
