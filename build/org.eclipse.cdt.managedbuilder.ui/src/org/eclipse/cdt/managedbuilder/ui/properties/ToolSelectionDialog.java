/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ToolSelectionDialog extends Dialog {

	private static final String EMPTY_STR = "";   //$NON-NLS-1$
	private static final int COL_WIDTH = 300;
	private Table t1, t2;
	private CLabel errorLabel;
	public ITool[] all, used;
	public IFolderInfo fi;
	ArrayList added, removed, left, right;
	Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
	Color red;
	
	public ToolSelectionDialog(Shell shell ) { 
		super (shell); 
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.getString("ToolSelectionDialog.0")); //$NON-NLS-1$
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 300;
		composite.setLayoutData(gd);

		added = new ArrayList();
		removed = new ArrayList();
		left = new ArrayList();
		right = new ArrayList();
		
		t1 = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		t1.setLayoutData(new GridData(GridData.FILL_BOTH));
		t1.setHeaderVisible(true);
		t1.setLinesVisible(true);
		
		TableColumn col = new TableColumn(t1, SWT.NONE);
		col.setText(Messages.getString("ToolSelectionDialog.1")); //$NON-NLS-1$
		col.setWidth(COL_WIDTH);
		
		Composite c2 = new Composite(composite, SWT.BORDER);
		c2.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		t2 = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		t2.setLayoutData(new GridData(GridData.FILL_BOTH));
		t2.setHeaderVisible(true);
		t2.setLinesVisible(true);
		
		col = new TableColumn(t2, SWT.NONE);
		col.setText(Messages.getString("ToolSelectionDialog.2")); //$NON-NLS-1$
		col.setWidth(COL_WIDTH);

		Display display = composite.getDisplay();
	    red = display.getSystemColor(SWT.COLOR_RED);
	    /*
	    Color gray = display.getSystemColor(SWT.COLOR_GRAY);
		ti.setForeground(0, red);
		ti = new TableItem(t3, 0);
		ti.setForeground(0, red);
		*/
		
		c2.setLayout(new GridLayout(1, false));
		Button b1 = new Button(c2, SWT.PUSH);
		b1.setText(Messages.getString("ToolSelectionDialog.12"));  //$NON-NLS-1$
		b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int x = t1.getSelectionIndex();
				ITool tool = (ITool)t1.getItem(x).getData();
				left.remove(tool);
				right.add(tool);
				updateData(true);
			}});
		b1.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		Button b2 = new Button(c2, SWT.PUSH);
		b2.setText(Messages.getString("ToolSelectionDialog.13"));  //$NON-NLS-1$
		b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int x = t2.getSelectionIndex();
				ITool tool = (ITool)t2.getItem(x).getData();
				right.remove(tool);
				left.add(ManagedBuildManager.getRealTool(tool));
				updateData(true);
			}});
		b2.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		errorLabel = new CLabel(composite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		errorLabel.setLayoutData(gd);
		errorLabel.setForeground(red);
			
		used = fi.getTools();
		for (int i=0; i< used.length; i++) {
			right.add(used[i]);
			for (int j = 0; j<all.length; j++) {
				if (all[j] != null && all[j].matches(used[i])) 
					all[j] = null;
			}
		}
		for (int j = 0; j<all.length; j++) {
			if (all[j] != null) left.add(all[j]); 
		}		
		updateData(false);
		
		return composite;
    }
	
	private void add(ITool tool, Table table, boolean bold) {
		TableItem ti = new TableItem(table, 0);
		ti.setText(tool.getUniqueRealName());
		if (bold) ti.setFont(boldFont);
		ti.setData(tool);
	}
	
	private void updateData(boolean check) {
		removed.clear();
		added.clear();
		t1.removeAll();
		t2.removeAll();
		Iterator it = left.iterator();
		while(it.hasNext()) {
			ITool t = (ITool)it.next();
			boolean exists = false;
			for (int i=0; i<all.length; i++) {
				if (all[i] != null && t.matches(all[i])) {
					exists = true;
					break;
				}
			}
			if (!exists) removed.add(t);
			add(t, t1, !exists);
		}
		it = right.iterator();
		while(it.hasNext()) {
			ITool t = (ITool)it.next();
			boolean exists = false;
			for (int i=0; i<used.length; i++) {
				if (t.matches(used[i])) {
					exists = true;
					break;
				}
			}
			if (!exists) added.add(t);
			add(t, t2, !exists);
		}
		IModificationStatus st = fi.getToolChainModificationStatus(
				(ITool[])removed.toArray(new ITool[removed.size()]), 
				(ITool[])added.toArray(new ITool[added.size()]));
		if (st.isOK()) {
			errorLabel.setText(EMPTY_STR);
			if(getButton(IDialogConstants.OK_ID) != null)
				getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			int c = st.getCode();
			String s = EMPTY_STR;
			if ((c & IModificationStatus.TOOLS_CONFLICT) != 0) {
				s = s + Messages.getString("ToolSelectionDialog.7"); //$NON-NLS-1$
				ITool[][] tools = st.getToolsConflicts();
				List conflictTools = new ArrayList();
				for (int k=0; k<t2.getItemCount(); k++) {
					TableItem ti = t2.getItem(k);
					ITool t = (ITool)ti.getData();
				loop:	
					for (int i=0;i<tools.length;i++) {
						for (int j=0;j<tools[i].length;j++) {
							if (t.matches(tools[i][j])) {
								conflictTools.add(ti.getText());
								ti.setForeground(red);
								break loop;
							}
						}
					}
				}
				//bug 189229 - provide more information in the error message for accessibility
				Iterator iterator = conflictTools.iterator();
				s = s+ " " + (String)iterator.next();
				while (iterator.hasNext()) {
					s = s + ", " + (String)iterator.next();
				}
			}
			if ((c & IModificationStatus.TOOLS_DONT_SUPPORT_MANAGED_BUILD) != 0) {
				s = s + Messages.getString("ToolSelectionDialog.8"); //$NON-NLS-1$
				ITool[] tools = st.getNonManagedBuildTools();
				for (int k=0; k<t2.getItemCount(); k++) {
					TableItem ti = t2.getItem(k);
					ITool t = (ITool)ti.getData();
					for (int i=0;i<tools.length;i++) {
						if (t.matches(tools[i])) {
							ti.setForeground(red);
							break;
						}
					}
				}
			}
			if ((c & IModificationStatus.PROPS_NOT_DEFINED) != 0) {
				s = s + Messages.getString("ToolSelectionDialog.9");  //$NON-NLS-1$
			}
			if ((c & IModificationStatus.PROPS_NOT_SUPPORTED) != 0) {
				s = s + Messages.getString("ToolSelectionDialog.10");  //$NON-NLS-1$
			}
			if ((c & IModificationStatus.REQUIRED_PROPS_NOT_SUPPORTED) != 0) {
				s = s + Messages.getString("ToolSelectionDialog.11");  //$NON-NLS-1$
			}
			errorLabel.setText(s);
			if(getButton(IDialogConstants.OK_ID) != null)
				getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}
}
