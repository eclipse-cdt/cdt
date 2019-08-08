/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Creates a breakpoint interactively, that is with user input as well as context
 * information gathered from editor location. This action delegate can be
 * contributed to an editor with the <code>editorActions</code> extension point.
 * This action is a factory that creates another action that performs the
 * actual breakpoint toggling. The created action acts on the editor's
 * <code>IToggleBreakpointsTargetCExtension</code> to create the breakpoint.
 * <p>
 * This action should be contributed to a vertical ruler context menu via the
 * <code>popupMenus</code> extension point, by referencing the ruler's context
 * menu identifier in the <code>targetID</code> attribute.
 * <pre>
 * &lt;extension point="org.eclipse.ui.popupMenus"&gt;
 *   &lt;viewerContribution
 *     targetID="example.rulerContextMenuId"
 *     id="example.RulerPopupActions"&gt;
 *       &lt;action
 *         label="Toggle Breakpoint"
 *         class="org.eclipse.debug.ui.actions.RulerCreateBreakpointInteractiveActionDelegate"
 *         menubarPath="additions"
 *         id="example.rulerContextMenu.createBreakpointAction"&gt;
 *       &lt;/action&gt;
 *   &lt;/viewerContribution&gt;
 * </pre>
 * </p>
 * <p>
 * Clients may refer to this class as an action delegate in plug-in XML.
 * </p>
 * @see IToggleBreakpointsTargetCExtension
 * @since 7.2
 * @noextend This class is not intended to be sub-classed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CAddBreakpointInteractiveRulerActionDelegate extends AbstractRulerActionDelegate {

	private IEditorPart fEditor = null;
	private CAddBreakpointInteractiveRulerAction fDelegate = null;

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		fDelegate = new CAddBreakpointInteractiveRulerAction(editor, null, rulerInfo);
		return fDelegate;
	}

	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		if (fEditor != null) {
			if (fDelegate != null) {
				fDelegate.dispose();
				fDelegate = null;
			}
		}
		fEditor = targetEditor;
		super.setActiveEditor(callerAction, targetEditor);
	}

	@Override
	public void init(IAction action) {
	}

	@Override
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
		fDelegate = null;
		fEditor = null;
	}
}
