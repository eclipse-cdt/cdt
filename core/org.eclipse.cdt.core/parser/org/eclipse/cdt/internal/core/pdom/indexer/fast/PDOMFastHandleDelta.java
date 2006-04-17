/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

class PDOMFastHandleDelta extends PDOMFastIndexerJob {

	private final ICElementDelta delta;

	private List addedTUs;

	private List changedTUs;

	private List removedTUs;

	public PDOMFastHandleDelta(PDOM pdom, ICElementDelta delta) {
		super("Delta Handler", pdom);
		this.delta = delta;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.subTask("Delta");
			long start = System.currentTimeMillis();
	
			processDelta(delta);
	
			int count = (addedTUs != null ? addedTUs.size() : 0)
					+ (changedTUs != null ? changedTUs.size() : 0)
					+ (removedTUs != null ? removedTUs.size() : 0);
	
			if (count == 0) {
				monitor.done();
				return Status.OK_STATUS;
			}
	
			if (addedTUs != null)
				for (Iterator i = addedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					ITranslationUnit tu = (ITranslationUnit) i.next();
					monitor.subTask(String.valueOf(count--) + " files remaining - "
							+ tu.getPath().toString());
					addTU(tu);
				}
	
			if (changedTUs != null)
				for (Iterator i = changedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					ITranslationUnit tu = (ITranslationUnit) i.next();
					monitor.subTask(String.valueOf(count--) + " files remaining - "
							+ tu.getPath().toString());
					changeTU(tu);
					monitor.worked(1);
				}
	
			if (removedTUs != null)
				for (Iterator i = removedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					ITranslationUnit tu = (ITranslationUnit) i.next();
					monitor.subTask(String.valueOf(count--) + " files remaining - "
							+ tu.getPath().toString());
					removeTU(tu);
					monitor.worked(1);
				}
	
			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null)
				if (showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
					System.out
							.println("PDOM Update Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
	
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
	}

	private void processDelta(ICElementDelta delta) {
		ICElement element = delta.getElement();

		// If this is a project add skip over to the reindex job
		if (element.getElementType() == ICElement.C_PROJECT
				&& delta.getKind() == ICElementDelta.ADDED) {
			new PDOMFastReindex(pdom).schedule();
			return;
		}

		// process the children first
		ICElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; ++i)
			processDelta(children[i]);

		// what have we got
		if (element.getElementType() == ICElement.C_UNIT) {
			ITranslationUnit tu = (ITranslationUnit) element;
			if (tu.isWorkingCopy())
				// Don't care about working copies either
				return;

			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				if (addedTUs == null)
					addedTUs = new LinkedList();
				addedTUs.add(element);
				break;
			case ICElementDelta.CHANGED:
				if (changedTUs == null)
					changedTUs = new LinkedList();
				changedTUs.add(element);
				break;
			case ICElementDelta.REMOVED:
				if (removedTUs == null)
					removedTUs = new LinkedList();
				removedTUs.add(element);
				break;
			}
		}
	}

	protected void changeTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;

		// get the AST in a "Fast" way
		IASTTranslationUnit ast = language.getASTTranslationUnit(tu,
				ILanguage.AST_USE_INDEX |
				ILanguage.AST_SKIP_INDEXED_HEADERS |
				ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
		
		if (ast == null)
			return;
		
		pdom.acquireWriteLock();
		try {
			pdom.removeSymbols(tu);
			pdom.addSymbols(language, ast);
		} finally {
			pdom.releaseWriteLock();
		}
	}
	
	protected void removeTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		pdom.acquireWriteLock();
		try {
			pdom.removeSymbols(tu);
			// TODO delete the file itself from the database
			// the removeSymbols only removes the names in the file
		} finally {
			pdom.releaseWriteLock();
		}
	}
}
