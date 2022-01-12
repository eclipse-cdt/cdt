/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Default model selection policy factory for DSF.
 * @since 1.1
 */
public class DefaultDsfModelSelectionPolicyFactory implements IModelSelectionPolicyFactory {

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory#createModelSelectionPolicyAdapter(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	public IModelSelectionPolicy createModelSelectionPolicyAdapter(Object element, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (element instanceof IDMVMContext) {
				IDMVMContext dmvmContext = (IDMVMContext) element;
				IDMContext dmContext = dmvmContext.getDMContext();
				if (dmContext != null) {
					return new DefaultDsfSelectionPolicy(dmContext);
				}
			}
		}
		return null;
	}

}
