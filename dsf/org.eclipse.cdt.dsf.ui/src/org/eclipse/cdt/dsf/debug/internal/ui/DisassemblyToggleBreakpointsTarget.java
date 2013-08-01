/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl4;
import org.eclipse.cdt.dsf.debug.service.IRunControl4.IContainerDMData;
import org.eclipse.cdt.dsf.debug.ui.actions.AbstractDisassemblyBreakpointsTarget;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle breakpoint target implementation for the disassembly part.
 */
public class DisassemblyToggleBreakpointsTarget extends AbstractDisassemblyBreakpointsTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.AbstractDisassemblyBreakpointsTarget#createLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
	 */
	@Override
	protected void createLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		CDIDebugModel.createLineBreakpoint( sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", true ); //$NON-NLS-1$
	}

    @Override
    protected void createLineBreakpointInteractive(IWorkbenchPart part, String sourceHandle, IResource resource, 
        int lineNumber) throws CoreException 
    {
        ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineBreakpoint();
        Map<String, Object> attributes = new HashMap<String, Object>();
        CDIDebugModel.setLineBreakpointAttributes(
            attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0, "" ); //$NON-NLS-1$
        openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.AbstractDisassemblyBreakpointsTarget#createAddressBreakpoint(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.IAddress)
	 */
	@Override
	protected void createAddressBreakpoint( IResource resource, IAddress address ) throws CoreException {
		String module = getModuleName();
		CDIDebugModel.createAddressBreakpoint( module, null, resource, getBreakpointType(), address, true, 0, "", true ); //$NON-NLS-1$
	}

    @Override
    protected void createAddressBreakpointInteractive(IWorkbenchPart part, IResource resource, IAddress address) 
        throws CoreException 
    {
        ICLineBreakpoint lineBp = CDIDebugModel.createBlankAddressBreakpoint();
		String module = getModuleName();
        Map<String, Object> attributes = new HashMap<String, Object>();
        CDIDebugModel.setAddressBreakpointAttributes(
            attributes, module, null, getBreakpointType(), -1, address, true, 0, "" ); //$NON-NLS-1$
        openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
    }

	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}

	private String getModuleName() {
		IContainerDMData data = null;
		IAdaptable dc = DebugUITools.getDebugContext();
		if (dc instanceof IDMVMContext) {
			IDMContext dmc = ((IDMVMContext)dc).getDMContext();
			final IContainerDMContext contDmc = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
			if (contDmc == null) {
				return null;
			}
			DsfSession session = DsfSession.getSession(contDmc.getSessionId());
			if (!session.isActive()) {
				return null;
			}
			Query<IContainerDMData> query = new Query<IContainerDMData>() {

				@Override
				protected void execute(DataRequestMonitor<IContainerDMData> rm) {
					DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), contDmc.getSessionId());
					IRunControl runControl = tracker.getService(IRunControl.class);
					tracker.dispose();
					if (runControl == null) {
						rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
						rm.done();
						return;
					}
					if (!(runControl instanceof IRunControl4)) {
						rm.done();
						return;
					}
					((IRunControl4)runControl).getContainerData(contDmc, rm);
				}
			};

			session.getExecutor().execute(query);
			try {
				data = query.get();
			}
			catch(InterruptedException e) {
			}
			catch( ExecutionException e ) {
			}
			
		}
		return (data != null) ? data.getExecutable() : null;
	}
}