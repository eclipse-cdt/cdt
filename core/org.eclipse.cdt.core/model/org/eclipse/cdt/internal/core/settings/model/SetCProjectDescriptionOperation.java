/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 * Baltasar Belyavsky (Texas Instruments) - bug 340219: Project metadata files are saved unnecessarily
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.model.CModelOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * The operation which actually causes the CProjectDescription to be serialized
 *
 * This organizes the firing the {@link CProjectDescriptionEvent}s to all listeners
 */
public class SetCProjectDescriptionOperation extends CModelOperation {
	/** The ProjectDescription Storage being used for this project description */
	private final AbstractCProjectDescriptionStorage fPrjDescStorage;
	private final CProjectDescription fSetDescription;
	private final int fFlags;

	/**
	 * Operation used for persisting the new CProjectDescription
	 * @param prjDescStorage
	 * @param cProject
	 * @param description
	 * @param flags
	 */
	public SetCProjectDescriptionOperation(AbstractCProjectDescriptionStorage prjDescStorage, ICProject cProject, CProjectDescription description, int flags){
		super(cProject);
		this.fPrjDescStorage = prjDescStorage;
		fFlags = flags;
		fSetDescription = description;
	}

	@Override
	protected void executeOperation() throws CModelException {
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		ICProject cProject = (ICProject)getElementToProcess();
		final IProject project = cProject.getProject();

		ICProjectDescription fOldDescriptionCache = mngr.getProjectDescription(project, false);

		AbstractCProjectDescriptionStorage.fireAboutToApplyEvent(fSetDescription, fOldDescriptionCache);
		CProjectDescription fNewDescriptionCache = null;
		SettingsContext context = new SettingsContext(project);
		boolean needsSerialization = false;

		if(fSetDescription != null){
			ICStorageElement newEl = null;
			ICSettingsStorage newStorage = null;
			try {
				ICStorageElement base = fSetDescription.getRootStorageElement();
				needsSerialization = fSetDescription.needsDescriptionPersistence();
//				el = base;
				// FIXME JBB there is deep magic going on here.  The project descriptions are being
				//           changed in non-obvious ways
				newEl = fPrjDescStorage.copyElement(base, false);
				newStorage = fPrjDescStorage.getStorageForElement(newEl);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}

			boolean creating = fOldDescriptionCache != null ? fOldDescriptionCache.isCdtProjectCreating() : true;
			if(creating)
				creating = fSetDescription.isCdtProjectCreating();

			if(!fSetDescription.isValid() && (!mngr.isEmptyCreatingDescriptionAllowed() || !creating))
				throw new CModelException(ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.17") + project.getName())); //$NON-NLS-1$

			fNewDescriptionCache = new CProjectDescription(fSetDescription, true, newStorage, newEl, creating);

			boolean envStates[] = getEnvStates(fNewDescriptionCache);
			try {
				fPrjDescStorage.setThreadLocalProjectDesc(fNewDescriptionCache);
				fNewDescriptionCache.applyDatas(context);
			} finally {
				fPrjDescStorage.setThreadLocalProjectDesc(null);
				setEnvStates(fNewDescriptionCache, envStates);
			}
		}

		ICDescriptionDelta delta = mngr.createDelta(fNewDescriptionCache, fOldDescriptionCache);
		mngr.checkRemovedConfigurations(delta);

		// Generate the c element deltas
		ICElementDelta cElementDeltas[] = mngr.generateCElementDeltas(cProject, delta);
		for (ICElementDelta d : cElementDeltas)
			addDelta(d);

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

		// fNewDescriptionCache is still writable and may be written to at this point
		AbstractCProjectDescriptionStorage.fireDataAppliedEvent(fNewDescriptionCache, fOldDescriptionCache, fSetDescription, delta);

		cProject.close(); // Why?

//		ExternalSettingsManager.getInstance().updateDepentents(delta);

		if(fNewDescriptionCache != null){
			fNewDescriptionCache.doneApplying();
		}

		// Set 'fSetProjectDescription' as the new read-only project description on the block
		fPrjDescStorage.setCurrentDescription(fNewDescriptionCache, true);

		CProjectDescriptionEvent event = AbstractCProjectDescriptionStorage.createAppliedEvent(fNewDescriptionCache, fOldDescriptionCache, fSetDescription, delta);
		mngr.notifyListeners(event);

		try {
			IWorkspaceRunnable toRun = null;
			if(fNewDescriptionCache != null && !CProjectDescriptionManager.checkFlags(fFlags, ICProjectDescriptionManager.SET_NO_SERIALIZE)){
				if(needsSerialization || isPersistentCoreSettingChanged(event)){
					toRun = fPrjDescStorage.createDesSerializationRunnable();
					if (toRun != null)
						context.addWorkspaceRunnable(toRun);
				}
			}
			toRun = context.createOperationRunnable();

			if(toRun != null)
				CProjectDescriptionManager.runWspModification(toRun, getSchedulingRule(), new NullProgressMonitor());
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	// Can't use project scoped rule see CProjectDescriptionBasicTests...
//	/*
//	 * Instead of using the workspace scheduling rule, use a more refined project scoped rule.
//	 * This must contain the rule in CConfigBasedDescriptor.setApply(...)
//	 * (non-Javadoc)
//	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#getSchedulingRule()
//	 */
//	@Override
//	public ISchedulingRule getSchedulingRule() {
////		return null;
//		return fPrjDescStorage.getProject();
//	}

	private static boolean isPersistentCoreSettingChanged(CProjectDescriptionEvent event){
		ICDescriptionDelta delta = event.getProjectDelta();
		if(delta == null)
			return false;
		if(delta.getDeltaKind() != ICDescriptionDelta.CHANGED)
			return true;

		if(delta.getChildren().length != 0)
			return true;

		int flags = delta.getChangeFlags();
		// check for any flag except ACTIVE_CFG and SETTING_CFG
		if((flags & ~(ICDescriptionDelta.ACTIVE_CFG | ICDescriptionDelta.SETTING_CFG)) != 0)
			return true;

		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * Returns a boolean array corresponding to whether the environemnt is
	 * dirty on the configurations in the array returned by projDesc.getConfigurations()
	 * @param pd
	 * @return boolean[] indicating which configurations have a dirty environment
	 */
	private boolean[] getEnvStates(CProjectDescription pd) {
		ICConfigurationDescription[] cfs = pd.getConfigurations();
		boolean[] result = new boolean[cfs.length];
		for (int i=0; i<cfs.length; i++) {
			if (cfs[i] instanceof IInternalCCfgInfo) {
				try {
					CConfigurationSpecSettings ss = ((IInternalCCfgInfo)cfs[i]).getSpecSettings();
					if (ss != null && ss.getEnvironment() != null)
						result[i] = ss.getEnvironment().isDirty();
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
		return result;
	}

	/**
	 * Set
	 * @param pd
	 * @param data
	 */
	private void setEnvStates(CProjectDescription pd, boolean[] data) {
		ICConfigurationDescription[] cfs = pd.getConfigurations();
		if (cfs == null || data == null)
			return;
		for (int i=0; i<cfs.length; i++) {
			if (data.length <= i) {
				CCorePlugin.log("Error: setEnvStates: different number of configurations as there are envDatas...", new Exception()); //$NON-NLS-1$
				break;
			}
			if (!data[i])
				continue; // write only TRUE values
			if (cfs[i] instanceof IInternalCCfgInfo) {
				try {
					CConfigurationSpecSettings ss = ((IInternalCCfgInfo)cfs[i]).getSpecSettings();
					if (ss != null && ss.getEnvironment() != null)
						ss.getEnvironment().setDirty(true);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

}
