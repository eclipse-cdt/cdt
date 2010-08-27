/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class CField extends CVariable implements IField {
	public static class CFieldProblem extends CVariable.CVariableProblem implements IField {
		private ICompositeType fOwner;

		public CFieldProblem(ICompositeType owner, IASTNode node, int id, char[] arg) {
			super(node, id, arg);
			fOwner = owner;
		}

		public ICompositeType getCompositeTypeOwner() {
			return fOwner;
		}
	}

	public CField(IASTName name) {
		super(name);
	}

	public ICompositeType getCompositeTypeOwner() {
		ICCompositeTypeScope scope = (ICCompositeTypeScope) getScope();
		return scope.getCompositeType();
	}

}
