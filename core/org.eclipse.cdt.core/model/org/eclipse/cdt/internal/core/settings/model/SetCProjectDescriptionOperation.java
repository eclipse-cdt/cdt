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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.internal.core.model.CModelOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SetCProjectDescriptionOperation extends CModelOperation {
	private CProjectDescription fSetDescription;
	private int fFlags;

	SetCProjectDescriptionOperation(ICProject cProject, CProjectDescription description, int flags){
		super(cProject);
		fFlags = flags;
		fSetDescription = description;
	}
	
	protected void executeOperation() throws CModelException {
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		ICProject cProject = (ICProject)getElementToProcess();
		final IProject project = cProject.getProject();
		
		CProjectDescription fOldDescriptionCache = (CProjectDescription)mngr.getProjectDescription(project, false);
		
		CProjectDescriptionEvent event = mngr.createAboutToApplyEvent(fSetDescription, fOldDescriptionCache);
		mngr.notifyListeners(event);
		CProjectDescription fNewDescriptionCache = null;
		SettingsContext context = new SettingsContext(project);
		boolean modified;
		if(fSetDescription != null){
			InternalXmlStorageElement el = null;
			try {
				el = mngr.copyElement((InternalXmlStorageElement)fSetDescription.getRootStorageElement(), false);
			} catch (CoreException e2) {
			}
	
			boolean creating = fOldDescriptionCache != null ? fOldDescriptionCache.isCdtProjectCreating() : true;
			if(creating)
				creating = fSetDescription.isCdtProjectCreating();
	
			if(!fSetDescription.isValid() && (!mngr.isEmptyCreatingDescriptionAllowed() || !creating))
				throw new CModelException(ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.17") + project.getName())); //$NON-NLS-1$
	
			fNewDescriptionCache = new CProjectDescription(fSetDescription, true, el, creating);
			try {
				mngr.setDescriptionApplying(project, fNewDescriptionCache);
				modified = fNewDescriptionCache.applyDatas(context);
			} finally {
				mngr.clearDescriptionApplying(project);
			}
		} else {
			modified = fOldDescriptionCache != null;
		}
		
		ICDescriptionDelta delta = mngr.createDelta(fNewDescriptionCache, fOldDescriptionCache);
		mngr.checkRemovedConfigurations(delta);
		
		
		ICElementDelta cElementDeltas[] = mngr.generateCElementDeltas(cProject, delta);

		if (cElementDeltas.length > 0) {
			for (int i = 0; i < cElementDeltas.length; i++) {
				addDelta(cElementDeltas[i]);
			}
		}

		mngr.setLoaddedDescription(project, fNewDescriptionCache, true);
		
		if(fSetDescription != null)
			fSetDescription.switchToCachedAppliedData(fNewDescriptionCache);
		
		try {
			final IProjectDescription eDes = context.getEclipseProjectDescription();
			if(mngr.checkHandleActiveCfgChange(fNewDescriptionCache, fOldDescriptionCache, eDes, new NullProgressMonitor())){
				context.setEclipseProjectDescription(eDes);
			}
		} catch (CoreException e2) {
			CCorePlugin.log(e2);
		}
		
		event = mngr.createDataAppliedEvent(fNewDescriptionCache, fOldDescriptionCache, fSetDescription, delta);
		mngr.notifyListeners(event);
		
		cProject.close();
		
//		ExternalSettingsManager.getInstance().updateDepentents(delta);
		
		if(fNewDescriptionCache != null){
			try {
				((InternalXmlStorageElement)fNewDescriptionCache.getRootStorageElement()).setReadOnly(true);
			} catch (CoreException e1) {
			}
	
			fNewDescriptionCache.doneApplying();
		}
		
		event = mngr.createAppliedEvent(fNewDescriptionCache, fOldDescriptionCache, fSetDescription, delta);
		mngr.notifyListeners(event);

		try {
			if(fNewDescriptionCache != null && !CProjectDescriptionManager.checkFlags(fFlags, ICProjectDescriptionManager.SET_NO_SERIALIZE)){
				if(modified || isPersistentCoreSettingChanged(event)){
					context.addWorkspaceRunnable(mngr.createDesSerializationRunnable(fNewDescriptionCache));
				}
			}
			IWorkspaceRunnable toRun = context.createOperationRunnable();
			
			if(toRun != null)
				mngr.runWspModification(toRun, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}
	
	private static boolean isPersistentCoreSettingChanged(CProjectDescriptionEvent event){
		ICDescriptionDelta delta = event.getProjectDelta();
		if(delta == null)
			return false;
		if(delta.getDeltaKind() != ICDescriptionDelta.CHANGED)
			return true;
		
		if(delta.getChildren().length != 0)
			return true;
		
		int flags = delta.getChangeFlags(); 
		if(flags != 0 && flags != ICDescriptionDelta.ACTIVE_CFG)
			return true;
		
		return false;
	}
	
	public boolean isReadOnly() {
		return false;
	}

}
