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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpInformation;

public class PullUpMemberTableEntry extends MemberTableEntry {
	
	
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
	
	/** Supported actions for pulling up a method */
	private final static TargetActions SUPPORTED_ACTIONS_METHOD = new TargetActions(
			TargetActions.NONE,
			TargetActions.PULL_UP, 
			TargetActions.DECLARE_VIRTUAL);
	
	/** Special instance for instances that only remove a declaration */
	private final static TargetActions SUPPORTED_ACTIONS_REMOVE = new TargetActions(
			TargetActions.REMOVE_METHOD);
	

	/** Supported actions for pulling up a field */
	private final static TargetActions SUPPORTED_ACTIONS_FIELD = new TargetActions(
			TargetActions.NONE,
			TargetActions.PULL_UP);
	
	
	
	
	public static PullUpMemberTableEntry forRemoval(ICPPMethod method) {
		return new PullUpMemberTableEntry(method, true);
	}
	
	
	
	public PullUpMemberTableEntry(ICPPMember member) {
		super(member, 
				(member instanceof ICPPField) 
					? SUPPORTED_ACTIONS_FIELD 
					: SUPPORTED_ACTIONS_METHOD, 
			TargetActions.NONE);
	}
	
	
	
	/**
	 * This constructor may only be used for creating table entries that remove a method
	 * declaration (done in {@link PullUpInformation#generateTree()}.
	 * 
	 * @param method The method to remove.
	 * @param removeOnly this field is ignored
	 */
	private PullUpMemberTableEntry(ICPPMethod method, boolean removeOnly) {
		super(method, SUPPORTED_ACTIONS_REMOVE, TargetActions.REMOVE_METHOD);
	}
}
