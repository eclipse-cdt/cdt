/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * Groups the CDT folding actions.
 *
 * @since 3.0
 */
public class FoldingActionGroup extends ActionGroup {

	private static abstract class PreferenceAction extends ResourceAction implements IUpdate {
		PreferenceAction(ResourceBundle bundle, String prefix, int style) {
			super(bundle, prefix, style);
			update();
		}
	}

	private class FoldingAction extends PreferenceAction {

		FoldingAction(ResourceBundle bundle, String prefix) {
			super(bundle, prefix, IAction.AS_PUSH_BUTTON);
		}

		@Override
		public void update() {
			setEnabled(FoldingActionGroup.this.isEnabled() && fViewer.isProjectionMode());
		}

	}

	private ProjectionViewer fViewer;
	private IProjectionListener fProjectionListener;

	private TextOperationAction fToggle;
	private TextOperationAction fExpand;
	private TextOperationAction fCollapse;
	private TextOperationAction fExpandAll;

	// since 4.0
	private TextOperationAction fCollapseAll;
	private PreferenceAction fRestoreDefaults;

	/**
	 * Creates a new projection action group for <code>editor</code>. If the
	 * supplied viewer is not an instance of <code>ProjectionViewer</code>, the
	 * action group is disabled.
	 *
	 * @param editor the text editor to operate on
	 * @param viewer the viewer of the editor
	 */
	public FoldingActionGroup(final ITextEditor editor, ITextViewer viewer) {
		if (!(viewer instanceof ProjectionViewer)) {
			fToggle = null;
			fExpand = null;
			fCollapse = null;
			fExpandAll = null;
			fCollapseAll = null;
			fRestoreDefaults = null;
			fProjectionListener = null;
			return;
		}

		fViewer = (ProjectionViewer) viewer;

		fProjectionListener = new IProjectionListener() {
			@Override
			public void projectionEnabled() {
				update();
			}

			@Override
			public void projectionDisabled() {
				update();
			}
		};

		fViewer.addProjectionListener(fProjectionListener);

		fToggle = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.Toggle.", editor, //$NON-NLS-1$
				ProjectionViewer.TOGGLE, true);
		fToggle.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
		editor.setAction("FoldingToggle", fToggle); //$NON-NLS-1$

		fExpandAll = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.ExpandAll.", editor, //$NON-NLS-1$
				ProjectionViewer.EXPAND_ALL, true);
		fExpandAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
		editor.setAction("FoldingExpandAll", fExpandAll); //$NON-NLS-1$

		fCollapseAll = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.CollapseAll.", editor, //$NON-NLS-1$
				ProjectionViewer.COLLAPSE_ALL, true);
		fCollapseAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
		editor.setAction("FoldingCollapseAll", fCollapseAll); //$NON-NLS-1$

		fExpand = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.Expand.", editor, //$NON-NLS-1$
				ProjectionViewer.EXPAND, true);
		fExpand.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
		editor.setAction("FoldingExpand", fExpand); //$NON-NLS-1$

		fCollapse = new TextOperationAction(FoldingMessages.getResourceBundle(), "Projection.Collapse.", editor, //$NON-NLS-1$
				ProjectionViewer.COLLAPSE, true);
		fCollapse.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
		editor.setAction("FoldingCollapse", fCollapse); //$NON-NLS-1$

		fRestoreDefaults = new FoldingAction(FoldingMessages.getResourceBundle(), "Projection.Restore.") { //$NON-NLS-1$
			@Override
			public void run() {
				if (editor instanceof CEditor) {
					CEditor cEditor = (CEditor) editor;
					cEditor.resetProjection();
				}
			}
		};
		fRestoreDefaults.setActionDefinitionId(IFoldingCommandIds.FOLDING_RESTORE);
		editor.setAction("FoldingRestore", fRestoreDefaults); //$NON-NLS-1$
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
	@Override
	public void dispose() {
		if (isEnabled()) {
			fViewer.removeProjectionListener(fProjectionListener);
			fViewer = null;
		}
		super.dispose();
	}

	/**
	 * Updates the actions.
	 */
	protected void update() {
		if (isEnabled()) {
			fToggle.update();
			fToggle.setChecked(fViewer.isProjectionMode());
			fExpand.update();
			fExpandAll.update();
			fCollapse.update();
			fCollapseAll.update();
			fRestoreDefaults.update();
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
			manager.add(fCollapseAll);
			manager.add(fRestoreDefaults);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() {
		update();
	}
}
