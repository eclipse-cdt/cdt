/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public TargetVariable(Directive parent, String target, String name, StringBuffer value, boolean override, int type) {
		super(parent, target, name, value, type);
		this.override = override;
	}

	public boolean isOverride() {
		return override;
	}

}
