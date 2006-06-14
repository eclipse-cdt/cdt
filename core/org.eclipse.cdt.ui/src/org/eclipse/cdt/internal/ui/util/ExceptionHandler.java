/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;


import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.internal.ui.CStatusConstants;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The default exception handler shows an error dialog when one of its handle methods
 * is called. If the passed exception is a <code>CoreException</code> an error dialog
 * pops up showing the exception's status information. For a <code>InvocationTargetException</code>
 * a normal message dialog pops up showing the exception's message. Additionally the exception
 * is written to the platform log.
 */
public class ExceptionHandler {

	private static ExceptionHandler fgInstance= new ExceptionHandler();
	
	/**
	 * Logs the given exception using the platform's logging mechanism. The exception is
	 * logged as an error with the error code <code>JavaStatusConstants.INTERNAL_ERROR</code>.
	 */
	public static void log(Throwable t, String message) {
		CUIPlugin.getDefault().log(new Status(IStatus.ERROR, CUIPlugin.getPluginId(), 
			CStatusConstants.INTERNAL_ERROR, message, t));
	}
	
	/**
	 * Handles the given <code>CoreException</code>. The workbench shell is used as a parent
	 * for the dialog window.
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, String title, String message) {
		handle(e, CUIPlugin.getActiveWorkbenchShell(), title, message);
	}
	
	/**
	 * Handles the given <code>CoreException</code>. 
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param parent the dialog window's parent shell
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, Shell parent, String title, String message) {
		fgInstance.perform(e, parent, title, message);
	}
	
	/**
	 * Handles the given <code>InvocationTargetException</code>. The workbench shell is used 
	 * as a parent for the dialog window.
	 * 
	 * @param e the <code>InvocationTargetException</code> to be handled
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(InvocationTargetException e, String title, String message) {
		handle(e, CUIPlugin.getActiveWorkbenchShell(), title, message);
	}
	
	/**
	 * Handles the given <code>InvocationTargetException</code>. 
	 * 
	 * @param e the <code>InvocationTargetException</code> to be handled
	 * @param parent the dialog window's parent shell
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(InvocationTargetException e, Shell parent, String title, String message) {
		fgInstance.perform(e, parent, title, message);
	}

	//---- Hooks for subclasses to control exception handling ------------------------------------
	
	protected void perform(CoreException e, Shell shell, String title, String message) {
		CUIPlugin.getDefault().log(e);
		IStatus status= e.getStatus();
		if (status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			displayMessageDialog(e, e.getMessage(), shell, title, message);
		}
	}

	protected void perform(InvocationTargetException e, Shell shell, String title, String message) {
		Throwable target= e.getTargetException();
		if (target instanceof CoreException) {
			perform((CoreException)target, shell, title, message);
		} else {
			CUIPlugin.getDefault().log(e);
			if (e.getMessage() != null && e.getMessage().length() > 0) {
				displayMessageDialog(e, e.getMessage(), shell, title, message);
			} else {
				displayMessageDialog(e, target.getMessage(), shell, title, message);
			}
		}
	}

	//---- Helper methods -----------------------------------------------------------------------
	
	private void displayMessageDialog(Throwable t, String exceptionMessage, Shell shell, String title, String message) {
		StringWriter msg= new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
		}
		if (exceptionMessage == null || exceptionMessage.length() == 0)
			msg.write(CUIMessages.getString("ExceptionDialog.seeErrorLogMessage")); //$NON-NLS-1$
		else
			msg.write(exceptionMessage);
		MessageDialog.openError(shell, title, msg.toString());			
	}	
}
