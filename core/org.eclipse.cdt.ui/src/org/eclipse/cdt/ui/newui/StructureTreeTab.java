/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * This tab is intended to browse
 * contents of whole class such as ResourceDescription,
 * ConfigurationDescription or ProjectDescription
 *
 * Notes:
 *
 * 1. Many strings in the file remain unlocalized
 *    since they represent method names.
 * 2. It is experimental functionality. Work is in progress.
 * 3. Tree depth is limited by 16. Deeper branches are truncated.
 *    But it seems to be very rare situation.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class StructureTreeTab  extends AbstractCPropertyTab {

	protected class LevelDialog extends Dialog {
		protected LevelDialog() {
			super(CUIPlugin.getActiveWorkbenchShell());
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = new Composite(parent, 0);
			c.setLayoutData(new GridData(GridData.FILL_BOTH));
			c.setLayout(new GridLayout(2, false));
			Label l = new Label(c, 0);
			l.setText(Messages.StructureTreeTab_0);
			c.setLayoutData(new GridData(GridData.BEGINNING));
			Spinner sp = new Spinner(c, SWT.BORDER);
			sp.setMaximum(NESTING_MAX);
			sp.setMinimum(0);
			sp.setSelection(currentLevel);
			sp.addSelectionListener(new SelectionAdapter () {
				@Override
				public void widgetSelected(SelectionEvent e) {
					currentLevel = ((Spinner)e.widget).getSelection();
				}
			});
			return c;
		}
	}
	private static final String BL = "["; //$NON-NLS-1$
	private static final String BR = "]"; //$NON-NLS-1$
	private static final Image IMG = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_REFACTORING_ERROR);
	private static final int NESTING_CFG = 5;
	private static final int NESTING_MAX = 16;
	private static final String NULL = "<NULL>"; //$NON-NLS-1$
	private int currentLevel = 4; // default depth
	private Combo combo;
	private Tree tree;
	private ICResourceDescription cfg;

	/*
	 * Common check for each tree item:
	 * - corresponding objects are not null.
	 * - tree item nesting is no too deep.
	 */
	private boolean check(TreeItem ti, Object obj) {
		if (obj == null || ti == null) return false;
		// data not used now
		// ti.setData(obj);
		int cnt = NESTING_MAX;
		TreeItem tiSaved = ti;
		while (--cnt > 0) {
			ti = ti.getParentItem();
			if (ti == null) return true;
		}
		tiSaved.setText(2, Messages.StructureTreeTab_1);
		tiSaved.setImage(IMG);
		return false;
	}

	private TreeItem create(TreeItem ti0, String text, boolean val) {
		TreeItem t = create(ti0, text, String.valueOf(val));
		t.setText(2, EMPTY_STR);
		return t;
	}

	private TreeItem create(TreeItem ti0, String text, int val) {
		TreeItem t = create(ti0, text, String.valueOf(val));
		t.setText(2, EMPTY_STR);
		return t;
	}
	private TreeItem create(TreeItem ti0, String text, long val) {
		TreeItem t = create(ti0, text, String.valueOf(val));
		t.setText(2, Messages.StructureTreeTab_2);
		return t;
	}

	private TreeItem create(TreeItem ti0, String text, String val) {
		TreeItem ti =  ti0 == null ? new TreeItem(tree, 0) : new TreeItem(ti0, 0);
		ti.setText(0, text == null ? NULL : text);
		ti.setText(1, val  == null ? NULL : val );
		ti.setText(2, Messages.StructureTreeTab_3);
		return ti;
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(5, false));

		Label lb = new Label(usercomp, 0);
		lb.setText(Messages.StructureTreeTab_4);
		lb.setLayoutData(new GridData(GridData.BEGINNING));

		combo = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.add(Messages.ConfigDescriptionTab_0);
		combo.add(Messages.ConfigDescriptionTab_1);
		if (page.isForFolder() || page.isForFile()) {
			combo.add(Messages.ConfigDescriptionTab_2);
			combo.select(2); // ResourceDescription
		} else
			combo.select(1); // ConfigurationDescription
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateData(cfg);
			}});

		Button b1 = new Button(usercomp, SWT.PUSH);
		GridData gd = new GridData(GridData.END);
		gd.minimumWidth = BUTTON_WIDTH;
		b1.setLayoutData(gd);
		b1.setText(Messages.StructureTreeTab_5);
		b1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tree.setRedraw(false);
				expandAll(tree.getItem(0), true, -1);
				tree.setRedraw(true);
			}});

		Button b2 = new Button(usercomp, SWT.PUSH);
		gd = new GridData(GridData.END);
		gd.minimumWidth = BUTTON_WIDTH;
		b2.setLayoutData(gd);
		b2.setText(Messages.StructureTreeTab_6);
		b2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LevelDialog ld = new LevelDialog();
				if (ld.open() == Window.OK) {
					tree.setRedraw(false);
					expandAll(tree.getItem(0), true, 0);
					tree.setRedraw(true);
				}
			}});

		Button b3 = new Button(usercomp, SWT.PUSH);
		gd = new GridData(GridData.END);
		gd.minimumWidth = BUTTON_WIDTH;
		b3.setLayoutData(gd);
		b3.setText(Messages.StructureTreeTab_7);
		b3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tree.setRedraw(false);
				expandAll(tree.getItem(0), false, -1);
				tree.setRedraw(true);
			}});

		tree = new Tree(usercomp, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 5;
		tree.setLayoutData(gd);

		TreeColumn tc = new TreeColumn(tree, 0);
		tc.setText(Messages.StructureTreeTab_8);
		tc.setWidth(300);
		tc = new TreeColumn(tree, 0);
		tc.setText(Messages.StructureTreeTab_9);
		tc.setWidth(100);
		tc = new TreeColumn(tree, 0);
		tc.setText(Messages.StructureTreeTab_10);
		tc.setWidth(200);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		tree.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
			}});
	}

	private TreeItem createObj(TreeItem ti0, String text, String name, Object obj) {
		TreeItem t = create(ti0, text, BL+name+BR);
		if (obj != null) t.setText(2, obj.getClass().getName());
		return t;
	}

	/**
	 * Adds conents of array to tree.
	 */
	private void expand(TreeItem ti0, String text, Object[] obs) {
		TreeItem ti = create(ti0, text, obs == null ? 0 : obs.length);
		if (obs == null || !check(ti, obs))
			return;
		for (int i=0; i<obs.length; i++) {
			String s = BL+i+BR;
			if (obs[i] instanceof String) create(ti, s, (String)obs[i]);
			else if (obs[i] instanceof CLanguageData) update(ti, s, (CLanguageData)obs[i]);
			else if (obs[i] instanceof CResourceData) update(ti, s, (CResourceData)obs[i]);
			else if (obs[i] instanceof ICExclusionPatternPathEntry) update(ti, s, (ICExclusionPatternPathEntry)obs[i]);
			else if (obs[i] instanceof ICExternalSetting) update(ti, s, (ICExternalSetting)obs[i]);
			else if (obs[i] instanceof ICLanguageSettingEntry) update(ti, s, (ICLanguageSettingEntry)obs[i]);
			else if (obs[i] instanceof ICResourceDescription) update(ti, s, (ICResourceDescription)obs[i]);
			else if (obs[i] instanceof ICSettingObject) update(ti, s, (ICSettingObject)obs[i]);
			else if (obs[i] instanceof IPath) update(ti, s, (IPath)obs[i]);
			else if (obs[i] instanceof IResource) update(ti, s, (IResource)obs[i]);
			else if (obs[i] instanceof IProjectNatureDescriptor) update(ti, s, (IProjectNatureDescriptor)obs[i]);
			else update(ti, s, obs[i]);
		}
	}

	private void expandAll(TreeItem ti, boolean b, int level) {
		if (level == -1) ti.setExpanded(b);
		else ti.setExpanded(level++ < currentLevel);

		TreeItem[] tis = ti.getItems();
		if (tis == null) return;
		for (TreeItem ti2 : tis)
			expandAll(ti2, b, level);
	}
	// used for languages kinds display
	private int[] flagsToArray(int flags){
		int arr[] = new int[32];
		int num = 0;
		for(int i = 1; i != 0; i = i << 1){
			if((flags & i) != 0)
				arr[num++] = i;
		}
		if(num == arr.length) return arr;
		else if(num == 0) return new int[0];
		int result[] = new int[num];
		System.arraycopy(arr, 0, result, 0, num);
		return result;
	}
	private int getDepth(TreeItem ti) {
		int x = 0;
		while (ti != null) {
			ti = ti.getParentItem();
			x++;
		}
		return x;
	}

	@Override
	protected void performApply(ICResourceDescription src,ICResourceDescription dst) {}

	@Override
	protected void performDefaults() {}

	private void update(ICProjectDescription prj) {
		TreeItem ti = new TreeItem(tree, 0);
		if (!check(ti, prj)) return;
		ti.setText(0, "ICProjectDescription");  //$NON-NLS-1$
		update(ti, "getActiveConfiguration()", prj.getActiveConfiguration()); //$NON-NLS-1$
		expand(ti, "getConfigurations()", prj.getConfigurations()); //$NON-NLS-1$
		create(ti,"getId()",prj.getId()); //$NON-NLS-1$
		create(ti,"getName()",prj.getName()); //$NON-NLS-1$
		update(ti, "getParent()", prj.getParent()); //$NON-NLS-1$
		update(ti, "getProject()", prj.getProject()); //$NON-NLS-1$
		create(ti,"getType()",prj.getType()); //$NON-NLS-1$
		create(ti,"isModified()",prj.isModified()); //$NON-NLS-1$
		create(ti,"isReadOnly()",prj.isReadOnly()); //$NON-NLS-1$
		create(ti,"isValid()",prj.isValid()); //$NON-NLS-1$
	}

	private TreeItem update(TreeItem ti0, String text, CBuildData bd) {
		TreeItem ti = createObj(ti0, text, bd  == null ? NULL : bd.getName(), bd);
		if (bd == null || !check(ti, bd)) return ti;
		// ALMOST THE SAME AS ICBuildSetting
		update(ti, "getBuilderCWD()", bd.getBuilderCWD()); //$NON-NLS-1$
		createObj(ti, "getBuildEnvironmentContributor()", EMPTY_STR, bd.getBuildEnvironmentContributor()); //$NON-NLS-1$
		expand(ti, "getErrorParserIDs()", bd.getErrorParserIDs()); //$NON-NLS-1$
		create(ti, "getId()", bd.getId()); //$NON-NLS-1$
		create(ti, "getName()", bd.getName()); //$NON-NLS-1$
		expand(ti, "getOutputDirectories()", bd.getOutputDirectories()); //$NON-NLS-1$
		create(ti, "getType()", bd.getType()); //$NON-NLS-1$
		create(ti, "isValid()",bd.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, CConfigurationData cd) {
		TreeItem ti = createObj(ti0, text, cd == null ? NULL : cd.getName(), cd);
		if (cd == null || !check(ti, cd)) return ti;
		update(ti, "getBuildData()", cd.getBuildData()); //$NON-NLS-1$
		createObj(ti, "getBuildVariablesContributor()", EMPTY_STR, cd.getBuildVariablesContributor()); //$NON-NLS-1$
		create(ti, "getDescription()", cd.getDescription()); //$NON-NLS-1$
		create(ti, "getId()", cd.getId()); //$NON-NLS-1$
		create(ti, "getName()", cd.getName()); //$NON-NLS-1$
		expand(ti, "getResourceDatas()", cd.getResourceDatas()); //$NON-NLS-1$
		update(ti, "getRootFolderData()", cd.getRootFolderData()); //$NON-NLS-1$
//		expand(ti, "getSourcePaths()", cd.getSourcePaths()); //$NON-NLS-1$
		update(ti, "getTargetPlatformData()", cd.getTargetPlatformData()); //$NON-NLS-1$
		create(ti,"getType()",cd.getType()); //$NON-NLS-1$
		create(ti,"isValid()",cd.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, CLanguageData ls) {
		TreeItem ti = createObj(ti0, text, ls == null ? NULL : ls.getName(), ls);
		if (ls == null || !check(ti, ls)) return ti;
		create(ti, "getId()", ls.getId()); //$NON-NLS-1$
		create(ti, "getLanguageId()", ls.getLanguageId()); //$NON-NLS-1$
		create(ti, "getName()", ls.getName()); //$NON-NLS-1$
		expand(ti, "getSourceContentTypeIds()", ls.getSourceContentTypeIds()); //$NON-NLS-1$
		expand(ti, "getSourceExtensions()", ls.getSourceExtensions()); //$NON-NLS-1$
		create(ti,"getType()",ls.getType()); //$NON-NLS-1$
		int k = ls.getSupportedEntryKinds();
		TreeItem ti1 = create(ti, "getSupportedEntryKinds()", k); //$NON-NLS-1$
		int[] kind = flagsToArray(k);
		for (int element : kind) {
			TreeItem ti2 = create(ti1, "Kind", element); //$NON-NLS-1$
			expand(ti2, "getEntries",ls.getEntries(element)); //$NON-NLS-1$
		}
		create(ti,"isValid()",ls.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, CResourceData bd) {
		TreeItem ti = createObj(ti0, text, bd == null ? NULL : bd.getName(), bd);
		if (bd == null || !check(ti, bd)) return ti;
		create(ti, "getId()", bd.getId()); //$NON-NLS-1$
		if (bd instanceof CFolderData)
		    expand(ti, "getLanguageDatas()", ((CFolderData)bd).getLanguageDatas()); //$NON-NLS-1$
		create(ti, "getName()", bd.getName()); //$NON-NLS-1$
		update(ti,"getPath()",bd.getPath()); //$NON-NLS-1$
		create(ti,"getType()",bd.getType()); //$NON-NLS-1$
//		create(ti,"isExcluded()",bd.isExcluded()); //$NON-NLS-1$
		create(ti,"isValid()",bd.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, CTargetPlatformData bd) {
		TreeItem ti = createObj(ti0, text, bd == null ? NULL : bd.getName(), bd);
		if (bd == null || !check(ti, bd)) return ti;
		expand(ti, "getBinaryParserIds()", bd.getBinaryParserIds()); //$NON-NLS-1$
		create(ti, "getId()", bd.getId()); //$NON-NLS-1$
		create(ti, "getName()", bd.getName()); //$NON-NLS-1$
		create(ti, "getType()",bd.getType()); //$NON-NLS-1$
		create(ti, "isValid()",bd.isValid()); //$NON-NLS-1$
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICBuildSetting obj) {
		TreeItem ti = createObj(ti0, text, obj == null ? NULL : obj.getName(), obj);
		if (obj == null || !check(ti, obj)) return ti;
		// ALMOST THE SAME AS CBuildData
		update(ti, "getBuilderCWD()", obj.getBuilderCWD()); //$NON-NLS-1$
		createObj(ti, "getBuildEnvironmentContributor()", EMPTY_STR, obj.getBuildEnvironmentContributor()); //$NON-NLS-1$
		ICConfigurationDescription cd = obj.getConfiguration();
		createObj(ti, "getConfiguration()", cd == null ? NULL : cd.getName(), cd); //$NON-NLS-1$
		expand(ti, "getErrorParserIDs()", obj.getErrorParserIDs()); //$NON-NLS-1$
		create(ti, "getId()", obj.getId()); //$NON-NLS-1$
		create(ti, "getName()", obj.getName()); //$NON-NLS-1$
		expand(ti, "getOutputDirectories()", obj.getOutputDirectories()); //$NON-NLS-1$
		update(ti, "getParent()", obj.getParent()); //$NON-NLS-1$
		create(ti, "getType()", obj.getType()); //$NON-NLS-1$
		create(ti, "isReadOnly()", obj.isReadOnly()); //$NON-NLS-1$
		create(ti, "isValid()",obj.isValid()); //$NON-NLS-1$
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICConfigurationDescription cfg) {
		TreeItem ti = createObj(ti0, text, cfg == null ? NULL : cfg.getName(), cfg);
		if (cfg == null || !check(ti, cfg)) return ti;
		if (getDepth(ti) > NESTING_CFG) return ti;

		update(ti, "getBuildSetting()", cfg.getBuildSetting()); //$NON-NLS-1$
		create(ti, "getBuildSystemId()", cfg.getBuildSystemId()); //$NON-NLS-1$
		createObj(ti, "getBuildVariablesContributor()", EMPTY_STR, cfg.getBuildVariablesContributor()); //$NON-NLS-1$
		update(ti, "getConfigurationData()", cfg.getConfigurationData()); //$NON-NLS-1$
		create(ti, "getDescription()", cfg.getDescription()); //$NON-NLS-1$
		expand(ti, "getExternalSettings()", cfg.getExternalSettings()); //$NON-NLS-1$
		expand(ti, "getFileDescriptions()", cfg.getFileDescriptions()); //$NON-NLS-1$
		expand(ti, "getFolderDescriptions()", cfg.getFolderDescriptions()); //$NON-NLS-1$
		create(ti, "getId()", cfg.getId()); //$NON-NLS-1$
		create(ti, "getName()", cfg.getName()); //$NON-NLS-1$
		update(ti, "getParent()", cfg.getParent()); //$NON-NLS-1$
		update(ti, "getReferenceInfo()", cfg.getReferenceInfo()); //$NON-NLS-1$
		expand(ti, "getResourceDescriptions()", cfg.getResourceDescriptions()); //$NON-NLS-1$
		update(ti, "getRootFolderDescription()", cfg.getRootFolderDescription()); //$NON-NLS-1$
		expand(ti, "getSourceEntries()", cfg.getSourceEntries()); //$NON-NLS-1$
		update(ti, "getTargetPlatformSetting()", cfg.getTargetPlatformSetting()); //$NON-NLS-1$
		create(ti,"getType()",cfg.getType()); //$NON-NLS-1$
		create(ti,"isActive()",cfg.isActive()); //$NON-NLS-1$
		create(ti,"isModified()",cfg.isModified()); //$NON-NLS-1$
		create(ti,"isPreferenceConfiguration()",cfg.isPreferenceConfiguration()); //$NON-NLS-1$
		create(ti,"isReadOnly()",cfg.isReadOnly()); //$NON-NLS-1$
		create(ti,"isValid()",cfg.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICExclusionPatternPathEntry s) {
		TreeItem ti = createObj(ti0, text, s.getName(), s);
		if (!check(ti, s)) return ti;
		char[][] chrs = s.fullExclusionPatternChars();
		TreeItem ti1 = create(ti, "fullExclusionPatternChars()", chrs.length); //$NON-NLS-1$
		for (int j=0; j<chrs.length; j++)
			create(ti1, BL+j+BR, new String(chrs[j]));
		expand(ti, "getExclusionPatterns()", s.getExclusionPatterns()); //$NON-NLS-1$
		create(ti,"getFlags()", s.getFlags()); //$NON-NLS-1$
		update(ti, "getFullPath()", s.getFullPath()); //$NON-NLS-1$
		create(ti,"getKind()", s.getKind()); //$NON-NLS-1$
		update(ti, "getLocation()", s.getLocation()); //$NON-NLS-1$
		create(ti, "getName()", s.getName()); //$NON-NLS-1$
		create(ti, "getValue()", s.getValue()); //$NON-NLS-1$
		create(ti, "isBuiltIn()", s.isBuiltIn()); //$NON-NLS-1$
		create(ti, "isReadOnly()", s.isReadOnly()); //$NON-NLS-1$
		create(ti, "isResolved()", s.isResolved()); //$NON-NLS-1$
		create(ti, "isValueWorkspacePath()", s.isValueWorkspacePath()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICExternalSetting es) {
		TreeItem ti = createObj(ti0, text, EMPTY_STR, es);
		if (!check(ti, es)) return ti;
		expand(ti, "getCompatibleContentTypeIds()", es.getCompatibleContentTypeIds()); //$NON-NLS-1$
		expand(ti, "getCompatibleExtensions()", es.getCompatibleExtensions()); //$NON-NLS-1$
		expand(ti, "getCompatibleLanguageIds()", es.getCompatibleLanguageIds()); //$NON-NLS-1$
		expand(ti, "getEntries()", es.getEntries()); //$NON-NLS-1$
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICResourceDescription rcfg) {
		TreeItem ti = createObj(ti0, text, rcfg == null ? NULL : rcfg.getName(), rcfg);
		if (rcfg == null || !check(ti, rcfg)) return ti;
		update(ti, "getConfiguration()", rcfg.getConfiguration()); //$NON-NLS-1$
		create(ti, "getId()", rcfg.getId()); //$NON-NLS-1$
		create(ti, "getName()", rcfg.getName()); //$NON-NLS-1$
		if (rcfg instanceof ICFileDescription)
			update(ti, "getLanguageSettings()", ((ICFileDescription)rcfg).getLanguageSetting()); //$NON-NLS-1$
		else if (rcfg instanceof ICFolderDescription) {
			expand(ti, "getLanguageSettings()", ((ICFolderDescription)rcfg).getLanguageSettings()); //$NON-NLS-1$
			ICResourceDescription[] rds = ((ICFolderDescription)rcfg).getNestedResourceDescriptions();
			if (getDepth(ti) > NESTING_CFG)
				create(ti, "getNestedResourceDescriptions()", rds.length); //$NON-NLS-1$
			else
				expand(ti, "getNestedResourceDescriptions()", rds); //$NON-NLS-1$
		}
		update(ti, "getParent()", rcfg.getParent()); //$NON-NLS-1$
		update(ti, "getParentFolderDescription()", rcfg.getParentFolderDescription()); //$NON-NLS-1$
		update(ti, "getPath()", rcfg.getPath()); //$NON-NLS-1$
		create(ti, "getType()", rcfg.getType()); //$NON-NLS-1$
		create(ti,"isExcluded()", rcfg.isExcluded()); //$NON-NLS-1$
		create(ti,"isReadOnly()", rcfg.isReadOnly()); //$NON-NLS-1$
		if (rcfg instanceof ICFolderDescription)
			create(ti,"isRoot()",((ICFolderDescription)rcfg).isRoot()); //$NON-NLS-1$
		create(ti,"isValid()",rcfg.isValid()); //$NON-NLS-1$
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICLanguageSetting ls) {
		TreeItem ti = createObj(ti0, text, ls == null ? NULL : ls.getName(), ls);
		if (ls == null || !check(ti, ls)) return ti;
		update(ti, "getConfiguration()", ls.getConfiguration()); //$NON-NLS-1$
		create(ti, "getId()", ls.getId()); //$NON-NLS-1$
		create(ti, "getLanguageId()", ls.getLanguageId()); //$NON-NLS-1$
		create(ti, "getName()", ls.getName()); //$NON-NLS-1$
		update(ti, "getParent()", ls.getParent()); //$NON-NLS-1$
		expand(ti, "getSourceContentTypeIds()", ls.getSourceContentTypeIds()); //$NON-NLS-1$
		expand(ti, "getSourceExtensions()", ls.getSourceExtensions()); //$NON-NLS-1$
		create(ti,"getType()",ls.getType()); //$NON-NLS-1$
		int k = ls.getSupportedEntryKinds();
		TreeItem ti1 = create(ti, "getSupportedEntryKinds()", k); //$NON-NLS-1$
		int[] kind = flagsToArray(k);
		for (int element : kind) {
			TreeItem ti2 = create(ti1, "Kind", element); //$NON-NLS-1$
			expand(ti2, "getResolvedSettingEntries",ls.getResolvedSettingEntries(element)); //$NON-NLS-1$
			expand(ti2, "getSettingEntries", ls.getSettingEntries(element)); //$NON-NLS-1$
		}
		create(ti,"isReadOnly()",ls.isReadOnly()); //$NON-NLS-1$
		create(ti,"isValid()",ls.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICLanguageSettingEntry ent) {
		TreeItem ti = createObj(ti0, text, ent == null ? NULL : ent.getName(), ent);
		if (ent == null || !check(ti, ent)) return ti;
		create(ti, "getFlags()", ent.getFlags()); //$NON-NLS-1$
		create(ti, "getKind()", ent.getKind()); //$NON-NLS-1$
		create(ti, "getName()", ent.getName()); //$NON-NLS-1$
		create(ti, "getValue()", ent.getValue()); //$NON-NLS-1$
		create(ti, "isBuiltIn()", ent.isBuiltIn()); //$NON-NLS-1$
		create(ti, "isReadOnly()", ent.isReadOnly()); //$NON-NLS-1$
		create(ti, "isResolved()", ent.isResolved()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICSettingContainer c) {
		TreeItem ti = createObj(ti0, text, EMPTY_STR, c);
		if (!check(ti, c)) return ti;
		if (getDepth(ti) > NESTING_CFG) return ti;
		expand(ti, "getChildSettings()", c.getChildSettings()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICSettingObject obj) {
		TreeItem ti = createObj(ti0, text, obj == null ? NULL : obj.getName(), obj);
		if (obj == null || !check(ti, obj)) return ti;
		if (obj instanceof ICTargetPlatformSetting)
			expand(ti, "getBinaryParserIds()", ((ICTargetPlatformSetting)obj).getBinaryParserIds()); //$NON-NLS-1$
		update(ti, "getConfiguration()", obj.getConfiguration()); //$NON-NLS-1$
		create(ti, "getId()", obj.getId()); //$NON-NLS-1$
		create(ti, "getName()", obj.getName()); //$NON-NLS-1$
		createObj(ti, "getParent()", EMPTY_STR, obj.getParent()); //$NON-NLS-1$
		create(ti, "getType()", obj.getType()); //$NON-NLS-1$
		create(ti,"isReadOnly()", obj.isReadOnly()); //$NON-NLS-1$
		create(ti,"isValid()",obj.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICTargetPlatformSetting obj) {
		TreeItem ti = createObj(ti0, text, obj == null ? NULL : obj.getName(), obj);
		if (obj == null || !check(ti, obj)) return ti;
		update(ti, "getConfiguration()", obj.getConfiguration()); //$NON-NLS-1$
		create(ti, "getId()", obj.getId()); //$NON-NLS-1$
		create(ti, "getName()", obj.getName()); //$NON-NLS-1$
		update(ti, "getParent()", obj.getParent()); //$NON-NLS-1$
		create(ti, "getType()", obj.getType()); //$NON-NLS-1$
		create(ti,"isReadOnly()", obj.isReadOnly()); //$NON-NLS-1$
		create(ti,"isValid()",obj.isValid()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IPath p) {
		TreeItem ti = createObj(ti0, text, p == null ? NULL : p.toString(), p);
		if (p == null || !check(ti, p)) return ti;
		create(ti, "getDevice()", p.getDevice()); //$NON-NLS-1$
		create(ti, "getFileExtension()", p.getFileExtension()); //$NON-NLS-1$
		create(ti, "hasTrailingSeparator()", p.hasTrailingSeparator()); //$NON-NLS-1$
		create(ti, "isAbsolute()", p.isAbsolute()); //$NON-NLS-1$
		create(ti, "isEmpty()", p.isEmpty()); //$NON-NLS-1$
		create(ti, "isRoot()", p.isRoot()); //$NON-NLS-1$
		create(ti, "isUNC()", p.isUNC()); //$NON-NLS-1$
		TreeItem ti1 = create(ti, "segmentCount()", p.segmentCount()); //$NON-NLS-1$
		for (int i=0; i<p.segmentCount(); i++)
			create(ti1, "segment("+i+")", p.segment(i)); //$NON-NLS-1$  //$NON-NLS-2$
		create(ti, "toOSString()", p.toOSString()); //$NON-NLS-1$
		create(ti, "toPortableString()", p.toPortableString()); //$NON-NLS-1$
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, IProject prj) {
		TreeItem ti = createObj(ti0, text, prj == null ? NULL : prj.getName(), prj);
		if (prj == null || !check(ti, prj)) return ti;
		create(ti, "exists()", prj.exists()); //$NON-NLS-1$
		try {
			create(ti, "getDefaultCharset()", prj.getDefaultCharset()); //$NON-NLS-1$
			prj.getDescription();
		} catch (CoreException e) {}
		update(ti, "getFullPath()", prj.getFullPath()); //$NON-NLS-1$
		create(ti, "getName()", prj.getName()); //$NON-NLS-1$
		update(ti, "getParent()", prj.getParent()); //$NON-NLS-1$
		try {
			IProject[] ps = prj.getReferencedProjects();
			TreeItem ti1 = create(ti, "getReferencedProjects()", ps == null ? 0 : ps.length); //$NON-NLS-1$
			if (ps != null)
				for (int i=0; i<ps.length; i++) update(ti1, BL+i+BR, ps[i]);
		} catch (CoreException e) {}
		prj.getResourceAttributes();
		create(ti, "getType()", prj.getType()); //$NON-NLS-1$
		update(ti, "getWorkspace()", prj.getWorkspace()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IProjectNatureDescriptor nd) {
		TreeItem ti = createObj(ti0, text, nd == null ? NULL : nd.getLabel(), nd);
		if (nd == null || !check(ti, nd)) return ti;
		create(ti, "getLabel()", nd.getLabel()); //$NON-NLS-1$
		create(ti, "getNatureId()", nd.getNatureId()); //$NON-NLS-1$
		expand(ti, "getNatureSetIds()", nd.getNatureSetIds()); //$NON-NLS-1$
		expand(ti, "getRequiredNatureIds()", nd.getRequiredNatureIds()); //$NON-NLS-1$
		create(ti, "isLinkingAllowed()", nd.isLinkingAllowed()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IResource c) {
		TreeItem ti = createObj(ti0, text, c == null ? NULL : c.getName(), c);
		if (c == null || !check(ti, c)) return ti;
		if (getDepth(ti) > NESTING_CFG) return ti;

		if (c instanceof IContainer)
			try {
				create(ti, "getDefaultCharset()", ((IContainer)c).getDefaultCharset()); //$NON-NLS-1$
			} catch (CoreException e) {}
		create(ti, "getFileExtension()", c.getFileExtension()); //$NON-NLS-1$
		update(ti, "getFullPath()", c.getFullPath()); //$NON-NLS-1$
// TODO:
//		c.getLocalTimeStamp());
		update(ti, "getLocation()", c.getLocation()); //$NON-NLS-1$
		update(ti, "getLocationURI()", c.getLocationURI()); //$NON-NLS-1$
//		c.getModificationStamp());
		create(ti, "getName()", c.getName()); //$NON-NLS-1$
		update(ti, "getParent()", c.getParent()); //$NON-NLS-1$
		update(ti, "getProject()", c.getProject()); //$NON-NLS-1$
		if (c instanceof IWorkspaceRoot)
			expand(ti, "getProjects()", ((IWorkspaceRoot)c).getProjects()); //$NON-NLS-1$
		update(ti, "getProjectRelativePath()", c.getProjectRelativePath()); //$NON-NLS-1$
		update(ti, "getRawLocation()", c.getRawLocation()); //$NON-NLS-1$
		update(ti, "getRawLocationURI()", c.getRawLocationURI()); //$NON-NLS-1$
		update(ti, "getResourceAttributes()", c.getResourceAttributes()); //$NON-NLS-1$
		create(ti, "getType()", c.getType()); //$NON-NLS-1$
		createObj(ti, "getWorkspace()", EMPTY_STR, c.getWorkspace()); //$NON-NLS-1$
		create(ti, "isAccessible()", c.isAccessible()); //$NON-NLS-1$
		create(ti, "isDerived()", c.isDerived()); //$NON-NLS-1$
		create(ti, "isLinked()", c.isLinked()); //$NON-NLS-1$
	//	create(ti, "isLocal(ZERO)", c.isLocal(0)); //$NON-NLS-1$
	//	create(ti, "isLocal(INIFINITE)", c.isLocal(2)); //$NON-NLS-1$
		create(ti, "isPhantom()", c.isPhantom()); //$NON-NLS-1$
	//	create(ti, "isReadOnly()", c.isReadOnly()); //$NON-NLS-1$
		create(ti, "isSynchronized(ZERO)", c.isSynchronized(0)); //$NON-NLS-1$
		create(ti, "isSynchronized(INFINITE)", c.isSynchronized(2)); //$NON-NLS-1$
		create(ti, "isTeamPrivateMember()", c.isTeamPrivateMember()); //$NON-NLS-1$
		if (c instanceof IContainer)
			try {
				expand(ti, "members()", ((IContainer)c).members()); //$NON-NLS-1$
			} catch (CoreException e) {}
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IWorkspace w) {
		TreeItem ti = createObj(ti0, text, EMPTY_STR, w);
		if (!check(ti, w)) return ti;
		update(ti, "getDescription()", w.getDescription()); //$NON-NLS-1$
		expand(ti, "getNatureDescriptors()", w.getNatureDescriptors()); //$NON-NLS-1$
		createObj(ti, "getPathVariableManager()", EMPTY_STR, w.getPathVariableManager()); //$NON-NLS-1$
		update(ti, "getRoot()", w.getRoot()); //$NON-NLS-1$
		createObj(ti, "getSynchronizer()", EMPTY_STR, w.getSynchronizer()); //$NON-NLS-1$
		create(ti, "isAutoBuilding()", w.isAutoBuilding()); //$NON-NLS-1$
		create(ti, "isTreeLocked()", w.isTreeLocked()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IWorkspaceDescription w) {
		TreeItem ti = createObj(ti0, text, EMPTY_STR, w);
		if (!check(ti, w)) return ti;
		expand(ti, "getBuildOrder()", w.getBuildOrder()); //$NON-NLS-1$
		create(ti, "getFileStateLongevity()", w.getFileStateLongevity()); //$NON-NLS-1$
		create(ti, "getMaxBuildIterations()", w.getMaxBuildIterations()); //$NON-NLS-1$
		create(ti, "getMaxFileStates()", w.getMaxFileStates()); //$NON-NLS-1$
		create(ti, "getMaxFileStateSize()", w.getMaxFileStateSize()); //$NON-NLS-1$
		create(ti, "getSnapshotInterval()", w.getSnapshotInterval()); //$NON-NLS-1$
		create(ti, "isAutoBuilding()", w.isAutoBuilding()); //$NON-NLS-1$
		return ti;
	}

	/*
	 * Default method to display unknown classes
	 */
	private TreeItem update(TreeItem ti0, String text, Object ob) {
		TreeItem ti = createObj(ti0, BL+text+BR, "???", ob); //$NON-NLS-1$
		check(ti, ob);
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, Map<String,String> m) {
		String s = (m == null) ? NULL : String.valueOf(m.size());
		TreeItem ti = createObj(ti0, text, s, m);
		if (m == null || !check(ti, m)) return ti;
		Iterator<String> it = m.keySet().iterator();
		while (it.hasNext()) {
			s = it.next();
			create(ti, s + " =", m.get(s)); //$NON-NLS-1$
		}
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ResourceAttributes ra) {
		TreeItem ti = createObj(ti0, text, EMPTY_STR, ra);
		if (!check(ti, ra)) return ti;
		create(ti, "isArchive()", ra.isArchive()); //$NON-NLS-1$
		create(ti, "isExecutable()", ra.isExecutable()); //$NON-NLS-1$
		create(ti, "isHidden()", ra.isHidden()); //$NON-NLS-1$
		create(ti, "isReadOnly()", ra.isReadOnly()); //$NON-NLS-1$
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, URI uri) {
		TreeItem ti = createObj(ti0, text, uri == null ? NULL : uri.toString(), uri);
		if (uri == null || !check(ti, uri)) return ti;
		create(ti, "getAuthority()", uri.getAuthority()); //$NON-NLS-1$
		create(ti, "getFragment()", uri.getFragment()); //$NON-NLS-1$
		create(ti, "getHost()", uri.getHost()); //$NON-NLS-1$
		create(ti, "getPath()", uri.getPath()); //$NON-NLS-1$
		create(ti, "getPort()", uri.getPort()); //$NON-NLS-1$
		create(ti, "getQuery()", uri.getQuery()); //$NON-NLS-1$
		create(ti, "isAbsolute()", uri.isAbsolute()); //$NON-NLS-1$
		create(ti, "isOpaque()", uri.isOpaque()); //$NON-NLS-1$
		create(ti, "toASCIIString()", uri.toASCIIString()); //$NON-NLS-1$
		return ti;
	}

	@Override
	public void updateData(ICResourceDescription rcfg) {
		cfg = rcfg;
		tree.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					tree.removeAll();
					TreeItem ti = new TreeItem(tree, 0);
					ti.setText(0, Messages.StructureTreeTab_11);
					tree.update();
					tree.setRedraw(false);
					tree.removeAll();
					switch (combo.getSelectionIndex()) {
					case 0:
						update(cfg.getConfiguration().getProjectDescription());
						break;
					case 1:
						update(null, "ICConfigurationDescription", cfg.getConfiguration()); //$NON-NLS-1$
						break;
					case 2:
						update(null, "ICResourceDescription", cfg); //$NON-NLS-1$
						break;
					}
				} finally {
					tree.setRedraw(true);
				}
			}
		});
	}

	// This page can be displayed if it's permitted in prefs
	@Override
	public boolean canBeVisible() {
		return CDTPrefUtil.getBool(CDTPrefUtil.KEY_DTREE);
	}

	@Override
	protected void updateButtons() {} // Do nothing. No buttons to update.
}
