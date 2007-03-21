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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

/**
 * 
 * Functionality temporary commented
 * because of changes in Languages manager.
 * 
 * 
 */

public class LanguagesTab extends AbstractCPropertyTab {

	Table table;
	LanguageManager lm = LanguageManager.getInstance();
	IContentTypeManager ctm = Platform.getContentTypeManager();
	IResource cr;
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		cr = (IResource)page.getElement();

		usercomp.setLayout(new GridLayout(2, false));
		
		table = new Table(usercomp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		String[] headers = new String[] {UIMessages.getString("LanguagesTab.0"), UIMessages.getString("LanguagesTab.1"), };  //$NON-NLS-1$ //$NON-NLS-2$

		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setText(headers[i]);
			tc.setWidth(200);
		}
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

	}

	class ComboDataHolder {
		String id;
		ILanguageDescriptor[] des;
		
		ComboDataHolder(String _id, ILanguageDescriptor[] _des) {
			id = _id;
			des = _des;
		}
	}
	
	public void updateData(ICResourceDescription cfgd2) {
		/*
		table.removeAll();
		String[] ids = lm.getRegisteredContentTypeIds();
		for (int i=0; i< ids.length; i++) {
			IContentType ct = ctm.getContentType(ids[i]);
			ILanguageDescriptor[] des = null; 
			boolean needForFile = true;
			if (cr instanceof IFile) {
				needForFile = ct.isAssociatedWith(cr.getName());
			}
			if (needForFile) {
				des = lm.getDescriptorsForContentTypeId(ids[i]);
			}
			if (des != null && des.length > 0) {
				TableItem t = new TableItem(table, SWT.NONE);
				t.setText(0, ct.getName());
				t.setData(ct);
				
			    TableEditor editor = new TableEditor(table);
			    CCombo combo = new CCombo(table, SWT.NONE);
		    	combo.setData(new ComboDataHolder(ct.getId(), des));
			    int pos = 0;
			    
			    ILanguageDescriptor curr = lm.getLanguageForContentTypeId(cr.getProject(), cr.getFullPath().removeFirstSegments(1), ids[i]);
			    for (int j=0; j<des.length; j++) {
			    	combo.add(des[j].getName());
			    	if (des[j].equals(curr)) pos = j; // selected language 
			    }
			    combo.select(pos);
			    combo.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {}
					public void widgetSelected(SelectionEvent e) {
						int n = ((CCombo)e.widget).getSelectionIndex();
						ComboDataHolder h = (ComboDataHolder)e.widget.getData();
						try {
							lm.setLanguageForContentTypeId(cr.getProject(), cr.getFullPath().removeFirstSegments(1), h.id, h.des[n]);
						} catch (CoreException e2) {}
					}});
			    editor.grabHorizontal = true;
			    editor.setEditor(combo, t, 1);
			}
		}
		*/
	}

    public void setVisible(boolean _visible) {
    	super.setVisible(_visible);
    	page.enableConfigSelection(!_visible);
    }

	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
//		ICProjectDescription p1 = src.getConfiguration().getProjectDescription();
//		ICProjectDescription p2 = dst.getConfiguration().getProjectDescription();
		
	}

	protected void performDefaults() {
		/*
		String[] ids = lm.getRegisteredContentTypeIds();
		for (int i=0; i< ids.length; i++) {
			IContentType ct = ctm.getContentType(ids[i]);
		    ILanguageDescriptor curr = lm.getDefaultLanguageDescriptor(ct); 
		    try {
		    	lm.setLanguageForContentTypeId(cr.getProject(), cr.getFullPath().removeFirstSegments(1), ids[i], curr);
		    } catch (CoreException e) {}
		}
		updateData(getResDesc());
		*/
	}
}
