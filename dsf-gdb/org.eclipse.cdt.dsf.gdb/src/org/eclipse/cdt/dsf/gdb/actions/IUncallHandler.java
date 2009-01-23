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
 */
public interface IUncallHandler {
	public boolean canUncall(ISelection debugContext);
	public void uncall(ISelection debugContext);
}
