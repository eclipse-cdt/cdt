/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.make.core.makefile.IParent;
import org.eclipse.cdt.make.core.makefile.IDirective;

/**
 * IParent
 */

public abstract class Parent extends Statement implements IParent {

	ArrayList children = new ArrayList();

	public IDirective[] getStatements() {
		children.trimToSize();
		return (IDirective[]) children.toArray(new IDirective[0]);
	}

	public void addStatement(IDirective statement) {
		children.add(statement);
	}

	public void addStatements(IDirective[] statements) {
		children.addAll(Arrays.asList(statements));
	}

	public void clearStatements() {
		children.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		IDirective[] stmts = getStatements();
		for (int i = 0; i < stmts.length; i++) {
			sb.append(stmts[i]);
		}
		return sb.toString();
	}

}
