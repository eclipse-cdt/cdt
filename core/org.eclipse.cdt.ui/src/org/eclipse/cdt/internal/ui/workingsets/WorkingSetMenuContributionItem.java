/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Menu contribution item which shows and lets select a working set.
 * 
 * @since 2.0
 */
public class WorkingSetMenuContributionItem extends ContributionItem {

	private int fId;
	IWorkingSet fWorkingSet;
	WorkingSetFilterActionGroup fActionGroup;
	private Image fImage;

	/**
	 * Constructor for WorkingSetMenuContributionItem.
	 * 
	 * @param id the id
	 * @param actionGroup the action group
	 * @param workingSet the working set
	 */
	public WorkingSetMenuContributionItem(int id, WorkingSetFilterActionGroup actionGroup, IWorkingSet workingSet) {
		super(getId(id));
		Assert.isNotNull(actionGroup);
		Assert.isNotNull(workingSet);
		fId= id;
		fActionGroup= actionGroup;
		fWorkingSet= workingSet;
	}

	/*
	 * Overrides method from ContributionItem.
	 */
	public void fill(Menu menu, int index) {
		MenuItem mi= new MenuItem(menu, SWT.RADIO, index);
		mi.setText("&" + fId + " " + fWorkingSet.getName());  //$NON-NLS-1$  //$NON-NLS-2$
		if (fImage == null)
			fImage= fWorkingSet.getImage().createImage();
		mi.setImage(fImage);
		mi.setSelection(fWorkingSet.equals(fActionGroup.getWorkingSet()));
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
				fActionGroup.setWorkingSet(fWorkingSet, true);
				manager.addRecentWorkingSet(fWorkingSet);
			}
		});
	}
	
	/*
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 * @since 3.0
	 */
	public void dispose() {
		if (fImage != null && !fImage.isDisposed())
			fImage.dispose();
		fImage= null;
		
		super.dispose();
	}
	
	/*
	 * @see org.eclipse.jface.action.IContributionItem#isDynamic()
	 */
	public boolean isDynamic() {
		return true;
	}

	static String getId(int id) {
		return WorkingSetMenuContributionItem.class.getName() + "." + id;  //$NON-NLS-1$
	}
}
