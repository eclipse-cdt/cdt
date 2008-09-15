/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch;

import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * Default DSF selection policy implementation modelled after platform version
 * (<code>DefaultSelectionPolicy</code>).
 * @since 1.1
 */
@SuppressWarnings("restriction")
public class DefaultDsfSelectionPolicy implements IModelSelectionPolicy {

	private IDMContext fDMContext;

	/**
	 * Create selection policy instance for the given data model context.
	 * 
	 * @param dmContext
	 */
	public DefaultDsfSelectionPolicy(IDMContext dmContext) {
		fDMContext= dmContext;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#contains(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean contains(ISelection selection, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				Object element= ss.getFirstElement();
				if (element instanceof IDMVMContext) {
					IDMVMContext dmvmContext= (IDMVMContext) element;
					IDMContext dmContext= dmvmContext.getDMContext();
					if (dmContext != null) {
						return fDMContext.getSessionId().equals(dmContext.getSessionId());
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#isSticky(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean isSticky(ISelection selection, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				Object element= ss.getFirstElement();
				return isSticky(element);
			}
		}
		return false;
	}

	protected boolean isSticky(Object element) {
		if (element instanceof IDMVMContext) {
			IDMVMContext dmvmContext= (IDMVMContext) element;
			IDMContext dmContext= dmvmContext.getDMContext();
			if (dmContext instanceof IFrameDMContext) {
				IExecutionDMContext execContext= DMContexts.getAncestorOfType(dmContext, IExecutionDMContext.class);
				if (execContext != null) {
					DsfServicesTracker servicesTracker = new DsfServicesTracker(DsfDebugUIPlugin.getBundleContext(), dmContext.getSessionId());
					try {
						IRunControl runControl= servicesTracker.getService(IRunControl.class);
						if (runControl != null) {
							if (runControl.isSuspended(execContext)) {
								return true;
							}
						}
					} finally {
						servicesTracker.dispose();
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#overrides(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (existing instanceof IStructuredSelection && candidate instanceof IStructuredSelection) {
				IStructuredSelection ssExisting = (IStructuredSelection) existing;
				IStructuredSelection ssCandidate = (IStructuredSelection) candidate;
				return overrides(ssExisting.getFirstElement(), ssCandidate.getFirstElement());
			}
		}
		return true;
	}

	
	protected boolean overrides(Object existing, Object candidate) {
		if (existing == null || existing.equals(candidate)) {
			return true;
		}
		if (existing instanceof IDMVMContext && candidate instanceof IDMVMContext) {
			IDMContext curr = ((IDMVMContext) existing).getDMContext();
			IDMContext cand = ((IDMVMContext) candidate).getDMContext();
			if (curr instanceof IFrameDMContext && cand instanceof IFrameDMContext) {
				IExecutionDMContext currExecContext= DMContexts.getAncestorOfType(curr, IExecutionDMContext.class);
				if (currExecContext != null) {
					IExecutionDMContext candExecContext= DMContexts.getAncestorOfType(cand, IExecutionDMContext.class);
					return currExecContext.equals(candExecContext) || !isSticky(existing);
				}
			}
		}
		return !isSticky(existing);
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#replaceInvalidSelection(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection)
	 */
	public ISelection replaceInvalidSelection(ISelection invalidSelection, ISelection newSelection) {
        if (invalidSelection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection)invalidSelection;
            if (treeSelection.getPaths().length == 1) {
                TreePath path = treeSelection.getPaths()[0];
                return new TreeSelection(path.getParentPath());
            }
        }
        return newSelection;
	}

}
