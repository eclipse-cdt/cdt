/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.IWorkingCopyManagerExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Assert;
import org.eclipse.ui.IEditorInput;


/**
 * This working copy manager works together with a given compilation unit document provider and
 * additionally offers to "overwrite" the working copy provided by this document provider.
 */
public class WorkingCopyManager implements IWorkingCopyManager, IWorkingCopyManagerExtension {
	
	private CDocumentProvider fDocumentProvider;
	private Map fMap;
	private boolean fIsShuttingDown;

	/**
	 * Creates a new working copy manager that co-operates with the given
	 * compilation unit document provider.
	 * 
	 * @param provider the provider
	 */
	public WorkingCopyManager(CDocumentProvider provider) {
		Assert.isNotNull(provider);
		fDocumentProvider= provider;
		fMap = new HashMap();
	}

	/*
	 * @see org.eclipse.cdt.ui.IWorkingCopyManager#connect(org.eclipse.ui.IEditorInput)
	 */
	public void connect(IEditorInput input) throws CoreException {
		fDocumentProvider.connect(input);
	}
	
	/*
	 * @see org.eclipse.cdt.ui.IWorkingCopyManager#disconnect(org.eclipse.ui.IEditorInput)
	 */
	public void disconnect(IEditorInput input) {
		fDocumentProvider.disconnect(input);
	}
	
	/*
	 * @see org.eclipse.cdt.ui.IWorkingCopyManager#shutdown()
	 */
	public void shutdown() {
		if (!fIsShuttingDown) {
			fIsShuttingDown= true;
			try {
				if (fMap != null) {
					fMap.clear();
					fMap= null;
				}
				fDocumentProvider.shutdown();
			} finally {
				fIsShuttingDown= false;
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.IWorkingCopyManager#getWorkingCopy(org.eclipse.ui.IEditorInput)
	 */
	public IWorkingCopy getWorkingCopy(IEditorInput input) {
		IWorkingCopy unit= fMap == null ? null : (IWorkingCopy) fMap.get(input);
		if(unit != null)
			return unit;
		IWorkingCopy copy = fDocumentProvider.getWorkingCopy(input);
		if(copy != null)
			fMap.put(input, copy);
		return copy;
	}
	/*
	 * @see org.eclipse.cdt.ui.IWorkingCopyManager#getWorkingCopy(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	public IWorkingCopy getWorkingCopy(ITranslationUnit unit){
		if((fMap == null) || (fMap.size() == 0)){
			return null;
		} else {
			List copies = new ArrayList(fMap.values());
			Iterator i = copies.iterator();
			while (i.hasNext()){
				IWorkingCopy copy = (IWorkingCopy)i.next();
				if(copy.getOriginalElement().equals(unit))
					return copy;
			}
		}
		return null;
	}
	/*
	 * @see org.eclipse.cdt.internal.ui.editor.IWorkingCopyManagerExtension#setWorkingCopy(org.eclipse.ui.IEditorInput, org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	public void setWorkingCopy(IEditorInput input, IWorkingCopy workingCopy) {
		if (fDocumentProvider.isConnected(input)) {
			if (fMap == null)
				fMap= new HashMap();
			fMap.put(input, workingCopy);
		}
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.editor.IWorkingCopyManagerExtension#removeWorkingCopy(org.eclipse.ui.IEditorInput)
	 */
	public void removeWorkingCopy(IEditorInput input) {
		fMap.remove(input);
		if (fMap.isEmpty())
			fMap= null;
	}
}
