/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Niefer (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

public class CField extends CVariable implements IField {
	public static class CFieldProblem extends ProblemBinding implements IField {
		private ICompositeType fOwner;

		public CFieldProblem(ICompositeType owner, IASTNode node, int id, char[] arg) {
			super(node, id, arg);
			fOwner = owner;
		}

		@Override
		public ICompositeType getCompositeTypeOwner() {
			return fOwner;
		}
	}

	public CField(IASTName name) {
		super(name);
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		ICCompositeTypeScope scope = (ICCompositeTypeScope) getScope();
		return scope.getCompositeType();
	}
}
