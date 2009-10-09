/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefsTab extends AbstractCPropertyTab {
	
	public Composite comp;
	private Tree tree;

	static private final String ACTIVE = "[" + UIMessages.getString("RefsTab.3") + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		initButtons(new String[] {UIMessages.getString("RefsTab.0"), UIMessages.getString("RefsTab.2")}, 120); //$NON-NLS-1$ //$NON-NLS-2$
		usercomp.setLayout(new GridLayout(1, false));
		
		tree = new Tree(usercomp, SWT.SINGLE | SWT.CHECK | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.getAccessible().addAccessibleListener(
            new AccessibleAdapter() {                       
                @Override
				public void getName(AccessibleEvent e) {
                        e.result = UIMessages.getString("RefsTab.4"); //$NON-NLS-1$
                }
            }
        );
		initData();
		tree.addSelectionListener(new SelectionAdapter() {

			@Override
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
			    	    	for (TreeItem obj : objs)
								obj.setChecked(false);
			    	    }
			    	} else {
			    		TreeItem parent = sel.getParentItem();
			    		TreeItem[] objs = parent.getItems();
			    		if (sel.getChecked()) {
			    			if (parent.getChecked()) {
			    				for (TreeItem obj : objs) {
			    				//	if (!sel.equals(objs[i]))
			    						obj.setChecked(false);
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
				updateButtons();
			}
		});
		
		tree.addTreeListener(new TreeListener() {
			public void treeCollapsed(TreeEvent e) {
				updateButtons(e, false, true); 
			}
			public void treeExpanded(TreeEvent e) {
				updateButtons(e, true, false); 
			}});
			
	}

	// Class which represents "Active" configuration
	private static class ActiveCfg {
		public ActiveCfg(IProject _project) {
		}
		@Override
		public String toString() {
			return ACTIVE;
		}
	}

    @Override
	public void buttonPressed(int n) {
   		for (TreeItem item : tree.getItems()) 
   			item.setExpanded(n==0);
    }

	@Override
	protected void updateData(ICResourceDescription cfgd) {
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
		} else {
			if ( !usercomp.getVisible()) 
				setAllVisible(true, null);
			initData();
		}
	}

	private void saveChecked() {
		TreeItem[] tr = tree.getItems();
		Map<String, String> refs = new HashMap<String, String>();
		for (TreeItem element : tr) {
			if (element.getChecked()) {
				TreeItem[] cfgs = element.getItems();
				for (int j=0; j<cfgs.length; j++) {
					if (cfgs[j].getChecked()) {
						String cfgId = EMPTY_STR;
						Object ob = cfgs[j].getData();
						if (ob instanceof ICConfigurationDescription) {
							cfgId = ((ICConfigurationDescription)ob).getId();
						}
						refs.put(element.getText(), cfgId);
						break;
					}				
				}
			}
		}
		getResDesc().getConfiguration().setReferenceInfo(refs);
	}

    private void initData() {
		tree.removeAll();
		IProject p = page.getProject();
		if (p == null)
			return;

		Map<String,String> refs = getResDesc().getConfiguration().getReferenceInfo();

		for (IProject prj : p.getWorkspace().getRoot().getProjects()) {
			ICConfigurationDescription[] cfgs = page.getCfgsReadOnly(prj);
			if (cfgs == null || cfgs.length == 0) 
				continue;

			String name = prj.getName();
			String ref = null;
			TreeItem ti = new TreeItem(tree, SWT.NONE);
			ti.setText(name);
			ti.setData(prj);
			if (refs.containsKey(name)) {
				ref = refs.get(name);
				ti.setChecked(true);
			}
			TreeItem ti1;
			if (!prj.equals(p)) {
				// [ Active ] config in the tree
				ti1 = new TreeItem(ti, SWT.NONE);
				ti1.setText(ACTIVE);
				ti1.setData(new ActiveCfg(prj));
				if (EMPTY_STR.equals(ref))
					ti1.setChecked(true);
			}
			// Name configurations in the tree
			for (ICConfigurationDescription cfg : cfgs) {
				// Don't include self configuration
				if (prj.equals(p) && cfg.getId().equals(page.getResDesc().getConfiguration().getId()))
					continue;
				ti1 = new TreeItem(ti, SWT.NONE);
				ti1.setText(cfg.getName());
				ti1.setData(cfg);
				if (cfg.getId().equals(ref)) {
					ti1.setChecked(true);
				}
			}
		}
		updateButtons();
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		dst.getConfiguration().setReferenceInfo(src.getConfiguration().getReferenceInfo());
	}

	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return page.isForProject() && ! page.isMultiCfg();
	}

	@Override
	protected void performDefaults() {
		getResDesc().getConfiguration().setReferenceInfo(new HashMap<String, String>());
		initData();
	}
	
	@Override
	protected void updateButtons() {
		updateButtons(null, false, false);
	}

	private void updateButtons(TreeEvent e, boolean stateE, boolean stateC) {
		boolean cntE = stateE;
		boolean cntC = stateC;
   		for (TreeItem item : tree.getItems()) {
   			if (e != null && e.widget.equals(item))
   				continue;
   			if (item.getExpanded())
   				cntE = true;
   			else 
   				cntC = true;
   		}
		buttonSetEnabled(0, cntC); // Expand All 
		buttonSetEnabled(1, cntE); // Collapse all 
	}

}

