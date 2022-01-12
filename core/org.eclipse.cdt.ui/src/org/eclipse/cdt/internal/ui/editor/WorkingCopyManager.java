/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.IWorkingCopyManagerExtension;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;

/**
 * This working copy manager works together with a given compilation unit document provider and
 * additionally offers to "overwrite" the working copy provided by this document provider.
 */
public class WorkingCopyManager implements IWorkingCopyManager, IWorkingCopyManagerExtension {
	private CDocumentProvider fDocumentProvider;
	private Map<IEditorInput, IWorkingCopy> fMap;
	private boolean fIsShuttingDown;
	private IBufferFactory fBufferFactory;

	/**
	 * Creates a new working copy manager that co-operates with the given
	 * compilation unit document provider.
	 *
	 * @param provider the provider
	 */
	public WorkingCopyManager(CDocumentProvider provider) {
		Assert.isNotNull(provider);
		fDocumentProvider = provider;
	}

	@Override
	public void connect(IEditorInput input) throws CoreException {
		fDocumentProvider.connect(input);
	}

	@Override
	public void disconnect(IEditorInput input) {
		fDocumentProvider.disconnect(input);
	}

	@Override
	public void shutdown() {
		if (!fIsShuttingDown) {
			fIsShuttingDown = true;
			try {
				if (fMap != null) {
					fMap.clear();
					fMap = null;
				}
				fDocumentProvider.shutdown();
			} finally {
				fIsShuttingDown = false;
			}
		}
	}

	@Override
	public IWorkingCopy getWorkingCopy(IEditorInput input) {
		IWorkingCopy unit = fMap == null ? null : fMap.get(input);
		return unit != null ? unit : fDocumentProvider.getWorkingCopy(input);
	}

	@Override
	public void setWorkingCopy(IEditorInput input, IWorkingCopy workingCopy) {
		if (fDocumentProvider.getDocument(input) != null) {
			if (fMap == null)
				fMap = new HashMap<>();
			fMap.put(input, workingCopy);
		}
	}

	@Override
	public void removeWorkingCopy(IEditorInput input) {
		fMap.remove(input);
		if (fMap.isEmpty())
			fMap = null;
	}

	public IBufferFactory getBufferFactory() {
		if (fBufferFactory == null) {
			synchronized (this) {
				if (fBufferFactory == null)
					fBufferFactory = new CustomBufferFactory();
			}
		}
		return fBufferFactory;
	}

	@Override
	public IWorkingCopy findSharedWorkingCopy(ITranslationUnit tu) {
		return CModelManager.getDefault().findSharedWorkingCopy(getBufferFactory(), tu);
	}

	@Override
	public IWorkingCopy[] getSharedWorkingCopies() {
		return CModelManager.getDefault().getSharedWorkingCopies(getBufferFactory());
	}

	@Override
	public IWorkingCopy getSharedWorkingCopy(ITranslationUnit original, IProblemRequestor requestor,
			IProgressMonitor progressMonitor) throws CModelException {
		return CModelManager.getDefault().getSharedWorkingCopy(getBufferFactory(), original, requestor,
				progressMonitor);
	}
}
