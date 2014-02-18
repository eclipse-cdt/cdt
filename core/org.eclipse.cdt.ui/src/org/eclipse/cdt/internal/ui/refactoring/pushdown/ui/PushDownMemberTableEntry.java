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

import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.MemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.Messages;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;



public class PushDownMemberTableEntry extends MemberTableEntry {
	
	private final static String[] COLUMN_NAMES = {
		Messages.PullUpRefactoring_columnMember,
		Messages.PullUpRefactoring_columnCurrentVisibility,
		Messages.PullUpRefactoring_columnTargetVisibility,
		Messages.PullUpRefactoring_columnTargetAction
	};
	
	
	
	private final static ColumnLabelProvider[] COLUMN_LABELS = {
		DECLARATOR_LABEL_PROVIDER,
		CURRENT_VISIBILITY_LABEL_PROVIDER,
		TARGET_VISIBILITY_LABEL_PROVIDER,
		TARGET_ACTION_LABEL_PROVIDER
	};
	
	
	
	public final static void createColumns(final TableViewer viewer) {
		final int bounds[] = {200, 100, 100, 100};
		final EditingSupport editing[] = {
			null,
			null,
			new VisibilityEditingSupport(viewer),
			new TargetActionEditingSupport(viewer, viewer.getTable())
		};
		
		for (int i = 0; i < COLUMN_NAMES.length; ++i) {
			final TableViewerColumn col = new TableViewerColumn(viewer, SWT.CHECK);
			final TableColumn column = col.getColumn();
			column.setText(COLUMN_NAMES[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
			col.setLabelProvider(COLUMN_LABELS[i]);
			col.setEditingSupport(editing[i]);
		}
	}
	
	/** Supported actions for pushing down a method */
	private final static TargetActions SUPPORTED_ACTIONS_METHOD = new TargetActions(
			TargetActions.NONE,
			TargetActions.PUSH_DOWN, 
			TargetActions.LEAVE_VIRTUAL);

	/** Supported actions for pushing down a field */
	private final static TargetActions SUPPORTED_ACTIONS_FIELD = new TargetActions(
			TargetActions.NONE,
			TargetActions.PUSH_DOWN);

	/** Key for {@link PropertyChangeSupport} */ 
	public final static String ACTION_PER_CLASS = "actionPerClass"; //$NON-NLS-1$
	
	
	private final Map<InheritanceLevel, String> actionsPerClass;
	
	
	public PushDownMemberTableEntry(ICPPMember member) {
		super(member, 
				(member instanceof ICPPField) 
					? SUPPORTED_ACTIONS_FIELD 
					: SUPPORTED_ACTIONS_METHOD, 
			TargetActions.NONE);
		this.actionsPerClass = new HashMap<InheritanceLevel, String>();
	}

	
	
	public void setActionForClass(InheritanceLevel cls, String action) {
		this.pcSupport.firePropertyChange(ACTION_PER_CLASS, 
				getAction(cls), action);
		this.actionsPerClass.put(cls, action);
	}
	
	

	public Map<InheritanceLevel, String> getActionsPerClass() {
		return this.actionsPerClass;
	}
	
	
	
	public String getAction(InheritanceLevel cls) {
		return this.actionsPerClass.get(cls);
	}
}
