/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 5.0
 */
public interface IModifyDialogTabPage {

	public interface IModificationListener {

		void updateStatus(IStatus status);

		void valuesModified();

	}

	/**
	 * A map containing key value pairs this tab page
	 * is must modify.
	 *
	 * @param workingValues the values to work with
	 */
	public void setWorkingValues(Map<String, String> workingValues);

	/**
	 * A modify listener which must be informed whenever
	 * a value in the map passed to {@link #setWorkingValues(Map)}
	 * changes. The listener can also be informed about status
	 * changes.
	 *
	 * @param modifyListener the listener to inform
	 */
	public void setModifyListener(IModificationListener modifyListener);

	/**
	 * Create the contents of this tab page.
	 *
	 * @param parent the parent composite
	 * @return created content control
	 */
	public Composite createContents(Composite parent);

	/**
	 * This is called when the page becomes visible.
	 * Common tasks to do include:
	 * <ul><li>Updating the preview.</li>
	 * <li>Setting the focus</li>
	 * </ul>
	 */
	public void makeVisible();

	/**
	 * Each tab page should remember where its last focus was, and reset it
	 * correctly within this method. This method is only called after
	 * initialization on the first tab page to be displayed in order to restore
	 * the focus of the last session.
	 */
	public void setInitialFocus();

}