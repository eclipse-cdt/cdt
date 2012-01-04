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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.RGB;


/**
 * An "manual" update policy which causes the view model provider cache to be 
 * flushed only as a result of an explicit user action.    
 * 
 * @since 1.0
 */
public class ManualUpdatePolicy implements IVMUpdatePolicy {

    public static String MANUAL_UPDATE_POLICY_ID = "org.eclipse.cdt.dsf.ui.viewmodel.update.manualUpdatePolicy";  //$NON-NLS-1$
    
    public static Object REFRESH_EVENT = new Object();

    private static class BlankDataElement implements IElementContentProvider, IElementLabelProvider {
        
        @Override
		public void update(IHasChildrenUpdate[] updates) {
            for (IHasChildrenUpdate update : updates) {
                update.setHasChilren(false);
                update.done();
            }
        }
        
        @Override
		public void update(IChildrenCountUpdate[] updates) {
            for (IChildrenCountUpdate update : updates) {
                update.setChildCount(0);
                update.done();
            }
        }
        
        @Override
		public void update(IChildrenUpdate[] updates) {
            for (IChildrenUpdate update : updates) {
                update.done();
            }
        }
        
        @Override
		public void update(ILabelUpdate[] updates) {
            RGB staleDataForeground = JFaceResources.getColorRegistry().getRGB(
                IDsfDebugUIConstants.PREF_COLOR_STALE_DATA_FOREGROUND);
            RGB staleDataBackground = JFaceResources.getColorRegistry().getRGB(
                IDsfDebugUIConstants.PREF_COLOR_STALE_DATA_BACKGROUND);
            for (ILabelUpdate update : updates) {
                update.setLabel(ViewModelUpdateMessages.ManualUpdatePolicy_InitialDataElement__label, 0);
                // Set the stale data color to the label.  Use foreground color if column modes are enabled, and 
                // background color when there are no columns.  
                if (update.getColumnIds() != null) {
                    update.setForeground(staleDataForeground, 0);
                } else {
                    update.setBackground(staleDataBackground, 0);
                }
                update.done();
            }
        }
    }

    private static class UserEditEventUpdateTester implements IElementUpdateTester {
        private final Set<Object> fElements;
        
        public UserEditEventUpdateTester(Set<Object> elements) {
            fElements = elements;
        }

        @Override
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
        
        @Override
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
        @Override
		public int getUpdateFlags(Object viewerInput, TreePath path) {
            return DIRTY; 
        }  
        
        @Override
		public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this);
        }
        
        @Override
        public String toString() {
            return "Manual (refresh = false) update tester"; //$NON-NLS-1$
        }
    };

    private static IElementUpdateTester fgRefreshUpdateTester = new IElementUpdateTester() {
        @Override
		public int getUpdateFlags(Object viewerInput, TreePath path) {
            return FLUSH | ARCHIVE; 
        }  
        
        @Override
		public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this) || tester.equals(fgUpdateTester) || tester instanceof UserEditEventUpdateTester;
        }

        @Override
        public String toString() {
            return "Manual (refresh = true) update tester"; //$NON-NLS-1$
        }
    };
    
    @Override
	public String getID() {
        return MANUAL_UPDATE_POLICY_ID;
    }

    @Override
	public String getName() {
        return ViewModelUpdateMessages.ManualUpdatePolicy_name;
    }

    @Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
        if (event.equals(REFRESH_EVENT)) {
            return fgRefreshUpdateTester;
        } else if (event instanceof UserEditEvent) {
            return new UserEditEventUpdateTester(((UserEditEvent)event).getElements());
        }
        return fgUpdateTester;
    }

    @Override
	public Object[] getInitialRootElementChildren(Object rootElement) {
        // Return an dummy element to show in the view.  The user will 
        // need to refresh the view to retrieve this data from the model.
        return new Object[] { new BlankDataElement() };
    }

    @Override
	public Map<String, Object> getInitialRootElementProperties(Object rootElement) {
        // Return an empty set of properties for the root element.  The user will 
        // need to refresh the view to retrieve this data from the model.
        return Collections.emptyMap();
    }
}
