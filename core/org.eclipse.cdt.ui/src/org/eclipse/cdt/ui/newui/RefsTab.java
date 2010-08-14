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
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefsTab extends AbstractCPropertyTab {

	/** gray colour for 'disabled' items */
	private final Color GRAY_COLOR = new Color(Display.getDefault(), 100, 100, 100);
	public Composite comp;
	private Tree tree;

	static private final String ACTIVE = "[" + Messages.RefsTab_Active + "]"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final int EXPAND_ALL_BUTTON = 0;
	private static final int COLLAPSE_ALL_BUTTON = 1;
	private static final int MOVEUP_BUTTON = 3;
	private static final int MOVEDOWN_BUTTON = 4;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		initButtons(new String[] {
				Messages.RefsTab_ExpandAll,
				Messages.RefsTab_CollapseAll,
				null,
				MOVEUP_STR,
				MOVEDOWN_STR}, 120);
		usercomp.setLayout(new GridLayout(1, false));

		tree = new Tree(usercomp, SWT.SINGLE | SWT.CHECK | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.getAccessible().addAccessibleListener(
            new AccessibleAdapter() {
                @Override
				public void getName(AccessibleEvent e) {
                	e.result = Messages.RefsTab_ProjectsList;
                }
            }
        );

		// Populate the tree
		initData();
		tree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if ((e.detail & SWT.CHECK) == SWT.CHECK && e.item != null && (e.item instanceof TreeItem)) {
					TreeItem sel = (TreeItem)e.item;
					Object data = sel.getData();

					// If data is not a configuration ID, then the user isn't allowed to select this...
					if (data == null) {
						sel.setChecked(false);
						return;
					}

					boolean checked = sel.getChecked();
					TreeItem parent = sel.getParentItem();

					if (parent == null) {
						// Project -- top-level item -- selected
						if (checked)
							sel.setExpanded(true);
						for (TreeItem child : sel.getItems()) {
							if (checked) {
								// Don't select a non-allowed configuration
								if (child.getData() != null) {
									child.setChecked(true);
									break;
								}
							} else
								child.setChecked(false);
						}
					} else {
						// Configuration selected (it has a parent)
		    			if (parent.getChecked()) {
		    				// Deselect other configs already selected
		    				for (TreeItem obj : parent.getItems())
	    						obj.setChecked(false);
		    				sel.setChecked(checked);
		    			}
			    		parent.setChecked(checked);
					}

					// Save the checked configurations
					saveChecked();
			    }
				updateButtons();
			}
		});

		tree.addTreeListener(new TreeListener() {
			public void treeCollapsed(TreeEvent e) {
				updateExpandButtons(e, false, true);
			}
			public void treeExpanded(TreeEvent e) {
				updateExpandButtons(e, true, false);
			}});

	}

    @Override
	public void buttonPressed(int n) {
    	switch (n)
    	{
    	case COLLAPSE_ALL_BUTTON:
    	case EXPAND_ALL_BUTTON:
       		for (TreeItem item : tree.getItems())
       			item.setExpanded(n==EXPAND_ALL_BUTTON);
       		updateButtons();
       		break;
    	case MOVEUP_BUTTON:
    	case MOVEDOWN_BUTTON:
    		// TODO cache this...
    		Map<String, String> oldMapping = getResDesc().getConfiguration().getReferenceInfo();
    		TreeItem ti = tree.getSelection()[0];
    		String projectName = ti.getText();
    		List<String> projNames = new ArrayList<String>(oldMapping.keySet());
    		int index = projNames.indexOf(projectName);
    		if (n == MOVEUP_BUTTON) {
    			if (index > 0) {
	    			projNames.set(index, projNames.get(index - 1));
	    			projNames.set(index - 1, projectName);
    			}
    		} else {
    			if (index < projNames.size() - 1) {
	    			projNames.set(index, projNames.get(index + 1));
	    			projNames.set(index + 1, projectName);
    			}
    		}
    		Map<String, String> newMapping = new LinkedHashMap<String, String>(oldMapping.size());
    		for (String name : projNames)
    			newMapping.put(name, oldMapping.get(name));
    		getResDesc().getConfiguration().setReferenceInfo(newMapping);
    		initData();
    		break;
    	}
    }

	@Override
	protected void updateData(ICResourceDescription cfgd) {
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
		} else {
			if (!usercomp.isVisible())
				setAllVisible(true, null);
			initData();
		}
	}

	/**
	 * Persist the checked configurations
	 */
	private void saveChecked() {
		Map<String, String> refs = new LinkedHashMap<String, String>();
		for (TreeItem project : tree.getItems()) {
			if (project.getChecked()) {
				if (project.getData() instanceof String) {
					assert(project.getData() != null);
					// Project is missing from the workspace, maintain references
					refs.put(project.getText(), (String)project.getData());
				} else {
					for (TreeItem config : project.getItems()) {
						if (config.getChecked()) {
							assert(config.getData() != null);
							refs.put(project.getText(), (String)config.getData());
							break; // only one configuration can be selected a time in a project
						}
					}
				}
			}
		}
		getResDesc().getConfiguration().setReferenceInfo(refs);
	}

	/**
	 * Initialises the tree.
	 *
	 * TreeItems are either
	 * TI:       Text            ,     Data
	 *   {IProject.getName()}    , {IProject}
	 *       {cfgName}           , {String cfgId}
	 *       {cfgName}           , {null}  // config is not allowed to be selected
	 *
	 * If the projects doesn't exist in the workspace:
	 *   {IProject.getName()}    , {String cfgId}
	 *
	 */
    private void initData() {
    	// Persist the current select / expand state to restore...
    	String currentSelection = tree.getSelectionCount() == 1 ? tree.getSelection()[0].getText() : null;
    	Set<String> currentExpanded = new HashSet<String>();
    	for (TreeItem ti : tree.getItems())
    		if (ti.getExpanded())
    			currentExpanded.add(ti.getText());

		tree.removeAll();
		IProject p = page.getProject();
		if (p == null)
			return;

		// Get all the CDT references
		Map<String,String> refs = getResDesc().getConfiguration().getReferenceInfo();

		// Preserve project order. All linked to projects occur before others
		Set<String> projects = new LinkedHashSet<String>(refs.keySet());
		for (IProject prj : p.getWorkspace().getRoot().getProjects())
			projects.add(prj.getName());

		for (String pname : projects) {
			// The referenced configuration ID
			String ref = refs.get(pname);
			IProject prj;
			ICConfigurationDescription[] cfgs;
			try {
				prj = p.getWorkspace().getRoot().getProject(pname);
				cfgs = page.getCfgsReadOnly(prj);
			} catch (Exception e) {
				CUIPlugin.log(Messages.RefsTab_ConfigurationsAccessError+pname, e);
				continue;
			}
			if (cfgs == null || cfgs.length == 0) {
				// If the project is referenced, then make sure the user knows about it!
				if (ref != null) {
					TreeItem ti = new TreeItem(tree, SWT.NONE);
					ti.setChecked(true);
					ti.setText(pname);
					ti.setData(refs.get(pname));
					ti.setForeground(GRAY_COLOR);
				}
				continue;
			}

			// Only show the current project if it's got more than 1 configuration
			if (page.getProject().equals(prj) && cfgs.length < 2)
				continue;

			// Add the project
			TreeItem ti = new TreeItem(tree, SWT.NONE);
			ti.setText(pname);
			ti.setData(prj);
			if (ref != null)
				ti.setChecked(true);

			// Add the configurations
			TreeItem ti1;
			if (!prj.equals(p)) {
				// [ Active ] config in the tree
				ti1 = new TreeItem(ti, SWT.NONE);
				ti1.setText(ACTIVE);
				ti1.setData(EMPTY_STR);
				if (EMPTY_STR.equals(ref)) {
					ti1.setChecked(true);
					ti1.setData(ref);
				}
			}
			// Name configurations in the tree
			for (ICConfigurationDescription cfg : cfgs) {
				// Don't include self configuration
				ti1 = new TreeItem(ti, SWT.NONE);
				ti1.setText(cfg.getName());
				if (prj.equals(p) && cfg.getId().equals(page.getResDesc().getConfiguration().getId())) {
					// users may *only* reference other configuration in the project
					// this data is deliberately null as it may not be selected...
					ti1.setForeground(GRAY_COLOR);
					continue;
				} else if (cfg.getId().equals(ref))
					ti1.setChecked(true);
				ti1.setData(cfg.getId());
			}
		}

		// Reselect / Re-expand previously selected & expanded items
		if (currentSelection != null)
			for (TreeItem ti : tree.getItems())
				if (ti.getText().equals(currentSelection)) {
					tree.setSelection(ti);
					break;
				}
		for (TreeItem ti : tree.getItems())
			if (currentExpanded.contains(ti.getText()))
				ti.setExpanded(true);

		updateButtons();
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		dst.getConfiguration().setReferenceInfo(src.getConfiguration().getReferenceInfo());
	}

	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return page.isForProject();
	}

	@Override
	protected void performDefaults() {
		if (!usercomp.isVisible())
			return;
		getResDesc().getConfiguration().setReferenceInfo(new HashMap<String, String>());
		initData();
	}

	@Override
	protected void updateButtons() {
		updateExpandButtons(null, false, false);
		updateMoveButtons();
	}

	@Override
	public void dispose() {
		super.dispose();
		GRAY_COLOR.dispose();
	}

	private void updateExpandButtons(TreeEvent e, boolean stateE, boolean stateC) {
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
		buttonSetEnabled(EXPAND_ALL_BUTTON, cntC); // Expand All
		buttonSetEnabled(COLLAPSE_ALL_BUTTON, cntE); // Collapse all
	}

	/**
	 * Make the move buttons enabled when a project is selected
	 */
	private void updateMoveButtons() {
		if (tree.getSelectionCount() == 1) {
			TreeItem ti = tree.getSelection()[0];
			// Is a project selected?
			if (ti.getParentItem() == null && ti.getChecked()) {
				int index = tree.indexOf(ti);
				buttonSetEnabled(MOVEUP_BUTTON, index > 0);
				buttonSetEnabled(MOVEDOWN_BUTTON, index < tree.getItemCount() - 1);
				return;
			}
		}
		buttonSetEnabled(MOVEUP_BUTTON, false);
		buttonSetEnabled(MOVEDOWN_BUTTON, false);
	}

}

