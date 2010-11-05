/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson           - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetChildrenInfo;

public class ExprMetaGetChildren extends ExprMetaCommand<ExprMetaGetChildrenInfo> {

	private int numChildLimit = IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED;
	
	public ExprMetaGetChildren(IExpressionDMContext ctx) {
		super(ctx);
	}
	
	/**
	 * @param ctx
	 * @param numChildLimit
	 * 
	 * @since 4.0
	 */
	public ExprMetaGetChildren(IExpressionDMContext ctx, int numChildLimit) {
		super(ctx);
		this.numChildLimit = numChildLimit;
	}
	
	/**
	 * @since 4.0
	 */
	public int getNumChildLimit() {
		return numChildLimit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + numChildLimit;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		ExprMetaGetChildren other = (ExprMetaGetChildren) obj;
		if (numChildLimit != other.numChildLimit)
			return false;
		return true;
	}
}