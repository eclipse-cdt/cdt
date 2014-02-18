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
package org.eclipse.cdt.internal.ui.refactoring.pushdown.ui;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownInformation;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.MemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.Messages;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.UIHelper;

public class PushDownMemberPageControl extends Composite {

	
	public final static UserInputWizardPage createWizardPage(final String name, 
			final PushDownInformation information) {
		
		return new UserInputWizardPage(name) {
			@Override
			public void createControl(Composite parent) {
				final Composite c = new PushDownMemberPageControl(this, parent, 
						SWT.NONE, information);
				this.setControl(c); 
				this.setTitle(name);
			}
		};
	}
	
	
	private final PushDownInformation information;
	
	private TableViewer viewer;
	private Button selectAll;
	private Button deselectAll;
	private Button selectRequired;
	
	
	public PushDownMemberPageControl(UserInputWizardPage parentPage, Composite parent, 
			int style, PushDownInformation information) {
		super(parent, style);
		this.information = information;
		this.createControl();
	}
	
	
	
	private final void createControl() {
		final GridLayout layout = new GridLayout(2, false);
		GridData grid = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		grid.horizontalAlignment = GridData.FILL;
		this.setLayoutData(grid);
		this.setLayout(layout);
		
		UIHelper.newLabel(this, NLS.bind(Messages.PushDownRefactoring_pushDownFrom, 
				this.information.getSource().getName()), 2);
		UIHelper.newLabel(this, Messages.PushDownRefactoring_selectMembersToPushDown, 2);
		
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
	
	
	
	private final void createTableViewer() {
		this.viewer = UIHelper.newTable(this, 1);
		PushDownMemberTableEntry.createColumns(this.viewer);
		this.viewer.setInput(this.information.getAllMembers());
		
		// select initial user selection
		final MemberTableEntry mte = this.information.findByMember(
				this.information.getSelectedMember());
		if (mte != null) {
			// HINT: selected member is null if user initially selected constructor
			//		 or destructor
			mte.setSelectedAction(TargetActions.PUSH_DOWN);
			this.viewer.refresh();
		}
	}
	
	
	
	private final void createSelectRequired(Composite parent) {
		this.selectRequired = UIHelper.newButton(parent, 
				Messages.PullUpRefactoring_selectRequired);
		this.selectRequired.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] selection = viewer.getTable().getSelection();
				if (selection.length < 1) {
					return;
				}
				final PushDownMemberTableEntry mte = (PushDownMemberTableEntry) selection[0].getData();
				if (mte.getSelectedAction() != TargetActions.PUSH_DOWN) {
					// if member is not pushed down, there are no required members
					return;
				}
				final Collection<PushDownMemberTableEntry> dependencies = 
						information.calculateDependencies(mte);
				for (final PushDownMemberTableEntry depend : dependencies) {
					depend.setSelectedAction(TargetActions.PUSH_DOWN);
				}
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	}
	
	
	
	private final void createSelectAll(Composite parent) {
		this.selectAll = UIHelper.newButton(parent,
				Messages.PullUpRefactoring_selectAll);
		this.selectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (final PushDownMemberTableEntry mte : information.getAllMembers()) {
					mte.setSelectedAction(TargetActions.NONE);
				}
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	
	
	private final void createDeselectAll(Composite parent) {
		this.deselectAll = UIHelper.newButton(parent, 
				Messages.PullUpRefactoring_deselectAll);
		this.deselectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (final PushDownMemberTableEntry mte : information.getAllMembers()) {
					mte.setSelectedAction(TargetActions.PUSH_DOWN);
				}
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
}
