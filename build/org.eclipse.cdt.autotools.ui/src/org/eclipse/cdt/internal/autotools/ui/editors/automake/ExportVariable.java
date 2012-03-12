/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - Modified for Automake usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

public class ExportVariable extends GNUVariableDef {

	public ExportVariable(Directive parent, String name, StringBuffer value, int type) {
		super(parent, name, value, type);
	}

	public boolean isExport() {
		return true;
	}
}
