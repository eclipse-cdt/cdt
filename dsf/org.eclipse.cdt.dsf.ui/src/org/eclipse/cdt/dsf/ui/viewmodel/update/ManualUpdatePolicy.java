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
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import java.util.Set;

import org.eclipse.jface.viewers.TreePath;


/**
 * An "manual" update policy which causes the view model provider cache to be 
 * flushed only as a result of an explicit user action.    
 */
public class ManualUpdatePolicy implements IVMUpdatePolicy {

    public static String MANUAL_UPDATE_POLICY_ID = "org.eclipse.cdt.dsf.ui.viewmodel.update.manualUpdatePolicy";  //$NON-NLS-1$
    
    public static Object REFRESH_EVENT = new Object();

    private static class UserEditEventUpdateTester implements IElementUpdateTester {
        private final Set<Object> fElements;
        
        public UserEditEventUpdateTester(Set<Object> elements) {
            fElements = elements;
        }

        public int getUpdateFlags(Object viewerInput, TreePath path) {
            if (fElements.contains(viewerInput)) {
                return FLUSH;
            }
            for (int i = 0; i < path.getSegmentCount(); i++) {
                if (fElements.contains(path.getSegment(i))) {
                    return FLUSH;
                }
            }
            return 0;
        }
        
        public boolean includes(IElementUpdateTester tester) {
            return 
                tester instanceof UserEditEventUpdateTester &&
                fElements.equals(((UserEditEventUpdateTester)tester).fElements);
        }
        
        @Override
        public String toString() {
            return "Edit (" + fElements + ") update tester"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private static IElementUpdateTester fgUpdateTester = new IElementUpdateTester() {
        public int getUpdateFlags(Object viewerInput, TreePath path) {
            return DIRTY; 
        }  
        
        public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this);
        }
        
        @Override
        public String toString() {
            return "Manual (refresh = false) update tester"; //$NON-NLS-1$
        }
    };

    private static IElementUpdateTester fgRefreshUpdateTester = new IElementUpdateTester() {
        public int getUpdateFlags(Object viewerInput, TreePath path) {
            return FLUSH | ARCHIVE; 
        }  
        
        public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this) || tester.equals(fgUpdateTester) || tester instanceof UserEditEventUpdateTester;
        }

        @Override
        public String toString() {
            return "Manual (refresh = true) update tester"; //$NON-NLS-1$
        }
    };
    
    public String getID() {
        return MANUAL_UPDATE_POLICY_ID;
    }

    public String getName() {
        return ViewModelUpdateMessages.ManualUpdatePolicy_name;
    }

    public IElementUpdateTester getElementUpdateTester(Object event) {
        if (event.equals(REFRESH_EVENT)) {
            return fgRefreshUpdateTester;
        } else if (event instanceof UserEditEvent) {
            return new UserEditEventUpdateTester(((UserEditEvent)event).getElements());
        }
        return fgUpdateTester;
    }

}
