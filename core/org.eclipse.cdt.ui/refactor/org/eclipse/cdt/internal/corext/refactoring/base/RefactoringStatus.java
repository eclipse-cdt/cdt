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



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.core.runtime.IStatus;


/**
 * A <code>RefactoringStatus</code> object represents the outcome of a precondition checking
 * operation.
 * It keeps a list of <code>RefactoringStatusEntries</code>.
 * Clients can instantiate.
 * This class is not intented to be subclassed.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */ 
public class RefactoringStatus {
	
	public static final int OK= 0;
	public static final int INFO= 1;
	public static final int WARNING= 2;
	public static final int ERROR= 3;
	public static final int FATAL= 4;
	
	private List fEntries;
	private int fSeverity= OK;
	
	public RefactoringStatus(){
		fEntries= new ArrayList(0);
	}
	
	/**
	 * Creates a <code>RefactorngStatus</code> with one INFO entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createInfoStatus(String msg){
		return createStatus(INFO, msg); 
	}
	
	/**
	 * Creates a <code>RefactorngStatus</code> with one INFO entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createInfoStatus(String msg, Context context){
		return createStatus(INFO, msg, context); 
	}
	
	/**
	 * Creates a <code>RefactorngStatus</code> with one WARNING entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createWarningStatus(String msg){
		return createStatus(WARNING, msg); 
	}

	/**
	 * Creates a <code>RefactorngStatus</code> with one WARNING entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createWarningStatus(String msg, Context context){
		return createStatus(WARNING, msg, context);  
	}
	
	/**
	 * Creates a <code>RefactorngStatus</code> with one ERROR entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createErrorStatus(String msg){
		return createStatus(ERROR, msg); 
	}

	/**
	 * Creates a <code>RefactorngStatus</code> with one ERROR entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createErrorStatus(String msg, Context context){
		return createStatus(ERROR, msg, context);  
	}
		
	/**
	 * Creates a <code>RefactorngStatus</code> with one FATAL entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg){
		return createStatus(FATAL, msg); 
	}

	/**
	 * Creates a <code>RefactorngStatus</code> with one FATAL entry.
	 * This is a convenience method.
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg, Context context){
		return createStatus(FATAL, msg, context); 
	}

	/**
	 * Creates a <code>RefactorngStatus</code> from the given <code>IStatus</code>
	 */	
	public static RefactoringStatus create(IStatus status){
		if (status.isOK())
			return new RefactoringStatus();
		
		if (! status.isMultiStatus()){
			switch (status.getSeverity()){
				case IStatus.INFO:
					return RefactoringStatus.createWarningStatus(status.getMessage());
				case IStatus.WARNING:
					return RefactoringStatus.createErrorStatus(status.getMessage());
				case IStatus.ERROR:
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
				default:	
					return new RefactoringStatus();
			}
		} else {
			IStatus[] children= status.getChildren();
			RefactoringStatus result= new RefactoringStatus();
			for (int i= 0; i < children.length; i++) {
				result.merge(RefactoringStatus.create(children[i]));
			}
			return result;
		}
	}

	/*
	 * @see RefactoringStatusCodes
	 */
	public static RefactoringStatus createStatus(int severity, String msg, Context context, Object data, int code) {
		RefactoringStatus result= new RefactoringStatus(); 
		result.fEntries.add(new RefactoringStatusEntry(msg, severity, context, data, code));
		result.fSeverity= severity;
		return result;
	}
	
	public static RefactoringStatus createStatus(int severity, String msg, Context context) {
		return createStatus(severity, msg, context, null, RefactoringStatusCodes.NONE);
	}
	
	public static RefactoringStatus createStatus(int severity, String msg){
		return createStatus(severity, msg, null);
	}
	
	/**
	 * Adds an info to this status.
	 * If the current severity was <code>OK</code> it will be changed to <code>INFO</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 */ 
	public void addInfo(String msg){
		addInfo(msg, null);
	}
	
	/**
	 * Adds an info to this status.
	 * If the current severity was <code>OK</code> it will be changed to <code>INFO</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 */ 
	public void addInfo(String msg, Context context){
		fEntries.add(RefactoringStatusEntry.createInfo(msg, context));
		fSeverity= Math.max(fSeverity, INFO);
	}
	
	/**
	 * Adds a warning to this status.
	 * If the current severity was <code>OK</code> or <code>INFO</code> it will be changed to <code>WARNING</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 * @see #WARNING
	 */
	public void addWarning(String msg){
		addWarning(msg, null);
	}
	
	/**
	 * Adds a warning to this status.
	 * If the current severity was <code>OK</code> or <code>INFO</code> it will be changed to <code>WARNING</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 * @see #WARNING
	 */
	public void addWarning(String msg, Context context){
		fEntries.add(RefactoringStatusEntry.createWarning(msg, context));
		fSeverity= Math.max(fSeverity, WARNING);
	}
	
	
	/**
	 * Adds an error to this status.
	 * If the current severity was <code>OK</code>, <code>INFO</code> or <code>WARNING</code>
	 * it will be changed to <code>ERROR</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 * @see #WARNING
	 * @see #ERROR
	 */	
	public void addError(String msg){
		addError(msg, null);
	}

	/**
	 * Adds an error to this status.
	 * If the current severity was <code>OK</code>, <code>INFO</code> or <code>WARNING</code>
	 * it will be changed to <code>ERROR</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 * @see #WARNING
	 * @see #ERROR
	 */	
	public void addError(String msg, Context context){
		fEntries.add(RefactoringStatusEntry.createError(msg, context));
		fSeverity= Math.max(fSeverity, ERROR);
	}


	/**
	 * Adds a fatal error to this status.
	 * If the current severity was <code>OK</code>, <code>INFO</code>, <code>WARNING</code>
	 * or <code>ERROR</code> it will be changed to <code>FATAL</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 * @see #WARNING
	 * @see #ERROR
	 * @see #FATAL
	 */	
	public void addFatalError(String msg){
		addFatalError(msg, null);
	}
	
	/**
	 * Adds a fatal error to this status.
	 * If the current severity was <code>OK</code>, <code>INFO</code>, <code>WARNING</code>
	 * or <code>ERROR</code> it will be changed to <code>FATAL</code>.
	 * It will remain unchanged otherwise.
	 * @see #OK
	 * @see #INFO	 
	 * @see #WARNING
	 * @see #ERROR
	 * @see #FATAL
	 */	
	public void addFatalError(String msg, Context context){
		fEntries.add(RefactoringStatusEntry.createFatal(msg, context));
		fSeverity= Math.max(fSeverity, FATAL);
	}
	
	/**
	 * Adds an <code>RefactoringStatusEntry</code>.
	 * 
	 * @param entry the <code>RefactoringStatusEntry</code> to be added
	 */
	public void addEntry(RefactoringStatusEntry entry) {
		Assert.isNotNull(entry);
		fEntries.add(entry);
		fSeverity= Math.max(fSeverity, entry.getSeverity());
	}
	
	/**
	 * Returns <code>true</code> iff there were no errors, warings or infos added.
	 * @see #OK
	 * @see #INFO
 	 * @see #WARNING
 	 * @see #ERROR
	 */
	public boolean isOK(){
		return fSeverity == OK;
	}
	
	/**
	 * Returns <code>true</code> if the current severity is <code>FATAL</code>.
	 * @see #FATAL
	 */
	public boolean hasFatalError() {
		return fSeverity == FATAL;
	}
	
	/**
	 * Returns <code>true</code> if the current severity is <code>FATAL</code> or
	 * <code>ERROR</code>.
	 * @see #FATAL
	 * @see #ERROR
	 */
	public boolean hasError() {
		return fSeverity == FATAL || fSeverity == ERROR;
	}
	
	/**
	 * Returns <code>true</code> if the current severity is <code>FATAL</code>, 
	 * <code>ERROR</code> or <code>WARNING</code>.
	 * @see #FATAL
	 * @see #ERROR
	 * @see #WARNING
	 */
	public boolean hasWarning() {
		return fSeverity == FATAL || fSeverity == ERROR || fSeverity == WARNING;
	}
	
	/**
	 * Returns <code>true</code> if the status has an entry with the given code.
	 * Otherwise <code>false</code> is returned.
	 * 
	 * @param code the code of the <tt>RefactoringStatusEntry</tt>.
	 * @return <code>true</code> if the status has an entry with the given code.
	 * Otherwise <code>false</code> is returned.
	 */
	public boolean hasEntryWithCode(int code) {
		for (Iterator iter= fEntries.iterator(); iter.hasNext();) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry) iter.next();
			if (entry.getCode() == code)
				return true;
		}
		return false;
	}
	
	/**
	 * Merges the receiver and the parameter statuses.
	 * The resulting list of entries in the receiver will contain entries from both.
	 * The resuling severity in the reciver will be the more severe of its current severity
	 * and the parameter's severity.
	 * Merging with <code>null</code> is allowed - it has no effect.
	 * @see #getSeverity	 
	 */
	public void merge(RefactoringStatus that){
		if (that == null)
			return;
		fEntries.addAll(that.getEntries());
		fSeverity= Math.max(fSeverity, that.getSeverity());
	}


	/**
	 * Returns the current severity.
	 * Severities are ordered as follows: <code>OK < INFO < WARNING < ERROR</code>
	 */
	public int getSeverity(){
		return fSeverity;
	}
		
	/**
	 * Returns all entries.
	 * Returns a List of <code>RefactoringStatusEntries</code>.
	 * This list is empty if there are no entries.
	 */
	public List getEntries(){
		return fEntries;
	}

	/**
	 * Returns the <tt>RefactoringStatusEntry</tt> at the specified index.
	 * 
	 * @param index of entry to return
	 * @return the enrty at the specified index
	 * 
     * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public RefactoringStatusEntry getEntry(int index) {
		return (RefactoringStatusEntry)fEntries.get(index);
	}

	/**
	 * Returns the first entry which severity is equal or greater than the given
	 * severity. Returns <code>null</code> if no element exists with
	 * the given severity.
	 * @param severity must be one of <code>FATAL</code>, <code>ERROR</code>, 
	 * 	<code>WARNING</code> or <code>INFO</code>.
	 */
	public RefactoringStatusEntry getFirstEntry(int severity) {
		Assert.isTrue(severity >= OK && severity <= FATAL);
		if (severity > fSeverity)
			return null;
		Iterator iter= fEntries.iterator();
		while(iter.hasNext()) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)iter.next();
			if (entry.getSeverity() >= severity)
				return entry;
		}
		return null;
	}


	/**
	 * Returns the first message which severity is equal or greater than the given
	 * severity. Returns <code>null</code> if no element exists with
	 * the given severity.
	 * @param severity must me one of <code>FATAL</code>, <code>ERROR</code>, 
	 * 	<code>WARNING</code> or <code>INFO</code>.
	 */
	public String getFirstMessage(int severity) {
		RefactoringStatusEntry entry= getFirstEntry(severity);
		if (entry == null)
			return null;
		return entry.getMessage();
	}


	/* non java-doc
	 * for debugging only
	 * not for nls
	 */	
	/*package*/ static String getSeverityString(int severity){
		Assert.isTrue(severity >= OK && severity <= FATAL);
		if (severity == RefactoringStatus.OK) return "OK"; //$NON-NLS-1$
		if (severity == RefactoringStatus.INFO) return "INFO"; //$NON-NLS-1$
		if (severity == RefactoringStatus.WARNING) return "WARNING"; //$NON-NLS-1$
		if (severity == RefactoringStatus.ERROR) return "ERROR"; //$NON-NLS-1$
		if (severity == RefactoringStatus.FATAL) return "FATALERROR"; //$NON-NLS-1$
		return null;
	}
	
	/* non java-doc
	 * for debugging only
	 */
	public String toString(){
		StringBuffer buff= new StringBuffer();
		buff.append("<") //$NON-NLS-1$
		    .append(getSeverityString(fSeverity))
		    .append("\n"); //$NON-NLS-1$
		if (!isOK()){
			for (Iterator iter= fEntries.iterator(); iter.hasNext();){
				buff.append("\t") //$NON-NLS-1$
				    .append(iter.next())
				    .append("\n"); //$NON-NLS-1$
			}
		}	
		buff.append(">"); //$NON-NLS-1$
		return buff.toString();
	}
}


