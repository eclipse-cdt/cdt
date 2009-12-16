/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.actions;

import org.eclipse.jface.viewers.ISelection;

/**
 * @since 2.0
 * @deprecated This interface was synchronous.  I has been replaced with the asynchronous
 *             interface @{link org.eclipse.cdt.debug.core.model.IReverseStepOverHandler}
 */
@Deprecated
public interface IReverseStepOverHandler {
	public boolean canReverseStepOver(ISelection debugContext);
	public void reverseStepOver(ISelection debugContext);
}
