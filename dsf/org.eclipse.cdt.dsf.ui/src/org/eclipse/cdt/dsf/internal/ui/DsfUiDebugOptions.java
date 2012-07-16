package org.eclipse.cdt.dsf.internal.ui;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

public class DsfUiDebugOptions implements DebugOptionsListener{
	
	private static final String DEBUG_FLAG = "org.eclipse.cdt.dsf.ui/debug"; //$NON-NLS-1$
	private static final String DEBUG_VM_CONTENT_PROVIDER_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/contentProvider"; //$NON-NLS-1$
	private static final String DEBUG_VM_DELTA_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/delta"; //$NON-NLS-1$
	private static final String DEBUG_VM_CACHE_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/cache"; //$NON-NLS-1$
	private static final String DEBUG_VM_PRESENTATION_ID_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/presentationId"; //$NON-NLS-1$
	private static final String DEBUG_VM_ATOMIC_UPDATE_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/atomicUpdate"; //$NON-NLS-1$
	private static final String DEBUG_STEPPING_FLAG = "org.eclipse.cdt.dsf.ui/debug/stepping"; //$NON-NLS-1$
	private static final String DEBUG_DISASSEMBLY_FLAG = "org.eclipse.cdt.dsf.ui/debug/disassembly"; //$NON-NLS-1$
	private static final String DEBUG_VM_UPDATES_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/updates"; //$NON-NLS-1$
	private static final String DEBUG_VM_UPDATES_REGEX_FLAG = "org.eclipse.cdt.dsf.ui/debug/vm/updates/regex"; //$NON-NLS-1$
	private static final String DEBUG_SOURCE_DISPLAY_ADAPTER_FLAG = "org.eclipse.cdt.dsf.ui/debug/source/display/adapter"; //$NON-NLS-1$
			
	public static boolean DEBUG = false;	
	public static boolean DEBUG_VM_CONTENT_PROVIDER = false;
	public static boolean DEBUG_VM_DELTA = false;
	public static boolean DEBUG_VM_CACHE = false;
	public static String DEBUG_VM_PRESENTATION_ID = ""; //$NON-NLS-1$
	public static boolean DEBUG_VM_ATOMIC_UPDATE = false;
	public static boolean DEBUG_STEPPING = false;
	public static boolean DEBUG_DISASSEMBLY = false;
	public static boolean DEBUG_VM_UPDATES = false;
	public static String DEBUG_VM_UPDATES_REGEX = ""; //$NON-NLS-1$
	public static boolean DEBUG_SOURCE_DISPLAY_ADAPTER = false;

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;

	/**
	 * Constructor
	 */
	public DsfUiDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, DsfUIPlugin.PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(DsfUIPlugin.PLUGIN_ID);
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_VM_CONTENT_PROVIDER = options.getBooleanOption(DEBUG_VM_CONTENT_PROVIDER_FLAG, false);
		DEBUG_VM_DELTA = options.getBooleanOption(DEBUG_VM_DELTA_FLAG, false);
		DEBUG_VM_CACHE = options.getBooleanOption(DEBUG_VM_CACHE_FLAG, false);
		DEBUG_VM_PRESENTATION_ID = options.getOption(DEBUG_VM_PRESENTATION_ID_FLAG, ""); //$NON-NLS-1$
		DEBUG_VM_ATOMIC_UPDATE = options.getBooleanOption(DEBUG_VM_ATOMIC_UPDATE_FLAG, false);
		DEBUG_STEPPING = options.getBooleanOption(DEBUG_STEPPING_FLAG, false);
		DEBUG_DISASSEMBLY = options.getBooleanOption(DEBUG_DISASSEMBLY_FLAG, false);
		DEBUG_VM_UPDATES = options.getBooleanOption(DEBUG_VM_UPDATES_FLAG, false);
		DEBUG_VM_UPDATES_REGEX = options.getOption(DEBUG_VM_UPDATES_REGEX_FLAG, ""); //$NON-NLS-1$
		DEBUG_SOURCE_DISPLAY_ADAPTER = options.getBooleanOption(DEBUG_SOURCE_DISPLAY_ADAPTER_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		//divide the string into substrings of 100 chars or less for printing
		//to console
		String systemPrintableMessage = message; 
		while (systemPrintableMessage.length() > 100) {
			String partial = systemPrintableMessage.substring(0, 100); 
			systemPrintableMessage = systemPrintableMessage.substring(100);
			System.out.println(partial + "\\"); //$NON-NLS-1$
		}
		System.out.println(systemPrintableMessage);
		//then pass the original message to be traced into a file
		if(fgDebugTrace != null) {
			fgDebugTrace.trace(option, message, throwable);
		}
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 *
	 * @param message the message or <code>null</code>
	 */
	public static void trace(String message) {
		trace(null, message, null);
	}
	
	/**
	 * Looks at the optional filter (regular expression) set in the tracing
	 * options for VMViewerUpdates and determines if this class passes the
	 * filter (should be traced). If a filter is not set, then we trace all
	 * classes. Note that for optimization reasons, we expect the caller to
	 * first check that DEBUG_VMUPDATES is true before invoking us; we do not
	 * check it here (other than to assert it).
	 * 
	 * @return true if this class's activity should be traced
	 */
    public static boolean matchesFilterRegex(Class<?> clazz) {
    	assert DEBUG && DEBUG_VM_UPDATES;
    	if (DEBUG_VM_UPDATES_REGEX == null || DEBUG_VM_UPDATES_REGEX.length() == 0) {
    		return true;
    	}
    	try {
	    	Pattern regex = Pattern.compile(DEBUG_VM_UPDATES_REGEX);
	    	Matcher matcher = regex.matcher(clazz.toString());
	    	return matcher.find();
    	}
    	catch (PatternSyntaxException exc) {
    		return false;
    	}
    }
}
