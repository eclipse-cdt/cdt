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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.cdt.internal.ui.CPluginImages;

public abstract class AbstractExportTab extends AbstractCPropertyTab {
	protected Table table;
	protected TableViewer tv;
	protected ICConfigurationDescription cfg;
	protected static final LanguageManager lm = LanguageManager.getInstance();
	protected static final IContentTypeManager ctm = Platform.getContentTypeManager();
	
//	protected boolean showBI = false;
//	boolean  savedShowBI  = false;
//	List incs;
	
	private static final String ALL = "[All]"; //$NON-NLS-1$
	private static final String LIST = "[List]"; //$NON-NLS-1$
	private static final Image IMG_FS = CPluginImages.get(CPluginImages.IMG_FILESYSTEM); 
	private static final Image IMG_WS = CPluginImages.get(CPluginImages.IMG_WORKSPACE); 
	private static final Image IMG_MK = CPluginImages.get(CPluginImages.IMG_OBJS_MACRO);
	private static Map names_l = new HashMap();
	private static Map names_t = new HashMap();
	private static String[] names_ls; 	
	private static String[] names_ts;
	private List namesList;

	static {
		ILanguage[] ls = lm.getRegisteredLanguages();
		names_ls = new String[ls.length];
		for (int i=0; i<ls.length; i++) {
			names_l.put(ls[i].getName(), ls[i].getId());
			names_ls[i] = ls[i].getName();
		}
		String[] ids = lm.getRegisteredContentTypeIds();
		names_ts = new String[ids.length];
		for (int i=0; i<ids.length; i++) {
			IContentType ct = ctm.getContentType(ids[i]);
			names_t.put(ct.getName(), ct.getId());
			names_ts[i] = ct.getName();
		}
		
	}
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
	    table = new Table(usercomp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
	    GridData gd = new GridData(GridData.FILL_BOTH);
	    gd.widthHint = 255;
	    table.setLayoutData(gd);
  	    table.setHeaderVisible(true);
  	    table.setLinesVisible(true);

  	    tv = new TableViewer(table);

		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
  	    
  	    tv.setLabelProvider(new RichLabelProvider());
  	    
	    table.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		setButtons();
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		if (buttonIsEnabled(1) && table.getSelectionIndex() != -1)
    				buttonPressed(1);
	    	}
	    });
 	    TableColumn c = new TableColumn(table, SWT.NONE);
	    c.setWidth(hasValues() ? 100 : 200);
	    c.setText(NewUIMessages.getResourceString("EnvDialog.0")); //$NON-NLS-1$
 	    c = new TableColumn(table, SWT.NONE);
	    c.setWidth(hasValues() ? 100 : 0);
	    c.setText(NewUIMessages.getResourceString("EnvDialog.1")); //$NON-NLS-1$
 	    c = new TableColumn(table, SWT.NONE);
	    c.setWidth(100);
	    c.setText(NewUIMessages.getResourceString("LanguagesTab.1")); //$NON-NLS-1$
 	    c = new TableColumn(table, SWT.NONE);
	    c.setWidth(100);
	    c.setText(NewUIMessages.getResourceString("LanguagesTab.0")); //$NON-NLS-1$
	    
	    initButtons(new String[] {ADD_STR, EDIT_STR, DEL_STR});
	    updateData(getResDesc());
	}
	
	
    /**
     * Updates state of add/edit/delete buttons
     * Called when table selection changes.
     */
    protected void setButtons() {
    	int i = table.getSelectionIndex();
		boolean x = i != -1;
		boolean y = x;
		if (x) {
			ICLanguageSettingEntry ent = ((ExtData)(table.getItem(i).getData())).entry;
			if (ent.isReadOnly()) x = false;
			if (ent.isBuiltIn() || ent.isReadOnly()) y = false;
		}
    	buttonSetEnabled(1, x);
    	buttonSetEnabled(2, y);
    }
	
	/*
	 * Methods to be implemented in descendants 
	 */
	public abstract int getKind();
	public abstract ICLanguageSettingEntry doAdd(String s1, String s2);
	public abstract ICLanguageSettingEntry doEdit(String s1, String s2);
	public abstract boolean hasValues();
	
	/**
	 * Called when item added/edited/removed.
	 * Refreshes whole table contwnts
	 */
	public void update() {
		int x = table.getSelectionIndex();
		if (x == -1) x = 0;
		
		namesList = new ArrayList();
		ArrayList lst = new ArrayList();
		ICExternalSetting[] vals = cfg.getExternalSettings();
		if (vals == null || vals.length == 0) {
			tv.setInput(null);
			return;
		}
		for (int i=0; i<vals.length; i++) {
			ICLanguageSettingEntry[] ents = vals[i].getEntries(getKind());
			if (ents == null || ents.length == 0) continue;
			for (int j=0; j<ents.length; j++) {
				lst.add(new ExtData(vals[i], ents[j]));
				namesList.add(ents[j].getName());
			}
		}
		Collections.sort(lst, CDTListComparator.getInstance());
		tv.setInput(lst.toArray(new Object[lst.size()]));
		if (table.getItemCount() > x) table.select(x);
		else if (table.getItemCount() > 0) table.select(0);
		setButtons();
	}

	/**
	 * Called when configuration changed
	 * Refreshes languages list and calls table refresh.
	 */
	public void updateData(ICResourceDescription rcfg) {
		if (rcfg == null) return;
		cfg = rcfg.getConfiguration();
		update();
	}
	/**
	 * Unified "Add/Edit/Delete" buttons handler
	 */
	public void buttonPressed(int i) {
		ICLanguageSettingEntry[] ent = new ICLanguageSettingEntry[1];
		ExtData old;
		ExpDialog dlg;
		int n = table.getSelectionIndex();

		switch (i) {
		case 0: // add
			dlg = new ExpDialog(usercomp.getShell(), true,
					"Create", EMPTY_STR, EMPTY_STR, cfg,         //$NON-NLS-1$
					null, null, getKind(), names_ls, names_ts, namesList); 
			if (dlg.open()) {
				ent[0] = doAdd(dlg.text1.trim(), dlg.text2.trim());
				if (ent[0] != null)
					if (dlg.check1) { // apply to all ?
						ICConfigurationDescription[] cfgs = page.getCfgsEditable();
						for (int k=0; k<cfgs.length; k++) 
							cfgs[k].createExternalSetting(name2id(dlg.sel_langs, names_l), 
									name2id(dlg.sel_types, names_t), null, ent);
					} else {
						cfg.createExternalSetting(name2id(dlg.sel_langs, names_l), 
							name2id(dlg.sel_types, names_t), null, ent);
					}
				update();
			}
			break;
		case 1: // edit
			if (n == -1) return;
			old = (ExtData)(table.getItem(n).getData());
			if (old.entry.isReadOnly()) return;
			String s1, s2;
			if (getKind() == ICSettingEntry.MACRO) {
				s1 = old.getName();
				s2 = old.getValue();
			} else 
				s1 = s2 = old.getName();
			
			dlg = new ExpDialog(usercomp.getShell(), false,
					"Edit", s1, s2, cfg, //$NON-NLS-1$
					id2name(old.setting.getCompatibleLanguageIds(), names_l),
					id2name(old.setting.getCompatibleContentTypeIds(), names_t),
					getKind(), names_ls, names_ts, null); 
			if (dlg.open()) {
				ent[0] = doEdit(dlg.text1.trim(), dlg.text2.trim());
				ICLanguageSettingEntry[] ls = old.setting.getEntries(getKind());
				ICLanguageSettingEntry[] ls2 = new ICLanguageSettingEntry[ls.length];
				for (int x=0; x<ls.length; x++) 
					if (ls[x].equals(old.entry)) ls2[x] = ent[0];
					else ls2[x] = ls[x];
				cfg.removeExternalSetting(old.setting);
				cfg.createExternalSetting(name2id(dlg.sel_langs, names_l), name2id(dlg.sel_types, names_t), null, ls2);
				update();
				
			}
			break;
		case 2: // delete
			if (n == -1) return;
			TableItem[] its = table.getSelection();
			boolean checked[] = new boolean[its.length];
			for (int t=0; t<its.length; t++) {
				if (checked[t] || its[t] == null) continue;
				old = (ExtData)(its[t].getData());
				if (old.entry.isReadOnly() || old.entry.isBuiltIn()) continue;
				ICLanguageSettingEntry[] ls = old.setting.getEntries(getKind());
				ArrayList lst = new ArrayList();
outer:				
				for (int x=0; x<ls.length; x++) { 
					for (int y=t; y<its.length; y++) {
						if (its[y] == null) break;
						Object ob = its[y].getData();
						if (ob != null && (ob instanceof ExtData)) {  
							ExtData ex = (ExtData)ob;
							if (ls[x].equals(ex.entry)) {
								checked[y] = true;
								continue outer;
							}
						}
					}
					lst.add(ls[x]);
				}
				cfg.removeExternalSetting(old.setting);
				cfg.createExternalSetting(old.setting.getCompatibleLanguageIds(), 
						old.setting.getCompatibleContentTypeIds(),
						old.setting.getCompatibleExtensions(), 
						(ICLanguageSettingEntry[])lst.toArray(new ICLanguageSettingEntry[lst.size()])); 
			}
			update();
			break;

		default:
			break;
		}
	}

	public static String[] name2id(String[] ein, Map names) {
		if (ein != null)
			for (int k=0; k<ein.length; k++) ein[k] = (String)names.get(ein[k]);
		return ein;
	}

	public static String[] id2name(String[] ein, Map names) {
		if (ein != null)
			for (int i=0; i<ein.length; i++) {
				Iterator it = names.keySet().iterator();
				while (it.hasNext()) {
					String s = (String) it.next();
					if (ein[i].equals(names.get(s))) {
						ein[i] = s;
						break;
					}
				}
			}
		return ein;
	}
	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		ICConfigurationDescription c1 = src.getConfiguration();
		ICConfigurationDescription c2 = dst.getConfiguration();
		c2.removeExternalSettings();
		ICExternalSetting[] v = c1.getExternalSettings();
		for (int i=0; i<v.length; i++)
			cfg.createExternalSetting(v[i].getCompatibleLanguageIds(), 
				v[i].getCompatibleContentTypeIds(),
				v[i].getCompatibleExtensions(), v[i].getEntries()); 
	}

	protected void performDefaults() {
		cfg.removeExternalSettings();
		updateData(this.getResDesc());
	}
	
	// Extended label provider
	private class RichLabelProvider extends LabelProvider implements IFontProvider, ITableLabelProvider /*, IColorProvider*/{
		public RichLabelProvider(){}
		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex > 0) return null;
			ExtData data = (ExtData)element;
			if (data.entry.getKind() == ICSettingEntry.MACRO)
				return IMG_MK;				
			if ((data.entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0)
				return IMG_WS;
			else 
				return IMG_FS;
		}
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
		public String getColumnText(Object element, int columnIndex) {
			ExtData data = (ExtData)element;
			switch (columnIndex) {
			case 0:
				return data.getName();
			case 1:
				return data.getValue();
			case 2:
				return data.getLangStr();
			case 3:
				return data.getTypeStr();
			default:
				return EMPTY_STR;
			}
		}
		
		public Font getFont(Object element) {
			ExtData data = (ExtData)element;
			if (data.entry.isBuiltIn()) return null;    // built in
			if (data.entry.isReadOnly())                // read only
				return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
			else	                            // normal
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		}
	}

	public boolean canBeVisible() {
		if (! page.isForProject() ) return false;
		
		return true;
		/*
		if (getResDesc() == null) return true;
		ICLanguageSetting [] ls = getLangSetting(getResDesc());
		for (int i=0; i<ls.length; i++) {
			if ((ls[i].getSupportedEntryKinds() & getKind()) != 0)
				return true;
		}
		return false;
		*/
	}

	/**
	 *
	 */
	static class ExtData {
		ICExternalSetting setting;
		ICLanguageSettingEntry entry;
		
		ExtData(ICExternalSetting _s, ICLanguageSettingEntry _e) {
			setting = _s;
			entry = _e;
		}
		protected String getName() { 
			return entry.getName(); 
		}
		protected String getValue() {
			if (entry.getKind() == ICSettingEntry.MACRO) 
				return entry.getValue();
			else
				return EMPTY_STR;
		} 
		protected String getLangStr() { 
			return getLabel(setting.getCompatibleLanguageIds(), names_l);
		}
		protected String getTypeStr() {
			return getLabel(setting.getCompatibleContentTypeIds(), names_t);
		}
	}
	
	static protected String getLabel(String[] lst, Map names) {
		if (lst == null || lst.length == 0) return ALL;
		if (lst.length > 1) return LIST;
		Iterator it = names.keySet().iterator();
		while (it.hasNext()) {
			String s = (String)it.next();
			if (names.get(s).equals(lst[0]))
				return s;
		}
		return lst[0];
	}
	static protected String getList(String[] lst) {
		String s = EMPTY_STR;
		for (int i=0; i<lst.length; i++) s = s + lst[i] + '\n';
		return s;
	}
}
