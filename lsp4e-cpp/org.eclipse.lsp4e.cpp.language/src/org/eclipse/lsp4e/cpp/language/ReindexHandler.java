/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.ProjectSpecificLanguageServerWrapper;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.ui.PlatformUI;

/**
 * Temporary handler for temporary command in order to aid testing the indexing
 * support in Clangd.
 */
public class ReindexHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//FIXME; needs https://git.eclipse.org/r/#/c/101835/
//		IProject project = null;
//		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
//		if (selection instanceof IStructuredSelection) {
//			Object item = ((IStructuredSelection)selection).getFirstElement();
//			project = Adapters.adapt(item, IProject.class);
//		}
//		if (project == null) {
//			return null;
//		}
//
//		LanguageServerDefinition definition = LanguageServersRegistry.getInstance().getDefinition(CPPLanguageServer.ID);
//		try {
//			ProjectSpecificLanguageServerWrapper lsWrapperForConnection = LanguageServiceAccessor.getLSWrapperForConnection(project, definition, false);
//			if (lsWrapperForConnection != null) {
//				ExecuteCommandParams params = new ExecuteCommandParams("reindex", null);
//				lsWrapperForConnection.getServer().getWorkspaceService().executeCommand(params);
//			}
//		} catch (IOException e) {
//			return false;
//		}

		return null;
	}

}
