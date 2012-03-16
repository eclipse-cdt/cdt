/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.autotools.ui.AbstractAutotoolsCPropertyTab;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class AutotoolsEditorPropertyTab extends AbstractAutotoolsCPropertyTab {

	protected Combo fACVersionCombo;
	protected Combo fAMVersionCombo;
	IProject project;

//	private class ACVersionSelectionListener implements SelectionListener {
//		ICPropertyProvider p;
//		public ACVersionSelectionListener(ICPropertyProvider p) {
//			this.p = p;
//		}
//		
//		public void widgetSelected(SelectionEvent e) {
//			int index = fACVersionCombo.getSelectionIndex();
//			try {
//				AutotoolsEditorPropertyTab.getProject(p).setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION, fACVersionCombo.getItem(index));
//			} catch (CoreException ce) {
//				// FIXME: what can we do here?
//			}
//		}
//
//		public void widgetDefaultSelected(SelectionEvent e) {
//			// do nothing
//		}
//	}
//	
//	private class AMVersionSelectionListener implements SelectionListener {
//		ICPropertyProvider p;
//		public AMVersionSelectionListener(ICPropertyProvider p) {
//			this.p = p;
//		}
//		
//		public void widgetSelected(SelectionEvent e) {
//			int index = fAMVersionCombo.getSelectionIndex(); 
//			try {
//				AutotoolsEditorPropertyTab.getProject(p).setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION, fAMVersionCombo.getItem(index));
//			} catch (CoreException ce) {
//				// FIXME: what can we do here?
//			}
//		}
//
//		public void widgetDefaultSelected(SelectionEvent e) {
//			// do nothing
//		}
//	}
	
	private IProject getProject() {
		return page.getProject();
	}
	
	public boolean canBeVisible() {
		return true;
	}

	public void createControls(Composite parent) {
		// TODO Auto-generated method stub
		super.createControls(parent);
		Composite composite= usercomp;
		
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		project = getProject();
		
		/* check box for new editors */
		fACVersionCombo= new Combo(composite, SWT.CHECK | SWT.DROP_DOWN | SWT.READ_ONLY);
		fACVersionCombo.setItems(AutotoolsPropertyConstants.fACVersions);
		fACVersionCombo.select(AutotoolsPropertyConstants.fACVersions.length - 1);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fACVersionCombo.setLayoutData(gd);
		
		Label label= new Label(composite, SWT.LEFT);
		label.setText(AutotoolsPropertyMessages.getString("ACEditor.autoconfVersion")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);
		
		/* check box for new editors */
		fAMVersionCombo= new Combo(composite, SWT.CHECK | SWT.DROP_DOWN | SWT.READ_ONLY);
		fAMVersionCombo.setItems(AutotoolsPropertyConstants.fAMVersions);
		fAMVersionCombo.select(AutotoolsPropertyConstants.fAMVersions.length - 1);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fAMVersionCombo.setLayoutData(gd);
		
		Label label2= new Label(composite, SWT.LEFT);
		label2.setText(AutotoolsPropertyMessages.getString("ACEditor.automakeVersion")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label2.setLayoutData(gd);

		initialize();
	}

	public void performOK() {
		String acVer = null;
		String amVer = null;
		boolean changed = false;
		try {
			acVer = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION);
			amVer = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION);
		} catch (CoreException e) {
			acVer = "";
			amVer = "";
		}
		int index = fACVersionCombo.getSelectionIndex();
		String acVerSelected = fACVersionCombo.getItem(index);
		if (!acVerSelected.equals(acVer)) {
			changed = true;
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION, fACVersionCombo.getItem(index));
			} catch (CoreException ce) {
				// Not much we can do at this point
			}
		}

		index = fAMVersionCombo.getSelectionIndex();
		String amVerSelected = fAMVersionCombo.getItem(index);
		if (!amVerSelected.equals(amVer)) {
			changed = true;
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION, fAMVersionCombo.getItem(index));
			} catch (CoreException ce) {
				// Not much we can do here either
			}
		}

		// Notify any Autoconf editors that are open for this project that macro versioning
		// has changed.
		if (changed)
			AutotoolsPropertyManager.getDefault().notifyPropertyListeners(project, AutotoolsPropertyConstants.AUTOCONF_MACRO_VERSIONING);
	}
	
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}
	
	public void performDefaults() {
		// For default Autoconf and Automake versions, use the setting from the
		// Autotools preference dialog.
		String version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION);
		String[] items = fACVersionCombo.getItems();
		// Try and find which list item matches the current preference stored and
		// select it in the list.
		int i;
		for (i = 0; i < items.length; ++i) {
			if (items[i].equals(version))
				break;
		}
		if (i >= items.length)
			i = items.length - 1;
		fACVersionCombo.select(i);

		version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION);
		items = fAMVersionCombo.getItems();
		// Try and find which list item matches the current preference stored and
		// select it in the list
		for (i = 0; i < items.length; ++i) {
			if (items[i].equals(version))
				break;
		}
		if (i >= items.length)
			i = items.length - 1;
		fAMVersionCombo.select(i);
	}
	
	public void updateData(ICResourceDescription cfgd) {
		// Nothing to do
	}
	
	public void updateButtons() {
		// Nothing to do
	}

	public void setVisible (boolean b) {
		super.setVisible(b);
	}
	
//	private IProject getProject(ICPropertyProvider provider) {
//		Object element = provider.getElement();
//		if (element != null) { 
//			if (element instanceof IFile ||
//				element instanceof IProject ||
//				element instanceof IFolder)
//				{
//			IResource f = (IResource) element;
//			return f.getProject();
//				}
//			else if (element instanceof ICProject)
//				return ((ICProject)element).getProject();
//		}
//		return null;
//	}

	private void initialize() {
		initializeACVersion();
		initializeAMVersion();
	}
	
	void initializeACVersion() {
		String version = "";
		try {
			version = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION);
			if (version == null)
				version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION);
		} catch (CoreException e) {
			// do nothing
		}
		String[] items = fACVersionCombo.getItems();
		// Try and find which list item matches the current preference stored and
		// select it in the list.
		int i;
		for (i = 0; i < items.length; ++i) {
			if (items[i].equals(version))
				break;
		}
		if (i >= items.length)
			i = items.length - 1;
		fACVersionCombo.select(i);
	}
	
	void initializeAMVersion() {
		String version = "";
		try {
			version = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION);
			if (version == null)
				version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION);
		} catch (CoreException e) {
			// do nothing
		}
		String[] items = fAMVersionCombo.getItems();
		// Try and find which list item matches the current preference stored and
		// select it in the list.
		int i;
		for (i = 0; i < items.length; ++i) {
			if (items[i].equals(version))
				break;
		}
		if (i >= items.length)
			i = items.length - 1;
		fAMVersionCombo.select(i);
	}

}
