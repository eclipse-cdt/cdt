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
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;


public class ErrorParsTab extends AbstractCPropertyTab {
	protected HashMap mapParsers = new HashMap();
	protected Table table;
	protected CheckboxTableViewer tv;
	ICConfigurationDescription cfgd;
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new FillLayout());
		table = new Table(usercomp, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
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
		
		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveChecked();
			}
		});
		
		initButtons(new String[] {
				MOVEUP_STR, MOVEDOWN_STR, null, 
				UIMessages.getString("ErrorParsTab.0"), //$NON-NLS-1$
				UIMessages.getString("ErrorParsTab.1")  //$NON-NLS-1$
				});
		initMapParsers();
	}
	
	protected void initMapParsers() {
		mapParsers.clear();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				CCorePlugin.PLUGIN_ID,
				CCorePlugin.ERROR_PARSER_SIMPLE_ID
				);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (int i = 0; i < exts.length; i++) {
				if (exts[i].getConfigurationElements().length > 0) {
					mapParsers.put(exts[i].getUniqueIdentifier(), exts[i].getLabel());
				}
			}
		}
	}
	
	public void buttonPressed (int n) {
		switch (n) {
		case 0: // up
			moveItem(true);
			break;
		case 1: // down
			moveItem(false);
			break;
		case 2: // do nothing - it's not a button
			break;
			
		case 3: // check all
			tv.setAllChecked(true);
			break;
		case 4: // uncheck all	
			tv.setAllChecked(false);
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
		TableData d = (TableData)tv.getElementAt(n);
		boolean checked = tv.getChecked(d);
		tv.remove(d);
		n = up ? n - 1 : n + 1;
		tv.insert(d, n);
		tv.setChecked(d, checked);
		table.setSelection(n);
		saveChecked();
	}
	
	class TableData {
		String key;
		String value;
		public TableData (String _key, String _value) {
			key   = _key;
			value = _value;
		}
		public String toString() { return value; } 
	}
	
	public void updateData(ICResourceDescription _cfgd) {
		cfgd = _cfgd.getConfiguration();
		if (mapParsers == null) return;
		String[] ss = cfgd.getBuildSetting().getErrorParserIDs();
		
		ArrayList data = new ArrayList(mapParsers.size());
		ArrayList checked = new ArrayList(ss.length);
		HashMap cloneMap = new HashMap(mapParsers);
		// add checked elements
		for (int i=0; i<ss.length; i++) {
			String s = (String)cloneMap.get(ss[i]);
			if (s != null) {
				TableData d = new TableData(ss[i],s);
				data.add(d);
				checked.add(d);
				cloneMap.remove(ss[i]);
			}
		}
		// add remaining parsers (unchecked)
		Iterator it = cloneMap.keySet().iterator();
		while (it.hasNext()) {
			String s = (String)it.next();
			data.add(new TableData(s, (String)cloneMap.get(s)));
		}
		tv.setInput(data.toArray());
		tv.setCheckedElements(checked.toArray());
		updateButtons();
	}

	public void updateButtons() {
		int cnt = table.getItemCount();
		int pos = table.getSelectionIndex();
		buttonSetEnabled(0, pos > 0);
		buttonSetEnabled(1, pos != -1 && pos < (cnt - 1));
		buttonSetEnabled(3, cnt > 0);
		buttonSetEnabled(4, cnt > 0);
	}
	
	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		String[] s1 = src.getConfiguration().getBuildSetting().getErrorParserIDs();
		dst.getConfiguration().getBuildSetting().setErrorParserIDs(s1);
	}
	
	private void saveChecked() {
		Object[] objs = tv.getCheckedElements();
		ArrayList lst = new ArrayList();
		if (objs != null) {
			for (int i=0; i<objs.length; i++) 
				lst.add(((TableData)objs[i]).key);
		}
		cfgd.getBuildSetting().setErrorParserIDs((String[])lst.toArray(new String[lst.size()]));
	}
	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}

	protected void performDefaults() {
		cfgd.getConfiguration().getBuildSetting().setErrorParserIDs(null);
		updateData(getResDesc());
	}	
}
