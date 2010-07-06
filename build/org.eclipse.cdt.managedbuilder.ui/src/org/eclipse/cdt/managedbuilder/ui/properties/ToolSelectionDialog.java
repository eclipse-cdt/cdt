/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.tcmodification.CompatibilityStatus;
import org.eclipse.cdt.managedbuilder.tcmodification.IModificationOperation;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolListModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolModification;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ToolSelectionDialog extends Dialog {

	static private final Image IMG_ARROW   = CPluginImages.get(CPluginImages.IMG_PREFERRED);
	static private final String EMPTY_STR = "";   //$NON-NLS-1$
	static private final int COL_WIDTH = 300;
	static private Font boldFont  = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
	static private Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	static private Color gray  = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
	
	private Table t1, t2;
	private Button b_add, b_del, b_rep, b_all;
	private CLabel errorLabel, l1;
	private Text txt2;
	private ArrayList<ITool> left, right;

	public ArrayList<ITool> added, removed; 
	public ITool[] all, used;
	public IFolderInfo fi;
	public IToolListModification mod = null;
	
	public ToolSelectionDialog(Shell shell, IResourceInfo ri) { 
		super (shell); 
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.ToolSelectionDialog_0); 
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 300;
		composite.setLayoutData(gd);

		added = new ArrayList<ITool>();
		removed = new ArrayList<ITool>();
		left = new ArrayList<ITool>();
		right = new ArrayList<ITool>();
		
		Composite c1 = new Composite(composite, SWT.NONE);
		c1.setLayoutData(new GridData(GridData.FILL_BOTH));
		c1.setLayout(new GridLayout(1, false));

		Composite c2 = new Composite(composite, SWT.BORDER);
		c2.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		c2.setLayout(new GridLayout(1, false));
		
		Composite c3 = new Composite(composite, SWT.NONE);
		c3.setLayoutData(new GridData(GridData.FILL_BOTH));
		c3.setLayout(new GridLayout(1, false));
		
		t1 = new Table(c1, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		t1.setLayoutData(gd);
		t1.setHeaderVisible(true);
		t1.setLinesVisible(true);
		t1.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				handleReplace();
			}
			public void widgetSelected(SelectionEvent e) {
				handleSelection();
			}});
		
		TableColumn col = new TableColumn(t1, SWT.NONE);
		col.setText(Messages.ToolSelectionDialog_1); 
		col.setWidth(COL_WIDTH);
		
		l1 = new CLabel(c1, SWT.BORDER);
		l1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		t2 = new Table(c3, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		t2.setLayoutData(new GridData(GridData.FILL_BOTH));
		t2.setHeaderVisible(true);
		t2.setLinesVisible(true);
		t2.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				handleReplace();
			}
			public void widgetSelected(SelectionEvent e) {
				handleSelection();
			}});
		
		col = new TableColumn(t2, SWT.NONE);
		col.setText(Messages.ToolSelectionDialog_2); 
		col.setWidth(COL_WIDTH);

		txt2 = new Text(c3, SWT.BORDER | SWT.WRAP | SWT.MULTI |
				SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		gd.minimumHeight = 100;
		txt2.setLayoutData(gd);
		
		b_add = new Button(c2, SWT.PUSH);
		b_add.setText(Messages.ToolSelectionDialog_12);  
		b_add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int x = t1.getSelectionIndex();
				if (x == -1)
					return;
				ITool tool = (ITool)t1.getItem(x).getData();
				left.remove(tool);
				right.add(tool);
				mod.changeProjectTools(null, tool);
				updateData();
			}});
		b_add.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

		b_rep = new Button(c2, SWT.PUSH);
		b_rep.setText(Messages.ToolSelectionDialog_14); 
		b_rep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleReplace();
			}});
		b_rep.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		
		b_del = new Button(c2, SWT.PUSH);
		b_del.setText(Messages.ToolSelectionDialog_13);  
		b_del.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b_del.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int x = t2.getSelectionIndex();
				if (x == -1)
					return;
				ITool tool = (ITool)t2.getItem(x).getData();
				right.remove(tool);
				left.add(ManagedBuildManager.getRealTool(tool));
				
				mod.changeProjectTools(tool, null);
				
				updateData();
			}});
		b_del.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		
		// Grabs all place  
		new Label(c2, SWT.NONE).setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));
		
		b_all = new Button(c2, SWT.CHECK | SWT.WRAP);
		b_all.setText(Messages.ToolSelectionDialog_15); 
		b_all.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b_all.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelection();
			}});
		b_all.setLayoutData(new GridData(GridData.FILL, SWT.END, true, false));
		
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
		updateData();
		
		return composite;
    }
	
	/**
	 * Reaction for <Replace> button press or double-click.
	 */
	private void handleReplace() {
		if (! b_rep.isEnabled())
			return;
		int x1 = t1.getSelectionIndex();
		int x2 = t2.getSelectionIndex();
		if (x1 == -1 || x2 == -1)
			return;
		ITool tool1 = (ITool)t1.getItem(x1).getData();
		ITool tool2 = (ITool)t2.getItem(x2).getData();
		left.remove(tool1);
		right.add(tool1);
		right.remove(tool2);
		left.add(ManagedBuildManager.getRealTool(tool2));
		
		mod.changeProjectTools(tool2, tool1);

		updateData();
	}

	/**
	 * Removes "triangle" marks from all table's items.
	 * @param t - affected table.
	 */
	private void removeArrows(Table t) {
		for (int j=0; j<t.getItemCount(); j++) {
			TableItem ti = t.getItem(j);
			if (IMG_ARROW.equals(ti.getImage()))
				ti.setImage((Image)null);
		}
	}
	
	/**
	 * Adds "triangle" marks for items which can replace selected one.
	 * 
	 * @param src - table where selected element is located
	 * @param dst - table where marks should be set
	 * @param b   - button (add or del) to be enabled
	 */
	private void markReplace(Table src, Table dst, Button b) {
		int x = src.getSelectionIndex(); 
		if (x == -1)
			return;
		ITool tool = (ITool)src.getItem(x).getData();
		IToolModification tm = mod.getToolModification(tool);
		if (tm == null)
			return;
		IModificationOperation[] mo = tm.getSupportedOperations();
		if (mo == null || mo.length == 0)
			return;
		for (int j=0; j<dst.getItemCount(); j++) {
			TableItem ti = dst.getItem(j);
			ITool tt = (ITool)ti.getData();
			for (int i=0; i<mo.length; i++) { // List of modifications
				ITool t = mo[i].getReplacementTool();
				if (t == null)
					b.setEnabled(true); // enable Add or Del
				else if (t.matches(tt)) {
					ti.setImage(IMG_ARROW);
					break; // exit from modif. loop
				}
			}
		}
	}

	/**
	 * Called after user has selected item either in t1 or in t2.
	 */
	private void handleSelection() {
		removeArrows(t1);
		removeArrows(t2);
		b_add.setEnabled(b_all.getSelection() && t1.getItemCount() > 0); 
		b_rep.setEnabled(b_all.getSelection() && t1.getItemCount() > 0 && t2.getItemCount() > 0); 
		b_del.setEnabled(b_all.getSelection() && t2.getItemCount() > 0);

		if (t1.isFocusControl()) {
			markReplace(t1, t2, b_add);
			int j = adjustPosition(t2);
			markReplace(t2, t1, b_del);
			if (j != -1) 
				b_rep.setEnabled(true);	
		} else {
			markReplace(t2, t1, b_del);
			int j = adjustPosition(t1);
			markReplace(t1, t2, b_add);
			if (j != -1)
				b_rep.setEnabled(true);	
		}
		showErrorMessage(t1, false);
		showErrorMessage(t2, true);
	}
	
	/**
	 * Changes position of inactive table
	 * to enable replacement, if possible.
	 * 
	 * returns new position or -1 if there's no tools to replace.
	 */
	private int adjustPosition(Table t) {
		int j = t.getSelectionIndex();
		TableItem ti = t.getItem(j);
		if (IMG_ARROW.equals(ti.getImage()))
			return j;
		for (j=0; j<t.getItemCount(); j++) {
			ti = t.getItem(j);
			if (IMG_ARROW.equals(ti.getImage())) {
				t.select(j);
				return j;
			}
		}
		return -1;
	}
	
	/**
	 * Displays appropriate error message for selected tool.
	 * 
	 * @param t - affected table
	 * @param isPrj - whether 
	 */
	private void showErrorMessage(Table t, boolean isPrj) {
		int x = t.getSelectionIndex();
		if (isPrj)
			txt2.setText(EMPTY_STR);
		else {
			l1.setText(EMPTY_STR);
			l1.setImage(null);
		}
		if (x == -1)
			return;
		String message = EMPTY_STR;
		Image image = null;
		TableItem ti = t.getItem(x);
		ITool tool = (ITool)ti.getData();  
		IToolModification tm = mod.getToolModification(tool);
		if (tm == null || tm.isCompatible()) {
			if (IMG_ARROW.equals(ti.getImage()) && !isPrj) {
				TableItem[] tis = t2.getSelection();
				if (tis != null && tis.length > 0) {
					message = Messages.ToolSelectionDialog_16 + 
					((ITool)tis[0].getData()).getUniqueRealName();   
				}
			}
		} else {
			CompatibilityStatus cs = tm.getCompatibilityStatus();
			if (isPrj) {
				message = ToolChainEditTab.getCompatibilityMessage(cs);
			} /*else {
				message = cs.getMessage() + cs.getSeverity();
				image = ToolChainEditTab.getErrorIcon(cs);
			}*/
		}
		if (isPrj) {
			txt2.setText(message); // tmp
		} else {
			l1.setText(message);
			l1.setImage(image);
		}
	}

	/**
	 * Adds given tool to the table, sets appropriate font. 
	 * 
	 * @param tool  - tool to add
	 * @param table - affected table
	 * @param bold  - whether the tool should be marked by bold font.
	 */
	private void add(ITool tool, Table table, boolean bold) {
		IToolModification tm = mod.getToolModification(tool);
		TableItem ti = new TableItem(table, 0);
		ti.setText(tool.getUniqueRealName());
		if (bold) 
			ti.setFont(boldFont);
		ti.setData(tool);
		if (tm != null /*&& table.equals(t2)*/ && !tm.isCompatible()) 
			ti.setForeground(table.equals(t2) ? red : gray);
	}
	
	/**
	 * Refresh data in t1 and t2 tables
	 */
	private void updateData() {
		removed.clear();
		added.clear();
		int t1_pos = t1.getSelectionIndex();
		int t2_pos = t2.getSelectionIndex();
		t1.removeAll();
		t2.removeAll();
		
		Collections.sort(left, BuildListComparator.getInstance());
		
		for (ITool t : left) {
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
		for (ITool t : right) {
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
				removed.toArray(new ITool[removed.size()]), 
				added.toArray(new ITool[added.size()]));
		if (st.isOK()) {
			errorLabel.setText(EMPTY_STR);
			if(getButton(IDialogConstants.OK_ID) != null)
				getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			int c = st.getCode();
			String s = EMPTY_STR;
			if ((c & IModificationStatus.TOOLS_CONFLICT) != 0) {
				s = s + Messages.ToolSelectionDialog_7; 
				ITool[][] tools = st.getToolsConflicts();
				List<String> conflictTools = new ArrayList<String>();
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
				Iterator<String> iterator = conflictTools.iterator();
				s = s+" "+ iterator.next(); //$NON-NLS-1$
				while (iterator.hasNext()) {
					s = s + ", " + iterator.next(); //$NON-NLS-1$
				}
			}
			if ((c & IModificationStatus.TOOLS_DONT_SUPPORT_MANAGED_BUILD) != 0) {
				s = s + Messages.ToolSelectionDialog_8; 
				ITool[] tools = st.getNonManagedBuildTools();
				for (int k=0; k<t2.getItemCount(); k++) {
					TableItem ti = t2.getItem(k);
					ITool t = (ITool)ti.getData();
					for (int i=0;i<tools.length;i++) {
						if (t.matches(tools[i])) {
	//						ti.setBackground(gray);
							break;
						}
					}
				}
			}
			if ((c & IModificationStatus.PROPS_NOT_DEFINED) != 0) {
				s = s + Messages.ToolSelectionDialog_9;  
			}
			if ((c & IModificationStatus.PROPS_NOT_SUPPORTED) != 0) {
				s = s + Messages.ToolSelectionDialog_10;  
			}
			if ((c & IModificationStatus.REQUIRED_PROPS_NOT_SUPPORTED) != 0) {
				s = s + Messages.ToolSelectionDialog_11;  
			}
			errorLabel.setText(s);
//			if(getButton(IDialogConstants.OK_ID) != null)
//				getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		if (t1_pos > -1 && t1_pos < t1.getItemCount())
			t1.select(t1_pos);
		else if (t1.getItemCount() > 0)
			t1.select(0);
		if (t2_pos > -1 && t2_pos < t2.getItemCount())
			t2.select(t2_pos);
		else if (t2.getItemCount() > 0)
			t2.select(0);
		handleSelection();
	}
}
