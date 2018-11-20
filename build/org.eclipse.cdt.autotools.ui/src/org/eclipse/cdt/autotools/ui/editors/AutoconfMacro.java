/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

public class AutoconfMacro implements Comparable<Object> {

	protected String name;
	protected String parms;

	public AutoconfMacro(String name, String parms) {
		this.name = name;
		this.parms = parms;
	}

	public String getName() {
		return name;
	}

	public String getTemplate() {
		return name + (hasParms() ? "()" : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getParms() {
		return parms;
	}

	public boolean hasParms() {
		return (parms.length() > 0);
	}

	@Override
	public int compareTo(Object x) {
		AutoconfMacro y = (AutoconfMacro) x;
		return getName().compareTo(y.getName());
	}

	@Override
	public boolean equals(Object x) {
		if (x == null)
			return false;
		AutoconfMacro y = (AutoconfMacro) x;
		return getName().equals(y.getName());
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

}
