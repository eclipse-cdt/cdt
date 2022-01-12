/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Red Hat Inc. - modified for Automake usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

/**
 *   Here is the syntax of a static pattern rule:
 *
 *    TARGETS ...: VARIABLE-ASSIGNMENT
 *    TARGETS ...: override VARIABLE-ASSIGNMENT
 */
public class TargetVariable extends GNUVariableDef {

	boolean override;

	public TargetVariable(Directive parent, String target, String name, StringBuffer value, boolean override,
			int type) {
		super(parent, target, name, value, type);
		this.override = override;
	}

	@Override
	public boolean isOverride() {
		return override;
	}

}
