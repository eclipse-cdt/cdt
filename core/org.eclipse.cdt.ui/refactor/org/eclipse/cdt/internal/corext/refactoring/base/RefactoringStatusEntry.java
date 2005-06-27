/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;

import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * An immutable tuple (message, severity) representing an entry in the list in 
 * <code>RefactoringStatus</code>.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */
public class RefactoringStatusEntry{
	
	private final String fMessage;
	private final int fSeverity;
	private final Context fContext;
	private final Object fData;
	private final int fCode;
	
	public RefactoringStatusEntry(String msg, int severity, Context context, Object data, int code){
		Assert.isTrue(severity == RefactoringStatus.INFO 
				   || severity == RefactoringStatus.WARNING
				   || severity == RefactoringStatus.ERROR
				   || severity == RefactoringStatus.FATAL);
		Assert.isNotNull(msg);
		fMessage= msg;
		fSeverity= severity;
		fContext= context;
		fData= data;
		fCode= code;
	}

	/**
	 * Creates an entry with the given severity.
	 * @param msg message
	 * @param severity severity
	 * @param context a context which can be used to show more detailed information
	 * 	about this error in the UI
	 */
	public RefactoringStatusEntry(String msg, int severity, Context context){
		this(msg, severity, context, null, RefactoringStatusCodes.NONE);
	}
	
	/**
	 * Creates an entry with the given severity. The corresponding resource and source range are set to <code>null</code>.
	 * @param severity severity
	 * @param msg message
	 */
	public RefactoringStatusEntry(String msg, int severity) {
		this(msg, severity, null);
	}
	
	/**
	 * Creates an entry with <code>RefactoringStatus.INFO</code> status.
	 * @param msg message
	 */
	public static RefactoringStatusEntry createInfo(String msg) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.INFO);
	}
	
	/**
	 * Creates an entry with <code>RefactoringStatus.INFO</code> status.
	 * @param msg message
	 */
	public static RefactoringStatusEntry createInfo(String msg, Context context) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.INFO, context);
	}

	/**
	 * Creates an entry with <code>RefactoringStatus.WARNING</code> status.
	 * @param msg message
	 */	
	public static RefactoringStatusEntry createWarning(String msg) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.WARNING);
	}

	/**
	 * Creates an entry with <code>RefactoringStatus.WARNING</code> status.
	 * @param msg message
	 */	
	public static RefactoringStatusEntry createWarning(String msg, Context context) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.WARNING, context);
	}
	
	/**
	 * Creates an entry with <code>RefactoringStatus.ERROR</code> status.
	 * @param msg message
	 */	
	public static RefactoringStatusEntry createError(String msg) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.ERROR);
	}

	/**
	 * Creates an entry with <code>RefactoringStatus.ERROR</code> status.
	 * @param msg message
	 */		
	public static RefactoringStatusEntry createError(String msg, Context context) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.ERROR, context);
	}
	
	/**
	 * Creates an entry with <code>RefactoringStatus.FATAL</code> status.
	 * @param msg message
	 */	
	public static RefactoringStatusEntry createFatal(String msg) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.FATAL);
	}

	/**
	 * Creates an entry with <code>RefactoringStatus.FATAL</code> status.
	 * @param msg message
	 */	
	public static RefactoringStatusEntry createFatal(String msg, Context context) {
		return new RefactoringStatusEntry(msg, RefactoringStatus.FATAL, context);
	}
	
	/**
	 * @return <code>true</code> iff (severity == <code>RefactoringStatus.FATAL</code>).
	 */
	public boolean isFatalError() {
		return fSeverity == RefactoringStatus.FATAL;
	}
	
	/**
	 * @return <code>true</code> iff (severity == <code>RefactoringStatus.ERROR</code>).
	 */
	public boolean isError() {
		return fSeverity == RefactoringStatus.ERROR;
	}
	
	/**
	 * @return <code>true</code> iff (severity == <code>RefactoringStatus.WARNING</code>).
	 */
	public boolean isWarning() {
		return fSeverity == RefactoringStatus.WARNING;
	}
	
	/**
	 * @return <code>true</code> iff (severity == <code>RefactoringStatus.INFO</code>).
	 */
	public boolean isInfo() {
		return fSeverity == RefactoringStatus.INFO;
	}

	/**
	 * @return message.
	 */
	public String getMessage() {
		return fMessage;
	}

	/**
	 * @return severity level.
	 * @see RefactoringStatus#INFO
	 * @see RefactoringStatus#WARNING
	 * @see RefactoringStatus#ERROR
	 * @see RefactoringStatus#FATAL
	 */	
	public int getSeverity() {
		return fSeverity;
	}

	/**
	 * Returns the context which can be used to show more detailed information
	 * regarding this status entry in the UI. The method may return <code>null
	 * </code> indicating that no context is available.
	 * 
	 * @return the status entry's context
	 */
	public Context getContext() {
		return fContext;
	}

	public Object getData() {
		return fData;
	}

	public int getCode() {
		return fCode;
	}
	
	/**
	 * Converts this <tt>RefactoringStatusEntry</tt> into an <tt>IStatus</tt>.
	 * The mapping is done as follows: 
	 * <ul>
	 *   <li>Fatal entries are mapped to <code>IStatus.ERROR</code>.
	 *   </li>
	 *   <li>Error and warning entries are mapped to <code>IStatus.WARNING</code>.
	 *   </li>
	 *   <li>Information entries are mapped to <code>IStatus.INFO</code>.</li>
	 * </ul>
	 * @return IStatus
	 */
	public IStatus asStatus () {
		int statusSeverity= IStatus.ERROR;
		switch (fSeverity) {
			case RefactoringStatus.OK:
				statusSeverity= IStatus.OK;
				break;
			case RefactoringStatus.INFO:
				statusSeverity= IStatus.INFO;
				break;
			case RefactoringStatus.WARNING:
			case RefactoringStatus.ERROR:
				statusSeverity= IStatus.WARNING;
				break; 
		}
		return new Status(statusSeverity, CUIPlugin.getPluginId(), fCode, fMessage, null);
	}
	
	/* non java-doc
	 * for debugging only
	 */
	public String toString() {
		String contextString= fContext == null ? "<Unspecified context>": fContext.toString(); //$NON-NLS-1$
		return 	"\n" //$NON-NLS-1$
				+ RefactoringStatus.getSeverityString(fSeverity) 
				+ ": "  //$NON-NLS-1$
				+ fMessage 
				+ "\nContext: " //$NON-NLS-1$
				+ contextString
				+ "\nData: "  //$NON-NLS-1$
				+ getData()
				+"\ncode: "  //$NON-NLS-1$
				+ fCode
				+ "\n";  //$NON-NLS-1$
	}
}
