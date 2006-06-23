/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorInput;

/**
 * A disassembly listener is notified when the storage 
 * retrieval operation for given stack frame is completed.
 */
public interface IDisassemblyListener {

	/**
	 * Notifies this listener that the input is computed.
	 * 
	 * @param frame the stack frame. 
	 * @param status the result status.
	 * @param input the resulting editor input. 
	 * <code>null</code> if status is not OK.
	 */
	public void inputComputed( ICStackFrame frame, IStatus status, IEditorInput input );
}
