/*******************************************************************************
 * Copyright (c) 2008, 2009 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType.CProjectDescriptionStorageTypeProxy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Version;

import com.ibm.icu.text.MessageFormat;

/**
 * This abstract class provides an extension point for functionality for loading
 * a CDT Project Description from some kind of backing store. This allows
 * extenders to provide their own backing store for a CDT project description.
 *
 * This class provides the ICProjectDescription root of the project configuration tree in
 * which is contained storage modules and other members of the storage element tree.
 *
 * It is the responsibility of the storage that access to the project storage are threadsafe
 * i.e. return writable descriptions aren't shared between multiple threads (or if they
 * are, they are suitable synchronized) and setProjectDescription must be aware that
 * getProjectDescription may also be called concurrently
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 *
 * @since 6.0
 */
public abstract class AbstractCProjectDescriptionStorage {

	/** The {@link ICProjectDescriptionStorageType} extension parent of this */
	public final CProjectDescriptionStorageTypeProxy type;
	/** The version of the project description storage that was loaded */
	public final Version version;

	/** The project this project-storage is responsible for */
	protected volatile IProject project;

	/** Flag used to detect if setProjectDescription(...) is called by the thread already in a setProjectDescription(...) */
	final private ThreadLocal<Boolean> setProjectDescriptionOperationRunning = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	/** Before the description is fully applied / loaded, consumers of CProjectDescriptionEvent and CProjectDescription.applyDatas()
	 *  assume that getProjectDescription(...) will return the writable project description in the process of being created / modified...
	 *  Cached temporarily in a thread local variable for this very purpose. */
	final private ThreadLocal<ICProjectDescription> currentThreadProjectDescription = new ThreadLocal<>();

	/**
	 * @param type CProjectDescriptionStorageTypeProxy
	 * @param project IProject
	 * @param version Version
	 */
	public AbstractCProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type, IProject project,
			Version version) {
		this.type = type;
		this.project = project;
		this.version = version;
	}

	/**
	 * Returns the project associated with this storage
	 * @return the IProject associated with the current project
	 */
	public final IProject getProject() {
		return project;
	}

	/**
	 * Called in response to a project move event
	 * @param newProject
	 * @param oldProject
	 */
	public void handleProjectMove(IProject newProject, IProject oldProject) {
		project = newProject;
	}

	/**
	 * Return an ICSettingsStorage root for the given ICStorageElement
	 * @param element
	 * @return ICSettingsStorage based off of ICStorageElement
	 */
	public abstract ICSettingsStorage getStorageForElement(ICStorageElement element) throws CoreException;

	/*
	 * T O    C L E A N U P
	 */

	/*
	 * FIXME REMOVE
	 *
	 *  We shouldn't have this in this interface, but SetCProjectDescription creates
	 * a new description based on an existing description for the delta event.  It reconciles
	 * them later. But this will be non-optimal for most backends
	 *
	 * Returns a 'writable' ICStorageElement tree clone of el
	 */
	public ICStorageElement copyElement(ICStorageElement el, boolean writable) throws CoreException {
		return null;
	}

	/*
	 * get/setProjectDescription methods
	 */

	/**
	 * Return the ICProjectDescription for the specified project.
	 *
	 * Use the ICProjectDescriptionManager flags to control the project
	 * description to be returned
	 *
	 * Implementors should call super.getProjectDescription(...) first
	 * so that current 'threadLocal' project description is returned
	 *
	 * @param flags Or of {@link ICProjectDescriptionManager} flags
	 * @param monitor
	 * @return an ICProjectDescription corresponding to the given
	 * @see ICProjectDescriptionManager#GET_WRITABLE
	 * @see ICProjectDescriptionManager#GET_IF_LOADDED
	 * @see ICProjectDescriptionManager#GET_EMPTY_PROJECT_DESCRIPTION
	 * @see ICProjectDescriptionManager#GET_CREATE_DESCRIPTION
	 * @see ICProjectDescriptionManager#PROJECT_CREATING
	 */
	public ICProjectDescription getProjectDescription(int flags, IProgressMonitor monitor) throws CoreException {
		if (!project.isAccessible())
			throw ExceptionFactory.createCoreException(
					MessageFormat.format(CCorePlugin.getResourceString("ProjectDescription.ProjectNotAccessible"), //$NON-NLS-1$
							new Object[] { getProject().getName() }));
		return currentThreadProjectDescription.get();
	}

	/**
	 * The method called by the CProjectDescriptionManager for serializing the project settings
	 * @param description the project description being set
	 * @param flags
	 * @param monitor
	 * @throws CoreException
	 */
	public void setProjectDescription(final ICProjectDescription description, final int flags, IProgressMonitor monitor)
			throws CoreException {
		try {
			if (monitor == null)
				monitor = new NullProgressMonitor();

			ICProject cproject = CModelManager.getDefault().create(project);

			// The CProjectDescriptionOperation fires the appropriate CElementDeltas calling the callbacks
			// below for actual project serialization
			SetCProjectDescriptionOperation op = new SetCProjectDescriptionOperation(this, cproject,
					(CProjectDescription) description, flags);

			// Safety: Verify that the listeners of the event call-backs don't recursively call setProjectDescription(...)
			//         While in the past this recursion hasn't been infinite, the behaviour is 'undefined'.
			if (setProjectDescriptionOperationRunning.get()) {
				CCorePlugin.log("API Error: setProjectDescription() shouldn't be called recursively.", new Exception()); //$NON-NLS-1$
				Job j = new Job("setProjectDescription rescheduled") { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							setProjectDescription(description, flags, monitor);
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
						return Status.OK_STATUS;
					}
				};
				j.setSystem(true);
				j.schedule();
				return;
			}

			try {
				setProjectDescriptionOperationRunning.set(true);
				op.runOperation(monitor);
			} catch (IllegalArgumentException e) {
				throw ExceptionFactory.createCoreException(e);
			} finally {
				setProjectDescriptionOperationRunning.set(false);
			}
		} finally {
			monitor.done();
		}
	}

	/*
	 * C A L L B A C K S
	 * Callbacks for the SetCProjectDescriptionOperation to allow AbstractCProjectDescriptionStorage overrides
	 *
	 */

	/**
	 * Callback
	 *
	 * 	 - Actually set the current in memory ICProjectDescription to this
	 *   - If this requires modification of workspace resources then don't serialize
	 *
	 * @param des
	 * @param overwriteIfExists
	 * @return boolean indicating whether existing read-only project description should be replaced
	 */
	public abstract boolean setCurrentDescription(ICProjectDescription des, boolean overwriteIfExists);

	/**
	 * Callback
	 *   -  Return an IWorkspaceRunnable which will actually perform the serialization of the
	 *      current project description or null if not required
	 * @return IWorkspaceRunnable that will perform the serialization
	 */
	public abstract IWorkspaceRunnable createDesSerializationRunnable() throws CoreException;

	/*
	 * R E S O U R C E     C H A N G E    E V E N T S
	 */

	/**
	 * Event fired as a result of a project being moved
	 */
	public void projectMove(IProject newProject) {
		project = newProject;
	}

	/**
	 * Event fired as a result of the project being closed or removed
	 * to allow cleanup of state
	 */
	public void projectCloseRemove() {
		// NOP
	}

	/*
	 * C P R O J E C T        D E L T A          E V E N T S        F I R E D
	 * Protected methods for notifying project description listeners of changes to the proj desc
	 */

	public static final void fireLoadedEvent(ICProjectDescription desc) {
		CProjectDescriptionManager.getInstance()
				.notifyListeners(new CProjectDescriptionEvent(CProjectDescriptionEvent.LOADED, null, desc, null, null));
	}

	/**
	 * Fire an event stating that a copy of a description has been created
	 *
	 * This is fired when:
	 *   - New writable description is created from read-only store
	 * @param newDes The new description copy
	 * @param oldDes The old description
	 */
	public static final void fireCopyCreatedEvent(ICProjectDescription newDes, ICProjectDescription oldDes) {
		CProjectDescriptionManager.getInstance().notifyListeners(
				new CProjectDescriptionEvent(CProjectDescriptionEvent.COPY_CREATED, null, newDes, oldDes, null));
	}

	public static final void fireAboutToApplyEvent(ICProjectDescription newDes, ICProjectDescription oldDes) {
		CProjectDescriptionManager.getInstance().notifyListeners(
				new CProjectDescriptionEvent(CProjectDescriptionEvent.ABOUT_TO_APPLY, null, newDes, oldDes, null));
	}

	public static final CProjectDescriptionEvent createAppliedEvent(ICProjectDescription newDes,
			ICProjectDescription oldDes, ICProjectDescription appliedDes, ICDescriptionDelta delta) {
		return new CProjectDescriptionEvent(CProjectDescriptionEvent.APPLIED, delta, newDes, oldDes, appliedDes);
	}

	public static final void fireAppliedEvent(ICProjectDescription newDes, ICProjectDescription oldDes,
			ICProjectDescription appliedDes, ICDescriptionDelta delta) {
		CProjectDescriptionManager.getInstance().notifyListeners(createAppliedEvent(newDes, oldDes, appliedDes, delta));
	}

	/**
	 * @param newDes - a *writeable* description
	 * @param oldDes
	 * @param appliedDes - the description being applied
	 * @param delta
	 */
	public static final void fireDataAppliedEvent(ICProjectDescription newDes, ICProjectDescription oldDes,
			ICProjectDescription appliedDes, ICDescriptionDelta delta) {
		CProjectDescriptionManager.getInstance().notifyListeners(
				new CProjectDescriptionEvent(CProjectDescriptionEvent.DATA_APPLIED, delta, newDes, oldDes, appliedDes));
	}

	/**
	 * Helper method to check whether the specified flags are set
	 * @param flags
	 * @param check
	 * @return boolean indicating whether flags are set
	 */
	protected static final boolean checkFlags(int flags, int check) {
		return (flags & check) == check;
	}

	/**
	 * Set the threadLocal project description
	 *
	 * Only intended to be used by implementors and package.
	 *
	 * This should be used in the following pattern:
	 * try {
	 * 		setThreadLocaProjectDesc(prjDesc);
	 * 		fireEvent();
	 * } finally {
	 * 		setThreadLocalProjectDesc(null);
	 * }
	 *
	 * @param currentDesc
	 * @return the previously set thread local project desc (or null)
	 */
	public ICProjectDescription setThreadLocalProjectDesc(ICProjectDescription currentDesc) {
		ICProjectDescription current = currentThreadProjectDescription.get();
		currentThreadProjectDescription.set(currentDesc);
		return current;
	}
}
