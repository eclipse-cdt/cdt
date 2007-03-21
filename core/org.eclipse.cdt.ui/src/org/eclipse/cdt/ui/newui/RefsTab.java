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
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

public class RefsTab extends AbstractCPropertyTab {
	
	TreeViewer tv;
	public Composite comp;
	Tree tree;

	static private final String ACTIVE = "[Active]"; //$NON-NLS-1$
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		initButtons(new String[] {UIMessages.getString("RefsTab.0"), UIMessages.getString("RefsTab.1"), UIMessages.getString("RefsTab.2")}, 120); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		usercomp.setLayout(new GridLayout(1, false));
		
		tree = new Tree(usercomp, SWT.SINGLE | SWT.CHECK | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		initData();
		tree.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if ((e.detail & SWT.CHECK) == SWT.CHECK && e.item != null && (e.item instanceof TreeItem)) {
					TreeItem sel = (TreeItem)e.item;
					Object x = sel.getData();
					if (x instanceof IProject) {
						TreeItem[] objs = sel.getItems();
						if (sel.getChecked()) {
							sel.setExpanded(true);
			    		    objs[0].setChecked(true);
			    	    } else {
			    	    	for (int i=0; i<objs.length; i++)
			    	    		objs[i].setChecked(false);
			    	    }
			    	} else {
			    		TreeItem parent = sel.getParentItem();
			    		TreeItem[] objs = parent.getItems();
			    		if (sel.getChecked()) {
			    			if (parent.getChecked()) {
			    				for (int i=0; i<objs.length; i++) {
			    				//	if (!sel.equals(objs[i]))
			    						objs[i].setChecked(false);
			    				}
			    				sel.setChecked(true);
			    			} else
			    				parent.setChecked(true);
			    		} else {
			    			parent.setChecked(false);
			    		}
		    		}
					saveChecked();
			    }
			}
		});
	}
	
	// Class which represents "Active" configuration
	class ActiveCfg {
		IProject project;
		public ActiveCfg(IProject _project) {
			project = _project; 
		}
		public String toString() {
			return ACTIVE;
		}
	}

    public void buttonPressed(int n) {
   		TreeItem[] items = tree.getItems();
   		switch (n) {
    	case 0: // expand
    	case 1: // expand selected
    	case 2: // collapse
     		for (int i=0; i<items.length; i++) 
     			items[i].setExpanded(n==0 || (n==1 && items[i].getChecked()));
    		break;
    	default:	
    		break;
    	}
    }

	public void updateData(ICResourceDescription cfgd) {
		initData();
	}

	private void saveChecked() {
		TreeItem[] tr = tree.getItems();
		Map refs = new HashMap();
		for (int i=0; i<tr.length; i++) {
			if (tr[i].getChecked()) {
				TreeItem[] cfgs = tr[i].getItems();
				for (int j=0; j<cfgs.length; j++) {
					if (cfgs[j].getChecked()) {
						String cfgId = EMPTY_STR;
						if (j > 0) { // cfgs[0] is "Active": has no cfg Id
							Object ob = cfgs[j].getData();
							if (ob instanceof ICConfigurationDescription) {
								cfgId = ((ICConfigurationDescription)ob).getId();
							}
						}
						refs.put(tr[i].getText(), cfgId);
						break;
					}				
				}
			}
		}
		getResDesc().getConfiguration().setReferenceInfo(refs);
	}

    public void initData() {
		tree.removeAll();
		IProject p = page.getProject();
		if (p == null) return;
		IProject[] ps = p.getWorkspace().getRoot().getProjects();

		Map refs = getResDesc().getConfiguration().getReferenceInfo();

		
		TreeItem ti, ti1;
		for (int i=0; i<ps.length; i++) {
			
			if (!p.equals(ps[i])) { 

				ICConfigurationDescription[] cfgs = page.getCfgsReadOnly(ps[i]);
				if (cfgs == null || cfgs.length == 0) continue;
					
				String name = ps[i].getName();
				String ref = null;
				ti = new TreeItem(tree, SWT.NONE);
				ti.setText(name);
				ti.setData(ps[i]);
				if (refs.containsKey(name)) {
					ref = (String)refs.get(name);
					ti.setChecked(true);
				}
				ti1 = new TreeItem(ti, SWT.NONE);
				ti1.setText(ACTIVE);
				ti1.setData(new ActiveCfg(ps[i]));
				if (EMPTY_STR.equals(ref))
					ti1.setChecked(true);
				for (int j=0; j<cfgs.length; j++) {
					ti1 = new TreeItem(ti, SWT.NONE);
					ti1.setText(cfgs[j].getName());
					ti1.setData(cfgs[j]);
					if (cfgs[j].getId().equals(ref)) {
						ti1.setChecked(true);
					}
				}
			}
		}
	}

	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		dst.getConfiguration().setReferenceInfo(src.getConfiguration().getReferenceInfo());
	}

	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject();
	}

	protected void performDefaults() {
		getResDesc().getConfiguration().setReferenceInfo(new HashMap());
		initData();
	}
}

