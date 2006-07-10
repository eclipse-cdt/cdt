/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class TabFolderOptionBlock {

	protected boolean initializingTabs = true;
	private Composite composite;
	private boolean bShowMessageArea;
	private String fErrorMessage;
	private boolean bIsValid = true;

	private Label messageLabel;
	private ArrayList pages = new ArrayList();
	protected ICOptionContainer fParent;
	private ICOptionPage fCurrentPage;

	private TabFolder fFolder;

	public TabFolderOptionBlock(boolean showMessageArea) {
		bShowMessageArea = showMessageArea;
	}

	public TabFolderOptionBlock(ICOptionContainer parent, boolean showMessageArea) {
		bShowMessageArea = showMessageArea;
		setOptionContainer(parent);
	}

	/**
	 * @param parent
	 */
	public void setOptionContainer(ICOptionContainer parent) {
		fParent = parent;
	}

	public TabFolderOptionBlock(ICOptionContainer parent) {
		this(parent, true);
	}

	protected void addOptionPage(ICOptionPage page) {
		if (!pages.contains(page)) {
			pages.add(page);
		}
	}

	protected List getOptionPages() {
		return pages;
	}

	public Control createContents(Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		if (bShowMessageArea) {
			messageLabel = new Label(composite, SWT.LEFT);
			messageLabel.setFont(composite.getFont());
			messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label separator = new Label(composite, SWT.HORIZONTAL);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		createFolder(composite);

		addTabs();
		setCurrentPage((ICOptionPage)pages.get(0));
		initializingTabs = false;
		String desc = ((ICOptionPage)pages.get(0)).getDescription();
		if (messageLabel != null && desc != null) {
			messageLabel.setText(desc);
		}
		return composite;
	}

	protected ICOptionPage getStartPage() {
		return (ICOptionPage)pages.get(0);
	}

	public int getPageIndex() {
		return pages.indexOf(getCurrentPage());
	}

	protected void createFolder(Composite parent) {
		fFolder = new TabFolder(parent, SWT.NONE);
		fFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		fFolder.setLayout(new TabFolderLayout());

		fFolder.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (!initializingTabs) {
					setCurrentPage((ICOptionPage) ((TabItem)e.item).getData());
					fParent.updateContainer();
				}
			}
		});
	}

	protected void addTab(ICOptionPage tab) {
		TabItem item = new TabItem(fFolder, SWT.NONE);
		item.setText(tab.getTitle());
		Image img = tab.getImage();
		if (img != null)
			item.setImage(img);
		item.setData(tab);
		tab.setContainer(fParent);
		tab.createControl(item.getParent());
		item.setControl(tab.getControl());
		addOptionPage(tab);
	}

	abstract protected void addTabs();

	public boolean performApply(IProgressMonitor monitor) {
		if (initializingTabs)
			return false;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", pages.size()); //$NON-NLS-1$
		try {
			Iterator iter = pages.iterator();
			while (iter.hasNext()) {
				ICOptionPage tab = (ICOptionPage)iter.next();
				try {
					tab.performApply(new SubProgressMonitor(monitor, 1));
				} catch (CoreException e) {
					CUIPlugin.errorDialog(
							composite.getShell(),
							CUIMessages.getString("TabFolderOptionBlock.error"), CUIMessages.getString("TabFolderOptionBlock.error.settingOptions"), e, true); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}
		} finally {
			monitor.done();
		}
		return true;
	}

	public void setVisible(boolean visible) {
		if (initializingTabs)
			return;
		if (fCurrentPage != null) {
			fCurrentPage.setVisible(visible);
		}
		update();
	}

	public void update() {
		if (initializingTabs)
			return;
		boolean ok = true;
		Iterator iter = pages.iterator();
		while (iter.hasNext()) {
			ICOptionPage tab = (ICOptionPage)iter.next();
			ok = tab.isValid();
			if (!ok) {
                String errorMessage = tab.getErrorMessage();
                if (!tab.getControl().isVisible()) {
                    setErrorMessage(CUIMessages.getFormattedString(
                            "TabFolderOptionBlock.error.message", tab.getTitle())); //$NON-NLS-1$
                } else {
                    setErrorMessage(errorMessage);
                }
				break;
			}
		}
		if (ok) {
			setErrorMessage(null);
			ICOptionPage tab = getCurrentPage();
			if (messageLabel != null) {
				messageLabel.setText(tab.getDescription() != null ? tab.getDescription() : ""); //$NON-NLS-1$
			}
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
		if (initializingTabs)
			return;
		getCurrentPage().performDefaults();
	}

	public ICOptionPage getCurrentPage() {
		return fCurrentPage;
	}

	public void setCurrentPage(ICOptionPage page) {
		//Make the new page visible
		ICOptionPage oldPage = fCurrentPage;
		fCurrentPage = page;
		fCurrentPage.setVisible(true);
		if (oldPage != null)
			oldPage.setVisible(false);
	}
}
