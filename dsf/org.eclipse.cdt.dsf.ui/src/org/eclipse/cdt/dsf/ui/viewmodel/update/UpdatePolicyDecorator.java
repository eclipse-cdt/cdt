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
package org.eclipse.cdt.dsf.ui.viewmodel.update;

/**
 * An update policy decorator which can override behaviour of an underlying update policy.
 * 
 * @since 1.1
 */
public abstract class UpdatePolicyDecorator implements IVMUpdatePolicy {

	private final IVMUpdatePolicy fBasePolicy;
    
    protected UpdatePolicyDecorator(IVMUpdatePolicy base) {
    	fBasePolicy= base;
    }
    
    protected final IVMUpdatePolicy getBaseUpdatePolicy() {
    	return fBasePolicy;
    }
   
    public final String getID() {
    	return fBasePolicy.getID();
    }

    public String getName() {
    	return fBasePolicy.getName();
    }

    public IElementUpdateTester getElementUpdateTester(Object event) {
        return fBasePolicy.getElementUpdateTester(event);
    }
}
