/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SetCProjectDescriptionOperation extends CModelOperation {
//	private IProject fProject;
//	private ICProject fCProject;
	private CProjectDescription fSetDescription;
//	private CProjectDescription fNewDescriptionCache;
//	private ICProjectDescription fOldDescriptionCache;
	

	SetCProjectDescriptionOperation(ICProject cProject, CProjectDescription description){
		super(cProject);
//		fCProject = cProject;
		fSetDescription = description;
	}
	
	protected void executeOperation() throws CModelException {
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		ICProject cProject = (ICProject)getElementToProcess();
		IProject project = cProject.getProject();
		CProjectDescription fOldDescriptionCache = (CProjectDescription)mngr.getProjectDescription(project, false);
		
		CProjectDescriptionEvent event = mngr.createAboutToApplyEvent(fSetDescription, fOldDescriptionCache);
		mngr.notifyListeners(event);
		
		InternalXmlStorageElement el = null;
		try {
			el = mngr.copyElement((InternalXmlStorageElement)fSetDescription.getRootStorageElement(), false);
		} catch (CoreException e2) {
		}

		CProjectDescription fNewDescriptionCache = new CProjectDescription(fSetDescription, true, el);
		
		ICDescriptionDelta delta = mngr.createDelta(fNewDescriptionCache, fOldDescriptionCache);
		mngr.checkRemovedConfigurations(delta);
		
		
		ICElementDelta cElementDeltas[] = mngr.generateCElementDeltas(cProject, delta);

		if (cElementDeltas.length > 0) {
			for (int i = 0; i < cElementDeltas.length; i++) {
				addDelta(cElementDeltas[i]);
			}
		}

		mngr.setLoaddedDescription(project, fNewDescriptionCache, true);
		mngr.checkActiveCfgChange(fNewDescriptionCache, fOldDescriptionCache, new NullProgressMonitor());
		
		event = mngr.createAppliedEvent(fNewDescriptionCache, fOldDescriptionCache, fSetDescription, delta);
		mngr.notifyListeners(event);
		
		cProject.close();
		
		ExternalSettingsManager.getInstance().updateDepentents(delta);
		
		try {
			((InternalXmlStorageElement)fNewDescriptionCache.getRootStorageElement()).setReadOnly(true);
		} catch (CoreException e1) {
		}

		try {
			mngr.serialize(fNewDescriptionCache);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}
	
	public boolean isReadOnly() {
		return false;
	}

}
