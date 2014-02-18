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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;


public abstract class MemberTableEntry implements HasActions {
	
	public final static ColumnLabelProvider DECLARATOR_LABEL_PROVIDER = 
			new ColumnLabelProvider() {
		
		
		@Override
		public String getText(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			
			return PullUpHelper.getMemberString(mte.member);
		}
		
		@Override
		public Image getImage(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			final boolean isField = mte.member instanceof ICPPField;
			
			String key = null;
			switch (mte.member.getVisibility()) {
			case ICPPASTVisibilityLabel.v_private:
				key = isField 
					? CDTSharedImages.IMG_OBJS_PRIVATE_FIELD
					: CDTSharedImages.IMG_OBJS_PRIVATE_METHOD;
				break;
			case ICPPASTVisibilityLabel.v_protected:
				key = isField 
					? CDTSharedImages.IMG_OBJS_PROTECTED_FIELD
					: CDTSharedImages.IMG_OBJS_PROTECTED_METHOD;
				break;
			case ICPPASTVisibilityLabel.v_public:
				key = isField 
					? CDTSharedImages.IMG_OBJS_PUBLIC_FIELD
					: CDTSharedImages.IMG_OBJS_PUBLIC_METHOD;
				break;
			default:
				throw new IllegalStateException();
			}

			return CDTSharedImages.getImage(key);
		}
	};
	
	
	
	private final static String[] VISIBILITIES = {
		Keywords.PUBLIC,
		Keywords.PROTECTED,
		Keywords.PRIVATE
	};
	
	
	public final static ColumnLabelProvider CURRENT_VISIBILITY_LABEL_PROVIDER = 
			new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			final VisibilityEnum v = mte.getCurrentVisibility();
			return v.toString();
		}
	};
	
	
	
	public final static ColumnLabelProvider TARGET_VISIBILITY_LABEL_PROVIDER = 
			new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			final VisibilityEnum v = mte.getTargetVisibility();
			return v.toString();
		}
	};
	
	
	
	public final static class VisibilityEditingSupport extends EditingSupport {
		
		private final TableViewer viewer;
	
		public VisibilityEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}
			
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			final ComboBoxCellEditor cce = new ComboBoxCellEditor(
					this.viewer.getTable(), VISIBILITIES, SWT.READ_ONLY);
			cce.setValue(0);
			return cce;
		}

		@Override
		protected Object getValue(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			return mte.getTargetVisibility().getVisibilityLabelValue();
		}

		
		@Override
		protected void setValue(Object element, Object value) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			final Integer idx = ((Integer) value);
			final VisibilityEnum v = VisibilityEnum.getEnumForStringRepresentation(VISIBILITIES[idx]);
			mte.setTargetVisibility(v);
			this.viewer.update(element, null);
		}
	}
	
	
	
	public final static ColumnLabelProvider TARGET_ACTION_LABEL_PROVIDER = 
			new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			return mte.getSelectedAction().toString();
		}
	};
	
	
	
	public final static class TargetActionEditingSupport extends EditingSupport {
		private final TableViewer viewer;
		private final Composite parent;
		
		public TargetActionEditingSupport(TableViewer viewer, Composite parent) {
			super(viewer);
			this.viewer = viewer;
			this.parent = parent;
		}
		
		
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			final ComboBoxCellEditor cce = new ComboBoxCellEditor(
					parent, mte.getActions().getSupported(), SWT.READ_ONLY);
			cce.setValue(0);
			return cce;
		}
		
		

		@Override
		protected Object getValue(Object element) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			return mte.getActions().index(mte.getSelectedAction());
		}

		
		
		@Override
		protected void setValue(Object element, Object value) {
			final MemberTableEntry mte = (MemberTableEntry) element;
			final int idx = (Integer) value;
			if (idx >= 0) {
				final String action = mte.getActions().getSupported()[idx];
				mte.setSelectedAction(action);
				this.viewer.update(element, null);
			}
		}
	}
	

	
	/** Key for {@link PropertyChangeSupport} */ 
	public final static String VISIBILITY = "visibility"; //$NON-NLS-1$
	
	/** Key for {@link PropertyChangeSupport} */ 
	public final static String TARGET_ACTION = "targetAction"; //$NON-NLS-1$
	
	
	protected final ICPPMember member;
	protected final VisibilityEnum currentVisibility;
	protected final PropertyChangeSupport pcSupport;
	protected final TargetActions actions;
	protected VisibilityEnum targetVisibility;
	private String selectedTargetAction;
	
	
	
	public MemberTableEntry(ICPPMember member, TargetActions actions, 
			String defaultAction) {
		this.member = member;
		this.selectedTargetAction = defaultAction;
		this.actions = actions;
		this.currentVisibility = VisibilityEnum.from(member.getVisibility());
		this.targetVisibility = this.currentVisibility;
		this.pcSupport = new PropertyChangeSupport(this);
	}
	
	
	
	public VisibilityEnum getTargetVisibility() {
		return this.targetVisibility;
	}
	
	
	
	public void setTargetVisibility(VisibilityEnum visibility) {
		this.pcSupport.firePropertyChange(VISIBILITY, this.targetVisibility, 
				this.targetVisibility = visibility);
	}
	
	
	
	@Override
	public void setSelectedAction(String action) {
		if (this.actions.index(action) < 0) {
			throw new IllegalArgumentException(
					"unsupported action: '" + action + "'");  //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.pcSupport.firePropertyChange(TARGET_ACTION, this.selectedTargetAction, 
				this.selectedTargetAction = action);
	}
	
	
	
	@Override
	public String getSelectedAction() {
		return this.selectedTargetAction;
	}
	
	
	
	@Override
	public TargetActions getActions() {
		return this.actions;
	}
	
	
	
	public ICPPMember getMember() {
		return this.member;
	}
	
	
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcSupport.addPropertyChangeListener(listener);
	}
	
	
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcSupport.removePropertyChangeListener(listener);
	}



	public VisibilityEnum getCurrentVisibility() {
		return this.currentVisibility;
	}
}
