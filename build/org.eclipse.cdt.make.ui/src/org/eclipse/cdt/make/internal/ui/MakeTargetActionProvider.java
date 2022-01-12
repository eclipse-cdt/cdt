/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.ui.views.BuildTargetAction;
import org.eclipse.cdt.make.ui.views.CopyTargetAction;
import org.eclipse.cdt.make.ui.views.DeleteTargetAction;
import org.eclipse.cdt.make.ui.views.EditTargetAction;
import org.eclipse.cdt.make.ui.views.PasteTargetAction;
import org.eclipse.cdt.make.ui.views.RebuildLastTargetAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class MakeTargetActionProvider extends CommonActionProvider {

	private EditTargetAction editTargetAction;
	private CopyTargetAction copyTargetAction;
	private PasteTargetAction pasteTargetAction;
	private DeleteTargetAction deleteTargetAction;
	private BuildTargetAction buildTargetAction;
	private RebuildLastTargetAction buildLastTargetAction;

	private Clipboard clipboard;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);

		Shell shell = aSite.getViewSite().getShell();
		clipboard = new Clipboard(shell.getDisplay());

		editTargetAction = new EditTargetAction(aSite.getViewSite().getShell());
		pasteTargetAction = new PasteTargetAction(shell, clipboard);
		copyTargetAction = new CopyTargetAction(shell, clipboard, pasteTargetAction);
		deleteTargetAction = new DeleteTargetAction(shell);
		buildTargetAction = new BuildTargetAction(shell);
		buildLastTargetAction = new RebuildLastTargetAction();

		deleteTargetAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);

		aSite.getStructuredViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof IMakeTarget) {
					buildTargetAction.run();
				}
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		clipboard.dispose();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.add(editTargetAction);
		menu.add(new Separator());
		menu.add(copyTargetAction);
		menu.add(pasteTargetAction);
		menu.add(deleteTargetAction);
		menu.add(new Separator());
		menu.add(buildTargetAction);
		menu.add(buildLastTargetAction);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		TextActionHandler handler = new TextActionHandler(actionBars);
		handler.setCopyAction(copyTargetAction);
		handler.setPasteAction(pasteTargetAction);
		handler.setDeleteAction(deleteTargetAction);
	}

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);

		if (context != null) {
			IStructuredSelection selection = (IStructuredSelection) context.getSelection();
			editTargetAction.selectionChanged(selection);
			copyTargetAction.selectionChanged(selection);
			pasteTargetAction.selectionChanged(selection);
			deleteTargetAction.selectionChanged(selection);
			buildTargetAction.selectionChanged(selection);
			buildLastTargetAction.selectionChanged(selection);
		}
	}

}
