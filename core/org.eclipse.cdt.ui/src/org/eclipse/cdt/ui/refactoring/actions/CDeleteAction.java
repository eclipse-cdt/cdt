/*******************************************************************************
 * Copyright (c) 2015 Luis Yanes. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.cdt.ui.refactoring.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * Launches a delete refactoring.
 * 
 * @since 5.12
 * @noextend This class is not intended to be subclassed by clients.
 * @author Luis Yanes
 */

public class CDeleteAction extends RefactoringAction {
	
	public CDeleteAction(){
		super(Messages.CDeleteAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		throw new UnsupportedOperationException("Invoking this action from the editor is not supported");
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		final ICElement[] elements = new ICElement[] { elem };
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					CoreModel.getDefault().getCModel().delete(elements, false, monitor);
				} catch (CModelException e) {
					throw new InvocationTargetException(e);
				}				
			}
		};
		try {
			IRunnableContext context = new ProgressMonitorDialog(shellProvider.getShell());
			context.run(true, true, runnable);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, Messages.CDeleteAction_error_title, Messages.CDeleteAction_error_message); 
		} catch (InterruptedException e) {
			// Safely ignore
		}
	}
}
