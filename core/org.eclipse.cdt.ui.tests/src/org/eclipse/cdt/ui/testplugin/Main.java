package org.eclipse.cdt.ui.testplugin;

// copied from startup.jar. planned to be removed soon


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
/**
 * Startup class for Eclipse. Creates a class loader using
 * supplied URL of platform installation, loads and calls
 * the Eclipse Boot Loader.  The startup arguments are as follows:
 * <dl>
 * <dd>
 *    -application &lt;id&gt;: the identifier of the application to run
 * </dd>
 * <dd>
 *    -boot &lt;location&gt;: the location, expressed as a URL, of the platform's boot.jar
 * </dd>
 * <dd>
 *    -consolelog : enables log to the console. Handy when combined with -debug
 * </dd>
 * <dd>
 *    -data &lt;location&gt;: sets the workspace location and the default location for projects
 * </dd>
 * <dd>
 *    -debug [options file]: turns on debug mode for the platform and optionally specifies a location
 * for the .options file. This file indicates what debug points are available for a
 * plug-in and whether or not they are enabled. If a location is not specified, the platform searches
 * for the .options file under the install directory
 * </dd>
 * <dd>
 *    -dev [entries]: turns on dev mode and optionally specifies comma-separated class path entries
 * which are added to the class path of each plug-in
 * </dd>
 * <dd>
 *    -keyring &lt;location&gt;: the location of the authorization database on disk. This argument
 * has to be used together with the -password argument
 * </dd>
 * <dd>
 *    -password &lt;passwd&gt;: the password for the authorization database
 * </dd>
 * <dd>
 *    -plugins &lt;location&gt;: The arg is a URL pointing to a file which specs the plugin 
 * path for the platform.  The file is in property file format where the keys are user-defined 
 * names and the values are comma separated lists of either explicit paths to plugin.xml 
 * files or directories containing plugins. (e.g., .../eclipse/plugins).
 * </dd>
 * <dd>
 *    -ws &lt;window system&gt;: sets the window system value
 * </dd>
 * </dl>
 */
public class Main {
	/**
	 * Indicates whether this instance is running in debug mode.
	 */
	protected boolean debug = false;
	
	/**
	 * The location of the launcher to run.
	 */
	protected String bootLocation = null;
	
	/**
	 * The identifier of the application to run.
	 */
	protected String application;
	
	/**
	 * The path for finding find plugins.
	 */
	protected URL pluginPathLocation;
	
	/**
	 * The boot path location.
	 */
	protected String location;
	
	/**
	 * Indicates whether items for UNinstallation should be looked for.
	 */
	protected boolean uninstall = false;
	
	/**
	 * The item to be uninstalled.
	 */
	protected String uninstallCookie;
	
	/**
	 * The class path entries.
	 */
	protected String devClassPath = null;
	
	/**
	 * Indicates whether this instance is running in development mode.
	 */
	protected boolean inDevelopmentMode = false;

	// static token describing how to take down the splash screen
	private static String endSplash = null;
	
	// constants
	private static final String APPLICATION = "-application"; //$NON-NLS-1$
	private static final String BOOT = "-boot"; //$NON-NLS-1$
	private static final String DEBUG = "-debug"; //$NON-NLS-1$
	private static final String DEV = "-dev"; //$NON-NLS-1$
	private static final String ENDSPLASH = "-endsplash"; //$NON-NLS-1$
	private static final String UNINSTALL = "-uninstall"; //$NON-NLS-1$
	private static final String PI_BOOT = "org.eclipse.core.boot"; //$NON-NLS-1$
	private static final String BOOTLOADER = "org.eclipse.core.boot.BootLoader"; //$NON-NLS-1$
	private static final String UPDATELOADER = "org.eclipse.core.internal.boot.LaunchInfo"; //$NON-NLS-1$

	// The project containing the boot loader code.  This is used to construct
	// the correct class path for running in VAJ and VAME.
	private static final String PROJECT_NAME = "Eclipse Core Boot"; //$NON-NLS-1$

	private static boolean inVAJ;
	static {
		try {
			Class.forName("com.ibm.uvm.lang.ProjectClassLoader"); //$NON-NLS-1$
			inVAJ = true;
		} catch (Exception e) {
			inVAJ = false;
		}
	}
	private static boolean inVAME;
	static {
		try {
			Class.forName("com.ibm.eclipse.core.VAME"); //$NON-NLS-1$
			inVAME = true;
		} catch (Exception e) {
			inVAME = false;
		}
	}

/**
 * Executes the launch.
 * 
 * @return the result of performing the launch
 * @param args command-line arguments
 * @exception Exception thrown if a problem occurs during the launch
 */
protected Object basicRun(String[] args) throws Exception {
	Class clazz = getBootLoader(bootLocation);
	Method method = clazz.getDeclaredMethod("run", new Class[] { String.class, URL.class, String.class, String[].class }); //$NON-NLS-1$
	try {
		return method.invoke(clazz, new Object[] { application, pluginPathLocation, location, args });
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		throw e;
	}
}

/**
 * Returns the result of converting a list of comma-separated tokens into an array
 * 
 * @return the array of string tokens
 * @param prop the initial comma-separated string
 */
private String[] getArrayFromList(String prop) {
	if (prop == null || prop.trim().equals("")) //$NON-NLS-1$
		return new String[0];
	Vector list = new Vector();
	StringTokenizer tokens = new StringTokenizer(prop, ","); //$NON-NLS-1$
	while (tokens.hasMoreTokens()) {
		String token = tokens.nextToken().trim();
		if (!token.equals("")) //$NON-NLS-1$
			list.addElement(token);
	}
	return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[0]);
}
/**
 * Creates and returns a platform <code>BootLoader</code> which can be used to start
 * up and run the platform.  The given base, if not <code>null</code>,
 * is the location of the boot loader code.  If the value is <code>null</code>
 * then the boot loader is located relative to this class.
 * 
 * @return the new boot loader
 * @param base the location of the boot loader
 */
public Class getBootLoader(String base) throws Exception {
	URLClassLoader loader = new URLClassLoader(getBootPath(base), null);
	return loader.loadClass(BOOTLOADER);
}
/**
 * Returns the <code>URL</code>-based class path describing where the boot classes
 * are located when running in development mode.
 * 
 * @return the url-based class path
 * @param base the base location
 * @exception MalformedURLException if a problem occurs computing the class path
 */
protected URL[] getDevPath(URL base) throws MalformedURLException {
	URL url;
	String devBase = base.toExternalForm();
	if (!inDevelopmentMode) {
		url = new URL(devBase + "boot.jar"); //$NON-NLS-1$
		return new URL[] {url};
	}
	String[] locations = getArrayFromList(devClassPath);
	ArrayList result = new ArrayList(locations.length);
	for (int i = 0; i < locations.length; i++) {
		String spec = devBase + locations[i];
		char lastChar = spec.charAt(spec.length() - 1);
		if ((spec.endsWith(".jar") || (lastChar == '/' || lastChar == '\\'))) //$NON-NLS-1$
			url = new URL (spec);
		else
			url = new URL(spec + "/"); //$NON-NLS-1$
		//make sure URL exists before adding to path
		if (new java.io.File(url.getFile()).exists())
			result.add(url);
	}
	url = new URL(devBase + "boot.jar"); //$NON-NLS-1$
	if (new java.io.File(url.getFile()).exists())
		result.add(url);
	return (URL[])result.toArray(new URL[result.size()]);
}

/**
 * Returns the <code>URL</code>-based class path describing where the boot classes are located.
 * 
 * @return the url-based class path
 * @param base the base location
 * @exception MalformedURLException if a problem occurs computing the class path
 */
protected URL[] getBootPath(String base) throws MalformedURLException {
	URL url = null;
	// if the given location is not null, assume it is correct and use it. 
	if (base != null) {
		url = new URL(base);
		if (debug)
			System.out.println("Boot URL: " + url.toExternalForm()); //$NON-NLS-1$
		return new URL[] {url};	
	}
	// Create a URL based on the location of this class' code.
	// strip off jar file and/or last directory to get 
	// to the directory containing projects.
	URL[] result = null;
	url = getClass().getProtectionDomain().getCodeSource().getLocation();
	String path = url.getFile();
	if (path.endsWith(".jar")) //$NON-NLS-1$
		path = path.substring(0, path.lastIndexOf("/")); //$NON-NLS-1$
	else 
		if (path.endsWith("/")) //$NON-NLS-1$
			path = path.substring(0, path.length() - 1);
	if (inVAJ || inVAME) {
		int ix = path.lastIndexOf("/"); //$NON-NLS-1$
		path = path.substring(0, ix + 1);
		path = path + PROJECT_NAME + "/"; //$NON-NLS-1$
		url = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
		result = new URL[] {url};
	} else {
		path = searchForPlugins(path);
		path = searchForBoot(path);
		// add on any dev path elements
		url = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
		result = getDevPath(url);
	}
	if (debug) {
		System.out.println("Boot URL:"); //$NON-NLS-1$
		for (int i = 0; i < result.length; i++)
			System.out.println("    " + result[i].toExternalForm());	 //$NON-NLS-1$
	}
	return result;
}

/**
 * Searches for a plugins root starting at a given location.  If one is
 * found then this location is returned; otherwise an empty string is
 * returned.
 * 
 * @return the location where plugins were found, or an empty string
 * @param start the location to begin searching at
 */
protected String searchForPlugins(String start) {
	File path = new File(start);
	while (path != null) {
		File test = new File(path, "plugins"); //$NON-NLS-1$
		if (test.exists())
			return test.toString();
		path = path.getParentFile();
		path = (path == null || path.length() == 1)  ? null : path;
	}
	return ""; //$NON-NLS-1$
}
/**
 * Searches for a boot directory starting at a given location.  If one
 * is found then this location is returned; otherwise an empty string
 * is returned.
 * 
 * @return the location where plugins were found, or an empty string
 * @param start the location to begin searching at
 */
protected String searchForBoot(String start) {
	FileFilter filter = new FileFilter() {
		public boolean accept(File candidate) {
			return candidate.getName().startsWith(PI_BOOT);
		}
	};
	File[] boots = new File(start).listFiles(filter);
	String result = null;
	String maxVersion = null;
	for (int i = 0; i < boots.length; i++) {
		String name = boots[i].getName();
		int index = name.lastIndexOf('_');
		if (index == -1) {
			result = boots[i].getAbsolutePath();
			i = boots.length;
		} else {
			if (index > 0) {
				String version = name.substring(index + 1);
				if (maxVersion == null) {
					result = boots[i].getAbsolutePath();
					maxVersion = version;
				} else
					if (maxVersion.compareTo(version) == -1) {
						result = boots[i].getAbsolutePath();
						maxVersion = version;
					}						
			}
		}
	}
	if (result == null)
		throw new RuntimeException("Could not find bootstrap code. Check location of boot plug-in or specify -boot."); //$NON-NLS-1$
	return result.replace(File.separatorChar, '/') + "/"; //$NON-NLS-1$
}
/**
 * Returns the update loader for the given boot path.
 * 
 * @return the update loader
 * @param base the boot path base
 * @exception Exception thrown is a problem occurs determining this loader
 */
public Class getUpdateLoader(String base) throws Exception {
	URLClassLoader loader = new URLClassLoader(getBootPath(base), null);
	return loader.loadClass(UPDATELOADER);
}
/**
 * Runs the platform with the given arguments.  The arguments must identify
 * an application to run (e.g., <code>-application com.example.application</code>).
 * After running the application <code>System.exit(N)</code> is executed.
 * The value of N is derived from the value returned from running the application.
 * If the application's return value is an <code>Integer</code>, N is this value.
 * In all other cases, N = 0.
 * <p>
 * Clients wishing to run the platform without a following <code>System.exit</code>
 * call should use <code>run()</code>.
 *
 * @see #run
 * 
 * @param args the command line arguments
 */
public static void main(String[] args) {
	Object result = null;
	try {
		result = new Main().run(args);
	} catch (Throwable e) {
		// try and take down the splash screen.
		endSplash();
		System.out.println("Exception launching the Eclipse Platform:"); //$NON-NLS-1$
		e.printStackTrace();
	}
	int exitCode = result instanceof Integer ? ((Integer) result).intValue() : 0;
	System.exit(exitCode);
}
/**
 * Tears down the currently-displayed splash screen.
 */
public static void endSplash() {
	if (endSplash == null)
		return;
	try {
		Runtime.getRuntime().exec(endSplash);
	} catch (Exception e) {
	}
}

/**
 * Runs this launcher with the arguments specified in the given string.
 * 
 * @param argString the arguments string
 * @exception Exception thrown if a problem occurs during launching
 */
public static void main(String argString) throws Exception {
	Vector list = new Vector(5);
	for (StringTokenizer tokens = new StringTokenizer(argString, " "); tokens.hasMoreElements();) //$NON-NLS-1$
		list.addElement(tokens.nextElement());
	main((String[]) list.toArray(new String[list.size()]));
}

/**
 * Processes the command line arguments
 * 
 * @return the arguments to pass through to the launched application
 * @param args the command line arguments
 */
protected String[] processCommandLine(String[] args) throws Exception {
	int[] configArgs = new int[100];
	configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
	int configArgIndex = 0;
	for (int i = 0; i < args.length; i++) {
		boolean found = false;
		// check for args without parameters (i.e., a flag arg)
		// check if debug should be enabled for the entire platform
		if (args[i].equalsIgnoreCase(DEBUG)) {
			debug = true;
			// passed thru this arg (i.e., do not set found = true
			continue;
		}
		
		// check if development mode should be enabled for the entire platform
		// If this is the last arg or there is a following arg (i.e., arg+1 has a leading -), 
		// simply enable development mode.  Otherwise, assume that that the following arg is
		// actually some additional development time class path entries.  This will be processed below.
		if (args[i].equalsIgnoreCase(DEV) && ((i + 1 == args.length) || ((i + 1 < args.length) && (args[i + 1].startsWith("-"))))) { //$NON-NLS-1$
			inDevelopmentMode = true;
			// do not mark the arg as found so it will be passed through
			continue;
		}

		// done checking for args.  Remember where an arg was found 
		if (found) {
			configArgs[configArgIndex++] = i;
			continue;
		}
		// check for args with parameters. If we are at the last argument or if the next one
		// has a '-' as the first character, then we can't have an arg with a parm so continue.
		if (i == args.length - 1 || args[i + 1].startsWith("-"))  //$NON-NLS-1$
			continue;
		String arg = args[++i];

		// look for the laucher to run
		if (args[i - 1].equalsIgnoreCase(BOOT)) {
			bootLocation = arg;
			found = true;
		}

		// look for the development mode and class path entries.  
		if (args[i - 1].equalsIgnoreCase(DEV)) {
			inDevelopmentMode = true;
			devClassPath = arg;
			continue;
		}

		// look for the application to run
		if (args[i - 1].equalsIgnoreCase(APPLICATION)) {
			application = arg;
			found = true;
		}

		// look for token to use to end the splash screen
		if (args[i - 1].equalsIgnoreCase(ENDSPLASH)) {
			endSplash = arg;
			continue;
		}

		// look for items to uninstall
		if (args[i - 1].equalsIgnoreCase(UNINSTALL)) {
			uninstall = true;
			uninstallCookie = arg;
			found = true;
		}

		// done checking for args.  Remember where an arg was found 
		if (found) {
			configArgs[configArgIndex++] = i - 1;
			configArgs[configArgIndex++] = i;
		}
	}
	// remove all the arguments consumed by this argument parsing
	if (configArgIndex == 0)
		return args;
	String[] passThruArgs = new String[args.length - configArgIndex];
	configArgIndex = 0;
	int j = 0;
	for (int i = 0; i < args.length; i++) {
		if (i == configArgs[configArgIndex])
			configArgIndex++;
		else
			passThruArgs[j++] = args[i];
	}
	return passThruArgs;
}
/**
 * Runs the application to be launched.
 * 
 * @return the return value from the launched application
 * @param args the arguments to pass to the application
 * @exception thrown if a problem occurs during launching
 */
public Object run(String[] args) throws Exception {
	String[] passThruArgs = processCommandLine(args);
	if (uninstall)
		return updateRun(UNINSTALL, uninstallCookie, passThruArgs);
	return basicRun(passThruArgs);
}
/**
 * Performs an update run.
 * 
 * @return the return value from the update loader
 * @param flag flag to give to the update loader
 * @param value value to give to the update loader
 * @param args arguments to give to the update loader.
 * @exception Exception thrown if a problem occurs during execution
 */
protected Object updateRun(String flag, String value, String[] args) throws Exception {
	Class clazz = getUpdateLoader(bootLocation);
	Method method = clazz.getDeclaredMethod("run", new Class[] { String.class, String.class, String.class, String[].class }); //$NON-NLS-1$
	try {
		return method.invoke(clazz, new Object[] { flag, value, location, args });
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		throw e;
	}
}
}
