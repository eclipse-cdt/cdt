/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation - initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class PathEntryVariablePreferencePage extends PreferencePage 
implements IWorkbenchPreferencePage {
	
	private Label topLabel;
	
	private PathEntryVariablesGroup pathEntryVariablesGroup;
	
	/**
	 * Constructs a preference page of path variables.
	 * Omits "Restore Defaults"/"Apply Changes" buttons.
	 */
	public PathEntryVariablePreferencePage() {
		pathEntryVariablesGroup = new PathEntryVariablesGroup(true, IResource.FILE | IResource.FOLDER);
		this.noDefaultAndApplyButton();
	}
	
	/**
	 * Resets this page's internal state and creates its UI contents.
	 * 
	 * @see PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICHelpContextIds.PATHENTRY_VARIABLES_PREFERENCE_PAGE);
	
		// define container & its gridding
		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		pageComponent.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		pageComponent.setLayoutData(data);
		pageComponent.setFont(font);
		
		
		topLabel = new Label(pageComponent, SWT.NONE);
		topLabel.setText(PreferencesMessages.getString("PathEntryVariablePreference.explanation")); //$NON-NLS-1$
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		topLabel.setLayoutData(data);
		topLabel.setFont(font);
		
		pathEntryVariablesGroup.createContents(pageComponent);
		
		return pageComponent;
	}
	
	/**
	 * Creates a tab of one horizontal spans.
	 *
	 * @param parent  the parent in which the tab should be created
	 */
	protected static void createSpace(Composite parent) {
		Label vfiller = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData();
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		vfiller.setLayoutData(gridData);
	}
	
	/**
	 * Disposes the path variables group.
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		pathEntryVariablesGroup.dispose();
		super.dispose();
	}
	
	/**
	 * Empty implementation. This page does not use the workbench.
	 * 
	 * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Commits the temporary state to the path variable manager in response to user
	 * confirmation.
	 * 
	 * @see PreferencePage#performOk()
	 * @see PathVariablesGroup#performOk()
	 */
	public boolean performOk() {
		return pathEntryVariablesGroup.performOk();
	}
	
}
