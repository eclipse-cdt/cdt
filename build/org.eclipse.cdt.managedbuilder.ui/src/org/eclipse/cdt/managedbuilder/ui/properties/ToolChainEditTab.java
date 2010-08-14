/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
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
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.tcmodification.CompatibilityStatus;
import org.eclipse.cdt.managedbuilder.tcmodification.IConfigurationModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IConflict;
import org.eclipse.cdt.managedbuilder.tcmodification.IFileInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IFolderInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IModificationOperation;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolChainModificationManager;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolListModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolModification;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
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

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ToolChainEditTab extends AbstractCBuildPropertyTab {

	private static final IToolChainModificationManager tcmmgr = ManagedBuildManager.getToolChainModificationManager();
	private static final String NO_TC = Messages.StdProjectTypeHandler_0;
	private static final IToolChain[] r_tcs = ManagedBuildManager.getRealToolChains();
	private static final IBuilder[]    r_bs = ManagedBuildManager.getRealBuilders();
	private static final ITool[]    r_tools = ManagedBuildManager.getRealTools();
	private static final Color          red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private static final String SPACE = " "; //$NON-NLS-1$

	static private final Image IMG_WARNING = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_WARNING);
	static private final Image IMG_ERROR   = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_ERROR);
	static private final Image IMG_INFO    = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_INFO);

	private Text text;
	private Button b_dispCompatible;
	private Combo  c_toolchain;
	private Combo  c_builder;
	private Combo  c_tool;
	private Button button_edit;
	private Group tools_group;
	private Group single_tool_group;
	private Label st_builder;
	private Label st_toolchain;
	private Label st_tool;

	private IBuilder[] v_bs;
	private IToolChain[] v_tcs;
	private ITool[] v_tools;

	private IConfiguration cfg;
	private IResourceInfo ri;
	private IToolListModification mod;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		b_dispCompatible = setupCheck(usercomp, Messages.ToolChainEditTab_0, 2, GridData.BEGINNING);
		b_dispCompatible.setSelection(true);

		setupLabel(usercomp, Messages.ToolChainEditTab_1, 1, GridData.BEGINNING);
		c_toolchain = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		c_toolchain.setLayoutData(gd);
		c_toolchain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modifyToolchain();
			}});
		c_toolchain.setEnabled(!page.isForFile());

		st_toolchain = setupLabel(usercomp, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
		st_toolchain.setForeground(red);

		setupLabel(usercomp, Messages.ToolChainEditTab_2, 1, GridData.BEGINNING);
		c_builder = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		c_builder.setLayoutData(gd);
		c_builder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modifyBuilder();
			}});
		c_builder.setEnabled(page.isForProject());

		st_builder = setupLabel(usercomp, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
		st_builder.setForeground(red);

		// make table for tools list
		if (page.isForFile()) {
			single_tool_group = setupGroup(usercomp, Messages.ToolChainEditTab_5, 2, GridData.FILL_BOTH);
			setupControl(single_tool_group, 2, GridData.FILL_BOTH);
			c_tool = new Combo(single_tool_group, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			c_tool.setLayoutData(gd);
			c_tool.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					saveToolSelected();
				}});

			st_tool = setupLabel(single_tool_group, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
			st_tool.setForeground(red);
		} else { // Folder or Project
			tools_group = setupGroup(usercomp, Messages.ToolChainEditTab_3, 2, GridData.FILL_BOTH);
			setupControl(tools_group, 2, GridData.FILL_BOTH);

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
				@Override
				public void widgetSelected(SelectionEvent event) {
					modifyTools();
				}});

			button_edit.setLayoutData(new GridData(GridData.END));
			button_edit.setText(Messages.ToolChainEditTab_4);
		}
	}

	private IToolListModification getModification() {
		if (ri instanceof IFolderInfo)
			return tcmmgr.createModification((IFolderInfo)ri);
		else
			return tcmmgr.createModification((IFileInfo)ri);
	}

	@Override
	protected void updateData(ICResourceDescription rcfg) {
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return;
		} else {
			setAllVisible(true, null);
		}
		cfg = getCfg(rcfg.getConfiguration());
		ri = cfg.getResourceInfo(rcfg.getPath(), false);
		mod = getModification();
		updateData();
	}

	private void updateData() {
		showErrorMessage();
		fillToolChainCombo();
		fillBuilderCombo();
		fillToolsList();
	}

	private void showErrorMessage() {
		if (ri instanceof IFolderInfo) {
			IFolderInfoModification foim = (IFolderInfoModification)mod;
			IToolChain toolChain = ((IFolderInfoModification)mod).getToolChain();
			IToolChain realToolChain = ManagedBuildManager.getRealToolChain(toolChain);
			if (realToolChain==null) {
				// The toolchain is not derived from extension
				String errText = NLS.bind(Messages.ToolChainEditTab_OrphanedToolchain, toolChain.getId(), toolChain.getName());
				st_toolchain.setText(errText);
			} else if (foim.isToolChainCompatible()) {
				st_toolchain.setText(EMPTY_STR);
			} else {
				String s = foim.getToolChainCompatibilityStatus().getMessage();
				st_toolchain.setText(s);
			}
			st_builder.setText(EMPTY_STR);
			if (foim instanceof IConfigurationModification) {
				IConfigurationModification cm = (IConfigurationModification)foim;
				if (!cm.isBuilderCompatible()) {
					IBuilder builder = cfg.getBuilder();
					IBuilder realBuilder = ManagedBuildManager.getRealBuilder(builder);
					if (realBuilder==null) {
						// The builder is not derived from extension
						String errText = NLS.bind(Messages.ToolChainEditTab_OrphanedBuilder, builder.getId(), builder.getName());
						st_builder.setText(errText);
					} else {
						CompatibilityStatus cs = cm.getBuilderCompatibilityStatus();
						if (cs != null) {
							String s = cs.getMessage();
							st_builder.setText(s);
						}
					}
				}
			}
//		} else { // FileInfo
//			IFileInfoModification fim = (IFileInfoModification)mod;
//			fim.getProjectToolModifications();
		}
	}

	private void fillToolChainCombo() {
		IToolChain realToolChain = null;
		if (ri instanceof IFolderInfo) {
			IToolChain toolChainInstance = ((IFolderInfoModification)mod).getToolChain();
			realToolChain = ManagedBuildManager.getRealToolChain(toolChainInstance);
			if (realToolChain==null)
				realToolChain = toolChainInstance;
		}

		c_toolchain.removeAll();
		boolean isMng = cfg.getBuilder().isManagedBuildOn();
		ArrayList<IToolChain> list = new ArrayList<IToolChain>();

		IToolChain[] tcs = r_tcs;
		if (b_dispCompatible.getSelection() && (ri instanceof IFolderInfo)) {
			IFolderInfoModification fim = (IFolderInfoModification)mod;
			tcs = fim.getCompatibleToolChains();
			IToolChain[] tcs1 = new IToolChain[tcs.length + 1];
			System.arraycopy(tcs, 0, tcs1, 0, tcs.length);
			tcs1[tcs.length] = realToolChain; // add existing toolchain
			tcs = tcs1;
		}
		for (IToolChain tc : tcs) {
			if ( tc.isSystemObject() && !(((ToolChain)tc).isPreferenceToolChain() && !isMng) ) {
				// NO TOOLCHAIN
				continue;
			}
			list.add(tc);
		}
		Collections.sort(list, BuildListComparator.getInstance());

		int pos = -1;
		v_tcs = list.toArray(new IToolChain[list.size()]);
		for (int i=0; i<v_tcs.length; i++) {
			c_toolchain.add(v_tcs[i].getUniqueRealName());
			if (v_tcs[i].matches(realToolChain)) pos = i;
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
		IBuilder realBuilder = ManagedBuildManager.getRealBuilder(cfg.getBuilder());
		if (realBuilder==null)
			realBuilder = cfg.getBuilder();
		int pos = -1;
		c_builder.removeAll();
		ArrayList<IBuilder> list = new ArrayList<IBuilder>();

		IBuilder[] bs = r_bs;

		if (b_dispCompatible.getSelection() && (ri instanceof IFolderInfo)) {
			IFolderInfoModification fim = (IFolderInfoModification)mod;
			if (fim instanceof IConfigurationModification) {
				bs = ((IConfigurationModification)fim).getCompatibleBuilders();
				IBuilder[] bs1 = new IBuilder[bs.length + 1];
				System.arraycopy(bs, 0, bs1, 0, bs.length);
				bs1[bs.length] = realBuilder;
				bs = bs1;
			}
		}
		for (int i=0; i<bs.length; i++) {
			if (bs[i].isSystemObject() && ! bs[i].equals(realBuilder))
				continue;
			list.add(bs[i]);
		}
		bs = null;
		Collections.sort(list, BuildListComparator.getInstance());
		v_bs = list.toArray(new IBuilder[list.size()]);
		for (int i=0; i<v_bs.length; i++) {
			c_builder.add(v_bs[i].getUniqueRealName());
			if (v_bs[i].matches(realBuilder)) pos = i;
		}
		if (pos != -1)
			c_builder.select(pos);
		else
			c_builder.setText(EMPTY_STR);

	}

	private ITool getToolForFile() {
		ITool[] tools = ((IFileInfoModification)mod).getProjectTools();
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
			c_tool.add(Messages.ToolChainEditTab_6);
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
			for (ITool tool : tools) {
				s = s + tool.getUniqueRealName() + "\n"; //$NON-NLS-1$
			}
			text.setText(s);
		}
	}

	private void updateAllTools() {
		int cnt = 0;
		v_tools = new ITool[r_tools.length];
		for (ITool r_tool : r_tools) {
			if (r_tool.isSystemObject()) continue;
			if (r_tool.isAbstract()) continue;
			v_tools[cnt++] = r_tool;
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
		IFileInfoModification fim = (IFileInfoModification)mod;
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
		ArrayList<ITool> list = new ArrayList<ITool>();
		IFileInfoModification fim = (IFileInfoModification)mod;

		if (real != null) { // Current tool exists
			real = ManagedBuildManager.getRealTool(real);
			list.add(real);
			IToolModification tm = fim.getToolModification(real);
			IModificationOperation[] mos = tm.getSupportedOperations();
			for (IModificationOperation mo : mos) {
				ITool t = mo.getReplacementTool();
				if (t == null)
					result = true;
				else {
					if (! t.isSystemObject() && ! t.isAbstract())
						list.add(t);
				}
			}
		} else { // Current tool is NO_TOOL
			result = true;
			IToolModification[] tms = fim.getSystemToolModifications();
			for (IToolModification tm : tms) {
				IModificationOperation[] mos = tm.getSupportedOperations();
				for (IModificationOperation mo : mos) {
					if (mo.getReplacementTool() == null) {
						ITool t = tm.getTool();
						if (! t.isSystemObject() && ! t.isAbstract())
							list.add(t);
						break;
					}
				}
			}
		}
		Collections.sort(list, BuildListComparator.getInstance());
		v_tools = list.toArray(new ITool[list.size()]);
		return result;
	}

    @Override
	protected void checkPressed(SelectionEvent e) {
    	updateData();
    }
	@Override
	protected void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		if (mod == null)
			return;
		IConfiguration cfg = getCfg(dst.getConfiguration());
		try {
			IToolListModification tlm = (ri instanceof IFolderInfo) ?
				(IToolListModification)tcmmgr.createModification(cfg, (IFolderInfoModification)mod) :
				(IToolListModification)tcmmgr.createModification(cfg, (IFileInfoModification)mod);
			tlm.apply();
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	@Override
	protected void performDefaults() {
		if (mod != null) {
			mod.restoreDefaults();
			apply();
			updateData();
		}
	}

	@Override
	protected void updateButtons() {} // Do nothing. No buttons to update.

	private void saveToolSelected() {

		IFileInfoModification fim = (IFileInfoModification)mod;

		// Find old tool
		ITool oldTool = null;
		ITool[] ots = fim.getProjectTools();
		for (int i=0; i<ots.length; i++) {
			if (!ots[i].getCustomBuildStep()) {
				oldTool = ots[i];
				break;
			}
		}
		// Find new Tool
		ITool newTool = null;
		int pos = c_tool.getSelectionIndex();
		if (pos < (c_tool.getItemCount() - 1))
			newTool = v_tools[pos];

		// May be we've seleceted the same tool
		if (newTool == null && oldTool == null)
			return; // nothing to change
		if (newTool != null && newTool.matches(oldTool))
			return; // nothing to change

		// Apply changes
		fim.changeProjectTools(oldTool, newTool);
		apply();
		updateData();
	}

	/**
	 * Opens Tools selection dialog.
	 * Depending of result (OK/Cancel)
	 * either applies tools changes,
	 * or re-creates modification to abandon changes
	 */
	private void modifyTools() {

		ToolSelectionDialog d = new ToolSelectionDialog(usercomp.getShell(), ri);
		d.all = v_tools;
		d.fi = (IFolderInfo)ri;
		d.mod = mod;
		int result = d.open();
		if (d.removed.size() > 0 || d.added.size() > 0) {
			if (result == 0) {
				apply();
				updateData();
			} else {
				mod = getModification(); // re-read data
			}
		}
	}

	/**
	 * Applies changes to Modification
	 */
	private void apply() {
		try {
			mod.apply();
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	private void modifyToolchain() {
		IFolderInfoModification foim = (IFolderInfoModification)mod;
		int x = c_toolchain.getSelectionIndex();
		if (NO_TC.equals(c_toolchain.getItem(x))) {
			foim.setToolChain(null);
		} else {
			IToolChain tc = v_tcs[x];
			if (tc == null) return;
			foim.setToolChain(tc);
		}
		apply();
		updateData();
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
		IConfigurationModification cm = (IConfigurationModification)mod;
		cm.setBuilder(v_bs[c_builder.getSelectionIndex()]);
		apply();
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
				t = Messages.ToolChainEditTab_7;
				break;
			case IConflict.SOURCE_EXT_CONFLICT:
				t = Messages.ToolChainEditTab_8;
				break;
			}

			String o = EMPTY_STR;
			switch (cons[i].getObjectType()) {
			case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
				o = Messages.ToolChainEditTab_9;
				break;
			case IRealBuildObjectAssociation.OBJECT_BUILDER:
				o = Messages.ToolChainEditTab_10;
				break;
			case IRealBuildObjectAssociation.OBJECT_CONFIGURATION:
				o = Messages.ToolChainEditTab_11;
				break;
			case IRealBuildObjectAssociation.OBJECT_FILE_INFO:
				o = Messages.ToolChainEditTab_12;
				break;
			case IRealBuildObjectAssociation.OBJECT_FOLDER_INFO:
				o = Messages.ToolChainEditTab_13;
				break;
			case IRealBuildObjectAssociation.OBJECT_TOOL:
				o = Messages.ToolChainEditTab_14;
				break;
			}

			result.append(Messages.ToolChainEditTab_15 +
					(i+1) + Messages.ToolChainEditTab_16 +
					SPACE + t + SPACE + o + SPACE + n +
					Messages.ToolChainEditTab_17);
		}
		String s = result.toString();
		if (s.trim().length() == 0)
			s = cs.getMessage();
		if (s == null)
			s = EMPTY_STR;
		return s;
	}
}
