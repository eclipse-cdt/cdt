package org.eclipse.cdt.ui;
/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class TabFolderOptionBlock {
	private String fErrorMessage;
	private boolean bIsValid;

	private Label messageLabel;
	private TabItem fCurrentItem;
	private TabFolder folder;
	private ArrayList tabs;
	private ICOptionContainer fParent;

	public TabFolderOptionBlock(ICOptionContainer parent) {
		fParent = parent;
	}

	protected TabItem addTab(ICOptionPage tab) {
		if (tabs == null) {
			tabs = new ArrayList();
		}
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(tab.getLabel());
		Image img = tab.getImage();
		if (img != null)
			item.setImage(img);
		item.setData(tab);
		tab.setContainer(fParent);
		tab.createControl(folder);
		item.setControl(tab.getControl());
		tabs.add(tab);
		return item;
	}

	public Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		messageLabel = new Label(composite, SWT.LEFT);
		messageLabel.setFont(composite.getFont());
		messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label separator = new Label(composite, SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		folder = new TabFolder(composite, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setLayout(new TabFolderLayout());

		fCurrentItem = addTabs();

		folder.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				fCurrentItem = (TabItem) e.item;
				fParent.updateContainer();
			}
		});

		messageLabel.setText(((ICOptionPage) tabs.get(0)).getMessage());
		return composite;
	}
	
	abstract protected TabItem addTabs();

	public boolean performOk(IProgressMonitor monitor) {
		Iterator iter = tabs.iterator();
		while (iter.hasNext()) {
			ICOptionPage tab = (ICOptionPage) iter.next();
			try {
				tab.performApply(new NullProgressMonitor());
			} catch (CoreException e) {
				CUIPlugin.errorDialog(folder.getShell(), "Error", "Error setting options", e);
				return false;
			}
		}
		return true;
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		Iterator iter = tabs.iterator();
		while (iter.hasNext()) {
			ICOptionPage tab = (ICOptionPage) iter.next();
			tab.setVisible(visible);
		}
		update();
		folder.setFocus();
	}

	public void update() {
		boolean ok = true;
		Iterator iter = tabs.iterator();
		while (iter.hasNext()) {
			ICOptionPage tab = (ICOptionPage) iter.next();
			ok = tab.isValid();
			if (!ok) {
				setErrorMessage(tab.getErrorMessage());
				break;
			}
		}
		if (ok && fCurrentItem != null) {
			setErrorMessage(null);
			ICOptionPage tab = (ICOptionPage) fCurrentItem.getData();
			messageLabel.setText(tab.getMessage());
		}
		setValid(ok);
	}

	private void setValid(boolean ok) {
		bIsValid = ok;
	}

	private void setErrorMessage(String message) {
		fErrorMessage = message;
	}

	public String getErrorMessage() {
		return fErrorMessage;
	}

	public boolean isValid() {
		return bIsValid;
	}

	public void performDefaults() {
		ICOptionPage tab = (ICOptionPage) fCurrentItem.getData();
		tab.performDefaults();
	}

}
