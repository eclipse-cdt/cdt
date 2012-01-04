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
import org.eclipse.cdt.debug.core.model.IResumeAtAddress;
import org.eclipse.cdt.debug.internal.ui.actions.IResumeAtLineTarget;
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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Resume at line target adapter for the DSF Disassembly view
 * 
 * @since 2.1
 */
public class DisassemblyResumeAtLineAdapter implements IResumeAtLineTarget {

	@Override
	public void resumeAtLine(IWorkbenchPart part, ISelection selection,	ISuspendResume target) throws CoreException {
		if (part instanceof IDisassemblyPart && selection instanceof ITextSelection) {
			if (!(selection instanceof IDisassemblySelection)) {
				selection = new DisassemblySelection((ITextSelection)selection, (IDisassemblyPart)part);
			}
			IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
			final IAddress address = disassemblySelection.getStartAddress();

	    	if (address != null && target instanceof IAdaptable) {
	    		final IResumeAtAddress resumeAtAddress = (IResumeAtAddress)((IAdaptable)target).getAdapter(IResumeAtAddress.class);
	    		if (resumeAtAddress != null && resumeAtAddress.canResumeAtAddress(address)) {
	    			try {
	    				resumeAtAddress.resumeAtAddress(address);								
	    			}
	    			catch(DebugException e) {
	    				failed(e);
	    			}								
	    		}
	    	}
		}
	}

	@Override
	public boolean canResumeAtLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
		if (target instanceof IAdaptable && part instanceof IDisassemblyPart && selection instanceof ITextSelection) {
			IResumeAtAddress resumeAtAddress = (IResumeAtAddress)((IAdaptable)target).getAdapter(IResumeAtAddress.class);
			if (resumeAtAddress == null) {
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

			return resumeAtAddress.canResumeAtAddress(address);
		}

		return false;
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus(CDIDebugModel.getPluginIdentifier(), IDsfStatusConstants.REQUEST_FAILED, "Resume At Line failed", null); //$NON-NLS-1$
		ms.add( new Status(IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), IDsfStatusConstants.REQUEST_FAILED, e.getMessage(), e));
		DsfUIPlugin.log(ms);
	}
}
