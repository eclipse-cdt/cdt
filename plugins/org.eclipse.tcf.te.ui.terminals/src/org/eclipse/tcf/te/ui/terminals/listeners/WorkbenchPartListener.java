/*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.listeners;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * The part listener implementation. Takes care of
 * activation and deactivation of key binding contexts.
 */
public class WorkbenchPartListener implements IPartListener2 {

	// The context activations per workbench part reference
	private final Map<IWorkbenchPartReference, IContextActivation> activations = new HashMap<IWorkbenchPartReference, IContextActivation>();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if ("org.eclipse.tcf.te.ui.terminals.TerminalsView".equals(partRef.getId())) { //$NON-NLS-1$
			IWorkbenchPart part = partRef.getPart(false);
			if (part != null && part.getSite() != null) {
				IContextService service = (IContextService)part.getSite().getService(IContextService.class);
				if (service != null) {
					IContextActivation activation = service.activateContext(partRef.getId());
					if (activation != null) {
						activations.put(partRef, activation);
					} else {
						activations.remove(partRef);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		if ("org.eclipse.tcf.te.ui.terminals.TerminalsView".equals(partRef.getId())) { //$NON-NLS-1$
			IWorkbenchPart part = partRef.getPart(false);
			if (part != null && part.getSite() != null) {
				IContextService service = (IContextService)part.getSite().getService(IContextService.class);
				if (service != null) {
					IContextActivation activation = activations.remove(partRef);
					if (activation != null) {
						service.deactivateContext(activation);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

}
