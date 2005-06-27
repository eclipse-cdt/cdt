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
package org.eclipse.cdt.internal.corext.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.corext.refactoring.base.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;


/**
 * Represents a composite change.
 */
public class CompositeChange extends Change implements ICompositeChange {

	private List fChanges;
	private IChange fUndoChange;
	private String fName;
	
	public CompositeChange() {
		this(RefactoringCoreMessages.getString("CompositeChange.CompositeChange")); //$NON-NLS-1$
	}

	public CompositeChange(String name, IChange[] changes) {
		this(name, new ArrayList(changes.length));
		addAll(changes);
	}
			
	public CompositeChange(String name) {
		this(name, new ArrayList(5));
	}
	
	public CompositeChange(String name, int initialCapacity) {
		this(name, new ArrayList(initialCapacity));
	}
		
	private CompositeChange(String name, List changes) {
		fChanges= changes;
		fName= name;
	}
	
	/* (Non-Javadoc)
	 * Method declared in IChange.
	 */
	public final RefactoringStatus aboutToPerform(ChangeContext context, IProgressMonitor pm) {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		RefactoringStatus result= new RefactoringStatus();
		pm.beginTask("", fChanges.size() + 1); //$NON-NLS-1$
		result.merge(super.aboutToPerform(context, new SubProgressMonitor(pm,1)));
		for (Iterator iter= fChanges.iterator(); iter.hasNext(); ) {
			result.merge(((IChange)iter.next()).aboutToPerform(context, new SubProgressMonitor(pm,1)));
		}
		return result;
	}
	
	/* (Non-Javadoc)
	 * Method declared in IChange.
	 */
	public final void performed() {
		for (Iterator iter= fChanges.iterator(); iter.hasNext(); ) {
			((IChange)iter.next()).performed();
		}
	} 
	
	/* non java-doc
	 * @see IChange#getUndoChange
	 */
	public final IChange getUndoChange() {
		return fUndoChange;
	}

	public void addAll(IChange[] changes) {
		for (int i= 0; i < changes.length; i++) {
			add(changes[i]);
		}
	}
	
	public void add(IChange change) {
		if (change != null)
			fChanges.add(change);	
	}
		
	public IChange[] getChildren() {
		if (fChanges == null)
			return null;
		return (IChange[])fChanges.toArray(new IChange[fChanges.size()]);
	}
	
	final List getChanges() {
		return fChanges;
	}
	
	/**
	 * to reverse a composite means reversing all changes in reverse order
	 */ 
	private List createUndoList(ChangeContext context, IProgressMonitor pm) throws CModelException {
		List undoList= null;
		try {
			undoList= new ArrayList(fChanges.size());
			pm.beginTask("", fChanges.size()); //$NON-NLS-1$
			for (Iterator iter= fChanges.iterator(); iter.hasNext();) {
				try {
					IChange each= (IChange)iter.next();
					each.perform(context, new SubProgressMonitor(pm, 1));
					undoList.add(each.getUndoChange());
					context.addPerformedChange(each);
				} catch (Exception e) {
					handleException(context, e);
				}
			}
			pm.done();
			Collections.reverse(undoList);
			return undoList;
		} catch (Exception e) {
			handleException(context, e);
		}
		if (undoList == null)
			undoList= new ArrayList(0);
		return undoList;	
	}

	/* non java-doc
	 * @see IChange#perform
	 */
	public final void perform(ChangeContext context, IProgressMonitor pm) throws CModelException {
		pm.beginTask("", 1); //$NON-NLS-1$
		pm.setTaskName(RefactoringCoreMessages.getString("CompositeChange.performingChangesTask.name")); //$NON-NLS-1$
		if (!isActive()) {
			fUndoChange= new NullChange();
		} else {
			fUndoChange= new CompositeChange(fName, createUndoList(context, new SubProgressMonitor(pm, 1)));
		}	
		pm.done();
	}
	
	/* non java-doc
	 * for debugging only
	 */	
	public String toString() {
		StringBuffer buff= new StringBuffer();
		buff.append("CompositeChange\n"); //$NON-NLS-1$
		for (Iterator iter= fChanges.iterator(); iter.hasNext();) {
			buff.append("<").append(iter.next().toString()).append("/>\n"); //$NON-NLS-2$ //$NON-NLS-1$
		}
		return buff.toString();
	}
	
	/* non java-doc
	 * @see IChange#getName()
	 */
	public String getName() {
		return fName;
	}

	/* non java-doc
	 * @see IChange#getModifiedLanguageElement()
	 */	
	public Object getModifiedLanguageElement() {
		return null;
	}

	/* non java-doc
	 * @see IChange#setActive
	 * This method activates/disactivates all subchanges of this change. The
	 * change itself is always active to ensure that sub changes are always
	 * considered if they are active.
	 */
	public void setActive(boolean active) {
		for (Iterator iter= fChanges.iterator(); iter.hasNext(); ) {
			((IChange)iter.next()).setActive(active);
		}
	}	
	
	/*non java-doc
	 * @see IChange#isUndoable()
	 * Composite can be undone iff all its sub-changes can be undone.
	 */
	public boolean isUndoable() {
		for (Iterator iter= fChanges.iterator(); iter.hasNext(); ) {
			IChange each= (IChange)iter.next();
			if (! each.isUndoable())
				return false;
		}
		return true;
	}
}
