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

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownInformation;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.MemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.Messages;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.UIHelper;

public class PushDownTargetClassControl extends Composite {
	
	
	public final static UserInputWizardPage createWizardPage(final String name, 
			final PushDownInformation information) {
		
		return new UserInputWizardPage(name) {
			@Override
			public void createControl(Composite parent) {
				final Composite c = new PushDownTargetClassControl(parent, SWT.NONE, 
						information);
				this.setControl(c);
				this.setTitle(name);
			}
		};
	}
	
	
	
	
	private final static class MyCellModifier implements ICellModifier {

		private final PushDownInformation information;
		private final Viewer viewer;
		private final ITreeContentProvider model;
		
		
		public MyCellModifier(Viewer viewer, ITreeContentProvider model, PushDownInformation information) {
			this.viewer = viewer;
			this.model = model;
			this.information = information;
		}
		
		
		
		@Override
		public boolean canModify(Object element, String property) {
			//if (element instanceof TargetTableEntry) {
				return true;
			//}
			//return false;
		}

		
		
		@Override
		public Object getValue(Object element, String property) {
			if (element instanceof TargetTableEntry) {
				final TargetTableEntry tte = (TargetTableEntry) element;
				if (TargetTableEntry.TARGET_ACTION.equals(property)) {
					return tte.getActions().index(tte.getSelectedAction());
				}
			}
			return 0;
		}
		
		

		@Override
		public void modify(Object element, String property, Object value) {
			if (!(element instanceof TreeItem)) {
				return;
			}
			final TreeItem item = (TreeItem) element;
			
			int idx = (Integer) value;
			idx = Math.max(idx, 0);
			
			
			if (item.getData() instanceof InheritanceLevel) {
				final InheritanceLevel lvl = (InheritanceLevel) item.getData();
				
				for (final Object obj : this.model.getChildren(lvl)) {
					if (obj instanceof TargetTableEntry) {
						final TargetTableEntry tte = (TargetTableEntry) obj;
						this.modifyTargetTableEntry(tte, property, idx);
					}
				}
			} else {
				final TargetTableEntry tte = (TargetTableEntry) item.getData();
				this.modifyTargetTableEntry(tte, property, idx);
			}
			this.viewer.refresh();
		}
		
		
		
		private void modifyTargetTableEntry(TargetTableEntry tte, String property, int idx) {
			String action = tte.getActions().getSupported()[idx];
			if (TargetTableEntry.TARGET_ACTION.equals(property)) {
				if (action == TargetActions.NONE && this.information.isMandatory(
						tte.getMember(), tte.getParent())) {
					// can not set action to 'NONE' if this is a mandatory member
					action = tte.getSelectedAction();
				}
				if (tte.getMember().getMember() instanceof ICPPField) {
					// fields only support 'NONE' and 'EXISTING DEFINITION'
					if (action != TargetActions.NONE && action != TargetActions.EXISTING_DEFINITION) {
						action = tte.getSelectedAction();
					}
				}
				tte.setSelectedAction(action);
				tte.getMember().setActionForClass(tte.getParent(), action);
			}
		}
	}
	
	
	
	/**
	 * Tree model for showing base classes with the previously selected members as children
	 * @author Simon Taddiken
	 */
	private final static class TargetClassContentProvider 
			implements ITreeContentProvider {

		private Map<InheritanceLevel, List<TargetTableEntry>> tree;
		
		
		public TargetClassContentProvider(PushDownInformation information) {
			this.tree = information.generateTree();
		}
		

		
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		
		
		
		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof InheritanceLevel) {
				return this.tree.get(element).toArray();
			}
			return new TargetTableEntry[0];
		}
		
		
		
		@Override
		public Object[] getElements(Object element) {
			return (InheritanceLevel[]) element;
		}
		
		
		
		@Override
		public Object getParent(Object element) {
			if (element instanceof TargetTableEntry) {
				return ((TargetTableEntry) element).getParent();
			}
			return null;
		}
		
		
		
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof InheritanceLevel) {
				final List<TargetTableEntry> elements = this.tree.get(element);
				return elements != null && !elements.isEmpty();
			}
			return false;
		}
		
		
		@Override
		public void dispose() {}
	}
	
	

	/**
	 * Provides the column labels for the tree view.
	 * 
	 * @author Simon Taddiken
	 */
	private final static class TreeLabelProvider implements ITableLabelProvider {

		private final PushDownInformation information;
		
		public TreeLabelProvider(PushDownInformation information) {
			this.information = information;
		}
		
		
		
		@Override
		public Image getColumnImage(Object element, int column) {
			if (column == 0) {
				if (element instanceof TargetTableEntry) {
					final TargetTableEntry tte = (TargetTableEntry) element;
					return MemberTableEntry.DECLARATOR_LABEL_PROVIDER.getImage(tte.getMember());
				} else if (element instanceof InheritanceLevel) {
					return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CLASS);
				}
				throw new IllegalArgumentException();
			}
			return null;
		}
		
		

		@Override
		public String getColumnText(Object element, int column) {
			switch (column) {
			case 0:
				if (element instanceof TargetTableEntry) {
					final TargetTableEntry tte = (TargetTableEntry) element;
					return MemberTableEntry.DECLARATOR_LABEL_PROVIDER.getText(tte.getMember());
				} else if (element instanceof InheritanceLevel) {
					final InheritanceLevel lvl = (InheritanceLevel) element;
					return lvl.getClazz().getName();
				}
				throw new IllegalArgumentException();
			case 1:
				if (element instanceof TargetTableEntry) {
					return ((TargetTableEntry) element).getSelectedAction();
				} else if (element instanceof InheritanceLevel) {
					return null;
				}
				throw new IllegalArgumentException();
			case 2:
				if (element instanceof TargetTableEntry) {
					final TargetTableEntry tte = (TargetTableEntry) element;
					if (this.information.isMandatory(tte.getMember(), tte.getParent())) {
						return Messages.PushDownRefactoring_cantModifyMandatory;
					}
					return null;
				}
			}
			return null;
		}



		@Override
		public void dispose() {
		}



		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	
	
	private final PushDownInformation information;
	private TreeViewer treeViewer;
	
	
	PushDownTargetClassControl(Composite parent, int style, 
			PushDownInformation information) {
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
		
		UIHelper.newLabel(this, Messages.PushDownRefactoring_selectTargetClasses, 2);
		
		this.createTreeViewer();
	}

	
	
	private void createTreeViewer() {
		this.treeViewer = new TreeViewer(this, 
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.verticalAlignment = SWT.FILL;
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		this.treeViewer.getTree().setLayoutData(grid);
		
	    final TreeColumn member = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
	    member.setAlignment(SWT.LEFT);
	    member.setText(Messages.PullUpRefactoring_columnMember);
	    member.setWidth(200);
	    
	    final TreeColumn action = new TreeColumn(this.treeViewer.getTree(), SWT.CENTER);
	    action.setAlignment(SWT.LEFT);
	    action.setText(Messages.PushDownRefactoring_columnInsert);
	    action.setWidth(120);
	    
	    final TreeColumn info = new TreeColumn(this.treeViewer.getTree(), SWT.CENTER);
	    info.setAlignment(SWT.LEFT);
	    info.setText(Messages.PushDownRefactoring_columnInfo);
	    info.setWidth(210);
	}
	
	
	
	private void initializeTreeViewer() {
		if (this.treeViewer == null) {
			this.createTreeViewer();
		}
		
	    final ITreeContentProvider content = new TargetClassContentProvider(this.information);
	    this.treeViewer.getTree().setHeaderVisible(true);
	    this.treeViewer.setLabelProvider(new TreeLabelProvider(this.information));
		this.treeViewer.setContentProvider(content);
		
		final InheritanceLevel[] roots = 
				new InheritanceLevel[this.information.getTargets().size()];
		this.information.getTargets().toArray(roots);
		this.treeViewer.setInput(roots);
		this.treeViewer.expandAll();
		
		final String[] columnProps = {"", TargetTableEntry.TARGET_ACTION, ""}; //$NON-NLS-1$ //$NON-NLS-2$
	    this.treeViewer.setColumnProperties(columnProps);
	    this.treeViewer.setCellModifier(new MyCellModifier(this.treeViewer, content, this.information));
	    this.treeViewer.setCellEditors(new CellEditor[] { 
    		null, 
    		new ComboBoxCellEditor(this.treeViewer.getTree(), 
    				TargetTableEntry.PER_CLASS_TARGET_ACTIONS.getSupported(), 
    				SWT.READ_ONLY),
			null
		} );
	}
	
	
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			this.information.resetPerClassActions();
			this.initializeTreeViewer();
		}
		super.setVisible(visible);
	}
}
