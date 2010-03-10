/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMultiFolderDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.ICMultiResourceDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.MultiLanguageSetting;

import org.eclipse.cdt.internal.ui.CPluginImages;

public abstract class AbstractLangsListTab extends AbstractCPropertyTab {
	protected Table table;
	protected TableViewer tv;
	protected Tree langTree;
	protected TreeColumn langCol;
	protected Button showBIButton;
	protected boolean toAllCfgs = false;
	protected boolean toAllLang = false;
	protected Label  lb1, lb2;
	protected TableColumn columnToFit = null; 
	
	protected ICLanguageSetting lang;
	protected LinkedList<ICLanguageSettingEntry> shownEntries;
	protected ArrayList<ICSettingEntry> exported; 
	protected SashForm sashForm;
	protected ICLanguageSetting [] ls; // all languages known
	
	protected final static String[] BUTTONS = {ADD_STR, EDIT_STR, DEL_STR, 
			UIMessages.getString("AbstractLangsListTab.2"), //$NON-NLS-1$
    		null, MOVEUP_STR, MOVEDOWN_STR };
	protected final static String[] BUTTSYM = {ADD_STR, EDIT_STR, DEL_STR, 
		UIMessages.getString("AbstractLangsListTab.2")}; //$NON-NLS-1$

	private static final Comparator<Object> comp = CDTListComparator.getInstance();

	private final static Image IMG_FS = CPluginImages.get(CPluginImages.IMG_FILESYSTEM); 
	private final static Image IMG_WS = CPluginImages.get(CPluginImages.IMG_WORKSPACE); 
	private final static Image IMG_MK = CPluginImages.get(CPluginImages.IMG_OBJS_MACRO);
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 30 };
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, true));
		
		// Create the sash form
		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		sashForm.setLayoutData(gd);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		addTree(sashForm).setLayoutData(new GridData(GridData.FILL_VERTICAL));
	    table = new Table(sashForm, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
	    gd = new GridData(GridData.FILL_BOTH);
	    gd.widthHint = 150;
	    table.setLayoutData(gd);
  	    table.setHeaderVisible(isHeaderVisible());
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
	    	@Override
			public void widgetSelected(SelectionEvent e) {
	    		updateButtons();
	    	}
	    	@Override
			public void widgetDefaultSelected(SelectionEvent e) {
	    		if (buttonIsEnabled(1) && table.getSelectionIndex() != -1)
    				buttonPressed(1);
	    	}
	    });
	    
	    table.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				setColumnToFit();
			}
			public void controlResized(ControlEvent e) {
				setColumnToFit();
			}});
	    
	    setupLabel(usercomp, EMPTY_STR, 1, 0);
	    
	    lb1 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
	    lb1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    lb1.setToolTipText(UIMessages.getString("EnvironmentTab.15")); //$NON-NLS-1$
	    lb1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CDTPrefUtil.spinDMode();
				update();
			}});

	    showBIButton = setupCheck(usercomp, UIMessages.getString("AbstractLangsListTab.0"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
	    showBIButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
			public void widgetSelected(SelectionEvent e) {
	    		update(); 
	    	}
	    });
	    
	    lb2 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
	    lb2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    lb2.setToolTipText(UIMessages.getString("EnvironmentTab.23")); //$NON-NLS-1$
	    lb2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CDTPrefUtil.spinWMode();
				updateLbs(null, lb2);
			}});

	    additionalTableSet();
	    initButtons((getKind() == ICSettingEntry.MACRO) ? BUTTSYM : BUTTONS); 
	    updateData(getResDesc());
	}
	
    /**
     * Updates state for all buttons
     * Called when table selection changes.
     */
    @Override
	protected void updateButtons() {
    	int index = table.getSelectionIndex();
    	int[] ids = table.getSelectionIndices(); 
    	boolean canAdd = langTree.getItemCount() > 0;
		boolean canExport = index != -1;
		boolean canEdit = canExport && ids.length == 1;
		boolean canDelete = canExport;
		ICLanguageSettingEntry ent = null;
		if (canExport) {
			ent = (ICLanguageSettingEntry)(table.getItem(index).getData());
			if (ent.isReadOnly()) canEdit = false;
			if (ent.isReadOnly()) canDelete = false;
			if (exported.contains(ent))
				buttonSetText(3, UIMessages.getString("AbstractLangsListTab.4")); //$NON-NLS-1$
			else
				buttonSetText(3, UIMessages.getString("AbstractLangsListTab.2")); //$NON-NLS-1$
		} else {
			buttonSetText(3, UIMessages.getString("AbstractLangsListTab.2")); //$NON-NLS-1$
		}
		boolean canMoveUp = false;
		boolean canMoveDown = false;
		if (ent != null) {
			canMoveUp = canEdit && index > 0 && !ent.isBuiltIn();
			canMoveDown = canEdit && (index < table.getItemCount() - 1) && !ent.isBuiltIn();
		}
    	if (canMoveDown && showBIButton.getSelection()) {
    		ent = (ICLanguageSettingEntry)(table.getItem(index+1).getData());
    		if (ent.isBuiltIn()) canMoveDown = false; // cannot exchange with built in
    	}
    	buttonSetEnabled(0, canAdd); // add
    	buttonSetEnabled(1, canEdit); // edit
    	buttonSetEnabled(2, canDelete); // delete
    	buttonSetEnabled(3, canExport && !page.isMultiCfg()); // export
    	// there is a separator instead of button #4
    	buttonSetEnabled(5, canMoveUp && !page.isMultiCfg()); // up
    	buttonSetEnabled(6, canMoveDown && !page.isMultiCfg()); // down
    }
	
	private Tree addTree(Composite comp) {
		langTree = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		langTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		langTree.setHeaderVisible(true);
		
		langTree.addSelectionListener(new SelectionAdapter() {
			@Override
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
		langCol.setText(UIMessages.getString("AbstractLangsListTab.1")); //$NON-NLS-1$
		langCol.setWidth(200);
		langCol.setResizable(false);
		langCol.setToolTipText(UIMessages.getString("AbstractLangsListTab.1")); //$NON-NLS-1$
//		langTree.getAccessible().addAccessibleListener(
//			   new AccessibleAdapter() {                       
//	               @Override
//				public void getName(AccessibleEvent e) {
//	                       e.result = UIMessages.getString("AbstractLangsListTab.1"); //$NON-NLS-1$
//	               }
//			   }
//		);
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
			
			shownEntries = getIncs(); 
			tv.setInput(shownEntries.toArray(new Object[shownEntries.size()]));
			if (table.getItemCount() > x) table.setSelection(x);
			else if (table.getItemCount() > 0) table.setSelection(0);
		}		
		
		updateLbs(lb1, lb2);
		updateButtons();
	}

	protected LinkedList<ICLanguageSettingEntry> getIncs() {
		LinkedList<ICLanguageSettingEntry> l = new LinkedList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> lst = getSettingEntriesList(getKind());
		if (lst != null) {
			for (ICLanguageSettingEntry ent : lst) {
				if (!ent.isBuiltIn()) 
					l.add(ent);
			}
			if (showBIButton.getSelection()) {
				for (ICLanguageSettingEntry ent : lst)
					if (ent.isBuiltIn()) 
						l.add(ent);
			}
		}
		return l;
	}
	
	
	/**
	 * Called when configuration changed
	 * Refreshes languages list and calls table refresh.
	 */
	@Override
	public void updateData(ICResourceDescription cfg) {
		if (cfg == null || !canBeVisible()) return;
		updateExport();
		langTree.removeAll();
		TreeItem firstItem = null;
		ls = getLangSetting(cfg);
		if (ls != null) {
			Arrays.sort(ls, CDTListComparator.getInstance());
			for (int i=0; i<ls.length; i++) {
				if ((ls[i].getSupportedEntryKinds() & getKind()) != 0) {
					TreeItem t = new TreeItem(langTree, SWT.NONE);
					String s = ls[i].getLanguageId();
					if (s != null && !s.equals(EMPTY_STR)) {
						// Bug #178033: get language name via LangManager.
						ILanguageDescriptor ld = LanguageManager.getInstance().getLanguageDescriptor(s);
						if (ld == null)
							s = null;
						else
							s = ld.getName();
					}
					if (s == null || s.equals(EMPTY_STR))
						s = ls[i].getName();
					t.setText(0, s);
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
		exported = new ArrayList<ICSettingEntry>();
		ICExternalSetting[] vals = getResDesc().getConfiguration().getExternalSettings();
		if (!(vals == null || vals.length == 0)) {
			for (ICExternalSetting v : vals) {
				ICSettingEntry[] ents = v.getEntries(getKind());
				if (ents == null || ents.length == 0) continue;
				for (ICSettingEntry en : ents)
					exported.add(en);
			}
		}
	}
	
	private void performAdd(ICLanguageSettingEntry ent) {
		if (ent != null) {
			if ((toAllCfgs || toAllLang) && ! (getResDesc() instanceof ICMultiResourceDescription)) {
				addToAll(ent);
			} else {
				if (isWModifyMode() && (lang instanceof MultiLanguageSetting)) {
					performMulti(ent, null);
				} else {
					changeIt(ent, null);
				}
			}
			update();
		}
	}

	private void changeIt(ICLanguageSettingEntry add, ICLanguageSettingEntry[] del) {
		List<ICLanguageSettingEntry> ls = getSettingEntriesList(getKind());
		if (del != null) {
			for (ICLanguageSettingEntry d : del) {
				for (ICLanguageSettingEntry e : ls) {
					if (d.getName().equals(e.getName())) {
						ls.remove(e);
						break;
					}
				}
			}
		}
		if (add != null)
			ls.add(add);
		setSettingEntries(getKind(), ls, toAllLang);
	}
	
	private void performMulti(ICLanguageSettingEntry ent, ICLanguageSettingEntry del) {
		MultiLanguageSetting ms = (MultiLanguageSetting)lang;
		ICLanguageSetting[] ls = (ICLanguageSetting[])ms.getItems();
		ICLanguageSettingEntry[][] es = ms.getSettingEntriesM(getKind());
		for (int i=0; i<ls.length; i++) {
			List<ICLanguageSettingEntry> entries = 
				new ArrayList<ICLanguageSettingEntry>(Arrays.asList(es[i]));
			if (del != null) {
				for (ICLanguageSettingEntry e : entries) { 
					if (e.getName().equals(del.getName())) {
						entries.remove(e);
						break;
					}
				}
			}
			if (ent != null)
				entries.add(ent);
			ls[i].setSettingEntries(getKind(), entries);
		}
	}
	
	private void performEdit(int n) {
		if (n == -1) return;
		ICLanguageSettingEntry old = (ICLanguageSettingEntry)(table.getItem(n).getData());
		if (old.isReadOnly()) return;
		ICLanguageSettingEntry ent = doEdit(old);
		toAllLang = false;
		if (ent != null) {
			if (isWModifyMode() && (lang instanceof MultiLanguageSetting)) {
				performMulti(ent, old);
			} else {
				ICLanguageSettingEntry[] del = null;  
				if (! ent.getName().equals(old.getName()))
					del = new ICLanguageSettingEntry[] {old};
				changeIt(ent, del);
			}
			update();
		}
	}
	
	private void performDelete(int n) {
		if (n == -1) return;
		int[] ids = table.getSelectionIndices();
		if (isWModifyMode() && (lang instanceof MultiLanguageSetting)) {
			for (int x=ids.length-1; x>=0; x--) {
				ICLanguageSettingEntry old = (ICLanguageSettingEntry)(table.getItem(ids[x]).getData());
					performMulti(null, old);
			}
		} else {
			ICLanguageSettingEntry[] del = new ICLanguageSettingEntry[ids.length];
			for (int x=ids.length-1; x>=0; x--) {
				ICLanguageSettingEntry old = (ICLanguageSettingEntry)(table.getItem(ids[x]).getData());
//				if (old.isReadOnly()) continue;
				del[x] = old;
			}
			changeIt(null, del);
		}
		update();
		
	}
	/**
	 * Unified buttons handler
	 */
	@Override
	public void buttonPressed(int i) {
		ICLanguageSettingEntry old;
		int n = table.getSelectionIndex();
		int ids[] = table.getSelectionIndices();
		
		switch (i) {
		case 0: // add
			toAllCfgs = false;
			toAllLang = false;
			performAdd(doAdd());
			break;
		case 1: // edit
			performEdit(n);
			break;
		case 2: // delete
			performDelete(n);
			break;
		case 3: // toggle export	
			if (n == -1) return;
			for (int x=ids.length-1; x>=0; x--) {
				old = (ICLanguageSettingEntry)(table.getItem(ids[x]).getData());
				if (exported.contains(old)) {
					deleteExportSetting(old);
				} else {
					page.getResDesc().getConfiguration().createExternalSetting(new String[] {lang.getId()}, null, null, new ICLanguageSettingEntry[] {old});
				}
			}
			updateExport();
			update();
			break;
		// there is a separator instead of button #4
		case 5: // move up	
		case 6: // move down
			old = (ICLanguageSettingEntry)(table.getItem(n).getData());
			int x = shownEntries.indexOf(old);
			if (x < 0) break;
			if (i == 6) x++; // "down" simply means "up underlying item"
			old = shownEntries.get(x);
			ICLanguageSettingEntry old2 = shownEntries.get(x - 1);
			shownEntries.remove(x);
			shownEntries.remove(x - 1);
			shownEntries.add(x - 1, old);
			shownEntries.add(x, old2);
			
			setSettingEntries(getKind(), shownEntries, false);
			update(i == 5 ? -1 : 1);			
			break;			
		default:
			break;
		}
		table.setFocus();
	}

	private void deleteExportSetting(ICSettingEntry ent) {
//		if (ent.isReadOnly() || ent.isBuiltIn()) continue;
		ICConfigurationDescription cfg = getResDesc().getConfiguration();
		ICExternalSetting[] vals = cfg.getExternalSettings();
		if (!(vals == null || vals.length == 0)) {
			for (int i=0; i<vals.length; i++) {
				ICSettingEntry[] ents = vals[i].getEntries(getKind());
				if (ents == null || ents.length == 0) continue;
				for (int j=0; j<ents.length; j++) {
					if (ents[j].equalsByName(ent)) {
						ICSettingEntry[] arr = new ICSettingEntry[ents.length - 1];
						int index = 0;
						for (int k=0; k<ents.length; k++) 
							if (k != j) arr[index++] = ents[k];
						cfg.removeExternalSetting(vals[i]);
						cfg.createExternalSetting(vals[i].getCompatibleLanguageIds(), 
								vals[i].getCompatibleContentTypeIds(),
								vals[i].getCompatibleExtensions(),
								arr);
						return;
					}
				}
			}
		}
	}
	
	
	/**
	 * Adds entry to all configurations 
	 * @param ent - entry to add
	 */
	private void addToAll(ICLanguageSettingEntry ent) {
		ICConfigurationDescription[] cfgs = page.getCfgsEditable();
		ICResourceDescription cur_cfg = page.getResDesc();
		String id = lang.getName(); // getLanguageId() sometimes returns null.
		for (int i = 0; i < cfgs.length; i++) {
			ICResourceDescription rcfg = page.getResDesc(cfgs[i]); 
			if (rcfg == null) 
				continue;
			if (!toAllCfgs && !(cur_cfg.equals(rcfg)))
				continue;
			for (ICLanguageSetting l : getLangSetting(rcfg)) {
				if (id == l.getName() || toAllLang) {
					List<ICLanguageSettingEntry> lst = l.getSettingEntriesList(getKind());
					lst.add(ent);
					l.setSettingEntries(getKind(), lst);
				}
			}
		}
	}
	
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		if (page.isMultiCfg()) {
			ICLanguageSetting [] sr = ls;
			if (dst instanceof ICMultiItemsHolder) {
				for (Object ob : ((ICMultiItemsHolder)dst).getItems()) {
					if (ob instanceof ICResourceDescription) {
						ICLanguageSetting [] ds = getLangSetting((ICResourceDescription)ob);			
						if (ds == null || sr.length != ds.length) return;
						for (int i=0; i<sr.length; i++) {
							ICLanguageSettingEntry[] ents = null;
							ents = sr[i].getSettingEntries(getKind());
							ds[i].setSettingEntries(getKind(), ents);
						}
					}
				}
			}
		} else {
			ICLanguageSetting [] sr = getLangSetting(src);
			ICLanguageSetting [] ds = getLangSetting(dst);
			if (sr == null || ds == null || sr.length != ds.length) return;
			for (int i=0; i<sr.length; i++) {
				ICLanguageSettingEntry[] ents = null;
				ents = sr[i].getSettingEntries(getKind());
				ds[i].setSettingEntries(getKind(), ents);
			}
		}	
	}
	
	@Override
	protected void performDefaults() {
		TreeItem[] tis = langTree.getItems();
		for (int i=0; i<tis.length; i++) {
			Object ob = tis[i].getData();
			if (ob != null && ob instanceof ICLanguageSetting) {
				((ICLanguageSetting)ob).setSettingEntries(getKind(), (List<ICLanguageSettingEntry>)null);
			}
		}
		updateData(this.getResDesc());
	}
	
	// Extended label provider
	private class RichLabelProvider extends LabelProvider implements IFontProvider, ITableLabelProvider /*, IColorProvider*/{
		public RichLabelProvider(){}
		@Override
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
			return IMG_FS;
		}
		@Override
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
					s = s + UIMessages.getString("AbstractLangsListTab.3"); //$NON-NLS-1$
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
			// normal
			return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		}
	}

	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription)rcDes;
			if (foDes instanceof ICMultiFolderDescription) {
				return getLS((ICMultiFolderDescription)foDes);
			}  
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription)rcDes;
			ICLanguageSetting ls = fiDes.getLanguageSetting();
			return (ls != null) ? new ICLanguageSetting[] { ls } : null;
		}
		return null;
	}
	
	private ICLanguageSetting[] getLS(ICMultiFolderDescription foDes) {
		ICLanguageSetting[] lsets;
		
		ICLanguageSetting[][] ls = foDes.getLanguageSettingsM(comp);
		ICLanguageSetting[] fs = conv2LS(CDTPrefUtil.getListForDisplay(ls, comp));
		lsets = new ICLanguageSetting[fs.length];
		for (int i=0; i<fs.length; i++) {
			ArrayList<ICLanguageSetting> list = new ArrayList<ICLanguageSetting>(ls.length);
			for (int j=0; j<ls.length; j++) {
				int x = Arrays.binarySearch(ls[j], fs[i], comp);
				if (x >= 0)
					list.add(ls[j][x]);
			}
			if (list.size() == 1)
				lsets[i] = list.get(0);
			else if (list.size() > 1)
				lsets[i] = new MultiLanguageSetting(list, foDes.getConfiguration());
		}
		return lsets;
	}

	
	@Override
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
	
	private void setSettingEntries(int kind, List<ICLanguageSettingEntry> incs, boolean toAll) {
		if (page.isMultiCfg()) {
			((ICMultiResourceDescription)getResDesc()).setSettingEntries(lang, kind, incs, toAll);
		} else
			lang.setSettingEntries(kind, incs);
	}
	private List<ICLanguageSettingEntry> getSettingEntriesList(int kind) {
		if (page.isMultiCfg() && lang instanceof MultiLanguageSetting) {
			ICLanguageSettingEntry[][] lses = ((MultiLanguageSetting)lang).getSettingEntriesM(kind);
			Object[] res = CDTPrefUtil.getListForDisplay(lses, comp);
			ICLanguageSettingEntry[] out = new ICLanguageSettingEntry[res.length];
			System.arraycopy(res, 0, out, 0, res.length);
			return Arrays.asList(out);
		} 
		return lang.getSettingEntriesList(kind);
	}
	
	private ICLanguageSetting[] conv2LS(Object[] ob) {
		ICLanguageSetting[] se = new ICLanguageSetting[ob.length];
		System.arraycopy(ob, 0, se, 0, ob.length);
		return se;
	}

	protected boolean isHeaderVisible() {
		return true;
	}

	protected void setColumnToFit() {
		if (columnToFit != null)
			columnToFit.setWidth(table.getBounds().width - 4);
	}

}
