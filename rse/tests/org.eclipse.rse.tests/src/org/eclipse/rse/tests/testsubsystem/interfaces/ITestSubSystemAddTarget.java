/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.testsubsystem.interfaces;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for UI parts that support add actions.
 */
public interface ITestSubSystemAddTarget {
	
	/**
	 * Returns true if add action should be shown for the element. 
	 * @param element Element for which add should be shown.
	 * @return True if add should be shown.
	 */
	public boolean showAdd(Object element);

	/**
	 * Returns true if add action should be enabled for the element.
	 * @param element Element for which add should be enabled.
	 * @return True if add should be enabled.
	 */
	public boolean canAdd(Object element);

	/**
	 * Add action of the element.
	 * @param shell The current shell.
	 * @param container The container to wich the element should be added.
	 * @param element The element to add to the container.
	 * @param monitor The progressmonitor if needed during the add operation.
	 * @return True, if the element was added to the container.
	 */
	public boolean doAdd(Shell shell, Object container, Object element, IProgressMonitor monitor);
}
