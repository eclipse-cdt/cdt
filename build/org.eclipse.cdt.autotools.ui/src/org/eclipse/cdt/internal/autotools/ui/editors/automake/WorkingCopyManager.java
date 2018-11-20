/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.cdt.make.ui.IWorkingCopyManagerExtension;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;

/**
 * This working copy manager works together with a given compilation unit document provider and
 * additionally offers to "overwrite" the working copy provided by this document provider.
 */
public class WorkingCopyManager implements IWorkingCopyManager, IWorkingCopyManagerExtension {

	private IMakefileDocumentProvider fDocumentProvider;
	private Map<IEditorInput, IMakefile> fMap;
	private boolean fIsShuttingDown;

	/**
	 * Creates a new working copy manager that co-operates with the given
	 * compilation unit document provider.
	 *
	 * @param provider the provider
	 */
	public WorkingCopyManager(IMakefileDocumentProvider provider) {
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
	public IMakefile getWorkingCopy(IEditorInput input) {
		IMakefile unit = fMap == null ? null : (IMakefile) fMap.get(input);
		return unit != null ? unit : fDocumentProvider.getWorkingCopy(input);
	}

	@Override
	public void setWorkingCopy(IEditorInput input, IMakefile workingCopy) {
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
}
