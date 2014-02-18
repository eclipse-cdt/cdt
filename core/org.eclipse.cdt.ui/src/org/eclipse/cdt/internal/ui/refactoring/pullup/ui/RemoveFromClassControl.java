/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpInformation;

class RemoveFromClassControl extends Composite {

	public final static UserInputWizardPage createWizardPage(final String name,
			final PullUpInformation information) {
		
		return new UserInputWizardPage(name) {
			
			@Override
			public void createControl(Composite parent) {
				final Composite c = new RemoveFromClassControl(parent, SWT.NONE, 
						information);
				this.setControl(c);
				this.setTitle(name);
			}
		};
	}
	
	
	private final PullUpInformation information;
	private CheckboxTreeViewer treeViewer;
	
	RemoveFromClassControl(Composite parent, int style, PullUpInformation information) {
		super(parent, style);
		this.information = information;
		this.createControl();
	}
	
	
	
	private void createControl() {
		final GridLayout layout = new GridLayout(2, false);
		GridData grid = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		grid.horizontalAlignment = GridData.FILL;
		this.setLayoutData(grid);
		this.setLayout(layout);
		
		UIHelper.newLabel(this, Messages.PullUpRefactoring_checkAdditionalMembers, 2);
		
		this.createTreeViewer();
	}
	
	
	
	private void createTreeViewer() {
		this.treeViewer = new CheckboxTreeViewer(this, 
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.verticalAlignment = SWT.FILL;
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		this.treeViewer.getTree().setLayoutData(grid);
		
	    this.treeViewer.getTree().addListener(SWT.Selection, new Listener() {
	        @Override
			public void handleEvent(Event event) {
	            if (event.detail == SWT.CHECK) {
	                TreeItem item = (TreeItem) event.item;
	                boolean checked = item.getChecked();
	                information.toggleRemove((SubClassTreeEntry) item.getData(), checked);
	                UIHelper.checkItems(item, checked);
	                UIHelper.checkPath(item.getParentItem(), checked, false);
	            }
	        }
	    });
		
	    final TreeColumn member = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
	    member.setAlignment(SWT.LEFT);
	    member.setText(Messages.PullUpRefactoring_columnMember);
	    member.setWidth(200);
	}
	
	
	
	private void initializeTreeViewer() {
		if (this.treeViewer == null) {
			this.createTreeViewer();
		}
		
	    final Map<InheritanceLevel, List<SubClassTreeEntry>> tree = 
	    		this.information.generateTree();
	    final ITreeContentProvider content = new SubClassContentProvider(tree);
	    this.treeViewer.getTree().setHeaderVisible(true);
	    this.treeViewer.setLabelProvider(new MemberTreeLabelProvider(this.information));
		this.treeViewer.setContentProvider(content);
		
		final InheritanceLevel[] roots = 
				new InheritanceLevel[tree.size()];
		if (!tree.isEmpty()) {
			tree.keySet().toArray(roots);
		}
		this.treeViewer.setInput(roots);
		this.treeViewer.expandAll();
	}

	
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			this.information.resetAdditionalRemoves();
			this.initializeTreeViewer();
		}
		super.setVisible(visible);
	}
}
