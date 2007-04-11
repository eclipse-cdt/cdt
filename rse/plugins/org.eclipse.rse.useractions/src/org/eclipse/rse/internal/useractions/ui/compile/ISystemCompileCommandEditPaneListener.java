package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * This listener interface is implemented by any code desired to be kept aware
 *  of all user changes to a compile command in the SystemCompileCommandEditPane.
 */
public interface ISystemCompileCommandEditPaneListener {
	/**
	 * Callback method. The user has changed the compile command. It may or may not
	 *  be valid. If not, the given message is non-null. If it is, and you want it,
	 *  call getSystemCompileCommand() in the edit pane.
	 */
	public void compileCommandChanged(SystemMessage message);
}
