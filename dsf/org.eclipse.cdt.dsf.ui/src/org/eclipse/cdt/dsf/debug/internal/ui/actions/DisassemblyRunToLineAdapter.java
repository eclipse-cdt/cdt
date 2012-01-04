/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.actions;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblySelection;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Run to line target adapter for the DSF Disassembly view
 * 
 * @since 2.1
 */
public class DisassemblyRunToLineAdapter implements IRunToLineTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	@Override
	public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		if (part instanceof IDisassemblyPart && selection instanceof ITextSelection) {
			if (!(selection instanceof IDisassemblySelection)) {
				selection = new DisassemblySelection((ITextSelection)selection, (IDisassemblyPart)part);
			}
			IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
			final IAddress address = disassemblySelection.getStartAddress();

			if (address != null && target instanceof IAdaptable) {
				final IRunToAddress runToAddress = (IRunToAddress)((IAdaptable)target).getAdapter(IRunToAddress.class);
				if (runToAddress != null && runToAddress.canRunToAddress(address)) {
					try {
						boolean skipBreakpoints = DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);
						runToAddress.runToAddress(address, skipBreakpoints);								
					}
					catch(DebugException e) {
						failed(e);
					}								
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	@Override
	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
		if (target instanceof IAdaptable && part instanceof IDisassemblyPart && selection instanceof ITextSelection) {
			IRunToAddress runToAddress = (IRunToAddress)((IAdaptable)target).getAdapter(IRunToAddress.class);
			if (runToAddress == null) {
				return false;
			}
			
			if (!(selection instanceof IDisassemblySelection)) {
				selection = new DisassemblySelection((ITextSelection)selection, (IDisassemblyPart)part);
			}
			IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
			final IAddress address = disassemblySelection.getStartAddress();
			if (address == null) {
				return false;
			}

			return runToAddress.canRunToAddress(address);
		}

		return false;
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), IDsfStatusConstants.REQUEST_FAILED, "RunToLine failed", null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), IDsfStatusConstants.REQUEST_FAILED, e.getMessage(), e ) );
		DsfUIPlugin.log(ms);
	}
}
