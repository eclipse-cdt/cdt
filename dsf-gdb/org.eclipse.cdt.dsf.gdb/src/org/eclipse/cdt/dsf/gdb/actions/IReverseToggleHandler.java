/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.actions;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.jface.viewers.ISelection;

/**
 * @since 2.0
 */
public interface IReverseToggleHandler {
	public boolean canToggleReverse(ISelection debugContext);
	public void toggleReverse(ISelection debugContext);
	public boolean isReverseToggled(ISelection debugContext);
	public boolean isReverseToggled(ICommandControlDMContext debugContext);

}