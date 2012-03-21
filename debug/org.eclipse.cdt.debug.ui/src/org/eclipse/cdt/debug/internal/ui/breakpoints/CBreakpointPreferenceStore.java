/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     QNX Software Systems - Refactored to use platform implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A preference store that presents the state of the properties of a C/C++ breakpoint. 
 */
public class CBreakpointPreferenceStore implements IPersistentPreferenceStore {

	// This map is the current properties/values being maintained/manipulated
    private HashMap<String, Object> fProperties = new HashMap<String, Object>();
    
    // Original set of values. So we can see what has really changed on the save and
    // perform appropriate change operations. We only really want to operate on changed
    // values, to avoid generating churn.
    private HashMap<String, Object> fOriginalValues = new HashMap<String, Object>();
    private boolean fIsDirty = false; 
    private boolean fIsCanceled = false;
    private ListenerList fListeners;
    private final CBreakpointContext fContext;
    
    // TODO: remove after fixing add event breapoint dialog.
    public CBreakpointPreferenceStore() {
        this (null, null);
    }

    public CBreakpointPreferenceStore(CBreakpointContext context, Map<String, Object> attributes) {
        fListeners = new ListenerList(org.eclipse.core.runtime.ListenerList.IDENTITY);
        fContext = context;
        
        fOriginalValues.clear();
        fProperties.clear();
        if (context != null) {
            IMarker marker = context.getBreakpoint().getMarker();
            if (marker != null) {
                Map<String, Object> bpAttrs = Collections.emptyMap();
                try {
                    bpAttrs = marker.getAttributes();
                    fOriginalValues.putAll(bpAttrs);
                    fProperties.putAll(bpAttrs);
                } catch (CoreException e) {
                    DebugPlugin.log(e);
                }
            } 
        }
        if (attributes != null) {
            fProperties.putAll(attributes);
            fIsDirty = true;
        }
    }

    public Map<String, Object> getAttributes() {
        return fProperties;
    }
    
    public void setCanceled(boolean canceled) {
        fIsCanceled = canceled;
    }
    
    public void save() throws IOException {
        if (!fIsCanceled && fContext != null && fContext.getBreakpoint() != null) {
            ICBreakpoint bp = fContext.getBreakpoint();
            if (bp.getMarker() != null && fIsDirty) {
                saveToExistingMarker(bp, bp.getMarker());
            }
            else if (fContext.getResource() != null){
                saveToNewMarker(bp, fContext.getResource());
            } else {
                throw new IOException("Unable to create breakpoint: no resource specified."); //$NON-NLS-1$
            }
        }
        
    }
    
    private void saveToExistingMarker(final ICBreakpoint breakpoint, final IMarker marker) throws IOException {
		final List<String> changedProperties = new ArrayList<String>( 5 );
		Set<String> valueNames = fProperties.keySet();
		for ( String name : valueNames ) {
			if ( fProperties.containsKey( name ) ) {
				Object originalObject = fOriginalValues.get( name );
				Object currentObject  = fProperties.get( name );
				if ( originalObject == null ) {
					changedProperties.add( name );
				}
				else if ( ! originalObject.equals( currentObject ) ) {
					changedProperties.add( name );
				}
			}
		}
		if ( ! changedProperties.isEmpty() ) {
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {
				public void run( IProgressMonitor monitor ) throws CoreException {
					Iterator<String> changed = changedProperties.iterator();
					while( changed.hasNext() ) {
						String property = changed.next();
						if ( property.equals( ICBreakpoint.ENABLED ) ) {
							breakpoint.setEnabled( getBoolean( ICBreakpoint.ENABLED ) );
						}
						else if ( property.equals( ICBreakpoint.IGNORE_COUNT ) ) {
							breakpoint.setIgnoreCount( getInt( ICBreakpoint.IGNORE_COUNT ) );
						}
						else if ( property.equals( ICBreakpoint.CONDITION ) ) {
							breakpoint.setCondition( getString( ICBreakpoint.CONDITION ) );
						}
						else if ( property.equals( IMarker.LINE_NUMBER ) ) {
							// already workspace runnable, setting markers are safe
							breakpoint.getMarker().setAttribute(IMarker.LINE_NUMBER, getInt(IMarker.LINE_NUMBER));
							breakpoint.getMarker().setAttribute(ICLineBreakpoint2.REQUESTED_LINE, getInt(IMarker.LINE_NUMBER));
						} else {
						    // this allow set attributes contributed by other plugins
							Object value = fProperties.get(property);
							if ( value != null ) {
								marker.setAttribute(property, value);
							}
						}
					}
				}
			};
			try {
				ResourcesPlugin.getWorkspace().run( wr, null );
			}
			catch( CoreException ce ) {
	            throw new IOException("Cannot save properties to breakpoint.", ce); //$NON-NLS-1$
			}
    	}
    }

    private void saveToNewMarker(final ICBreakpoint breakpoint, final IResource resource) throws IOException {
        try {
            CDIDebugModel.createBreakpointMarker(breakpoint, resource, fProperties, true);
        }
        catch( CoreException ce ) {
            throw new IOException("Cannot save properties to new breakpoint.", ce); //$NON-NLS-1$
        }
    }
    
    ///////////////////////////////////////////////////////////////////////
    // IPreferenceStore
    
    public boolean needsSaving() { 
        return fIsDirty && !fIsCanceled; 
    }

    public boolean contains(String name) {
        return fProperties.containsKey(name);
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        fListeners.add(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        fListeners.remove(listener);
    }

    public void firePropertyChangeEvent(String name,
                                        Object oldValue,
                                        Object newValue) 
    {
        Object[] listeners = fListeners.getListeners();
        // Do we need to fire an event.
        if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
            PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
            for (int i = 0; i < listeners.length; ++i) {
                IPropertyChangeListener l = (IPropertyChangeListener) listeners[i];
                l.propertyChange(pe);
            }
        }
    }
    
    public boolean getBoolean(String name) {
        boolean retVal = false;
        Object o = fProperties.get(name);
        if (o instanceof Boolean) {
            retVal = ((Boolean)o).booleanValue();
        }
        return retVal;
    }

    public int getInt(String name) {
        int retVal = 0;
        Object o = fProperties.get(name);
        if (o instanceof Integer) {
            retVal = ((Integer)o).intValue();
        }
        return retVal;
    }

    public String getString(String name) {
        String retVal = null;
        Object o = fProperties.get(name);
        if (o instanceof String) {
            retVal = (String)o;
        }
        return retVal;
    }

    public double getDouble(String name) { return 0; }
    public float getFloat(String name) { return 0; }
    public long getLong(String name) { return 0; }

    public boolean isDefault(String name) { return false; }

    public boolean getDefaultBoolean(String name) { return false; }
    public double getDefaultDouble(String name) { return 0; }
    public float getDefaultFloat(String name) { return 0; }
    public int getDefaultInt(String name) { return 0; }
    public long getDefaultLong(String name) { return 0; }
    public String getDefaultString(String name) { return null; }

    public void putValue(String name, String value) {
        Object oldValue = fProperties.get(name);
        if ( oldValue == null || !oldValue.equals(value) ) {
            fProperties.put(name, value);
            setDirty(true);
        }
    }

    public void setDefault(String name, double value) {}
    public void setDefault(String name, float value) {}
    public void setDefault(String name, int value) {}
    public void setDefault(String name, long value) {}
    public void setDefault(String name, String defaultObject) {}
    public void setDefault(String name, boolean value) {}
    public void setToDefault(String name) {}

    public void setValue(String name, boolean value) {
        boolean oldValue = getBoolean(name);
        if (oldValue != value) {
            fProperties.put( name, new Boolean(value) );
            setDirty(true);
            firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(value) );
        }
    }

    public void setValue(String name, int value) {
        int oldValue = getInt(name);
        if (oldValue != value) {
            fProperties.put( name, new Integer(value) );
            setDirty(true);
            firePropertyChangeEvent(name, new Integer(oldValue), new Integer(value) );
        }
    }

    public void setValue(String name, String value) {
        Object oldValue = fProperties.get(name);
        if ( (oldValue == null && value != null) ||
             (oldValue != null && !oldValue.equals(value)) ) 
        {
            fProperties.put(name, value);
            setDirty(true);
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    public void setValue(String name, float value) {}        
    public void setValue(String name, double value) {}
    public void setValue(String name, long value) {}

    // IPreferenceStore
    ///////////////////////////////////////////////////////////////////////        

    private void setDirty(boolean isDirty) {
        fIsDirty = isDirty;
    }
}
