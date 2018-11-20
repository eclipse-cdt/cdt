/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

class VariableInfo extends SourceManipulationInfo {
	String typeStr = ""; //$NON-NLS-1$
	boolean isConst = false;
	boolean isVolatile = false;
	boolean isStatic = false;

	protected VariableInfo(CElement element) {
		super(element);
	}

	protected String getTypeName() {
		return typeStr;
	}

	protected void setTypeName(String type) {
		typeStr = type;
	}

	protected void setTypeString(String type) {
		typeStr = type;
	}

	protected boolean isConst() {
		return isConst;
	}

	protected void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	protected boolean isVolatile() {
		return isVolatile;
	}

	protected void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	protected boolean isStatic() {
		return isStatic;
	}

	protected void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	@Override
	public boolean hasSameContentsAs(SourceManipulationInfo otherInfo) {
		return (super.hasSameContentsAs(otherInfo) && (typeStr.equals(((VariableInfo) otherInfo).getTypeName()))
				&& (isConst() == ((VariableInfo) otherInfo).isConst())
				&& (isVolatile() == ((VariableInfo) otherInfo).isVolatile())
				&& (isStatic() == ((VariableInfo) otherInfo).isStatic()));
	}

}
