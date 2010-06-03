/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Property tester for update policy information available through the given 
 * object.  The object being tested should be either an {@link IVMContext}, 
 * through which an instance of {@link ICachingVMProvider} could be obtained.
 * Or it could be an {@link IWorkbenchPart}, which is tested to see if it
 * is a debug view through which a caching VM provider can be obtained.  
 * The Caching View Model provider is used to test the given property.
 * <p>
 * Three properties are supported:
 * <ul>
 * <li> "areUpdatePoliciesSupported" - Checks whether update policies are 
 * available at all given the receiver.</li>
 * <li> "isUpdatePolicyAvailable" - Checks whether the update policy in the 
 * expected value is available for the given receiver.</li>
 * <li> "isUpdatePolicyActive" - Checks whether the policy given in the expected 
 * value is the currently active policy for the given receiver.</li>
 * </ul>
 * </p>
 */
public class UpdatePoliciesPropertyTester extends PropertyTester {

    private static final String SUPPORTED = "areUpdatePoliciesSupported"; //$NON-NLS-1$
    private static final String AVAILABLE = "isUpdatePolicyAvailable"; //$NON-NLS-1$
    private static final String ACTIVE = "isUpdatePolicyActive"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IVMContext) {
            IVMProvider provider = ((IVMContext)receiver).getVMNode().getVMProvider();
            if (provider instanceof ICachingVMProvider) {
                return testProvider((ICachingVMProvider)provider, property, expectedValue);
            }
        } else if (receiver instanceof IDebugView) {
            IVMProvider provider = VMHandlerUtils.getVMProviderForPart((IDebugView)receiver);
            if (provider instanceof ICachingVMProvider) {
                return testProvider((ICachingVMProvider)provider, property, expectedValue);                    
            }
        }
        return false;
    }

    private boolean testProvider(ICachingVMProvider provider, String property, Object expectedValue) {
        if (SUPPORTED.equals(property)) {
            return true;
        } else if (AVAILABLE.equals(property)) {
            for (IVMUpdatePolicy policy : provider.getAvailableUpdatePolicies()) {
                if (policy.getID().equals(expectedValue)) {
                    return true;
                }
                return false;
            }
        } else if (ACTIVE.equals(property)) {
            return expectedValue != null && expectedValue.equals(provider.getActiveUpdatePolicy().getID());
        } 
        return false;
    }
    
}
