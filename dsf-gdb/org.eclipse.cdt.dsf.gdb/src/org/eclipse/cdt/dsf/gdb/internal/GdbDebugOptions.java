package org.eclipse.cdt.dsf.gdb.internal;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

public class GdbDebugOptions implements DebugOptionsListener {
	
	public static final String DEBUG_FLAG = "org.eclipse.cdt.dsf.gdb/debug"; //$NON-NLS-1$
	public static final String DEBUG_RX_FLAG = "org.eclipse.cdt.dsf.gdb/debug/rx"; //$NON-NLS-1$
	public static final String DEBUG_TX_FLAG = "org.eclipse.cdt.dsf.gdb/debug/tx"; //$NON-NLS-1$
	
	public static boolean DEBUG = false;
	public static boolean DEBUG_RX = false;
	public static boolean DEBUG_TX = false;
	
	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 * @since 3.8
	 */
	private static DebugTrace fgDebugTrace;
	
	/**
	 * Constructor
	 */
	public GdbDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, GdbPlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}
	
	// used to format debug messages
	public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$

	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(GdbPlugin.getUniqueIdentifier());
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_RX = DEBUG && options.getBooleanOption(DEBUG_RX_FLAG, false);
		DEBUG_TX = DEBUG && options.getBooleanOption(DEBUG_TX_FLAG, false);
	}
	
	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 * @since 3.8
	 */
	public static void trace(String option, String message, Throwable throwable) {
		System.out.println(message);
		if(fgDebugTrace != null) {
			fgDebugTrace.trace(option, message, throwable);
		}
	}
	
	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 * 
	 * @param message the message or <code>null</code>
	 * @since 3.8
	 */
	public static void trace(String message) {
		trace(null, message, null);
	}

}
