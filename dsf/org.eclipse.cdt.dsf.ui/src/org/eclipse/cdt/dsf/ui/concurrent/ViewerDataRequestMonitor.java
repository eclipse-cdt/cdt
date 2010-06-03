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
package org.eclipse.cdt.dsf.ui.concurrent;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * Data Request monitor that takes <code>IViewerUpdate</code> as a parent.
 * If the IViewerUpdate is canceled, this request monitor becomes canceled as well.
 * @see IViewerUpdate
 * 
 * @since 1.0
 */
public class ViewerDataRequestMonitor<V> extends DataRequestMonitor<V> {

    /**
     * Same as {@link DsfExecutable#DEBUG_MONITORS} 
     */
    static private boolean DEBUG_MONITORS = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.cdt.dsf/debug/monitors")); //$NON-NLS-1$
    

    private final IViewerUpdate fUpdate;


    public ViewerDataRequestMonitor(Executor executor, IViewerUpdate update) {
        super(executor, null);
        fUpdate = update;
        
        if (DEBUG_MONITORS) {
        	createMonitorBacktrace();
        }
    }
    
    @Override
    public synchronized boolean isCanceled() { 
        return fUpdate.isCanceled() || super.isCanceled();
    }
    
    @Override
    protected void handleSuccess() {
        fUpdate.done();
    }

    @Override
    protected void handleErrorOrWarning() {
        fUpdate.setStatus(getStatus());
        fUpdate.done();
    }
    
    @Override
    protected void handleCancel() {
        fUpdate.setStatus(getStatus());
        fUpdate.done();
    }

	/**
	 * Instrument this object with a backtrace of the monitors this instance is
	 * chained to. See {@link DsfExecutable#DEBUG_MONITORS}. The logic here has
	 * to subvert Java access protection by using reflection. That's OK; this is
	 * not production code. This stuff will only ever run when tracing is turned
	 * on.
	 */
    private void createMonitorBacktrace() {
    	StringBuilder str = new StringBuilder();
    	
    	RequestMonitor nextrm = this;
    	VMViewerUpdate nextupdate = null; 
    	String type = null;
    	while (true) {
    		StackTraceElement topFrame = null;
    		if (nextupdate != null) {
    			type = "update "; //$NON-NLS-1$  extra space to match length of 'monitor'  
    			topFrame = getCreatedAtTopFrame(nextupdate);
    			nextrm = getMonitor(nextupdate);
    			nextupdate = null;
    		}
    		else if (nextrm != null) {
				type = "monitor"; //$NON-NLS-1$
    			topFrame = getCreatedAtTopFrame(nextrm);
    			if (nextrm instanceof ViewerDataRequestMonitor<?>) {
    				ViewerDataRequestMonitor<?> vdrm = (ViewerDataRequestMonitor<?>)nextrm;
    				nextupdate = (vdrm.fUpdate instanceof VMViewerUpdate) ? (VMViewerUpdate)vdrm.fUpdate : null;
    				nextrm = null;
    			}
    			else {
    				nextrm = getParentMonitor(nextrm);
    				nextupdate = null;
    			}
    		}
    		else { 
    			break;
    		}
			if (topFrame != null) {
				str.append("[" + type + "] " + topFrame + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else {
				str.append("<unknown>\n"); //$NON-NLS-1$
			}
    	}

		Field field;
		try {
			field = RequestMonitor.class.getDeclaredField("fMonitorBacktrace"); //$NON-NLS-1$
			field.setAccessible(true);
			field.set(this, str.toString());
		} catch (IllegalAccessException e) {			
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
    }

	/**
	 * Utility used by {@link #createMonitorBacktrace()}. Subverts access
	 * protection.
	 */
    private static RequestMonitor getMonitor(VMViewerUpdate update) {
    	try {
			Field field = VMViewerUpdate.class.getDeclaredField("fRequestMonitor"); //$NON-NLS-1$
			field.setAccessible(true);
			return (RequestMonitor) field.get(update);
		} catch (IllegalAccessException e) {
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (ClassCastException e) {
		}
		return null;
    }

	/**
	 * Utility used by {@link #createMonitorBacktrace()}. Subverts access
	 * protection.
	 */
    private static RequestMonitor getParentMonitor(RequestMonitor rm) {
    	try {
			Field field = RequestMonitor.class.getDeclaredField("fParentRequestMonitor"); //$NON-NLS-1$
			field.setAccessible(true);
			return (RequestMonitor) field.get(rm);
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		}
		return null;
    }

	/**
	 * Utility used by {@link #createMonitorBacktrace()}. Subverts access
	 * protection.
	 */
    private static <T extends DsfExecutable> StackTraceElement getCreatedAtTopFrame(T dsfExecutable) {
    	try {
			Field field_fCreatedAt = DsfExecutable.class.getDeclaredField("fCreatedAt"); //$NON-NLS-1$
			field_fCreatedAt.setAccessible(true);
			Object obj_fCreatedAt = field_fCreatedAt.get(dsfExecutable);
			Class<?> class_StackTraceElement = Class.forName("org.eclipse.cdt.dsf.concurrent.StackTraceWrapper"); //$NON-NLS-1$
			Field field_fStackTraceElements = class_StackTraceElement.getDeclaredField("fStackTraceElements"); //$NON-NLS-1$
			field_fStackTraceElements.setAccessible(true);
			StackTraceElement[] frames = (StackTraceElement[])field_fStackTraceElements.get(obj_fCreatedAt);
			if (frames != null && frames.length > 0) {
				return frames[0];
			}
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		} catch (ClassNotFoundException e) {
		}
		return null;
    }
}
