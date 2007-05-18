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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.util.PixelConverter;

public class BinaryParsTab extends AbstractCPropertyTab {
	
	/* Settings from binary parser pages are NOT saved by prior CDT version.
	 * So existing binary parsers _always_ use default values.
	 * Moreover, obsolete interface is used while attempting to save. 
	 * 
	 * We have to affect both parsers and pages 
	 * to teach them to save data really.
	 * 
	 * It will be done in next versions. Currently pages are disabled.
	 */
	
	private static final int DEFAULT_HEIGHT = 160;
	private static final String ATTR_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_NAME_VISIBILITY = "visibility"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	private static final String ATTR_VALUE_PRIVATE = "private"; //$NON-NLS-1$

	protected Map configMap;
	protected Map fParserPageMap = null;
	protected Table table;
	protected CheckboxTableViewer tv;
	protected Composite parserGroup;
	
	private ICTargetPlatformSetting tps;
	
	protected class BinaryParserConfiguration {
		IExtension fExtension;
		public BinaryParserConfiguration(IExtension extension) { fExtension = extension; }
		public String getID() {	return fExtension.getUniqueIdentifier();}
		public String getName() { return fExtension.getLabel();	}
		public String toString() { return fExtension.getUniqueIdentifier();	}
		public boolean equals(Object obj) {
			if (obj instanceof BinaryParserConfiguration) {
				return this.getID().equals(((BinaryParserConfiguration) obj).getID());
			}
			return super.equals(obj);
		}
	}
	
	protected class BinaryParserPageConfiguration {
		ICOptionPage dynamicPage;
		IConfigurationElement fElement;
		public BinaryParserPageConfiguration(IConfigurationElement element) {
			fElement = element;
		}
		public ICOptionPage getPage() throws CoreException {
			if (dynamicPage == null) {
				dynamicPage = (ICOptionPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
			}
			return dynamicPage;
		}
	}
	
	protected String getParserId() {
		return CCorePlugin.BINARY_PARSER_SIMPLE_ID;
	}

	public void createControls(Composite parent) {
		super.createControls(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(usercomp, ICHelpContextIds.BINARY_PARSER_PAGE);

		usercomp.setLayout(new GridLayout(2, false));
		setupLabel(usercomp, UIMessages.getString("BinaryParsTab.0"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		table = new Table(usercomp, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBinaryParserChanged();
				updateButtons();
		}});
		tv = new CheckboxTableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tv.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				String txt = (element != null) ? element.toString() : EMPTY_STR;
				if (element instanceof BinaryParserConfiguration)
					txt = ((BinaryParserConfiguration)element).getName();
				return txt;
			}
		});

		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveChecked();
			}});
		
		// get "standard" buttons on my own place
		Composite c = new Composite(usercomp, SWT.NONE);
		c.setLayoutData(new GridData(GridData.END));
		initButtons(c, new String[] {MOVEUP_STR, MOVEDOWN_STR});

		parserGroup = new Composite(usercomp, SWT.NULL);
		GridData gd = new GridData();
		parserGroup.setLayout(new TabFolderLayout());
		
		PixelConverter converter = new PixelConverter(parent);
		gd.heightHint = converter.convertHorizontalDLUsToPixels(DEFAULT_HEIGHT);

		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		parserGroup.setLayoutData(gd);
		initializeParserList();
		initializeParserPageMap();
		handleBinaryParserChanged();
	}

    public void setVisible(boolean _visible) {
    	super.setVisible(_visible);
    	page.enableConfigSelection(!_visible);
    }

	public void updateData(ICResourceDescription cfgd) {
		tps = cfgd.getConfiguration().getTargetPlatformSetting(); 
		String[] ids = tps.getBinaryParserIds();
		Object[] data = new Object[configMap.size()];
		HashMap clone = new HashMap(configMap);
		// add checked elements
		int i;
		for (i=0; i<ids.length; i++) {
			data[i] = clone.get(ids[i]);
			clone.remove(ids[i]);
		}
		// add remaining parsers (unchecked)
		Iterator it = clone.keySet().iterator();
//		i = 0;
		while (it.hasNext()) {
			String s = (String)it.next();
			data[i++] = clone.get(s);
		}
		tv.setInput(data);
		tv.setAllChecked(false);
		// set check marks
		for (i=0; i<ids.length; i++) {
			if (configMap.containsKey(ids[i])) {
				tv.setChecked(configMap.get(ids[i]), true);
			}
		}
		updateButtons();
	}
	
	private void initializeParserList() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.BINARY_PARSER_SIMPLE_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			configMap = new HashMap(exts.length);
			for (int i = 0; i < exts.length; i++) {
				if (isExtensionVisible(exts[i])) {
					configMap.put(exts[i].getUniqueIdentifier(), new BinaryParserConfiguration(exts[i]));
				}
			}
		}
	}

	private void initializeParserPageMap() {
		fParserPageMap = new HashMap(5);

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, "BinaryParserPage"); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].getName().equals("parserPage")) { //$NON-NLS-1$
				String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
				fParserPageMap.put(id, new BinaryParserPageConfiguration(infos[i]));
			}
		}
	}

	private boolean isExtensionVisible(IExtension ext) {
 		IConfigurationElement[] elements = ext.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement[] children = elements[i].getChildren(ATTR_FILTER);
			for (int j = 0; j < children.length; j++) {
				String name = children[j].getAttribute(ATTR_NAME);
				if (name != null && name.equals(ATTR_NAME_VISIBILITY)) {
					String value = children[j].getAttribute(ATTR_VALUE);
					if (value != null && value.equals(ATTR_VALUE_PRIVATE)) {
						return false;
					}
				}
			}
			return true;
		}
		return false; // invalid extension definition (must have at least cextension elements)
	}
	
	public void updateButtons() {
		int cnt = table.getItemCount();
		int pos = table.getSelectionIndex();
		buttonSetEnabled(0, pos > 0);
		buttonSetEnabled(1, pos != -1 && pos < (cnt - 1));
	}
	protected void handleBinaryParserChanged() {
		String[] enabled = getBinaryParserIDs();
		ICOptionPage dynamicPage;
		for (int i = 0; i < enabled.length; i++) { // create all enabled pages
			dynamicPage = getBinaryParserPage(enabled[i]);
			
			if (dynamicPage != null) {
				if (dynamicPage.getControl() == null) {
					dynamicPage.setContainer(page);
					dynamicPage.createControl(parserGroup);
				} 
				dynamicPage.setVisible(false);
			}
		}
		// Retrieve the dynamic UI for the current parser
		String parserID = getCurrentBinaryParserID();
		dynamicPage = getBinaryParserPage(parserID);
		if (dynamicPage != null) { dynamicPage.setVisible(true); }
	}
	
	protected String[] getBinaryParserIDs() {
		return (String[]) configMap.keySet().toArray(new String[configMap.keySet().size()]);
	}

	protected ICOptionPage getBinaryParserPage(String parserID) {
		BinaryParserPageConfiguration configElement = (BinaryParserPageConfiguration) fParserPageMap.get(parserID);
		if (configElement != null) {
			try {
				return configElement.getPage();
			} catch (CoreException e) {}
		}
		return null;
	}

	protected String getCurrentBinaryParserID() {
		int x = table.getSelectionIndex();
		if (x < 0) return null;
		return ((BinaryParserConfiguration)table.getItem(x).getData()).getID();
	}

	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		ICTargetPlatformSetting tps1 = src.getConfiguration().getTargetPlatformSetting();
		ICTargetPlatformSetting tps2 = dst.getConfiguration().getTargetPlatformSetting();
		if (tps1 != null && tps2 != null) { // temporary
			tps2.setBinaryParserIds(tps1.getBinaryParserIds());
		}
	}
	
	protected void performDefaults() { 
		tps.setBinaryParserIds(null);
		informPages(false); 
		updateData(getResDesc());
	}	
	
	private void informPages(boolean apply) {	
		IProgressMonitor mon = new NullProgressMonitor();
		Iterator it = fParserPageMap.values().iterator();
		
		
		while (it.hasNext()) {
			try {
				ICOptionPage dynamicPage = ((BinaryParserPageConfiguration)it.next()).getPage();
				if (dynamicPage.isValid() && dynamicPage.getControl() != null) {
					if (apply)  
						dynamicPage.performApply(mon);
					else
						dynamicPage.performDefaults();
				}
			} catch (CoreException e) {}
		}
	}
	
	public void buttonPressed(int i) {
		switch (i) {
		case 0:
			moveItem(true);
			break;
		case 1:
			moveItem(false);
			break;
		default:
			break;
		}
	}

	// Move item up / down
	private void moveItem(boolean up) {
		int n = table.getSelectionIndex();
		if (n < 0 || 
				(up && n == 0) || 
				(!up && n+1 == table.getItemCount()))
			return;
		Object d = tv.getElementAt(n);
		boolean checked = tv.getChecked(d);
		tv.remove(d);
		n = up ? n - 1 : n + 1;
		tv.insert(d, n);
		tv.setChecked(d, checked);
		table.setSelection(n);
		saveChecked();
	}
	
	private void saveChecked() {
		Object[] objs = tv.getCheckedElements();
		String[] ids = null;
		if (objs != null) {
			ids = new String[objs.length];
			for (int i=0; i<objs.length; i++) {
				ids[i] = ((BinaryParserConfiguration)objs[i]).getID();
			}
		}
		if (tps != null) tps.setBinaryParserIds(ids);
	}
	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}
}
