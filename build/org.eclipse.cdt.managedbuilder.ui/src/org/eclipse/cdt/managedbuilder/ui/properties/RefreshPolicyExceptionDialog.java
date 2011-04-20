/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.Iterator;

import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.resources.RefreshExclusionContributionManager;
import org.eclipse.cdt.ui.resources.RefreshExclusionContributor;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author vkong
 * @since 8.0
 *
 */
public class RefreshPolicyExceptionDialog extends Dialog {
	
	private Combo exceptionTypeCombo;
	private Group exceptionPropertiesGroup;
	
	private IResource fResourceRoot = null;
	private RefreshExclusion fExclusionRoot = null;
	private RefreshExclusion fNewExclusion = null;
	private RefreshExclusionContributionManager fContrManager;
	private boolean fAddException = true;
	//should only be used by the Add Exception dialog
	private java.util.List<RefreshExclusionContributor> fExclusionContributors;
	
	private  RefreshPolicyExceptionDialog(Shell parent, boolean addException){
		super(parent);
		setShellStyle(getShellStyle());
		fContrManager = RefreshExclusionContributionManager.getInstance();
		fAddException = addException;
		fExclusionContributors = fContrManager.getContributors();
	}
	
	public RefreshPolicyExceptionDialog(Shell parent, IResource resource, java.util.List<RefreshExclusion> exclusions, boolean addException) {
		this(parent, addException);
		
		//this is only called when an user is adding a RefreshException to a given resource
		fResourceRoot = resource;
	}
	
	public RefreshPolicyExceptionDialog(Shell parent, RefreshExclusion exclusion, boolean addException) {
		this(parent, addException);
		fExclusionRoot = exclusion;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (fAddException)
			newShell.setText(Messages.RefreshPolicyExceptionDialog_addDialogLabel);
		else
			newShell.setText(Messages.RefreshPolicyExceptionDialog_editDialogLabel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		Label exceptionType = new Label(comp, SWT.NONE);
		exceptionType.setText(Messages.RefreshPolicyExceptionDialog_exceptionTypeDropdownLabel);
		
		exceptionTypeCombo = new Combo(comp, SWT.READ_ONLY);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		exceptionTypeCombo.setLayoutData(gridData);
		
		
		if (fAddException) {
			Iterator<RefreshExclusionContributor> iterator = fExclusionContributors.iterator();		
			while (iterator.hasNext()) {
				RefreshExclusionContributor contributor = iterator.next();
				exceptionTypeCombo.add(contributor.getName());
			}
		} else {
			exceptionTypeCombo.add(fContrManager.getContributor(fExclusionRoot.getContributorId()).getName());			
		}
		
		exceptionTypeCombo.select(0);
		exceptionTypeCombo.addSelectionListener(new SelectionAdapter() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (exceptionPropertiesGroup != null)
					exceptionPropertiesGroup.dispose();
								
				generateExceptionPropertiesGroup(comp, exceptionTypeCombo.getSelectionIndex());
				comp.layout();
			}
		});		
					
		generateExceptionPropertiesGroup(comp, exceptionTypeCombo.getSelectionIndex());
				
		return comp;
	}

	private void generateExceptionPropertiesGroup(Composite parent, int selectionIndex) {
		
		exceptionPropertiesGroup = new Group(parent, SWT.NONE);
		exceptionPropertiesGroup.setText(Messages.RefreshPolicyExceptionDialog_exceptionPropertiesGroupLabel);
		exceptionPropertiesGroup.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		exceptionPropertiesGroup.setLayoutData(gd);
		
		if (fAddException) {
			if (fNewExclusion == null || fNewExclusion.getContributorId() != fExclusionContributors.get(selectionIndex).getID()){
				fNewExclusion = fExclusionContributors.get(selectionIndex).createExclusion();
			}
			if (fResourceRoot != null) {
				fNewExclusion.setParentResource(fResourceRoot);
			} else {
				fNewExclusion.setParentExclusion(fExclusionRoot);
			}
			
			fExclusionContributors.get(selectionIndex).createProperiesUI(exceptionPropertiesGroup, fNewExclusion);

		} else { //edit an exception

			fContrManager.getContributor(fExclusionRoot.getContributorId()).createProperiesUI(exceptionPropertiesGroup, fExclusionRoot);
		}		
	}

	public RefreshExclusion getResult() {
		if (fAddException)
			return fNewExclusion;
		return fExclusionRoot;
	}
}
