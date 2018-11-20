/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.launch;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.PDABackend;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;

/**
 * Launches PDA program on a PDA interpretter written in Perl
 */
public class PDALaunchDelegate extends LaunchConfigurationDelegate {

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		// Need to configure the source locator before creating the launch
		// because once the launch is created and added to launch manager,
		// the adapters will be created for the whole session, including
		// the source lookup adapter.
		ISourceLocator locator = getSourceLocator(configuration);

		return new PDALaunch(configuration, mode, locator);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		// PDA programs do not require building.
		return false;
	}

	/**
	 * Returns a source locator created based on the attributes in the launch configuration.
	 */
	private ISourceLocator getSourceLocator(ILaunchConfiguration configuration) throws CoreException {
		String type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
		if (type == null) {
			type = configuration.getType().getSourceLocatorId();
		}
		if (type != null) {
			IPersistableSourceLocator locator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type);
			String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO,
					(String) null);
			if (memento == null) {
				locator.initializeDefaults(configuration);
			} else {
				if (locator instanceof IPersistableSourceLocator2)
					((IPersistableSourceLocator2) locator).initializeFromMemento(memento, configuration);
				else
					locator.initializeFromMemento(memento);
			}
			return locator;
		}
		return null;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String program = configuration.getAttribute(PDAPlugin.ATTR_PDA_PROGRAM, (String) null);
		if (program == null) {
			abort("Perl program unspecified.", null);
		}

		PDALaunch pdaLaunch = (PDALaunch) launch;
		initServices(pdaLaunch, program);
		createProcess(pdaLaunch);
	}

	/**
	 * Calls the launch to initialize DSF services for this launch.
	 */
	private void initServices(final PDALaunch pdaLaunch, final String program) throws CoreException {
		// Synchronization object to use when waiting for the services initialization.
		Query<Object> initQuery = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				pdaLaunch.initializeServices(program, rm);
			}
		};

		// Submit the query to the executor.
		pdaLaunch.getSession().getExecutor().execute(initQuery);
		try {
			// Block waiting for query results.
			initQuery.get();
		} catch (InterruptedException e1) {
			throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
		} catch (ExecutionException e1) {
			throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error in launch sequence", e1.getCause())); //$NON-NLS-1$
		}
	}

	private void createProcess(final PDALaunch pdaLaunch) throws CoreException {
		// Synchronization object to use when waiting for the services initialization.
		Query<Object[]> initQuery = new Query<Object[]>() {
			@Override
			protected void execute(DataRequestMonitor<Object[]> rm) {
				DsfServicesTracker tracker = new DsfServicesTracker(PDAPlugin.getBundleContext(),
						pdaLaunch.getSession().getId());
				PDABackend backend = tracker.getService(PDABackend.class);
				if (backend == null) {
					PDAPlugin.failRequest(rm, IDsfStatusConstants.INVALID_STATE, "PDA Backend service not available");
					return;
				}
				Object[] retVal = new Object[] { backend.getProcess(), backend.getProcessName() };
				rm.setData(retVal);
				rm.done();
			}
		};

		// Submit the query to the executor.
		pdaLaunch.getSession().getExecutor().execute(initQuery);
		try {
			// Block waiting for query results.
			Object[] processData = initQuery.get();
			DebugPlugin.newProcess(pdaLaunch, (Process) processData[0], (String) processData[1]);
		} catch (InterruptedException e1) {
			throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
		} catch (ExecutionException e1) {
			throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error in launch sequence", e1.getCause())); //$NON-NLS-1$
		}
	}

	/**
	 * Throws an exception with a new status containing the given
	 * message and optional exception.
	 *
	 * @param message error message
	 * @param e underlying exception
	 * @throws CoreException
	 */
	private void abort(String message, Throwable e) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, 0, message, e));
	}
}
