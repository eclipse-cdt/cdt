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
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.tcmodification.CompatibilityStatus;
import org.eclipse.cdt.managedbuilder.tcmodification.IConfigurationModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IConflict;
import org.eclipse.cdt.managedbuilder.tcmodification.IFileInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IFolderInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IModificationOperation;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolChainModificationManager;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolModification;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ToolChainEditTab extends AbstractCBuildPropertyTab {
	
	public  static final IToolChainModificationManager tcmm = ManagedBuildManager.getToolChainModificationManager();
	private static final String NO_TC = Messages.getString("StdProjectTypeHandler.0");  //$NON-NLS-1$
	private static final IToolChain[] r_tcs = ManagedBuildManager.getRealToolChains();
	private static final IBuilder[]    r_bs = ManagedBuildManager.getRealBuilders();
	private static final ITool[]    r_tools = ManagedBuildManager.getRealTools();
	private static final Color          red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private static final String SPACE = " "; //$NON-NLS-1$
	
	static final Image IMG_WARNING = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_WARNING);
	static final Image IMG_ERROR   = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_ERROR);
	static final Image IMG_INFO    = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_INFO);
	static final Image IMG_ARROW   = CPluginImages.get(CPluginImages.IMG_PREFERRED);
	
	private Text text;
	private Button b_dispCompatible;
	private Combo  c_toolchain;
	private Combo  c_builder;
	private Combo  c_tool; 
	private Button button_edit;
	private Group tools_group;
	
	private Label st_builder;
	private Label st_toolchain;
	private Label st_tool;
	
	private IBuilder[] v_bs;
	private IToolChain[] v_tcs;
	private ITool[] v_tools;

	private IConfiguration cfg;
	private IResourceInfo ri;
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		b_dispCompatible = setupCheck(usercomp, Messages.getString("ToolChainEditTab.0"), 2, GridData.BEGINNING); //$NON-NLS-1$
		b_dispCompatible.setSelection(true);

		setupLabel(usercomp, Messages.getString("ToolChainEditTab.1"), 2, GridData.BEGINNING); //$NON-NLS-1$
		c_toolchain = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		c_toolchain.setLayoutData(gd);
		c_toolchain.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				modifyToolchain();
			}});
		c_toolchain.setEnabled(!page.isForFile());

		st_toolchain = setupLabel(usercomp, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
		st_toolchain.setForeground(red);

		setupLabel(usercomp, Messages.getString("ToolChainEditTab.2"), 2, GridData.BEGINNING); //$NON-NLS-1$
		c_builder = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		c_builder.setLayoutData(gd);
		c_builder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				modifyBuilder();
			}});
		c_builder.setEnabled(page.isForProject());

		st_builder = setupLabel(usercomp, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
		st_builder.setForeground(red);

		// make table for tools list
		if (page.isForFile()) {
			Group g = setupGroup(usercomp, Messages.getString("ToolChainEditTab.5"), 2, GridData.FILL_BOTH); //$NON-NLS-1$
			c_tool = new Combo(g, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			c_tool.setLayoutData(gd);
			c_tool.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					saveToolSelected();
				}});
			
			st_tool = setupLabel(g, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
			st_tool.setForeground(red);
		} else { // Folder or Project
			tools_group = setupGroup(usercomp, Messages.getString("ToolChainEditTab.3"), 2, GridData.FILL_BOTH); //$NON-NLS-1$

			text = new Text(tools_group, SWT.BORDER | SWT.WRAP | SWT.MULTI |
					SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
			gd = new GridData(GridData.FILL_BOTH);
			gd.grabExcessHorizontalSpace = true;
			text.setLayoutData(gd);

			button_edit = new Button(tools_group, SWT.PUSH);
			GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
			gdb.grabExcessHorizontalSpace = false;
			gdb.horizontalAlignment = SWT.FILL;
			gdb.widthHint = 80;
			button_edit.setLayoutData(gdb);
			button_edit.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					modifyTools();
				}});

			button_edit.setLayoutData(new GridData(GridData.END));
			button_edit.setText(Messages.getString("ToolChainEditTab.4")); //$NON-NLS-1$
		}
	}
	public void updateData(ICResourceDescription rcfg) {
		cfg = getCfg(rcfg.getConfiguration());
		ri = cfg.getResourceInfo(rcfg.getPath(), false);
		updateData();
	}
	
	private void showErrorMessage() {
		if (ri instanceof IFolderInfo) {
			IFolderInfoModification foim = tcmm.getModification((IFolderInfo)ri);
			if (foim.isToolChainCompatible()) {
				st_toolchain.setText(EMPTY_STR);
			} else {
				String s = foim.getToolChainCompatibilityStatus().getMessage();
				st_toolchain.setText(s);
			}
			st_builder.setText(EMPTY_STR);
			if (foim instanceof IConfigurationModification) {
				IConfigurationModification cm = (IConfigurationModification)foim;
				if (!cm.isBuilderCompatible()) {
					CompatibilityStatus cs = cm.getBuilderCompatibilityStatus();
					if (cs != null) {
						String s = cs.getMessage();
						st_builder.setText(s);
					}
				}
			}
		} else { // FileInfo
			IFileInfoModification fim = tcmm.getModification((IFileInfo)ri);
			fim.getProjectToolModifications();
		}
	}
	
	private void fillToolChainCombo() {
		IToolChain tc = null;
		if (ri instanceof IFolderInfo)
			tc = ManagedBuildManager.getRealToolChain(((IFolderInfo)ri).getToolChain());

		c_toolchain.removeAll();
		boolean isMng = cfg.getBuilder().isManagedBuildOn();
		ArrayList list = new ArrayList();
		
		IToolChain[] tcs = r_tcs;
		if (b_dispCompatible.getSelection() && (ri instanceof IFolderInfo)) {
			IFolderInfoModification fim = tcmm.getModification((IFolderInfo)ri);
			tcs = fim.getCompatibleToolChains();
			IToolChain[] tcs1 = new IToolChain[tcs.length + 1];
			System.arraycopy(tcs, 0, tcs1, 0, tcs.length);
			tcs1[tcs.length] = tc; // add existing toolchain
			tcs = tcs1;
		}
		for (int i=0; i<tcs.length; i++) {
			if (tcs[i].isSystemObject() && 
			    !( ((ToolChain)tcs[i]).isPreferenceToolChain() &&
			       !isMng)
			     ) // NO TOOLCHAIN 
				continue;
			list.add(tcs[i]);
		}
		Collections.sort(list, BuildListComparator.getInstance());

		int pos = -1;
		v_tcs = (IToolChain[]) list.toArray(new IToolChain[list.size()]); 
		for (int i=0; i<v_tcs.length; i++) {
			c_toolchain.add(v_tcs[i].getUniqueRealName());
			if (v_tcs[i].matches(tc)) pos = i;
		}
		
		if (pos != -1) {
			c_toolchain.select(pos);
			c_builder.setEnabled(page.isForProject());
		}
		else {
			c_toolchain.setText(EMPTY_STR); // unprobable case
			c_builder.setEnabled(false);
		}
	}

	private void fillBuilderCombo() {
		IBuilder b = ManagedBuildManager.getRealBuilder(cfg.getBuilder());
		int pos = -1;
		c_builder.removeAll();
		ArrayList list = new ArrayList();
		
		IBuilder[] bs = r_bs;
		
		if (b_dispCompatible.getSelection() && (ri instanceof IFolderInfo)) {
			IFolderInfoModification fim = tcmm.getModification((IFolderInfo)ri);
			if (fim instanceof IConfigurationModification) {
				IBuilder[] bs1 = ((IConfigurationModification)fim).getCompatibleBuilders();
				bs = new IBuilder[bs1.length + 1];
				System.arraycopy(bs1, 0, bs, 0, bs1.length);
				bs[bs1.length] = b;
			}
		}
		for (int i=0; i<bs.length; i++) {
			if (bs[i].isSystemObject()) 
				continue;
			list.add(bs[i]);
		}
		bs = null;
		Collections.sort(list, BuildListComparator.getInstance());
		v_bs = (IBuilder[])list.toArray(new IBuilder[list.size()]);
		for (int i=0; i<v_bs.length; i++) {
			c_builder.add(v_bs[i].getUniqueRealName());
			if (v_bs[i].matches(b)) pos = i;
		}
		if (pos != -1)
			c_builder.select(pos);
		else
			c_builder.setText(EMPTY_STR);
		
	}
	
	private ITool getToolForFile() {
		ITool[] tools = ri.getTools();
		if (tools != null && tools.length > 0) {
			for (int i=0; i<tools.length; i++) {
				if (tools[i] != null && !tools[i].getCustomBuildStep()) {
					return tools[i];
				}
			}
		}
		return null;
	}

	private void fillToolCombo(boolean add, ITool curr) {
		c_tool.removeAll();
		int pos = (curr == null) ? v_tools.length : -1;
		for (int i=0; i<v_tools.length; i++) {
			if (pos == -1 && curr.matches(v_tools[i])) {
				pos = i;
				c_tool.add(curr.getUniqueRealName());  
			} else {
				c_tool.add(v_tools[i].getUniqueRealName());
			}
		}
		// Add NO_TOOL
		if (add) {
			c_tool.add(Messages.getString("ToolChainEditTab.6")); //$NON-NLS-1$
		}
		c_tool.select(pos);
	}
	
	private void fillToolsList() {
		updateAllTools(); // modifies v_tools inside !!!
		
		ToolChain tc = null;
		if (ri instanceof IFolderInfo)
			tc = (ToolChain)ManagedBuildManager.getRealToolChain(((IFolderInfo)ri).getToolChain());
		
		if (page.isForFile()) { // Edit tool in combo for File
			ITool curr = getToolForFile();
			boolean canAddNO_TOOL = true;
			if (ri instanceof IFileInfo && b_dispCompatible.getSelection())
				canAddNO_TOOL = updateCompatibleTools(curr); // modifies v_tools inside !!!
			fillToolCombo(canAddNO_TOOL, curr);
			showToolStatus(curr);
		} else if (tc != null && tc.isPreferenceToolChain()){ // display tools list for Folder and Project
			tools_group.setVisible(false);
		} else {
			tools_group.setVisible(true);
			String s = EMPTY_STR;
			ITool[] tools = ri.getTools();
			for (int i = 0; i < tools.length; i++)
				s = s + tools[i].getUniqueRealName() + "\n"; //$NON-NLS-1$
			text.setText(s);
		}
	}
	
	private void updateAllTools() {
		int cnt = 0;
		v_tools = new ITool[r_tools.length];
		for (int i=0; i<r_tools.length; i++) {
			if (r_tools[i].isSystemObject()) continue;
			if (r_tools[i].isAbstract()) continue;
			v_tools[cnt++] = r_tools[i];
		}
		ITool[] tmp = new ITool[cnt];
		System.arraycopy(v_tools, 0, tmp, 0, cnt);
		Arrays.sort(tmp, BuildListComparator.getInstance());
		v_tools = tmp;
	}

	private void showToolStatus(ITool tool) {
		st_tool.setText(EMPTY_STR);
		st_tool.setImage(null);
		if (tool == null)
			return;
		IFileInfoModification fim = tcmm.getModification((IFileInfo)ri);
		IToolModification tm = fim.getToolModification(tool);
		if (tm != null && !tm.isCompatible()) {
			CompatibilityStatus cs = tm.getCompatibilityStatus();
			if (cs != null) {
				st_tool.setText(cs.getMessage());
				st_tool.setImage(getErrorIcon(cs));
			}
		}
	}
	
	private boolean updateCompatibleTools(ITool real) {
		boolean result = false;
		ArrayList list = new ArrayList();
		IFileInfoModification fim = tcmm.getModification((IFileInfo)ri);
		
		if (real != null) { // Current tool exists 
			real = ManagedBuildManager.getRealTool(real);
			list.add(real);
			IToolModification tm = fim.getToolModification(real);
			IModificationOperation[] mo = tm.getSupportedOperations();
			for (int i=0; i<mo.length; i++) {
				ITool t = mo[i].getReplacementTool();
				if (t == null)
					result = true;
				else {
					if (! t.isSystemObject() && ! t.isAbstract())
						list.add(t);
				}
			}
		} else { // Current tool is NO_TOOL
			result = true;
			IToolModification[] tm = fim.getSystemToolModifications();
			for (int i=0; i<tm.length; i++) {
				IModificationOperation[] mo = tm[i].getSupportedOperations();
				for (int j=0; j<mo.length; j++) {
					if (mo[j].getReplacementTool() == null) {
						ITool t = tm[i].getTool(); 
						if (! t.isSystemObject() && ! t.isAbstract())
							list.add(t);
						break;
					}
				}
			}
		}
		Collections.sort(list, BuildListComparator.getInstance());
		v_tools = (ITool[]) list.toArray(new ITool[list.size()]);
		return result;
	}
	
	private void updateData() {	
		showErrorMessage();
		fillToolChainCombo();
		fillBuilderCombo();
		fillToolsList();
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
		} else if (ri1 instanceof IFileInfo && ri2 instanceof IFileInfo) {
			((IFileInfo)ri2).setTools(ri1.getTools());
		}
	}
	
	protected void performDefaults() {
		if (page.isForProject()) {
//			1.Per-project : change to the "default" tool-chain defined in the extension
//			super-class of the project configuration. NOTE: the makefile project case might
//			need a special handling in this case.
			
			IConfiguration cfg1 = cfg.getParent();
			if (cfg1.getToolChain() == null) {
				if (cfg.getToolChain() != null) {
					IToolChain tc1 = cfg.getToolChain().getSuperClass();
					if (tc1 != null) {
						IBuilder b = tc1.getBuilder();
						cfg.changeBuilder(b, ManagedBuildManager.calculateChildId(b.getId(), null), b.getUniqueRealName());
						try {
							((IFolderInfo)ri).modifyToolChain(ri.getTools(), tc1.getTools());
						} catch (BuildException e) {}
					}
				} else {
					cfg1 = ManagedBuildManager.getPreferenceConfiguration(true);
				}
			} else {
				IBuilder b = cfg1.getBuilder();
				cfg.changeBuilder(b, ManagedBuildManager.calculateChildId(b.getId(), null), b.getUniqueRealName());
				IResourceInfo ri1 = cfg1.getResourceInfo(ri.getPath(), false);
				if (!((ToolChain)cfg1.getToolChain()).isPreferenceToolChain())
					copyFI(ri1, ri);
				else {
					cfg1 = ManagedBuildManager.getPreferenceConfiguration(false);
					IToolChain tc = cfg1.getToolChain();
					try {
						((IFolderInfo)ri).changeToolChain(tc, ManagedBuildManager.calculateChildId(tc.getId(), null), tc.getUniqueRealName());
						((IFolderInfo)ri).modifyToolChain(ri.getTools(), tc.getTools());
						
					} catch (BuildException e) {}
				}
			}
		} else if (page.isForFolder()) {
//			2.per-folder : change to the same tool-chain as the one used by the parent
//			folder.
			IResourceInfo ri1 = cfg.getResourceInfo(ri.getPath().removeLastSegments(1), false);
			copyFI(ri1, ri);
			
		} else if (page.isForFile()) {
//			3.per-file : change to the tool from the parent folder's tool-chain suitable
//			for the given file. NOTE: the custom build step tool should be preserved!
			IResourceInfo ri1 = cfg.getResourceInfo(ri.getPath().removeLastSegments(1), false);
			String ext = ri.getPath().getFileExtension();

			ITool[] ts1 = ri1.getTools();
			ITool newTool = null; 
			for (int i=0; i<ts1.length; i++) {
				if (ts1[i].isInputFileType(ext)) {
					newTool = ts1[i];
					break;
				}				
			}
			
			ITool[] tools = ri.getTools();
			int pos = -1;
			for (int i=0; i<tools.length; i++) {
				if (tools[i] != null && !tools[i].getCustomBuildStep()) {
					pos = i; 
					break;
				}
			}

			if (newTool != null) {
				if (pos == -1) { // 1: NO TOOL -> tool 
					ITool[] ts2 = new ITool[tools.length + 1];
					System.arraycopy(tools, 0, ts2, 0, tools.length);
					ts2[tools.length] = newTool;
					tools = ts2;
				} else          // 2: tool -> tool
					tools[pos] = newTool;
			} else if (pos != -1){ // 3: tool -> NO TOOL;
				ITool[] ts2 = new ITool[tools.length - 1];
				if (pos > 0)
					System.arraycopy(tools, 0, ts2, 0, pos-1);
				if (pos < ts2.length)
					System.arraycopy(tools, pos+1, ts2, pos, ts2.length - pos);
				tools = ts2;
			}
			((IFileInfo)ri).setTools(tools);
		}			
		updateData();
	}
	
	private void copyFI(IResourceInfo src, IResourceInfo dst) {
		if (src == null || dst == null)
			return;
		if (src instanceof IFolderInfo && dst instanceof IFolderInfo) { 
			IFolderInfo fi1 = (IFolderInfo)src;
			IFolderInfo fi = (IFolderInfo)dst;
			IToolChain tc = fi1.getToolChain();
			ITool[] tools1 = fi1.getTools();
			ITool[] tools2 = fi.getTools();
			try {
				fi.changeToolChain(tc, ManagedBuildManager.calculateChildId(tc.getId(), null), tc.getUniqueRealName());
				fi.modifyToolChain(tools2, tools1);
			} catch (BuildException e) {}
		}
	}
	
	protected void updateButtons() {} // Do nothing. No buttons to update.
	
	private void saveToolSelected() {
		int pos = c_tool.getSelectionIndex();
		ITool[] oldTools = ri.getTools();
		if (oldTools != null && oldTools.length > 0) {
			if (pos == (c_tool.getItemCount() - 1)) {// NO TOOL selected
				List newTools = new ArrayList(oldTools.length);
				for (int i=0; i<oldTools.length; i++)
					if (oldTools[i].getCustomBuildStep()) // ignore all tools except customBuild's
						newTools.add(oldTools[i]);
				((IFileInfo)ri).setTools((ITool[])newTools.toArray(new ITool[newTools.size()]));
			} else { // modify only one (selected) tool 
				for (int i=0; i<oldTools.length; i++) {
					if (oldTools[i].getCustomBuildStep()) // skip custom steps
						continue;
					if ( ! oldTools[i].matches(v_tools[pos])) { // really changed ?
						oldTools[i] = v_tools[pos];
						((IFileInfo)ri).setTools(oldTools);
					}
					break; // exit after first not-custom tool met
				}
			}
		} else { // old list was empty.
			if (pos == (c_tool.getItemCount() - 1)) // NO TOOL selected
				((IFileInfo)ri).setTools(new ITool[0]);
			else
				((IFileInfo)ri).setTools(new ITool[] {v_tools[pos]});				
		}
		updateData();
	}
	
	private void modifyTools() {
		ToolSelectionDialog d = new ToolSelectionDialog(usercomp.getShell(), ri);
		d.all = v_tools;
		d.fi = (IFolderInfo)ri; 
		int result = d.open();
		if (result == 0 && (d.removed.size() > 0 || d.added.size() > 0)) {
			try {
				((IFolderInfo)ri).modifyToolChain(
						(ITool[])d.removed.toArray(new ITool[d.removed.size()]), 
						(ITool[])d.added.toArray(new ITool[d.added.size()]));
			} catch (BuildException b) {}
			updateData();
		}
	}
	
	private void modifyToolchain() {
		if (ri instanceof IFolderInfo) {
			int x = c_toolchain.getSelectionIndex();
			try {
				if (NO_TC.equals(c_toolchain.getItem(x))) {
					((IFolderInfo)ri).changeToolChain(null, null, null);
				} else {
					IToolChain tc = v_tcs[x];
					if (tc == null) return;
					((IFolderInfo)ri).changeToolChain(tc, ManagedBuildManager.calculateChildId(tc.getId(), null), tc.getUniqueRealName());
			}
			} catch (BuildException be) {}
			updateData();
		}
	}
	
	public static Image getErrorIcon(IStatus st) {
		if (st.isOK()) 
			return null;
		int sev = st.getSeverity();
		if (sev == IStatus.ERROR)
			return IMG_ERROR;
		else if (sev == IStatus.WARNING)
			return IMG_WARNING;
		else
			return IMG_INFO;
	}
	
	private void modifyBuilder() {
		int x = c_builder.getSelectionIndex();
		IBuilder b = v_bs[x];
		cfg.changeBuilder(b, ManagedBuildManager.calculateChildId(b.getId(), null), b.getUniqueRealName());
		updateData();
	}

	/**
	 * Forms a message containing 
	 * @param cs
	 * @return
	 */
	public static String getCompatibilityMessage(CompatibilityStatus cs) {
		IConflict[] cons = cs.getConflicts();
		StringBuffer result = new StringBuffer();
		for (int i=0; i<cons.length; i++) {
			IBuildObject bo = cons[i].getBuildObject();
			String n = (bo == null) ? 
					"NULL" : //$NON-NLS-1$
				    (bo instanceof ITool) ?
						((ITool)bo).getUniqueRealName() :
						bo.getName();
			String t = EMPTY_STR;
			switch (cons[i].getConflictType()) {
			case IConflict.INCOMPATIBLE:
				t = Messages.getString("ToolChainEditTab.7"); //$NON-NLS-1$
				break;
			case IConflict.SOURCE_EXT_CONFLICT:
				t = Messages.getString("ToolChainEditTab.8"); //$NON-NLS-1$
				break;
			}
			
			String o = EMPTY_STR;
			switch (cons[i].getObjectType()) {
			case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
				o = Messages.getString("ToolChainEditTab.9"); //$NON-NLS-1$
				break;
			case IRealBuildObjectAssociation.OBJECT_BUILDER:
				o = Messages.getString("ToolChainEditTab.10"); //$NON-NLS-1$
				break;
			case IRealBuildObjectAssociation.OBJECT_CONFIGURATION:
				o = Messages.getString("ToolChainEditTab.11"); //$NON-NLS-1$
				break;
			case IRealBuildObjectAssociation.OBJECT_FILE_INFO:
				o = Messages.getString("ToolChainEditTab.12"); //$NON-NLS-1$
				break;
			case IRealBuildObjectAssociation.OBJECT_FOLDER_INFO:
				o = Messages.getString("ToolChainEditTab.13"); //$NON-NLS-1$
				break;
			case IRealBuildObjectAssociation.OBJECT_TOOL:
				o = Messages.getString("ToolChainEditTab.14"); //$NON-NLS-1$
				break;
			}
			
			result.append(Messages.getString("ToolChainEditTab.15") + //$NON-NLS-1$
					(i+1) + Messages.getString("ToolChainEditTab.16") + //$NON-NLS-1$
					SPACE + t + SPACE + o + SPACE + n + 
					Messages.getString("ToolChainEditTab.17")); //$NON-NLS-1$
		}
		String s = result.toString();
		if (s.trim().length() == 0)
			s = cs.getMessage();
		if (s == null)
			s = EMPTY_STR;
		return s;
	}
} 
