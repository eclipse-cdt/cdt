/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.opentype;

import org.eclipse.swt.widgets.Shell;


/**
 * A dialog to select a type from a list of types.
 * 
 * @deprecated Use {@link ElementSelectionDialog} instead.
 */
public class OpenTypeDialog extends ElementSelectionDialog {

	/**
	 * Constructs an instance of <code>OpenTypeDialog</code>.
	 * @param parent  the parent shell.
	 */
	public OpenTypeDialog(Shell parent) {
		super(parent);
	}
}
