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

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class MethodInfo extends FunctionInfo {

	boolean isPureVirtual = false;
	boolean isInline = false;
	boolean isVirtual = false;
	boolean isFriend = false;
	ASTAccessVisibility visibility = null;

	MethodInfo(CElement element) {
		super(element);
		visibility = ASTAccessVisibility.PRIVATE;
	}

	public boolean isPureVirtual() {
		return isPureVirtual;
	}

	public void setPureVirtual(boolean isPureVirtual) {
		this.isPureVirtual = isPureVirtual;
	}

	public boolean isInline() {
		return isInline;
	}

	public void setInline(boolean isInline) {
		this.isInline = isInline;
	}

	public boolean isVirtual() {
		return isVirtual;
	}

	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	public boolean isFriend() {
		return isFriend;
	}

	public void setFriend(boolean isFriend) {
		this.isFriend = isFriend;
	}

	/**
	 * Returns the visibility.
	 * @return int
	 */
	public ASTAccessVisibility getVisibility() {
		return visibility;
	}

	/**
	 * Sets the visibility.
	 * @param visibility The visibility to set
	 */
	public void setVisibility(ASTAccessVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public boolean hasSameContentsAs(SourceManipulationInfo otherInfo) {
		return (super.hasSameContentsAs(otherInfo) && (isPureVirtual == ((MethodInfo) otherInfo).isPureVirtual())
				&& (isInline == ((MethodInfo) otherInfo).isInline())
				&& (isVirtual == ((MethodInfo) otherInfo).isVirtual())
				&& (isFriend == ((MethodInfo) otherInfo).isFriend())
				&& (visibility == ((MethodInfo) otherInfo).getVisibility()));
	}

}
