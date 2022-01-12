/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson           - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetChildCountInfo;

public class ExprMetaGetChildCount extends ExprMetaCommand<ExprMetaGetChildCountInfo> {

	private int numChildLimit = IMIExpressions.CHILD_COUNT_LIMIT_UNSPECIFIED;

	public ExprMetaGetChildCount(IExpressionDMContext ctx) {
		super(ctx);
	}

	/**
	 * @param ctx
	 * @param numChildLimit
	 *
	 * @since 4.0
	 */
	public ExprMetaGetChildCount(IExpressionDMContext ctx, int numChildLimit) {
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
		ExprMetaGetChildCount other = (ExprMetaGetChildCount) obj;
		if (numChildLimit != other.numChildLimit)
			return false;
		return true;
	}
}