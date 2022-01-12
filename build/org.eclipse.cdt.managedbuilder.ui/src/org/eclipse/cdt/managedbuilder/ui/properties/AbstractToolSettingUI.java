/*******************************************************************************
 * Copyright (c) 2004, 2010 Rational Software Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Intel corp. 2007 - modification for new CDT model.
 * Miwako Tokugawa (Intel Corporation) - Fixed-location tooltip support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class AbstractToolSettingUI extends FieldEditorPreferencePage {

	protected AbstractCBuildPropertyTab buildPropPage;
	protected IResourceInfo fInfo;
	private boolean dirty = false;
	private boolean toolTipBoxNeeded = false;

	/**
	 *
	 */
	protected AbstractToolSettingUI(IResourceInfo info) {
		this(info, GRID);
	}

	/**
	 * @since 5.1
	 */
	protected AbstractToolSettingUI(IResourceInfo info, int style) {
		super(style);
		noDefaultAndApplyButton();
		fInfo = info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		IPreferenceStore settings = getToolSettingsPrefStore();
		setPreferenceStore(settings);
	}

	/**
	 * @return the tool settings preference store
	 */
	protected ToolSettingsPrefStore getToolSettingsPrefStore() {
		return ToolSettingsPrefStore.getDefault();
	}

	/**
	 * @param flag indicating that tooltip box need to be displayed
	 * @since 7.0
	 */
	protected void setToolTipBoxNeeded(boolean flag) {
		toolTipBoxNeeded = flag;
	}

	/**
	 * @return true if this page needs to have a tool tip box.
	 * @since 7.0
	 */
	protected boolean isToolTipBoxNeeded() {
		return toolTipBoxNeeded;
	}

	/**
	 * Method called when the value of a dialog field changes
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			setDirty(true);
		}
	}

	public void setDirty(boolean b) {
		dirty = b;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void storeSettings() {
		super.performOk();
	}

	public abstract boolean isFor(Object obj1, Object obj2);

	public abstract void updateFields();

	public abstract void setValues();

}
