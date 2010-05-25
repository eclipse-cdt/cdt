/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.internal.ui;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.CSourceNotFoundDescriptionFactory;
import org.eclipse.cdt.dsf.debug.internal.ui.EvaluationContextManager;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyBackendDsfFactory;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceDocumentProvider;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.ui.DsfDebugUITools;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DsfUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.ui"; //$NON-NLS-1$

	// The shared instance
	private static DsfUIPlugin fgPlugin;
	
    private static BundleContext fgBundleContext; 

    // The document provider for source documents in the disassembly.
    private SourceDocumentProvider fSourceDocumentProvider;

    public static boolean DEBUG = false;

	/**
	 * The constructor
	 */
	public DsfUIPlugin() {
		fgPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
	    DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug"));  //$NON-NLS-1$//$NON-NLS-2$

        fSourceDocumentProvider = new SourceDocumentProvider();
        
		EvaluationContextManager.startup();
        
		// Register the DSF backend for our disassembly view (the CDT debug UI
		// plugin registers the CDI one)
        Platform.getAdapterManager().registerAdapters(new DisassemblyBackendDsfFactory(), IDMVMContext.class);
		// Register the factory that provides descriptions of stack frames
        // to the CSourceNotFoundEditor.
        Platform.getAdapterManager().registerAdapters(new CSourceNotFoundDescriptionFactory(), IFrameDMContext.class);

        DsfDebugUITools.enableActivity("org.eclipse.cdt.debug.ui.cdtActivity", true); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
        fSourceDocumentProvider.dispose();
        fSourceDocumentProvider = null;
		fgPlugin = null;
        fgBundleContext = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfUIPlugin getDefault() {
		return fgPlugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }
    
    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static SourceDocumentProvider getSourceDocumentProvider() {
        return getDefault().fSourceDocumentProvider;
    }
    

    /**
     * If the debug flag is set the specified message is printed to the console
     * @param message
     */
    public static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    /**
     * Logs the specified status with this plug-in's log.
     * 
     * @param status status to log
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Logs the specified throwable with this plug-in's log.
     * 
     * @param t throwable to log 
     */
    public static void log(Throwable t) {
        log(newErrorStatus(IDsfStatusConstants.INTERNAL_ERROR, "Error logged from Debug UI: ", t)); //$NON-NLS-1$
    }
    
    /**
     * Logs an internal error with the specified message.
     * 
     * @param message the error message to log
     */
    public static void logErrorMessage(String message) {
        // this message is intentionally not internationalized, as an exception may
        // be due to the resource bundle itself
        log(newErrorStatus(IDsfStatusConstants.INTERNAL_ERROR, "Internal message logged from Debug UI: " + message, null)); //$NON-NLS-1$   
    }
    
    /**
     * Returns a new error status for this plug-in with the given message
     * 
     * @param message the message to be included in the status
     * @param error code
     * @param exception the exception to be included in the status or <code>null</code> if none
     * @return a new error status
     * 
     * @since 2.0
     */
    public static IStatus newErrorStatus(int code, String message, Throwable exception) {
        return new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, code, message, exception);
    }

}
