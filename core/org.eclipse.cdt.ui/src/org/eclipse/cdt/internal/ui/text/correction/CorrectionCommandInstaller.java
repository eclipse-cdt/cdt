/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.LegacyHandlerSubmissionExpression;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;

public class CorrectionCommandInstaller {
	/**
	 * All correction commands must start with the following prefix.
	 */
	public static final String COMMAND_PREFIX= "org.eclipse.jdt.ui.correction."; //$NON-NLS-1$
	
	/**
	 * Commands for quick assist must have the following suffix.
	 */
	public static final String ASSIST_SUFFIX= ".assist"; //$NON-NLS-1$
	
	private List<IHandlerActivation> fCorrectionHandlerActivations;
	
	public CorrectionCommandInstaller() {
		fCorrectionHandlerActivations= null;
	}
	
	public void registerCommands(CEditor editor) {
		IWorkbench workbench= PlatformUI.getWorkbench();
		ICommandService commandService= workbench.getAdapter(ICommandService.class);
		IHandlerService handlerService= workbench.getAdapter(IHandlerService.class);
		if (commandService == null || handlerService == null) {
			return;
		}
		
		if (fCorrectionHandlerActivations != null) {
			CUIPlugin.logError("Correction handler activations not released"); //$NON-NLS-1$
		}
		fCorrectionHandlerActivations= new ArrayList<IHandlerActivation>();
		
		@SuppressWarnings("unchecked")
		Collection<String> definedCommandIds= commandService.getDefinedCommandIds();
		for (Object element : definedCommandIds) {
			String id= (String) element;
			if (id.startsWith(COMMAND_PREFIX)) {
				boolean isAssist= id.endsWith(ASSIST_SUFFIX);
				CorrectionCommandHandler handler= new CorrectionCommandHandler(editor, id, isAssist);
				IHandlerActivation activation= handlerService.activateHandler(id, handler, new LegacyHandlerSubmissionExpression(null, null, editor.getSite()));
				fCorrectionHandlerActivations.add(activation);
			}
		}
	}
	
	public void deregisterCommands() {
		IHandlerService handlerService= PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		if (handlerService != null && fCorrectionHandlerActivations != null) {
			handlerService.deactivateHandlers(fCorrectionHandlerActivations);
			fCorrectionHandlerActivations= null;
		}
	}
}
