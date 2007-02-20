/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class ToolChainEditTab extends AbstractCBuildPropertyTab {
	private static final IToolChain[] r_tcs = ManagedBuildManager.getRealToolChains();
	private static final IBuilder[]    r_bs = ManagedBuildManager.getRealBuilders();
	private static final ITool[]    r_tools = ManagedBuildManager.getRealTools();

	private Text text;
	private Button b_dispCompatible;
	private Combo  c_toolchain;
	private Combo  c_builder;
	
	private IBuilder[] v_bs;
	private IToolChain[] v_tcs;
	private ITool[] v_tools;

	private IConfiguration cfg;
	private IResourceInfo ri;
	private IFolderInfo fi;
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		b_dispCompatible = setupCheck(usercomp, Messages.getString("ToolChainEditTab.0"), 2, GridData.BEGINNING); //$NON-NLS-1$
		
		
		setupLabel(usercomp, Messages.getString("ToolChainEditTab.1"), 2, GridData.BEGINNING); //$NON-NLS-1$
		c_toolchain = new Combo(usercomp, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		c_toolchain.setLayoutData(gd);
		c_toolchain.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fi != null) {
					int x = c_toolchain.getSelectionIndex();
					IToolChain tc = v_tcs[x];
					if (tc == null) return;
					try {
						fi.changeToolChain(tc, ManagedBuildManager.calculateChildId(tc.getId(), null), tc.getUniqueRealName());
					} catch (BuildException be) {}
					updateData();
				}
			}});
		c_toolchain.setEnabled(!page.isForFile());
		
		setupLabel(usercomp, Messages.getString("ToolChainEditTab.2"), 2, GridData.BEGINNING); //$NON-NLS-1$
		c_builder = new Combo(usercomp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		c_builder.setLayoutData(gd);
		c_builder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int x = c_builder.getSelectionIndex();
				IBuilder b = v_bs[x];
				cfg.changeBuilder(b, ManagedBuildManager.calculateChildId(b.getId(), null), b.getUniqueRealName());
				updateData();
			}});
		c_builder.setEnabled(page.isForProject());

		// make table for tools list
		Group g = setupGroup(usercomp, Messages.getString("ToolChainEditTab.3"), 2, GridData.FILL_BOTH); //$NON-NLS-1$
		
		text = new Text(g, SWT.BORDER | SWT.WRAP | SWT.MULTI |
				SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		text.setLayoutData(gd);
		
		Button b = new Button(g, SWT.PUSH);
		GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		gdb.grabExcessHorizontalSpace = false;
		gdb.horizontalAlignment = SWT.FILL;
		gdb.widthHint = 80;
		b.setLayoutData(gdb);
		b.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent event) {
	    		ToolSelectionDialog d = new ToolSelectionDialog(usercomp.getShell());
	    		d.all = v_tools;
	    		d.fi = fi;
	    		int result = d.open();
	    		if (result == 0 && (d.removed.size() > 0 || d.added.size() > 0)) {
	    			try {
	    				fi.modifyToolChain(
	    					(ITool[])d.removed.toArray(new ITool[d.removed.size()]), 
	    					(ITool[])d.added.toArray(new ITool[d.added.size()]));
	    			} catch (BuildException b) {}
	    			updateData();
	    		}
	        }});

		b.setLayoutData(new GridData(GridData.END));
		b.setText(Messages.getString("ToolChainEditTab.4")); //$NON-NLS-1$
	}
	public void updateData(ICResourceDescription rcfg) {
		cfg = getCfg(rcfg.getConfiguration());
		ri = cfg.getResourceInfo(rcfg.getPath(), false);
		if (ri instanceof IFolderInfo) 
			fi = (IFolderInfo)ri;
		else
			fi = null;
		updateData();
	}
	private void updateData() {	
		v_tcs   = new IToolChain[r_tcs.length];
		v_bs    = new IBuilder[r_bs.length];
		v_tools = new ITool[r_tools.length];
		
		IToolChain tc = null;
		if (fi != null) {
			tc = ManagedBuildManager.getRealToolChain(fi.getToolChain());
		}
		
		int cnt = 0;
		int pos = -1;
		c_toolchain.removeAll();
		for (int i=0; i<r_tcs.length; i++) {
			if (r_tcs[i].isSystemObject()) continue;
			if (b_dispCompatible.getSelection() &&
				fi != null && ! fi.isToolChainCompatible(r_tcs[i]))
					continue;
			c_toolchain.add(r_tcs[i].getUniqueRealName());
			v_tcs[cnt] = r_tcs[i];
			if (r_tcs[i].equals(tc)) pos = cnt;
			cnt++;
		}
		if (pos != -1)
			c_toolchain.select(pos);
		else
			c_toolchain.setText(EMPTY_STR);
		
		IBuilder b = ManagedBuildManager.getRealBuilder(cfg.getBuilder());
		cnt = 0;
		pos = -1;
		c_builder.removeAll();
		for (int i=0; i<r_bs.length; i++) {
			if (r_bs[i].isSystemObject()) continue;
			if (b_dispCompatible.getSelection() && 
				! cfg.isBuilderCompatible(r_bs[i]))
					continue; // not compatible builder
			c_builder.add(r_bs[i].getUniqueRealName());
			v_bs[cnt] = r_bs[i];
			if (r_bs[i].equals(b)) pos = cnt;
			cnt++;
		}
		if (pos != -1)
			c_builder.select(pos);
		else
			c_builder.setText(EMPTY_STR);
		
		cnt = 0;
		for (int i=0; i<r_tools.length; i++) {
			if (r_tools[i].isSystemObject()) continue;
			v_tools[cnt++] = r_tools[i];
		}
		ITool[] tmp = new ITool[cnt];
		System.arraycopy(v_tools, 0, tmp, 0, cnt);
		v_tools = tmp;
		
		if (fi != null) {
			ITool[] tools = fi.getTools();
			String s = EMPTY_STR;
			for (int i = 0; i < tools.length; i++) {
				s = s + tools[i].getUniqueRealName() + "\n"; //$NON-NLS-1$
			}
			text.setText(s);
		}
		
	}
	
    public void checkPressed(SelectionEvent e) {
    	updateData();
    }
	public void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		IConfiguration cfg1 = getCfg(src.getConfiguration());
		IConfiguration cfg2 = getCfg(dst.getConfiguration());
		IBuilder b = cfg1.getBuilder();
		cfg2.changeBuilder(b, ManagedBuildManager.calculateChildId(b.getId(), null), b.getUniqueRealName());
		IResourceInfo ri1 = cfg1.getResourceInfo(src.getPath(), false);
		IResourceInfo ri2 = cfg2.getResourceInfo(dst.getPath(), false);
		if (ri1 instanceof IFolderInfo && ri2 instanceof IFolderInfo) { 
			IFolderInfo fi1 = (IFolderInfo)ri1;
			IFolderInfo fi2 = (IFolderInfo)ri2;
			IToolChain tc = fi1.getToolChain();
			ITool[] tools1 = fi1.getTools();
			ITool[] tools2 = fi2.getTools();
			try {
				fi2.changeToolChain(tc, ManagedBuildManager.calculateChildId(tc.getId(), null), tc.getUniqueRealName());
				fi2.modifyToolChain(tools2, tools1);
			} catch (BuildException e) {}
		} else {}
	}
	
	protected void performDefaults() {
		updateData();
	}
}
