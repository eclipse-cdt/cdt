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

class FunctionInfo extends SourceManipulationInfo {

	protected boolean isStatic;
	protected boolean isVolatile;
	protected boolean isConst;

	protected FunctionInfo(CElement element) {
		super(element);
	}

	/**
	 * Returns the isStatic.
	 * @return boolean
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Returns the isVolatile.
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	@Override
	public boolean hasSameContentsAs(SourceManipulationInfo otherInfo) {
		return (super.hasSameContentsAs(otherInfo) && (this.isStatic() == ((FunctionInfo) otherInfo).isStatic())
				&& (this.isVolatile() == ((FunctionInfo) otherInfo).isVolatile())
				&& (this.isConst() == ((FunctionInfo) otherInfo).isConst()));
	}

	/**
	 * Returns the isConst.
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

}
