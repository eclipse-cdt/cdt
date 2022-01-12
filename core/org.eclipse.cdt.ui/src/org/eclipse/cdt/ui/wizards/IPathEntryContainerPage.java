/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.jface.wizard.IWizardPage;

public interface IPathEntryContainerPage extends IWizardPage {
	/**
	 * Method <code>initialize()</code> is called before  <code>ICPathContainerPage.setSelection</code>
	 * to give additional information about the context the path container entry is configured in. This information
	 * only reflects the underlying dialogs current selection state. The user still can make changes after the
	 * the path container pages has been closed or decide to cancel the operation.
	 * @param project - The project the new or modified entry is added to. The project does not have to exist.
	 * Project can be <code>null</code>.
	 * @param currentEntries - The path entries currently selected to be set as the projects path. This can also
	 * include the entry to be edited.
	 */
	public void initialize(ICProject project, IPathEntry[] currentEntries);

	/**
	 * Called when the path container wizard is closed by selecting
	 * the finish button. Implementers typically override this method to
	 * store the page result (new/changed path entry returned in
	 * getSelection) into its model.
	 *
	 * @return if the operation was successful. Only when returned
	 * <code>true</code>, the wizard will close.
	 */
	public boolean finish();

	/**
	 * Method {@link #getNewContainers()} is called to get the the newly added containers.
	 * @return the path entries created on this page.
	 */
	public IContainerEntry[] getNewContainers();

	/**
	 * Sets the path container entry to be edited or <code>null</code>
	 * if a new entry should be created.
	 *
	 * @param containerEntry the path entry to edit or <code>null</code>.
	 * If not equals <code>null</code> then the path entry must be of
	 * kind <code>IPathEntry.CDT_CONTAINER</code>
	 */
	public void setSelection(IContainerEntry containerEntry);
}
