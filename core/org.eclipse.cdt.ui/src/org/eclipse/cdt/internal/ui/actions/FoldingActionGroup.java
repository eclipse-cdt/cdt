/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.jface.action.IMenuManager;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;


/**
 * Groups the JDT folding actions.
 *  
 * @since 3.0
 */
public class FoldingActionGroup extends ActionGroup {
	private ProjectionViewer fViewer;
	
	private TextOperationAction fToggle;
	private TextOperationAction fExpand;
	private TextOperationAction fCollapse;
	private TextOperationAction fExpandAll;

	private IProjectionListener fProjectionListener;
	
	/**
	 * Creates a new projection action group for <code>editor</code>. If the
	 * supplied viewer is not an instance of <code>ProjectionViewer</code>, the
	 * action group is disabled.
	 * 
	 * @param editor the text editor to operate on
	 * @param viewer the viewer of the editor
	 */
	public FoldingActionGroup(ITextEditor editor, ITextViewer viewer) {
		if (viewer instanceof ProjectionViewer) {
			fViewer= (ProjectionViewer) viewer;
			
			fProjectionListener= new IProjectionListener() {

				public void projectionEnabled() {
					update();
				}

				public void projectionDisabled() {
					update();
				}
			};
			
			fViewer.addProjectionListener(fProjectionListener);
			
			fToggle= new TextOperationAction(ActionMessages.getResourceBundle(), "Projection.Toggle.", editor, ProjectionViewer.TOGGLE, true); //$NON-NLS-1$
			fToggle.setChecked(true);
			fToggle.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
			editor.setAction("FoldingToggle", fToggle); //$NON-NLS-1$
			
			fExpandAll= new TextOperationAction(ActionMessages.getResourceBundle(), "Projection.ExpandAll.", editor, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
			fExpandAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
			editor.setAction("FoldingExpandAll", fExpandAll); //$NON-NLS-1$
			
			fExpand= new TextOperationAction(ActionMessages.getResourceBundle(), "Projection.Expand.", editor, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
			fExpand.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
			editor.setAction("FoldingExpand", fExpand); //$NON-NLS-1$
			
			fCollapse= new TextOperationAction(ActionMessages.getResourceBundle(), "Projection.Collapse.", editor, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
			fCollapse.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
			editor.setAction("FoldingCollapse", fCollapse); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns <code>true</code> if the group is enabled. 
	 * <pre>
	 * Invariant: isEnabled() <=> fViewer and all actions are != null.
	 * </pre>
	 * 
	 * @return <code>true</code> if the group is enabled
	 */
	protected boolean isEnabled() {
		return fViewer != null;
	}
	
	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		if (isEnabled()) {
			fViewer.removeProjectionListener(fProjectionListener);
			fViewer= null;
		}
		super.dispose();
	}
	
	/**
	 * Updates the actions.
	 */
	protected void update() {
		if (isEnabled()) {
			fToggle.update();
			fToggle.setChecked(fViewer.getProjectionAnnotationModel() != null);
			fExpand.update();
			fExpandAll.update();
			fCollapse.update();
		}
	}
	
	/**
	 * Fills the menu with all folding actions.
	 * 
	 * @param manager the menu manager for the folding submenu
	 */
	public void fillMenu(IMenuManager manager) {
		if (isEnabled()) {
			update();
			manager.add(fToggle);
			manager.add(fExpandAll);
			manager.add(fExpand);
			manager.add(fCollapse);
		}
	}
	
	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	public void updateActionBars() {
		update();
	}
}
