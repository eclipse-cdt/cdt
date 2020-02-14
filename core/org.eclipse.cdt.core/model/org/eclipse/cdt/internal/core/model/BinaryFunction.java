/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
import org.eclipse.cdt.core.model.IBinaryFunction;
import org.eclipse.cdt.core.model.ICElement;

public class BinaryFunction extends BinaryElement implements IBinaryFunction {

	public BinaryFunction(ICElement parent, String name, IAddress a) {
		super(parent, name, ICElement.C_FUNCTION, a);
	}

	@Override
	public String[] getExceptions() {
		return new String[0];
	}

	@Override
	public int getNumberOfParameters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getParameterInitializer(int pos) {
		// TODO Auto-generated method stub
		return ""; //$NON-NLS-1$
	}

	@Override
	public String[] getParameterTypes() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	public String getReturnType() {
		// TODO Auto-generated method stub
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getSignature() {
		return getElementName();
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
