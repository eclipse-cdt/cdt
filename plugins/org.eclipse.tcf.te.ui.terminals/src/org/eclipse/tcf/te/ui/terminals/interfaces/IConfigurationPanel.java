/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.interfaces;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

/**
 * Terminal launcher configuration panel.
 */
public interface IConfigurationPanel extends IMessageProvider {

	/**
	 * Returns the configuration panel container.
	 *
	 * @return The configuration panel container or <code>null</code>.
	 */
	public IConfigurationPanelContainer getContainer();

	/**
	 * Creates the terminal launcher configuration panel UI elements within the
	 * given parent composite. Terminal launcher configuration panels should always
	 * create another composite within the given composite, which is the panel top
	 * control. The top control is queried later from the stack layout to show the
	 * different panels if the selected terminal launcher changed.
	 *
	 * @param parent The parent composite to create the UI elements in. Must not be <code>null</code>.
	 */
	public void setupPanel(Composite parent);

	/**
	 * Cleanup all resources the wizard configuration panel might have been created.
	 */
	public void dispose();

	/**
	 * Returns the terminal launcher configuration panels top control, typically a
	 * composite control. This control is requested every time the stack layout is
	 * required to set a new top control because the selected terminal launcher changed.
	 *
	 * @return The top control or <code>null</code> if the configuration panel has been not setup yet.
	 */
	public Composite getControl();

	/**
	 * Validates the control and sets the message text and type so the parent
	 * page or control is able to display validation result informations.
	 * The default implementation of this method does nothing.
	 *
	 * @return Result of validation.
	 */
	public boolean isValid();

	/**
	 * Restore the widget values plain from the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to restore the widget values from. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix);

	/**
	 * Save the widget values plain to the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to save the widget values to. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix);

	/**
	 * Enables or disables all UI elements belonging to the wizard configuration panel.
	 *
	 * @param enabled <code>True</code> to enable the UI elements, <code>false</code> otherwise.
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Called when the panel gets the active panel.
	 */
	public void activate();

	/**
	 * Initialize the widgets based of the data from the given map.
	 * <p>
	 * This method may called multiple times during the lifetime of the panel and the given
	 * map might be even <code>null</code>.
	 *
	 * @param data The map or <code>null</code>.
	 */
	public void setupData(Map<String, Object> data);

	/**
	 * Extract the data from the widgets and write it back to the given map.
	 * <p>
	 * This method may called multiple times during the lifetime of the panel and the given
	 * map might be even <code>null</code>.
	 *
	 * @param data The map or <code>null</code>.
	 */
	public void extractData(Map<String, Object> data);

	/**
	 * Update the data from the given properties container which contains the current
	 * working data.
	 * <p>
	 * This method may called multiple times during the lifetime of the panel and the given
	 * map might be even <code>null</code>.
	 *
	 * @param data The map or <code>null</code>.
	 */
	public void updateData(Map<String, Object> data);

	/**
	 * Set the selection to the terminal launcher configuration panel.
	 *
	 * @param selection The selection or <code>null</code>.
	 */
	public void setSelection(ISelection selection);

	/**
	 * Returns the selection associated with the terminal launcher configuration panel.
	 *
	 * @return The selection or <code>null</code>.
	 */
	public ISelection getSelection();
}
