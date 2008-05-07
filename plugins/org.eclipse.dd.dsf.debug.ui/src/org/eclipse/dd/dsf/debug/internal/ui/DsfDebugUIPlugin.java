package org.eclipse.dd.dsf.debug.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.debug.internal.ui.disassembly.model.SourceDocumentProvider;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DsfDebugUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.dd.dsf.debug.ui"; //$NON-NLS-1$

	
	// The shared instance
	private static DsfDebugUIPlugin plugin;
    
    // BundleContext of this plugin
    private static BundleContext fgBundleContext;

	// The document provider for source documents in the disassembly.
	private SourceDocumentProvider fSourceDocumentProvider;

	/**
	 * The constructor
	 */
	public DsfDebugUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
        fgBundleContext = context;

		fSourceDocumentProvider = new SourceDocumentProvider();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		fSourceDocumentProvider.dispose();
		fSourceDocumentProvider = null;
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfDebugUIPlugin getDefault() {
		return plugin;
	}

    /**
     * Returns the bundle context
     *
     * @return the bundle context
     */
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
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, "Error within Debug UI: ", t); //$NON-NLS-1$
			log(status);	
		}
		ErrorDialog.openError(shell, title, message, status);
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		log(newErrorStatus("Error logged from Debug UI: ", t)); //$NON-NLS-1$
	}
	
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message the error message to log
	 */
	public static void logErrorMessage(String message) {
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log(newErrorStatus("Internal message logged from Debug UI: " + message, null)); //$NON-NLS-1$	
	}
	
	/**
	 * Returns a new error status for this plug-in with the given message
	 * @param message the message to be included in the status
	 * @param exception the exception to be included in the status or <code>null</code> if none
	 * @return a new error status
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, message, exception);
	}
}
