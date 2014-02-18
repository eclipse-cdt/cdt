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

import java.util.Collection;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpInformation;

public class PullUpMemberPageControl extends Composite {
	
	public final static UserInputWizardPage createWizardPage(String name, 
			final PullUpInformation information) {
		
		return new UserInputWizardPage(name) {
			@Override
			public void createControl(Composite parent) {
				final Composite c = new PullUpMemberPageControl(
						parent, SWT.NONE, information);
				this.setControl(c);
				this.setTitle(this.getName());
			}
		};
	}

	
	
	private final PullUpInformation information;
	
	private Button methodStubs;
	private Button pullUpIntoAbstract;
	private Button selectAll;
	private Button deselectAll;
	private Button selectRequired;
	private TableViewer viewer;
	private Combo targetCombo;
	
	
	
	private PullUpMemberPageControl(Composite parent, int style, 
			PullUpInformation information) {
		super(parent, style);
		this.information = information;
		this.createControl();
	}
	
	
	
	private void createTargetCombo() {
		this.targetCombo = new Combo(this, SWT.READ_ONLY);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.verticalIndent = 10;
		grid.grabExcessHorizontalSpace = true;
		grid.horizontalSpan = 2;
		this.targetCombo.setLayoutData(grid);
		
		for (final InheritanceLevel level : information.getTargets()) {
			this.targetCombo.add(level.toString());
		}
		this.targetCombo.select(0);
		this.information.setSelectedTarget(
				this.information.getTargets().get(0));
		
		this.targetCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int i = targetCombo.getSelectionIndex();
				final InheritanceLevel selected = 
						information.getTargets().get(i);
				information.setSelectedTarget(selected);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	
	
	
	private void createTableViewer() {
		this.viewer = UIHelper.newTable(this, 2);
		PullUpMemberTableEntry.createColumns(this.viewer);
		this.viewer.setInput(this.information.getAllMembers());
		final PullUpMemberTableEntry mte = this.information.findByMember(
				this.information.getSelectedMember());
		if (mte != null) {
			// HINT: selected member is null if user initially selected constructor
			//		 or destructor
			mte.setSelectedAction(TargetActions.PULL_UP);
			this.viewer.refresh();
		}
	}
	
	
	
	private void createPullUpIntoAbstract() {
		this.pullUpIntoAbstract = new Button(this, SWT.CHECK);
		this.pullUpIntoAbstract.setText(Messages.PullUpRefactoring_pullUpIntoPureAbstract);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.horizontalSpan = 3;
		this.pullUpIntoAbstract.setLayoutData(grid);
		this.pullUpIntoAbstract.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				information.setPullIntoPureAbstract(pullUpIntoAbstract.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	
	
	private void createInsertMethodStubs() {
		this.methodStubs = new Button(this, SWT.CHECK);
		this.methodStubs.setText(Messages.PullUpRefactoring_insertStubs);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.horizontalSpan = 3;
		this.methodStubs.setLayoutData(grid);
		this.methodStubs.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				information.setDoInsertMethodStubs(methodStubs.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	
	
	private void createSelectAll(Composite parent) {
		this.selectAll = UIHelper.newButton(parent, 
				Messages.PullUpRefactoring_selectAll);
		this.selectAll.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (final PullUpMemberTableEntry mte : information.getAllMembers()) {
					mte.setSelectedAction(TargetActions.PULL_UP);
				}
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	
	
	private void createDeselectAll(Composite parent) {
		this.deselectAll = UIHelper.newButton(parent, 
				Messages.PullUpRefactoring_deselectAll);
		this.deselectAll.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (final PullUpMemberTableEntry mte : information.getAllMembers()) {
					mte.setSelectedAction(TargetActions.NONE);
				}
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	
	
	private void createSelectRequired(Composite parent) {
		this.selectRequired = UIHelper.newButton(parent, 
				Messages.PullUpRefactoring_selectRequired);
		this.selectRequired.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] selection = viewer.getTable().getSelection();
				if (selection.length < 1) {
					return;
				}
				final PullUpMemberTableEntry mte = (PullUpMemberTableEntry) selection[0].getData();
				
				final Collection<PullUpMemberTableEntry> dependencies = 
						information.calculateDependencies(mte);
				for (final PullUpMemberTableEntry depend : dependencies) {
					depend.setSelectedAction(TargetActions.PULL_UP);
				}
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	
	
	
	private final void createControl() {
		final GridLayout layout = new GridLayout(3, false);
		GridData grid = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		grid.horizontalAlignment = GridData.FILL;
		this.setLayoutData(grid);
		this.setLayout(layout);

		UIHelper.newLabel(this, NLS.bind(Messages.PullUpRefactoring_pullUpFrom, 
				this.information.getSource().getName().toString()), 3);
		UIHelper.newLabel(this, 
				Messages.PullUpRefactoring_selectTarget, 1).verticalIndent = 10;
		this.createTargetCombo();
		this.createInsertMethodStubs();
		this.createPullUpIntoAbstract();
		
		UIHelper.newLabel(this, Messages.PullUpRefactoring_selectMembers, 3).verticalIndent = 5;
		this.createTableViewer();
		
		final Composite right = new Composite(this, SWT.NONE);
		right.setLayout(new GridLayout());
		grid = new GridData();
		grid.verticalAlignment = SWT.TOP;
		right.setLayoutData(grid);
		
		this.createSelectRequired(right);
		this.createSelectAll(right);
		this.createDeselectAll(right);
		
	}
}
