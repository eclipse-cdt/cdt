/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * THIS INTERFACE IS PROVISIONAL AND WILL CHANGE IN THE FUTURE
 * PLUG-INS USING THIS INTERFACE WILL NEED
 * TO BE REVISED TO WORK WITH FUTURE VERSIONS OF CDT.
 *
 */

public interface IBreakpointActionPage {

	public void actionDialogCanceled();

	public void actionDialogOK();

	public Composite createComposite(IBreakpointAction action, Composite composite, int style);

}
