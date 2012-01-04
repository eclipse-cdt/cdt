/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import java.util.Map;

import org.eclipse.jface.viewers.TreePath;

/**
 * An "automatic" update policy which causes the view model provider cache to 
 * be flushed whenever an event causes a delta to be generated in the given 
 * model.
 * 
 * @since 1.0
 */
public class AutomaticUpdatePolicy implements IVMUpdatePolicy {

    public static String AUTOMATIC_UPDATE_POLICY_ID = "org.eclipse.cdt.dsf.ui.viewmodel.update.defaultUpdatePolicy";  //$NON-NLS-1$
    
    public static IElementUpdateTester fgUpdateTester = new IElementUpdateTester() {
        @Override
		public int getUpdateFlags(Object viewerInput, TreePath path) {
            return FLUSH | ARCHIVE; 
        }  
        
        @Override
		public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this);
        }

        @Override
        public String toString() {
            return "Automatic update tester"; //$NON-NLS-1$
        }
    };
    
    @Override
	public String getID() {
        return AUTOMATIC_UPDATE_POLICY_ID;
    }

    @Override
	public String getName() {
        return ViewModelUpdateMessages.AutomaticUpdatePolicy_name;
    }

    @Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
        return fgUpdateTester;
    }

    @Override
	public Object[] getInitialRootElementChildren(Object rootElement) {
        return null;
    }
    
    @Override
	public Map<String, Object> getInitialRootElementProperties(Object rootElement) {
        return null;
    }
}
