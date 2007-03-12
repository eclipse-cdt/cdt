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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.cdt.internal.ui.CPluginImages;

public abstract class AbstractLangsListTab extends AbstractCPropertyTab {
	protected Table table;
	protected TableViewer tv;
	protected Tree langTree;
	protected TreeColumn langCol;
	protected Button showBIButton;
	protected boolean toAll = false;
	
//	protected boolean showBI = false;
//	boolean  savedShowBI  = false;
	protected ICLanguageSetting lang;
	protected LinkedList incs;
	protected ArrayList exported; 
	protected SashForm sashForm;
	
	protected final static String[] BUTTONS = {ADD_STR, EDIT_STR, DEL_STR, 
			NewUIMessages.getResourceString("AbstractLangsListTab.2"), //$NON-NLS-1$
    		null, MOVEUP_STR, MOVEDOWN_STR };
	protected final static String[] BUTTSYM = {ADD_STR, EDIT_STR, DEL_STR, 
		NewUIMessages.getResourceString("AbstractLangsListTab.2")}; //$NON-NLS-1$

	private final static Image IMG_FS = CPluginImages.get(CPluginImages.IMG_FILESYSTEM); 
	private final static Image IMG_WS = CPluginImages.get(CPluginImages.IMG_WORKSPACE); 
	private final static Image IMG_MK = CPluginImages.get(CPluginImages.IMG_OBJS_MACRO);
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 30 };
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		
		// Create the sash form
		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		addTree(sashForm).setLayoutData(new GridData(GridData.FILL_VERTICAL));
	    table = new Table(sashForm, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
	    GridData gd = new GridData(GridData.FILL_BOTH);
	    gd.widthHint = 255;
	    table.setLayoutData(gd);
  	    table.setHeaderVisible(true);
  	    table.setLinesVisible(true);

  	    sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
		
		sashForm.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.DRAG) return;
				int shift = event.x - sashForm.getBounds().x;
				GridData data = (GridData) langTree.getLayoutData();
				if ((data.widthHint + shift) < 20) return;
				Point computedSize = usercomp.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point currentSize = usercomp.getShell().getSize();
				boolean customSize = !computedSize.equals(currentSize);
				data.widthHint = data.widthHint;
				sashForm.layout(true);
				computedSize = usercomp.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (customSize)
					computedSize.x = Math.max(computedSize.x, currentSize.x);
				computedSize.y = Math.max(computedSize.y, currentSize.y);
				if (computedSize.equals(currentSize)) {
					return;
				}
			}
		});

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
	    		updateButtons();
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		if (buttonIsEnabled(1) && table.getSelectionIndex() != -1)
    				buttonPressed(1);
	    	}
	    });
	    setupLabel(usercomp, EMPTY_STR, 1, 0);
	    showBIButton = setupCheck(usercomp, NewUIMessages.getResourceString("AbstractLangsListTab.0"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
	    showBIButton.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		update(); 
	    	}
	    });
	    additionalTableSet();
	    initButtons((getKind() == ICSettingEntry.MACRO) ? BUTTSYM : BUTTONS); 
	    updateData(getResDesc());
	}
	
    /**
     * Updates state for all buttons
     * Called when table selection changes.
     */
    public void updateButtons() {
    	int index = table.getSelectionIndex();
    	boolean canAdd = langTree.getItemCount() > 0;
		boolean canExport = index != -1;
		boolean canEdit = canExport;
		boolean canDelete = canEdit;
		if (canExport) {
			ICLanguageSettingEntry ent = (ICLanguageSettingEntry)(table.getItem(index).getData());
			if (ent.isReadOnly()) canEdit = false;
			if (ent.isBuiltIn() || ent.isReadOnly()) canDelete = false;
		}
    	boolean canMoveUp = canDelete && index > 0;
    	boolean canMoveDown = canDelete && (index < table.getItemCount() - 1); 
    	if (canMoveDown && showBIButton.getSelection()) {
    		ICLanguageSettingEntry ent = (ICLanguageSettingEntry)(table.getItem(index+1).getData());
    		if (ent.isBuiltIn()) canMoveDown = false; // cannot exchange with built in
    	}
    	buttonSetEnabled(0, canAdd); // add
    	buttonSetEnabled(1, canEdit); // edit
    	buttonSetEnabled(2, canDelete); // delete
    	buttonSetEnabled(3, canExport); // export
    	// there is a separator instead of button #4
    	buttonSetEnabled(5, canMoveUp); // up
    	buttonSetEnabled(6, canMoveDown); // down
    }
	
	private Tree addTree(Composite comp) {
		langTree = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		langTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		langTree.setHeaderVisible(true);
		
		langTree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = langTree.getSelection();
				if (items.length > 0) {
					ICLanguageSetting ls = (ICLanguageSetting)items[0].getData(); 
					if (ls != null) {
						lang = ls;
						update();
					}
				}
			}});
		langTree.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = langTree.getBounds().width - 5;
				if (langCol.getWidth() != x)
					langCol.setWidth(x);
			}});
		
		langCol = new TreeColumn(langTree, SWT.NONE);
		langCol.setText(NewUIMessages.getResourceString("AbstractLangsListTab.1")); //$NON-NLS-1$
		langCol.setResizable(false);
		return langTree;
	}

	/*
	 * Methods to be implemented in descendants 
	 */
	public abstract int getKind();
	public abstract ICLanguageSettingEntry doAdd();
	public abstract ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent);
    public void additionalTableSet() {} // may be not overwritten
	
	/**
	 * Called when language changed or item added/edited/removed.
	 * Refreshes whole table contwnts
	 * 
	 * Note, this method is rewritten in Symbols tab.
	 */
	public void update() { update(0); } 
   
	public void update(int shift) {
		if (lang != null) {
			int x = table.getSelectionIndex();
			if (x == -1) x = 0; 
			else x += shift; // used only for UP/DOWN 
			
			incs = new LinkedList();
			List lst = lang.getSettingEntriesList(getKind());
			if (lst != null) {
				Iterator it = lst.iterator();
				while (it.hasNext()) {
					ICLanguageSettingEntry ent = (ICLanguageSettingEntry)it.next();
					if (!ent.isBuiltIn()) incs.add(ent);
				}
				if (showBIButton.getSelection()) {
					ArrayList lstS = new ArrayList();
					it = lst.iterator();
					while (it.hasNext()) {
						ICLanguageSettingEntry ent = (ICLanguageSettingEntry)it.next();
						if (ent.isBuiltIn()) lstS.add(ent);
					}
					incs.addAll(lstS);
				}
			}
			tv.setInput(incs.toArray(new Object[incs.size()]));
			if (table.getItemCount() > x) table.select(x);
			else if (table.getItemCount() > 0) table.select(0);
		}		
		updateButtons();
	}

	/**
	 * Called when configuration changed
	 * Refreshes languages list and calls table refresh.
	 */
	public void updateData(ICResourceDescription cfg) {
		if (cfg == null) return;
		updateExport();
		langTree.removeAll();
		TreeItem firstItem = null;
		ICLanguageSetting []ls = getLangSetting(cfg);
		if (ls != null) {
			Arrays.sort(ls, CDTListComparator.getInstance());
			for (int i=0; i<ls.length; i++) {
				if ((ls[i].getSupportedEntryKinds() & getKind()) != 0) {
					TreeItem t = new TreeItem(langTree, SWT.NONE);
					t.setText(0, ls[i].getName());
					t.setData(ls[i]);
					if (firstItem == null) { 
						firstItem = t;
						lang = ls[i];
					}
				}
			}

			if (firstItem != null && table != null) {
				langTree.setSelection(firstItem);
			}
		}
		update();
	}
	
	private void updateExport() {
		exported = new ArrayList();
		ICExternalSetting[] vals = getResDesc().getConfiguration().getExternalSettings();
		if (!(vals == null || vals.length == 0)) {
			for (int i=0; i<vals.length; i++) {
				ICLanguageSettingEntry[] ents = vals[i].getEntries(getKind());
				if (ents == null || ents.length == 0) continue;
				for (int j=0; j<ents.length; j++)
					exported.add(ents[j]);
			}
		}
	}
	/**
	 * Unified buttons handler
	 */
	public void buttonPressed(int i) {
		ICLanguageSettingEntry ent;
		ICLanguageSettingEntry old;
		int n = table.getSelectionIndex();

		switch (i) {
		case 0: // add
			toAll = false;
			ent = doAdd();
			if (ent != null) {
				if (toAll) {
					addToAll(ent);
				} else {
					incs.add(ent);
					lang.setSettingEntries(getKind(), incs);
				}
				update();
			}
			break;
		case 1: // edit
			if (n == -1) return;
			old = (ICLanguageSettingEntry)(table.getItem(n).getData());
			if (old.isReadOnly()) return;
			ent = doEdit(old);
			if (ent != null) {
				int toModify = incs.indexOf(old);
				incs.remove(toModify);
				incs.add(toModify, ent);
				lang.setSettingEntries(getKind(), incs);
				update();
			}
			break;
		case 2: // delete
			if (n == -1) return;
			old = (ICLanguageSettingEntry)(table.getItem(n).getData());
			if (old.isReadOnly() || old.isBuiltIn()) return;
			incs.remove(old);
			lang.setSettingEntries(getKind(), incs);
			update();
			break;
		case 3: // export	
			if (n == -1) return;
			old = (ICLanguageSettingEntry)(table.getItem(n).getData());
			page.getResDesc().getConfiguration().createExternalSetting(new String[] {lang.getId()}, null, null, new ICLanguageSettingEntry[] {old});
			updateExport();
			update();
			break;
		// there is a separator instead of button #4
		case 5: // move up	
		case 6: // move down
			old = (ICLanguageSettingEntry)(table.getItem(n).getData());
			int x = incs.indexOf(old);
			if (x < 0) break;
			if (i == 6) x++; // "down" simply means "up underlying item"
			old = (ICLanguageSettingEntry)incs.get(x);
			ICLanguageSettingEntry old2 = (ICLanguageSettingEntry)incs.get(x - 1);
			incs.remove(x);
			incs.remove(x - 1);
			incs.add(x - 1, old);
			incs.add(x, old2);
			lang.setSettingEntries(getKind(), incs);
			update(i == 5 ? -1 : 1);			
			break;			
		default:
			break;
		}
	}

	/**
	 * Adds entry to all configurations 
	 * @param ent - entry to add
	 */
	private void addToAll(ICLanguageSettingEntry ent) {
		ICConfigurationDescription[] cfgs = page.getCfgsEditable();
		String id = lang.getName(); // getLanguageId() sometimes returns null.
		for (int i = 0; i < cfgs.length; i++) {
			ICResourceDescription rcfg = page.getResDesc(cfgs[i]); 
			if (rcfg == null) continue;
			ICLanguageSetting [] ls = getLangSetting(rcfg);
			for (int j = 0; j < ls.length; j++ ) {
				if (id == ls[j].getName()) {
					List lst = ls[j].getSettingEntriesList(getKind());
					lst.add(ent);
					ls[j].setSettingEntries(getKind(), lst);
				}
			}
		}
	}
	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		ICLanguageSetting [] sr = getLangSetting(src);
		ICLanguageSetting [] ds = getLangSetting(dst);
		if (sr == null || ds == null || sr.length != ds.length) return;
		for (int i=0; i<sr.length; i++) {
			ds[i].setSettingEntries(getKind(), sr[i].getSettingEntries(getKind()));
		}
	}
	protected void performDefaults() {
		lang.setSettingEntries(getKind(), new ArrayList());
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
			if (! (element instanceof ICLanguageSettingEntry)) return null;
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			if (le.getKind() == ICSettingEntry.MACRO)
				return IMG_MK;				
			if ((le.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0)
				return IMG_WS;
			else 
				return IMG_FS;
		}
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
		public String getColumnText(Object element, int columnIndex) {
			if (! (element instanceof ICLanguageSettingEntry)) {
				return (columnIndex == 0) ? element.toString() : EMPTY_STR;
			}
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			if (columnIndex == 0) {
				String s = le.getName();
				if (exported.contains(le))
					s = s + NewUIMessages.getResourceString("AbstractLangsListTab.3"); //$NON-NLS-1$
				return s;
			}
			if (le.getKind() == ICSettingEntry.MACRO) {
				switch (columnIndex) {
					case 1: return le.getValue();
				}
			} 
			return EMPTY_STR;
		}
		
		public Font getFont(Object element) {
			if (! (element instanceof ICLanguageSettingEntry)) return null;
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			if (le.isBuiltIn()) return null;    // built in
			if (le.isReadOnly())                // read only
				return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
			else	                            // normal
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		}
	}

	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription)rcDes;
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription)rcDes;
			ICLanguageSetting ls = fiDes.getLanguageSetting();
			return (ls != null) ? new ICLanguageSetting[] { ls } : null;
		}
		return null;
	}
	
	public boolean canBeVisible() {
		if (getResDesc() == null) return true;
		ICLanguageSetting [] ls = getLangSetting(getResDesc());
		if (ls == null) return false;
		for (int i=0; i<ls.length; i++) {
			if ((ls[i].getSupportedEntryKinds() & getKind()) != 0)
				return true;
		}
		return false;
	}
}
