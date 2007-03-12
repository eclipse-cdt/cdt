/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * This tab is intended to browse 
 * contents of whole class such as ResourceDescription, 
 * ConfigurationDescription or ProjectDescription
 * 
 * Notes:
 * 
 * 1. All strings in the file remain unlocalized
 *    since they represent method names.
 * 2. It is experimental functionality. Work is in progress.  
 * 3. Tree depth is limited by 12. Deeper branches are truncated.
 *    In future, more flexible mechanism will be applied instead.
 * 4. Tree creation process is long enough now. To be optimized.   
 */
public class StructureTreeTab  extends AbstractCPropertyTab {

	private static final Class[] NOCLASS = new Class[0];
	private static final Image IMG = CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_ERROR);
	private static final String LB = "["; 
	private static final int NESTING_MAX = 12;
	private static final int NESTING_CFG = 2;
	private static final String RB = "]";
	
	protected Button r_cfg;
	protected Button r_prj;
	protected Button r_res;
	protected Tree tree;
	
	private boolean check(TreeItem ti, Object obj) {
		if (obj == null || ti == null) return false;
		int cnt = NESTING_MAX;
		TreeItem tiSaved = ti;
		while (--cnt > 0) {
			ti = ti.getParentItem();
			if (ti == null) return true;
		}
		tiSaved.setText(2, "Maximal tree nesting");
		tiSaved.setImage(IMG);
		return false;
	}
	
	private int getDepth(TreeItem ti) {
		int x = 0;
		while (ti != null) {
			ti = ti.getParentItem();
			x++;
		}
		return x;
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
		t.setText(2, "Long");
		return t;
	}

	private TreeItem create(TreeItem ti0, String text, String val) {
		TreeItem ti =  ti0 == null ? new TreeItem(tree, 0) : new TreeItem(ti0, 0);
		ti.setText(0, text == null ? "<NULL>" : text);
		ti.setText(1, val  == null ? "<NULL>" : val );
		ti.setText(2, "String");
		return ti;
	}
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		
		r_prj = new Button(usercomp, SWT.RADIO);
		r_prj.setLayoutData(new GridData(GridData.BEGINNING));
		r_prj.setText(NewUIMessages.getResourceString("ConfigDescriptionTab.0")); //$NON-NLS-1$
		r_prj.setSelection(true);		
		r_prj.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateData(getResDesc());
			}});

		Button b1 = new Button(usercomp, SWT.PUSH);
		b1.setLayoutData(new GridData(GridData.END));
		b1.setText("Expand all");
		b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				expandAll(tree.getItem(0), true);
			}});
		
		r_cfg = new Button(usercomp, SWT.RADIO);
		r_cfg.setLayoutData(new GridData(GridData.BEGINNING));
		r_cfg.setText(NewUIMessages.getResourceString("ConfigDescriptionTab.1")); //$NON-NLS-1$
		r_cfg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateData(getResDesc());
			}});

		Button b2 = new Button(usercomp, SWT.PUSH);
		b2.setLayoutData(new GridData(GridData.END));
		b2.setText("Expand level...");
		b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}});		

		r_res = new Button(usercomp, SWT.RADIO);
		r_res.setLayoutData(new GridData(GridData.BEGINNING));
		r_res.setText(NewUIMessages.getResourceString("ConfigDescriptionTab.2")); //$NON-NLS-1$
		r_res.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateData(getResDesc());
			}});

		Button b3 = new Button(usercomp, SWT.PUSH);
		b3.setLayoutData(new GridData(GridData.END));
		b3.setText("Collapse all");
		b3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				expandAll(tree.getItem(0), false);
			}});		

		tree = new Tree(usercomp, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		tree.setLayoutData(gd);
		
		TreeColumn tc = new TreeColumn(tree, 0);
		tc.setText("Property");
		tc.setWidth(200);
		tc = new TreeColumn(tree, 0);
		tc.setText("Value");
		tc.setWidth(100);
		tc = new TreeColumn(tree, 0);
		tc.setText("Class");
		tc.setWidth(100);
		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		tree.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
			}});
	}
	
	private TreeItem createObj(TreeItem ti0, String text, Object obj) {
		String s = EMPTY_STR;
		if (obj != null) try {
			Class c = obj.getClass();
			Method m = c.getMethod("getName", NOCLASS);
			s = (String)m.invoke(obj, null);
		} catch (NoSuchMethodException e) {}
		  catch(IllegalAccessException e) {}
		  catch(IllegalArgumentException e) {}
		  catch(InvocationTargetException e) {}
		  catch(ClassCastException e) {}

		TreeItem t = create(ti0, text, s);
		if (obj != null) t.setText(2, obj.getClass().getName());
		return t;
	}

	private void expand(TreeItem ti0, String text, CLanguageData[] lds) {
		TreeItem ti = create(ti0, text, lds == null ? 0 : lds.length);
		if (!check(ti, lds)) return;
		for (int i=0; i<lds.length; i++) update(ti, LB+i+RB, lds[i]);
	}
		
	private void expand(TreeItem ti0, String text, CResourceData[] rd) {
		TreeItem ti = create(ti0, text, rd.length);
		if (!check(ti, rd)) return;
		for (int i=0; i<rd.length; i++) update(ti, LB+i+RB, rd[i]);
	}

	private void expand(TreeItem ti0, String text, ICExclusionPatternPathEntry[] ses) {
		TreeItem ti = create(ti0, text, ses.length);
		if (!check(ti, ses)) return;
		for (int i=0; i<ses.length; i++) update(ti, LB+i+RB, ses[i]); 
	}
	
	private void expand(TreeItem ti0, String text, ICExternalSetting[] ess) {
		TreeItem ti = create(ti0, text, ess == null ? 0 : ess.length);
		if (!check(ti, ess)) return;
		for (int i=0; i<ess.length; i++) update(ti, LB+i+RB, ess[i]);
	}
	
	private void expand(TreeItem ti0, String text, ICFileDescription[] fds) {
		TreeItem ti = create(ti0, text, fds == null ? 0 : fds.length);
		if (!check(ti, fds)) return;
		for (int i=0; i<fds.length; i++) update(ti, LB+i+RB, fds[i]);
	}
	
	private void expand(TreeItem ti0, String text, ICFolderDescription[] fds) {
		TreeItem ti = create(ti0, text, fds == null ? 0 : fds.length);
		if (!check(ti, fds)) return;
		for (int i=0; i<fds.length; i++) update(ti, LB+i+RB, fds[i]);
	}

	private void expand(TreeItem ti0, String text, ICLanguageSettingEntry[] ents) {
		TreeItem ti = create(ti0, text, ents == null ? 0 : ents.length);
		if (!check(ti, ents)) return;
		for (int i=0; i<ents.length; i++) update(ti, LB+i+RB, ents[i]);
	}

	private void expand (TreeItem ti0, String text, ICResourceDescription[] rdes) {
		TreeItem ti = create(ti0, text, rdes.length);
		if (!check(ti, rdes)) return;
		for (int i=0; i<rdes.length; i++) update(ti, LB+i+RB, rdes[i]);
	}
	
	private void expand(TreeItem ti0, String text, ICSettingObject[] obs) {
		TreeItem ti = create(ti0, text, obs == null ? 0 : obs.length);
		if (!check(ti, obs)) return;
		for (int i=0; i<obs.length; i++) update(ti, LB+i+RB, obs[i]);
	}

	private void expand(TreeItem ti0, String text, IPath[] ps) {
		TreeItem ti = create(ti0, text, ps.length);
		if (!check(ti, ps)) return;
		for (int i=0; i<ps.length; i++) update(ti, LB+i+RB, ps[i]);
	}
	private void expand(TreeItem ti0, String text, IProjectNatureDescriptor[] nds) {
		TreeItem ti = create(ti0, text, nds.length);
		if (!check(ti, nds)) return;
		for (int i=0; i<nds.length; i++) update(ti, LB+i+RB, nds[i]);
	}

	private void expand(TreeItem ti0, String text, IResource[] rss) {
		TreeItem ti = create(ti0, text, rss == null ? 0 : rss.length);
		if (!check(ti, rss)) return;
		for (int i=0; i<rss.length; i++) update(ti, LB+i+RB, rss[i]);
	}
	private void expand(TreeItem ti0, String text, String[] ss) {
		TreeItem ti = create(ti0, text, ss == null ? 0 : ss.length);
		if (!check(ti, ss)) return;
		for (int j=0; j<ss.length; j++) create(ti, LB+j+RB, ss[j]);
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
	public void performApply(ICResourceDescription src,ICResourceDescription dst) {}
	protected void performDefaults() {}

	private void update(ICProjectDescription prj) {
		TreeItem ti = new TreeItem(tree, 0);
		if (!check(ti, prj)) return;
		ti.setText(0, "ICProjectDescription");
		update(ti, "getActiveConfiguration()", prj.getActiveConfiguration());
		expand(ti, "getChildSettings()", prj.getChildSettings());
		expand(ti, "getConfigurations()", prj.getConfigurations());
		create(ti,"getId()",prj.getId());
		create(ti,"getName()",prj.getName());
		update(ti, "getParent()", prj.getParent());
		update(ti, "getProject()", prj.getProject());
		create(ti,"getType()",prj.getType());
		create(ti,"isModified()",prj.isModified());
		create(ti,"isReadOnly()",prj.isReadOnly());
		create(ti,"isValid()",prj.isValid());
	}

	private TreeItem update(TreeItem ti0, String text, CBuildData bd) {
		TreeItem ti = createObj(ti0, text, bd);
		if (!check(ti, bd)) return ti;
		// ALMOST THE SAME AS ICBuildSetting
		update(ti, "getBuilderCWD()", bd.getBuilderCWD());
		createObj(ti, "getBuildEnvironmentContributor()", bd.getBuildEnvironmentContributor());
		expand(ti, "getErrorParserIDs()", bd.getErrorParserIDs());
		create(ti, "getId()", bd.getId());
		create(ti, "getName()", bd.getName());
		expand(ti, "getOutputDirectories()", bd.getOutputDirectories());
		create(ti, "getType()", bd.getType());
		create(ti, "isValid()",bd.isValid());
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, CConfigurationData cd) {
		TreeItem ti = createObj(ti0, text, cd);
		if (!check(ti, cd)) return ti;
		update(ti, "getBuildData()", cd.getBuildData());
		createObj(ti, "getBuildVariablesContributor()", cd.getBuildVariablesContributor());
		create(ti, "getDescription()", cd.getDescription());
		create(ti, "getId()", cd.getId());
		create(ti, "getName()", cd.getName());
		expand(ti, "getResourceDatas()", cd.getResourceDatas());
		update(ti, "getRootFolderData()", cd.getRootFolderData());
		expand(ti, "getSourcePaths()", cd.getSourcePaths());
		update(ti, "getTargetPlatformData()", cd.getTargetPlatformData());
		create(ti,"getType()",cd.getType());
		create(ti,"isValid()",cd.isValid());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, CLanguageData ls) {
		TreeItem ti = createObj(ti0, text, ls);
		if (!check(ti, ls)) return ti;
		create(ti, "getId()", ls.getId());
		create(ti, "getLanguageId()", ls.getLanguageId());
		create(ti, "getName()", ls.getName());
		expand(ti, "getSourceContentTypeIds()", ls.getSourceContentTypeIds());
		expand(ti, "getSourceExtensions()", ls.getSourceExtensions());
		create(ti,"getType()",ls.getType());
		int k = ls.getSupportedEntryKinds(); 
		TreeItem ti1 = create(ti, "getSupportedEntryKinds()", k);
		int[] kind = flagsToArray(k);
		for (int j=0; j<kind.length; j++) {
			TreeItem ti2 = create(ti1, "Kind", kind[j]);
			expand(ti2, "getEntries",ls.getEntries(kind[j]));
		}
		create(ti,"isValid()",ls.isValid());
		return ti;
	}
	
	private TreeItem update(TreeItem ti0, String text, CResourceData bd) {
		TreeItem ti = createObj(ti0, text, bd);
		if (!check(ti, bd)) return ti;
		create(ti, "getId()", bd.getId());
		if (bd instanceof CFolderData) 
		    expand(ti, "getLanguageDatas()", ((CFolderData)bd).getLanguageDatas());
		create(ti, "getName()", bd.getName());
		update(ti,"getPath()",bd.getPath());
		create(ti,"getType()",bd.getType());
		create(ti,"isExcluded()",bd.isExcluded());
		create(ti,"isValid()",bd.isValid());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, CTargetPlatformData bd) {
		TreeItem ti = createObj(ti0, text, bd);
		if (!check(ti, bd)) return ti;
		expand(ti, "getBinaryParserIds()", bd.getBinaryParserIds());
		create(ti, "getId()", bd.getId());
		create(ti, "getName()", bd.getName());
		create(ti, "getType()",bd.getType());
		create(ti, "isValid()",bd.isValid());
		return ti;
	}
		
	private TreeItem update(TreeItem ti0, String text, ICBuildSetting obj) {
		TreeItem ti = createObj(ti0, text, obj);
		if (!check(ti, obj)) return ti;
		// ALMOST THE SAME AS CBuildData
		update(ti, "getBuilderCWD()", obj.getBuilderCWD());
		createObj(ti, "getBuildEnvironmentContributor()", obj.getBuildEnvironmentContributor());
		createObj(ti, "getConfiguration()", obj.getConfiguration());
		expand(ti, "getErrorParserIDs()", obj.getErrorParserIDs());
		create(ti, "getId()", obj.getId());
		create(ti, "getName()", obj.getName());
		expand(ti, "getOutputDirectories()", obj.getOutputDirectories());
		update(ti, "getParent()", obj.getParent());
		create(ti, "getType()", obj.getType());
		create(ti, "isReadOnly()", obj.isReadOnly());
		create(ti, "isValid()",obj.isValid());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICConfigurationDescription cfg) {
		TreeItem ti = createObj(ti0, text, cfg);
		if (!check(ti, cfg)) return ti;
		if (getDepth(ti) > NESTING_CFG) return ti;
		
		update(ti, "getBuildSetting()", cfg.getBuildSetting());
		create(ti, "getBuildSystemId()", cfg.getBuildSystemId());
		createObj(ti, "getBuildVariablesContributor()", cfg.getBuildVariablesContributor());
		expand(ti, "getChildSettings()", cfg.getChildSettings());
		update(ti, "getConfigurationData()", cfg.getConfigurationData());
		create(ti, "getDescription()", cfg.getDescription());
		expand(ti, "getExternalSettings()", cfg.getExternalSettings());
		expand(ti, "getFileDescriptions()", cfg.getFileDescriptions());
		expand(ti, "getFolderDescriptions()", cfg.getFolderDescriptions());
		create(ti, "getId()", cfg.getId());
		create(ti, "getName()", cfg.getName());
		update(ti, "getParent()", cfg.getParent());
// TODO:		
		cfg.getReferenceInfo();		
		expand(ti, "getResourceDescriptions()", cfg.getResourceDescriptions());
		update(ti, "getRootFolderDescription()", cfg.getRootFolderDescription());
		expand(ti, "getSourceEntries()", cfg.getSourceEntries());
		update(ti, "getTargetPlatformSetting()", cfg.getTargetPlatformSetting());
		create(ti,"getType()",cfg.getType());
		create(ti,"isActive()",cfg.isActive());
		create(ti,"isModified()",cfg.isModified());
		create(ti,"isPreferenceConfiguration()",cfg.isPreferenceConfiguration());
		create(ti,"isReadOnly()",cfg.isReadOnly());
		create(ti,"isValid()",cfg.isValid());
	return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICExclusionPatternPathEntry s) {
		TreeItem ti = createObj(ti0, text, s);
		if (!check(ti, s)) return ti;
		char[][] chrs = s.fullExclusionPatternChars();
		TreeItem ti1 = create(ti, "fullExclusionPatternChars()", chrs.length);
		for (int j=0; j<chrs.length; j++) 
			create(ti1, LB+j+RB, new String(chrs[j]));
		expand(ti, "getExclusionPatterns()", s.getExclusionPatterns());
		create(ti,"getFlags()", s.getFlags());
		update(ti, "getFullPath()", s.getFullPath());
		create(ti,"getKind()", s.getKind());
		update(ti, "getLocation()", s.getLocation());
		create(ti, "getName()", s.getName());
		create(ti, "getValue()", s.getValue());
		create(ti, "isBuiltIn()", s.isBuiltIn());
		create(ti, "isReadOnly()", s.isReadOnly());
		create(ti, "isResolved()", s.isResolved());
		create(ti, "isValueWorkspacePath()", s.isValueWorkspacePath());
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICExternalSetting es) {
		TreeItem ti = createObj(ti0, text, es);
		if (!check(ti, es)) return ti;
		expand(ti, "getCompatibleContentTypeIds()", es.getCompatibleContentTypeIds());
		expand(ti, "getCompatibleExtensions()", es.getCompatibleExtensions());
		expand(ti, "getCompatibleLanguageIds()", es.getCompatibleLanguageIds());
		expand(ti, "getEntries()", es.getEntries());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICFileDescription fd) {
		TreeItem ti = createObj(ti0, text, fd);
		if (!check(ti, fd)) return ti;
		expand(ti, "getChildSettings()", fd.getChildSettings());
		create(ti, "getId()", fd.getId());
		create(ti, "getName()", fd.getName());
		update(ti, "getParent()", fd.getParent());
		update(ti, "getParentFolderDescription()", fd.getParentFolderDescription());
		update(ti, "getPath()", fd.getPath());
		create(ti,"getType()",fd.getType());
		create(ti,"isExcluded()",fd.isExcluded());
		create(ti,"isReadOnly()",fd.isReadOnly());
		create(ti,"isValid()",fd.isValid());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICFolderDescription fd) {
		TreeItem ti = createObj(ti0, text, fd);
		if (!check(ti, fd)) return ti;
		expand(ti, "getChildSettings()", fd.getChildSettings());
		expand(ti, "getLanguageSettings()", fd.getLanguageSettings());
		create(ti, "getId()", fd.getId());
		create(ti, "getName()", fd.getName());
		expand(ti, "getNestedResourceDescriptions()", fd.getNestedResourceDescriptions());
		update(ti, "getParent()", fd.getParent());
		update(ti, "getParentFolderDescription()", fd.getParentFolderDescription());
		update(ti, "getPath()", fd.getPath());
		create(ti,"getType()",fd.getType());
		create(ti,"isExcluded()",fd.isExcluded());
		create(ti,"isReadOnly()",fd.isReadOnly());
		create(ti,"isRoot()",fd.isRoot());
		create(ti,"isValid()",fd.isValid());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICLanguageSetting ls) {
		TreeItem ti = new TreeItem(tree, 0);
		if (!check(ti, ls)) return ti;
		update(ti, "getConfiguration()", ls.getConfiguration());
		create(ti, "getId()", ls.getId());
		create(ti, "getLanguageId()", ls.getLanguageId());
		create(ti, "getName()", ls.getName());
		update(ti, "getParent()", ls.getParent());
		expand(ti, "getSourceContentTypeIds()", ls.getSourceContentTypeIds());
		expand(ti, "getSourceExtensions()", ls.getSourceExtensions());
		create(ti,"getType()",ls.getType());
		int k = ls.getSupportedEntryKinds(); 
		TreeItem ti1 = create(ti, "getSupportedEntryKinds()", k);
		int[] kind = flagsToArray(k);
		for (int j=0; j<kind.length; j++) {
			TreeItem ti2 = create(ti1, "Kind", kind[j]);
			expand(ti2, "getResolvedSettingEntries",ls.getResolvedSettingEntries(kind[j]));
			expand(ti2, "getSettingEntries", ls.getSettingEntries(kind[j]));
		}
		create(ti,"isReadOnly()",ls.isReadOnly());
		create(ti,"isValid()",ls.isValid());
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICLanguageSettingEntry ent) {
		TreeItem ti = createObj(ti0, text, ent);
		if (!check(ti, ent)) return ti;
		create(ti, "getFlags()", ent.getFlags());
		create(ti, "getKind()", ent.getKind());
		create(ti, "getName()", ent.getName());
		create(ti, "getValue()", ent.getValue());
		create(ti, "isBuiltIn()", ent.isBuiltIn());
		create(ti, "isReadOnly()", ent.isReadOnly());
		create(ti, "isResolved()", ent.isResolved());
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, ICResourceDescription rcfg) {
		TreeItem ti = createObj(ti0, text, rcfg);
		if (!check(ti, rcfg)) return ti;
		expand(ti, "getChildSettings()", rcfg.getChildSettings());
		update(ti, "getConfiguration()", rcfg.getConfiguration());
		create(ti, "getId()", rcfg.getId());
		create(ti, "getName()", rcfg.getName());
		update(ti, "getParent()", rcfg.getParent());
		update(ti, "getParentFolderDescription()", rcfg.getParentFolderDescription());
		update(ti, "getPath()", rcfg.getPath());
		create(ti, "getType()", rcfg.getType());
		create(ti,"isExcluded()", rcfg.isExcluded());
		create(ti,"isReadOnly()", rcfg.isReadOnly());
		create(ti,"isValid()",rcfg.isValid());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICSettingContainer c) {
		TreeItem ti = createObj(ti0, text, c);
		if (!check(ti, c)) return ti;
		if (getDepth(ti) > NESTING_CFG) return ti;
		expand(ti, "getChildSettings()", c.getChildSettings());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ICSettingObject obj) {
		TreeItem ti = createObj(ti0, text, obj);
		if (!check(ti, obj)) return ti;
		if (obj instanceof ICTargetPlatformSetting) {
			expand(ti, "getBinaryParserIds()", ((ICTargetPlatformSetting)obj).getBinaryParserIds());
//			update(ti, "getParent()", obj.getParent());
//		} else { // do not expand configuration descr to avoid dead loop
		}
		createObj(ti, "getParent()", obj.getParent());
		update(ti, "getConfiguration()", obj.getConfiguration());
		create(ti, "getId()", obj.getId());
		create(ti, "getName()", obj.getName());
		create(ti, "getType()", obj.getType());
		create(ti,"isReadOnly()", obj.isReadOnly());
		create(ti,"isValid()",obj.isValid());
		return ti;
	}	
	private TreeItem update(TreeItem ti0, String text, ICTargetPlatformSetting obj) {
		TreeItem ti = createObj(ti0, text, obj);
		if (!check(ti, obj)) return ti;
		update(ti, "getConfiguration()", obj.getConfiguration());
		create(ti, "getId()", obj.getId());
		create(ti, "getName()", obj.getName());
		update(ti, "getParent()", obj.getParent());
		create(ti, "getType()", obj.getType());
		create(ti,"isReadOnly()", obj.isReadOnly());
		create(ti,"isValid()",obj.isValid());
		return ti;
	}	
	
	private TreeItem update(TreeItem ti0, String text, IPath p) {
		TreeItem ti = createObj(ti0, text, p);
		if (!check(ti, p)) return ti;
		create(ti, "getDevice()", p.getDevice()); 
		create(ti, "getFileExtension()", p.getFileExtension());
		create(ti, "hasTrailingSeparator()", p.hasTrailingSeparator());
		create(ti, "isAbsolute()", p.isAbsolute());
		create(ti, "isEmpty()", p.isEmpty());
		create(ti, "isRoot()", p.isRoot());
		create(ti, "isUNC()", p.isUNC());
		TreeItem ti1 = create(ti, "segmentCount()", p.segmentCount());
		for (int i=0; i<p.segmentCount(); i++) 
			create(ti1, "segment("+i+")", p.segment(i)); 
		create(ti, "toOSString()", p.toOSString());
		create(ti, "toPortableString()", p.toPortableString());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IProject prj) {
		TreeItem ti = createObj(ti0, text, prj);
		if (!check(ti, prj)) return ti;
		create(ti, "exists()", prj.exists());
		try {
			create(ti, "getDefaultCharset()", prj.getDefaultCharset());
			prj.getDescription();
		} catch (CoreException e) {}
		update(ti, "getFullPath()", prj.getFullPath());
		create(ti, "getName()", prj.getName());
		update(ti, "getParent()", prj.getParent());
		try {
			IProject[] ps = prj.getReferencedProjects();
			TreeItem ti1 = create(ti, "getReferencedProjects()", ps == null ? 0 : ps.length);
			if (ps != null)
				for (int i=0; i<ps.length; i++) update(ti1, LB+i+RB, ps[i]);
		} catch (CoreException e) {}			
		prj.getResourceAttributes();
		create(ti, "getType()", prj.getType());
		update(ti, "getWorkspace()", prj.getWorkspace());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IProjectNatureDescriptor nd) {
		TreeItem ti = createObj(ti0, text, nd);
		if (!check(ti, nd)) return ti;
		create(ti, "getLabel()", nd.getLabel());
		create(ti, "getNatureId()", nd.getNatureId());
		expand(ti, "getNatureSetIds()", nd.getNatureSetIds());
		expand(ti, "getRequiredNatureIds()", nd.getRequiredNatureIds());
		create(ti, "isLinkingAllowed()", nd.isLinkingAllowed());
		return ti;
	}
	private TreeItem update(TreeItem ti0, String text, IResource c) {
		TreeItem ti = createObj(ti0, text, c);
		if (!check(ti, c)) return ti;
		if (c instanceof IContainer)
			try {
				create(ti, "getDefaultCharset()", ((IContainer)c).getDefaultCharset());
			} catch (CoreException e) {}
		create(ti, "getFileExtension()", c.getFileExtension());
		update(ti, "getFullPath()", c.getFullPath());
// TODO:		
//		c.getLocalTimeStamp());
		update(ti, "getLocation()", c.getLocation());
		update(ti, "getLocationURI()", c.getLocationURI());
//		c.getModificationStamp());
		create(ti, "getName()", c.getName());
		update(ti, "getParent()", c.getParent());
		update(ti, "getProject()", c.getProject());
		if (c instanceof IWorkspaceRoot)
			expand(ti, "getProjects()", ((IWorkspaceRoot)c).getProjects());
		update(ti, "getProjectRelativePath()", c.getProjectRelativePath());
		update(ti, "getRawLocation()", c.getRawLocation());
		update(ti, "getRawLocationURI()", c.getRawLocationURI());
		update(ti, "getResourceAttributes()", c.getResourceAttributes());
		create(ti, "getType()", c.getType());
		createObj(ti, "getWorkspace()", c.getWorkspace());
		create(ti, "isAccessible()", c.isAccessible());
		create(ti, "isDerived()", c.isDerived());
		create(ti, "isLinked()", c.isLinked());
		create(ti, "isLocal(ZERO)", c.isLocal(0));
		create(ti, "isLocal(INIFINITE)", c.isLocal(2));
		create(ti, "isPhantom()", c.isPhantom());
		create(ti, "isReadOnly()", c.isReadOnly());
		create(ti, "isSynchronized(ZERO)", c.isSynchronized(0));
		create(ti, "isSynchronized(INFINITE)", c.isSynchronized(2));
		create(ti, "isTeamPrivateMember()", c.isTeamPrivateMember());
		if (c instanceof IContainer)
			try {
				expand(ti, "members()", ((IContainer)c).members());
			} catch (CoreException e) {}
		return ti;
	}
	
	private TreeItem update(TreeItem ti0, String text, IWorkspace w) {
		TreeItem ti = createObj(ti0, text, w);
		if (!check(ti, w)) return ti;
		// TODO:
		w.getDanglingReferences();
		update(ti, "getDescription()", w.getDescription());
		expand(ti, "getNatureDescriptors()", w.getNatureDescriptors());
		createObj(ti, "getPathVariableManager()", w.getPathVariableManager());
		update(ti, "getRoot()", w.getRoot());
		createObj(ti, "getSynchronizer()", w.getSynchronizer());
		create(ti, "isAutoBuilding()", w.isAutoBuilding());
		create(ti, "isTreeLocked()", w.isTreeLocked());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, IWorkspaceDescription w) {
		TreeItem ti = createObj(ti0, text, w);
		if (!check(ti, w)) return ti;
		expand(ti, "getBuildOrder()", w.getBuildOrder());
		create(ti, "getFileStateLongevity()", w.getFileStateLongevity());
		create(ti, "getMaxBuildIterations()", w.getMaxBuildIterations());
		create(ti, "getMaxFileStates()", w.getMaxFileStates());
		create(ti, "getMaxFileStateSize()", w.getMaxFileStateSize());
		create(ti, "getSnapshotInterval()", w.getSnapshotInterval());
		create(ti, "isAutoBuilding()", w.isAutoBuilding());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, ResourceAttributes ra) {
		TreeItem ti = createObj(ti0, text, ra);
		if (!check(ti, ra)) return ti;
		create(ti, "isArchive()", ra.isArchive());
		create(ti, "isExecutable()", ra.isExecutable());
		create(ti, "isHidden()", ra.isHidden());
		create(ti, "isReadOnly()", ra.isReadOnly());
		return ti;
	}

	private TreeItem update(TreeItem ti0, String text, URI uri) {
		TreeItem ti = createObj(ti0, text, uri);
		if (!check(ti, uri)) return ti;
		create(ti, "getAuthority()", uri.getAuthority());
		create(ti, "getFragment()", uri.getFragment());
		create(ti, "getHost()", uri.getHost());
		create(ti, "getPath()", uri.getPath());
		create(ti, "getPort()", uri.getPort());
		create(ti, "getQuery()", uri.getQuery());
		create(ti, "getRawAuthority()", uri.getRawAuthority());
		create(ti, "getRawFragment()", uri.getRawFragment());
		create(ti, "getRawHost()", uri.getRawHost());
		create(ti, "getRawPath()", uri.getRawPath());
		create(ti, "getRawQuery()", uri.getRawQuery());
		create(ti, "getRawSchemeSpecificPart()", uri.getRawSchemeSpecificPart());
		create(ti, "getRawUserInfo()", uri.getRawUserInfo());
		create(ti, "isAbsolute()", uri.isAbsolute());
		create(ti, "isOpaque()", uri.isOpaque());
		create(ti, "toASCIIString()", uri.toASCIIString());
		create(ti, "uri.toIRIString()", uri.toIRIString());
		return ti;
	}

	private void expandAll(TreeItem ti, boolean b) {
		ti.setExpanded(b);
		TreeItem[] tis = ti.getItems();
		if (tis == null) return;
		for (int i=0; i<tis.length; i++) 
			expandAll(tis[i], b);
	}
	
	public void updateData(ICResourceDescription rcfg) {
		tree.removeAll();
		if (r_prj.getSelection()) update(rcfg.getConfiguration().getProjectDescription());
		if (r_cfg.getSelection()) update(null, "ICConfigurationDescription", rcfg.getConfiguration());
		if (r_res.getSelection()) update(null, "ICResourceDescription", rcfg);
	}
}
