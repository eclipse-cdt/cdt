/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariable;

public class BinaryVariable extends BinaryElement implements IVariable {

	public BinaryVariable(ICElement parent, String name, IAddress a) {
		super(parent, name, ICElement.C_VARIABLE, a);
	}

	@Override
	public String getInitializer() {
		// TODO Auto-generated method stub
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setTypeName(String type) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		return false;
	}

}
