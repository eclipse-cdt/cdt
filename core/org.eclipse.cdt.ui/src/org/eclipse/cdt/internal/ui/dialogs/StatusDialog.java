/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.swt.widgets.Shell;

/**
 * @deprecated. This class is deprecated since CDT 6.1. Use {@link org.eclipse.jface.dialogs.StatusDialog} instead.
 * 
 * An abstract base class for dialogs with a status bar and ok/cancel buttons.
 * The status message must be passed over as StatusInfo object and can be
 * an error, warning or ok. The OK button is enabled or disabled depending
 * on the status.
 */
@Deprecated
public abstract class StatusDialog extends org.eclipse.jface.dialogs.StatusDialog {
	public StatusDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
	}
}

