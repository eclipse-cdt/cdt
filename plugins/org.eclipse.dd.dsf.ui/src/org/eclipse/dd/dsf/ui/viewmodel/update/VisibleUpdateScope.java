/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel.update;

import org.eclipse.jface.viewers.TreePath;

/**
 * An "automatic" update policy which causes the view model provider cache to 
 * be flushed whenever an event causes a delta to be generated in the given 
 * model.
 */
public class VisibleUpdateScope implements IVMUpdateScope {

    public static String VISIBLE_UPDATE_SCOPE_ID = "org.eclipse.dd.dsf.ui.viewmodel.update.visibleUpdateScope";  //$NON-NLS-1$
    
    public static IElementUpdateTester fgUpdateTester = new IElementUpdateTester() {
        public int getUpdateFlags(Object viewerInput, TreePath path) {
           return 0;
        }  
        
        public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this);
        }

        @Override
        public String toString() {
            return ViewModelUpdateMessages.VisibleUpdateScope_name;
        }
    };
    
    public String getID() {
        return VISIBLE_UPDATE_SCOPE_ID;
    }

    public String getName() {
        return ViewModelUpdateMessages.VisibleUpdateScope_name;
    }
}
