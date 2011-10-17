/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *     oyvind.harboe@zylin.com - http://bugs.eclipse.org/250638
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.cdtvariables.IUserVarSupplier;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.cdt.internal.core.CDTLogWriter;
import org.eclipse.cdt.internal.core.CdtVarPathEntryVariableManager;
import org.eclipse.cdt.internal.core.ICConsole;
import org.eclipse.cdt.internal.core.PositionTrackerManager;
import org.eclipse.cdt.internal.core.cdtvariables.CdtVariableManager;
import org.eclipse.cdt.internal.core.cdtvariables.UserVarSupplier;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.errorparsers.ErrorParserExtensionManager;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

import com.ibm.icu.text.MessageFormat;

/**
 * CCorePlugin is the life-cycle owner of the core plug-in, and starting point for access to many core APIs.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CCorePlugin extends Plugin {

	public static final int STATUS_CDTPROJECT_EXISTS = 1;
	public static final int STATUS_CDTPROJECT_MISMATCH = 2;
	public static final int CDT_PROJECT_NATURE_ID_MISMATCH = 3;
	/**
	 * Status code for core exception that is thrown if a pdom grew larger than the supported limit.
	 * @since 5.2
	 */
	public static final int STATUS_PDOM_TOO_LARGE = 4;

	public static final String PLUGIN_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$

	public static final String BUILDER_MODEL_ID = PLUGIN_ID + ".CBuildModel"; //$NON-NLS-1$
	public static final String BINARY_PARSER_SIMPLE_ID = "BinaryParser"; //$NON-NLS-1$
	public final static String BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + BINARY_PARSER_SIMPLE_ID; //$NON-NLS-1$
	public final static String PREF_BINARY_PARSER = "binaryparser"; //$NON-NLS-1$
	public final static String DEFAULT_BINARY_PARSER_SIMPLE_ID = "ELF"; //$NON-NLS-1$
	public final static String DEFAULT_BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + DEFAULT_BINARY_PARSER_SIMPLE_ID; //$NON-NLS-1$
	public final static String PREF_USE_STRUCTURAL_PARSE_MODE = "useStructualParseMode"; //$NON-NLS-1$
	
	public static final String INDEX_SIMPLE_ID = "CIndex"; //$NON-NLS-1$
	public static final String INDEX_UNIQ_ID = PLUGIN_ID + "." + INDEX_SIMPLE_ID; //$NON-NLS-1$
	 		
	public static final String INDEXER_SIMPLE_ID = "CIndexer"; //$NON-NLS-1$
	public static final String INDEXER_UNIQ_ID = PLUGIN_ID + "." + INDEXER_SIMPLE_ID; //$NON-NLS-1$
	public static final String PREF_INDEXER = "indexer"; //$NON-NLS-1$
	public static final String DEFAULT_INDEXER = IPDOMManager.ID_FAST_INDEXER;
	
	/**
	 * Name of the extension point for contributing an error parser
	 */
	public final static String ERROR_PARSER_SIMPLE_ID = "ErrorParser"; //$NON-NLS-1$
	/**
	 * Full unique name of the extension point for contributing an error parser
	 */
	public final static String ERROR_PARSER_UNIQ_ID = PLUGIN_ID + "." + ERROR_PARSER_SIMPLE_ID; //$NON-NLS-1$

	// default store for pathentry
	public final static String DEFAULT_PATHENTRY_STORE_ID = PLUGIN_ID + ".cdtPathEntryStore"; //$NON-NLS-1$

	// Build Model Interface Discovery
	public final static String BUILD_SCANNER_INFO_SIMPLE_ID = "ScannerInfoProvider"; //$NON-NLS-1$
	public final static String BUILD_SCANNER_INFO_UNIQ_ID = PLUGIN_ID + "." + BUILD_SCANNER_INFO_SIMPLE_ID; //$NON-NLS-1$

	public static final String DEFAULT_PROVIDER_ID = CCorePlugin.PLUGIN_ID + ".defaultConfigDataProvider"; //$NON-NLS-1$

	private final static String SCANNER_INFO_PROVIDER2_NAME = "ScannerInfoProvider2"; //$NON-NLS-1$
	private final static String SCANNER_INFO_PROVIDER2 = PLUGIN_ID + "." + SCANNER_INFO_PROVIDER2_NAME; //$NON-NLS-1$ 
	
	/**
	 * Name of the extension point for contributing a source code formatter
	 */
	public static final String FORMATTER_EXTPOINT_ID = "CodeFormatter" ; //$NON-NLS-1$

    /**
     * Possible  configurable option ID.
     * @see #getDefaultOptions
     */
    public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$
	
	/**
	 * IContentType id for C Source Unit
	 */
	public final static String CONTENT_TYPE_CSOURCE =  "org.eclipse.cdt.core.cSource"; //$NON-NLS-1$
	/**
	 * IContentType id for C Header Unit
	 */
	public final static String CONTENT_TYPE_CHEADER =  "org.eclipse.cdt.core.cHeader"; //$NON-NLS-1$
	/**
	 * IContentType id for C++ Source Unit
	 */
	public final static String CONTENT_TYPE_CXXSOURCE = "org.eclipse.cdt.core.cxxSource"; //$NON-NLS-1$
	/**
	 * IContentType id for C++ Header Unit
	 */
	public final static String CONTENT_TYPE_CXXHEADER = "org.eclipse.cdt.core.cxxHeader"; //$NON-NLS-1$
	/**
	 * IContentType id for ASM Unit
	 */
	public final static String CONTENT_TYPE_ASMSOURCE = "org.eclipse.cdt.core.asmSource"; //$NON-NLS-1$
	/**
	 * IContentType id for Binary Files
	 */
	public final static String CONTENT_TYPE_BINARYFILE = "org.eclipse.cdt.core.binaryFile"; //$NON-NLS-1$
	
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String INSERT = "insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String TAB = "tab"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String SPACE = "space"; //$NON-NLS-1$

	private static CCorePlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;

	/**
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public CDTLogWriter cdtLog = null;

	private volatile CProjectDescriptionManager fNewCProjectDescriptionManager;

	private CoreModel fCoreModel;
	
	private PDOMManager pdomManager;

	private CdtVarPathEntryVariableManager fPathEntryVariableManager;
	
	private final class NullConsole implements IConsole {
		private ConsoleOutputStream nullStream = new ConsoleOutputStream() {
			@Override
			public void write(byte[] b) throws IOException {
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
			}
			@Override
			public void write(int c) throws IOException {
			}
		};
		public void start(IProject project) {
		}
		public ConsoleOutputStream getOutputStream() {
			return nullStream;
		}
		public ConsoleOutputStream getInfoStream() {
			return nullStream; 
		}
		public ConsoleOutputStream getErrorStream() {
			return nullStream;
		}
	}

	// -------- static methods --------

	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.internal.core.CCorePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	/**
     * Returns the shared working copies currently registered for the default buffer factory. 
	 * @since 5.1
	 */
	public static IWorkingCopy[] getSharedWorkingCopies() {
		return CModelManager.getDefault().getSharedWorkingCopies(null);
	}

	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new Object[] { arg });
	}

	@SuppressWarnings("cast") // java.text.MessageFormat would require the cast
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[])args);
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

    public static IPositionTrackerManager getPositionTrackerManager() {
        return PositionTrackerManager.getInstance();
    }
    
	public static CCorePlugin getDefault() {
		return fgCPlugin;
	}


	/**
	 * @see Plugin#shutdown
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			pdomManager.shutdown();
			
            PositionTrackerManager.getInstance().uninstall();
            
//			if (fDescriptorManager != null) {
//				fDescriptorManager.shutdown();
//			}
            
			if (fCoreModel != null) {
				fCoreModel.shutdown();
			}
			
			if (cdtLog != null) {
				cdtLog.shutdown();
			}

			if (fPathEntryVariableManager != null) {
				fPathEntryVariableManager.shutdown();
			}

            fNewCProjectDescriptionManager.shutdown();
            ResourceLookup.shutdown();
            
            savePluginPreferences();
		} finally {
			super.stop(context);
		}
	}

	/**
	 * @see Plugin#startup
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// do harmless stuff first.
		cdtLog = new CDTLogWriter(CCorePlugin.getDefault().getStateLocation().append(".log").toFile()); //$NON-NLS-1$
		configurePluginDebugOptions();
        PositionTrackerManager.getInstance().install();
        ResourceLookup.startup();
        
        // new project model needs to register the resource listener first.
        CProjectDescriptionManager descManager = CProjectDescriptionManager.getInstance();
		final Job post1 = descManager.startup();
		fNewCProjectDescriptionManager = descManager;

		fPathEntryVariableManager = new CdtVarPathEntryVariableManager();
		fPathEntryVariableManager.startup();

		fCoreModel = CoreModel.getDefault();
		fCoreModel.startup();

		pdomManager = new PDOMManager();
		final Job post2= pdomManager.startup();
        
        // bug 186755, when started after the platform has been started the job manager
        // is no longer suspended. So we have to start a job at the very end to make
        // sure we don't trigger a concurrent plug-in activation from within the job.
		post1.schedule();
		post2.schedule();
	}

	/**
     * TODO: Add all options here
     * Returns a table of all known configurable options with their default values.
     * These options allow to configure the behavior of the underlying components.
     * The client may safely use the result as a template that they can modify and
     * then pass to <code>setOptions</code>.
     * 
     * Helper constants have been defined on CCorePlugin for each of the option ID and 
     * their possible constant values.
     * 
     * Note: more options might be added in further releases.
     * <pre>
     * RECOGNIZED OPTIONS:
     * 
     * CORE / Specify Default Source Encoding Format
     *    Get the encoding format for translated sources. This setting is read-only, it is equivalent
     *    to 'ResourcesPlugin.getEncoding()'.
     *     - option id:         "org.eclipse.cdt.core.encoding"
     *     - possible values:   { any of the supported encoding names}.
     *     - default:           <platform default>
     * </pre>
     * 
     * @return a mutable map containing the default settings of all known options
     *   (key type: <code>String</code>; value type: <code>String</code>)
     * @see #setOptions
     */
    public static HashMap<String, String> getDefaultOptions() {
        HashMap<String, String> defaultOptions = new HashMap<String, String>(10);

        // see #initializeDefaultPluginPreferences() for changing default settings
        Preferences preferences = getDefault().getPluginPreferences();
        HashSet<String> optionNames = CModelManager.OptionNames;
        
        // get preferences set to their default
        for (String propertyName : preferences.defaultPropertyNames()){
            if (optionNames.contains(propertyName))
                defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
        }       
        // get preferences not set to their default
        for (String propertyName : preferences.propertyNames()) {
            if (optionNames.contains(propertyName)) 
                defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
        }       
        // get encoding through resource plugin
        defaultOptions.put(CORE_ENCODING, ResourcesPlugin.getEncoding()); 
        
        return defaultOptions;
    }

    /**
     * Helper method for returning one option value only. Equivalent to <code>(String)CCorePlugin.getOptions().get(optionName)</code>
     * Note that it may answer <code>null</code> if this option does not exist.
     * <p>
     * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
     * </p>
     * 
     * @param optionName the name of an option
     * @return the String value of a given option
     * @see CCorePlugin#getDefaultOptions
     */
    public static String getOption(String optionName) {
        
        if (CORE_ENCODING.equals(optionName)){
            return ResourcesPlugin.getEncoding();
        }
        if (CModelManager.OptionNames.contains(optionName)){
            Preferences preferences = getDefault().getPluginPreferences();
            return preferences.getString(optionName).trim();
        }
        return null;
    }
    
    /**
     * Returns the table of the current options. Initially, all options have their default values,
     * and this method returns a table that includes all known options.
     * <p>
     * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
     * </p>
     * 
     * @return table of current settings of all options 
     *   (key type: <code>String</code>; value type: <code>String</code>)
     * @see CCorePlugin#getDefaultOptions
     */
    public static HashMap<String, String> getOptions() {
        HashMap<String, String> options = new HashMap<String, String>(10);

        // see #initializeDefaultPluginPreferences() for changing default settings
        Plugin plugin = getDefault();
        if (plugin != null) {
            Preferences preferences = plugin.getPluginPreferences();
            HashSet<String> optionNames = CModelManager.OptionNames;
            
            // get preferences set to their default
            for (String propertyName : preferences.defaultPropertyNames()){
                if (optionNames.contains(propertyName)){
                    options.put(propertyName, preferences.getDefaultString(propertyName));
                }
            }       
            // get preferences not set to their default
            for (String propertyName : preferences.propertyNames()){
                if (optionNames.contains(propertyName)){
                    options.put(propertyName, preferences.getString(propertyName).trim());
                }
            }       
            // get encoding through resource plugin
            options.put(CORE_ENCODING, ResourcesPlugin.getEncoding());
        }
        return options;
    }

    /**
     * Sets the current table of options. All and only the options explicitly included in the given table 
     * are remembered; all previous option settings are forgotten, including ones not explicitly
     * mentioned.
     * <p>
     * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
     * </p>
     * 
     * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
     *   or <code>null</code> to reset all options to their default values
     * @see CCorePlugin#getDefaultOptions
     */
    public static void setOptions(HashMap<String, String> newOptions) {
    
        // see #initializeDefaultPluginPreferences() for changing default settings
        Preferences preferences = getDefault().getPluginPreferences();

        if (newOptions == null){
            newOptions = getDefaultOptions();
        }
        for (String key : newOptions.keySet()){
            if (!CModelManager.OptionNames.contains(key)) continue; // unrecognized option
            if (key.equals(CORE_ENCODING)) continue; // skipped, contributed by resource prefs
            String value = newOptions.get(key);
            preferences.setValue(key, value);
        }
    
        // persist options
        getDefault().savePluginPreferences();
    }    
    

	/**
	 * Create CDT console adapter for build console defined as an extension.
	 * See {@code org.eclipse.cdt.core.CBuildConsole} extension point.
	 * If the console class is instance of {@link ICConsole} it is initialized
	 * with context id, name and icon to be shown in the list of consoles in the
	 * Console view.
	 * 
	 * @param extConsoleId - console id defined in the extension point.
	 * @param contextId - context menu id in the Console view. A caller needs to define
	 *    a distinct one for own use.
	 * @param name - name of console to appear in the list of consoles in context menu
	 *    in the Console view.
	 * @param iconUrl - a {@link URL} of the icon for the context menu of the Console
	 *    view. The url is expected to point to an image in eclipse OSGi bundle.
	 *    Here is an example how to retrieve URL:<br/>
	 *    <code>
	 *    URL iconUrl = CUIPlugin.getDefault().getBundle().getEntry("icons/obj16/flask.png");
	 *    </code>
	 * 
	 * @return CDT console adapter.
	 */
	private IConsole getConsole(String extConsoleId, String contextId, String name, URL iconUrl) {
		try {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "CBuildConsole"); //$NON-NLS-1$
			if (extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				for (IExtension extension : extensions) {
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					for (IConfigurationElement configElement : configElements) {
						String consoleID = configElement.getAttribute("id"); //$NON-NLS-1$
						if ((extConsoleId == null && consoleID == null) || (extConsoleId != null && extConsoleId.equals(consoleID))) {
							IConsole console = (IConsole) configElement.createExecutableExtension("class"); //$NON-NLS-1$
							if (console instanceof ICConsole) {
								((ICConsole) console).init(contextId, name, iconUrl);
							}
							return console;
						}
					}
				}
			}
		} catch (CoreException e) {
			log(e);
		}
		return new NullConsole();
	}

	/**
	 * Create CDT console adapter.
	 * The adapter serves as a bridge between core plugin and UI console API in a way that
	 * a user can create a UI console from plugins having no dependencies to UI.
	 *  
	 * @param id - id of the console specified in extension point to instantiate
	 *    console adapter.
	 * @return CDT console adapter.
	 */
	public IConsole getConsole(String id) {
		return getConsole(id, null, null, null);
	}

	/**
	 * Create CDT console adapter for build console. A new instance of class
	 * {@code org.eclipse.cdt.internal.ui.buildconsole.CBuildConsole} is created
	 * and initialized with the parameters.
	 * 
	 * @param contextId - context menu id in the Console view. A caller needs to define
	 *    a distinct one for own use.
	 * @param name - name of console to appear in the list of consoles in context menu
	 *    in the Console view.
	 * @param iconUrl - a {@link URL} of the icon for the context menu of the Console
	 *    view. The url is expected to point to an image in eclipse OSGi bundle.
	 *    Here is an example how to retrieve URL:<br/>
	 *    <code>
	 *    URL iconUrl = CUIPlugin.getDefault().getBundle().getResource("icons/obj16/flask.png");
	 *    </code>
	 *    <br/>
	 *    {@code iconUrl} can be <b>null</b>, in that case the default image is used.
	 *    See {@code org.eclipse.cdt.internal.ui.buildconsole.BuildConsole(IBuildConsoleManager, String, String, URL)}
	 * 
	 * @return CDT console adapter.
	 * 
	 * @since 5.3
	 */
	public IConsole getBuildConsole(String contextId, String name, URL iconUrl) {
		return getConsole(null, contextId, name, iconUrl);
	}

	/**
	 * Create CDT console adapter connected to the default build console.
	 */
	public IConsole getConsole() {
		String consoleID = System.getProperty("org.eclipse.cdt.core.console"); //$NON-NLS-1$
		return getConsole(consoleID);
	}

	/**
	 * @deprecated Use {@link #getDefaultBinaryParserExtensions(IProject)} instead.
	 */
	@Deprecated
	public ICExtensionReference[] getBinaryParserExtensions(IProject project) throws CoreException {
		ICExtensionReference ext[] = new ICExtensionReference[0];
		if (project != null) {
			try {
				ICDescriptor cdesc = getCProjectDescription(project, false);
				if (cdesc==null)
					return ext;
				ICExtensionReference[] cextensions = cdesc.get(BINARY_PARSER_UNIQ_ID, true);
				if (cextensions != null && cextensions.length > 0)
					ext = cextensions;
			} catch (CoreException e) {
				log(e);
			}
		}
		return ext;
	}

	/**
	 * Returns the binary parser extensions for the default settings configuration.
	 * @since 5.2
	 */
	public ICConfigExtensionReference[] getDefaultBinaryParserExtensions(IProject project) throws CoreException {
		ICConfigExtensionReference ext[] = new ICConfigExtensionReference[0];
		if (project != null) {
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project, false);
			if (desc != null) {
				ICConfigurationDescription cfgDesc = desc.getDefaultSettingConfiguration();
				if (cfgDesc != null) {
					ext = cfgDesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				}
			}
		}
		return ext;
	}

	/**
	 * @deprecated - use getBinaryParserExtensions(IProject project)
	 */
	@Deprecated
	public IBinaryParser[] getBinaryParser(IProject project) throws CoreException {
		IBinaryParser parsers[] = null;
		if (project != null) {
			try {
				ICDescriptor cdesc = getCProjectDescription(project, false);
				ICExtensionReference[] cextensions = cdesc.get(BINARY_PARSER_UNIQ_ID, true);
				if (cextensions.length > 0) {
					ArrayList<IBinaryParser> list = new ArrayList<IBinaryParser>(cextensions.length);
					for (ICExtensionReference ref : cextensions) {
						IBinaryParser parser = null;
						try {
							parser = (IBinaryParser)ref.createExtension();
						} catch (ClassCastException e) {
							log(e); // wrong binary parser definition ?
						}
						if (parser != null) {
							list.add(parser); 
						}
					}
					parsers = new IBinaryParser[list.size()];
					list.toArray(parsers);
				}
			} catch (CoreException e) {
				// ignore since we fall back to a default....
			}
		}
		if (parsers == null) {
			IBinaryParser parser = getDefaultBinaryParser();
			if (parser != null) {
				parsers = new IBinaryParser[] {parser};
			}
		}
		return parsers;
	}

	public IBinaryParser getDefaultBinaryParser() throws CoreException {
		IBinaryParser parser = null;
		String id = getPluginPreferences().getDefaultString(PREF_BINARY_PARSER);
		if (id == null || id.isEmpty()) {
			id = DEFAULT_BINARY_PARSER_UNIQ_ID;
		}
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, BINARY_PARSER_SIMPLE_ID);
		IExtension extension = extensionPoint.getExtension(id);
		if (extension != null) {
			IConfigurationElement element[] = extension.getConfigurationElements();
			for (IConfigurationElement element2 : element) {
				if (element2.getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
					parser = (IBinaryParser) element2.createExecutableExtension("run"); //$NON-NLS-1$
					break;
				}
			}
		} else {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("CCorePlugin.exception.noBinaryFormat"), null); //$NON-NLS-1$
			throw new CoreException(s);
		}
		return parser;
	}

	public CoreModel getCoreModel() {
		return fCoreModel;
	}

	public static IIndexManager getIndexManager() {
		return getDefault().pdomManager;
	}
	
	public IPathEntryVariableManager getPathEntryVariableManager() {
		return fPathEntryVariableManager;
	}

	/**
	 * @deprecated use {@link #getProjectDescription(IProject, boolean)} instead
	 */
	@Deprecated
	public ICDescriptor getCProjectDescription(IProject project) throws CoreException {
		return fNewCProjectDescriptionManager.getDescriptorManager().getDescriptor(project);
	}

	/**
	 * Please use {@link #getProjectDescription(IProject, boolean)} to fetch the
	 * ICProjectDescription for the project. And use {@link ICProjectDescription#getConfigurations()} 
	 * to get an array of ICConfigurationDescriptions, which have similar API to ICDescriptor,
	 * allowing you to store settings and configure extensions at the Configuration level
	 * rather than at the project level.
	 *
	 * @param project
	 * @param create
	 * @return ICDescriptor or <b>null</b> if <b>create</b> is <b>false</b> and no .cdtproject file exists on disk.
	 * @throws CoreException
	 * @deprecated
	 */
	@Deprecated
	public ICDescriptor getCProjectDescription(IProject project, boolean create) throws CoreException {
		return fNewCProjectDescriptionManager.getDescriptorManager().getDescriptor(project, create);
	}

	public void mapCProjectOwner(IProject project, String id, boolean override) throws CoreException {
		try {
		if (!override) {
			fNewCProjectDescriptionManager.getDescriptorManager().configure(project, id);
		} else {
			fNewCProjectDescriptionManager.getDescriptorManager().convert(project, id);
		}
		} catch (Exception e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	/**
	 * @deprecated Settings should be set per ICConfigurationDescription rather than
	 * global to the project.  Please use {@link #getProjectDescription(IProject, boolean)} 
	 * to fetch the ICProjectDescription for the project. And use 
	 * {@link ICProjectDescription#getConfigurations()} to get an array of 
	 * ICConfigurationDescriptions, which have similar API to ICDescriptor,
	 * allowing you to store settings and configure extensions at the Configuration level
	 * rather than at the project level.
	 */
	@Deprecated
	public ICDescriptorManager getCDescriptorManager() {
		return fNewCProjectDescriptionManager.getDescriptorManager();
	}

	/**
	 * Creates a C project resource given the project handle and description.
	 *
	 * @param description the project description to create a project resource for
	 * @param projectHandle the project handle to create a project resource for
	 * @param monitor the progress monitor to show visual progress with
	 * @param projectID required for mapping the project to an owner
	 *
	 * @exception CoreException if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	public IProject createCProject(
		final IProjectDescription description,
		final IProject projectHandle,
		IProgressMonitor monitor,
		final String projectID)
		throws CoreException, OperationCanceledException {

		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					if (monitor == null) {
						monitor = new NullProgressMonitor();
					}
					monitor.beginTask("Creating C Project...", 3); //$NON-NLS-1$
					if (!projectHandle.exists()) {
						projectHandle.create(description, new SubProgressMonitor(monitor, 1));
					}
					
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					
					// Open first.
					projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1));

					mapCProjectOwner(projectHandle, projectID, false);

					// Add C Nature ... does not add duplicates
					CProjectNature.addCNature(projectHandle, new SubProgressMonitor(monitor, 1));
				} finally {
					monitor.done();
				}
			}
		}, getWorkspace().getRoot(), 0, monitor);
		return projectHandle;
	}

	public IProject createCDTProject(
			final IProjectDescription description,
			final IProject projectHandle,
			IProgressMonitor monitor) throws CoreException, OperationCanceledException{
		return createCDTProject(description, projectHandle, null, monitor);
	}

	public IProject createCDTProject(
			final IProjectDescription description,
			final IProject projectHandle,
			final String bsId,
			IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {

			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						if (monitor == null) {
							monitor = new NullProgressMonitor();
						}
						monitor.beginTask("Creating C Project...", 3); //$NON-NLS-1$
						if (!projectHandle.exists()) {
							projectHandle.create(description, new SubProgressMonitor(monitor, 1));
						}
						
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						
						// Open first.
						projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1));

//						mapCProjectOwner(projectHandle, projectID, false);

						// Add C Nature ... does not add duplicates
						CProjectNature.addCNature(projectHandle, new SubProgressMonitor(monitor, 1));
						
						if(bsId != null){
							ICProjectDescription projDes = createProjectDescription(projectHandle, true);
							ICConfigurationDescription cfgs[] = projDes.getConfigurations();
							ICConfigurationDescription cfg = null;
							for (ICConfigurationDescription cfg2 : cfgs) {
								if(bsId.equals(cfg2.getBuildSystemId())){
									cfg = cfg2;
									break;
								}
							}
							
							if(cfg == null){
								ICConfigurationDescription prefCfg = getPreferenceConfiguration(bsId);
								if(prefCfg != null){
									cfg = projDes.createConfiguration(CDataUtil.genId(prefCfg.getId()), prefCfg.getName(), prefCfg);
								}
							}
							
							if(cfg != null){
								setProjectDescription(projectHandle, projDes);
							}
						}
					} finally {
						monitor.done();
					}
				}
			}, getWorkspace().getRoot(), 0, monitor);
			return projectHandle;
		}

	/**
	 * Method convertProjectFromCtoCC converts
	 * a C Project to a C++ Project
	 * The newProject MUST, not be null, already have a C Nature 
	 * && must NOT already have a C++ Nature
	 * 
	 * @param projectHandle
	 * @param monitor
	 * @throws CoreException
	 */

	public void convertProjectFromCtoCC(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
		if ((projectHandle != null)
			&& projectHandle.hasNature(CProjectNature.C_NATURE_ID)
			&& !projectHandle.hasNature(CCProjectNature.CC_NATURE_ID)) {
			// Add C++ Nature ... does not add duplicates        
			CCProjectNature.addCCNature(projectHandle, monitor);
		}
	}

	/**
	 * Method to convert a project to a C nature 
	 * All checks should have been done externally
	 * (as in the Conversion Wizards). 
	 * This method blindly does the conversion.
	 */
	public void convertProjectToC(IProject projectHandle, IProgressMonitor monitor, String projectID)
		throws CoreException {
		if ((projectHandle == null) || (monitor == null) || (projectID == null)) {
			return;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
		description.setLocation(projectHandle.getFullPath());
		createCProject(description, projectHandle, monitor, projectID);
	}

	public void convertProjectToNewC(IProject projectHandle, String bsId, IProgressMonitor monitor)
		throws CoreException {
		if ((projectHandle == null) || (monitor == null) || (bsId == null)) {
			throw new NullPointerException();
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
		description.setLocation(projectHandle.getFullPath());
		createCDTProject(description, projectHandle, bsId, monitor);
	}

	/**
	 * Method to convert a project to a C++ nature 
	 */
	public void convertProjectToCC(IProject projectHandle, IProgressMonitor monitor, String projectID)
		throws CoreException {
		if ((projectHandle == null) || (monitor == null) || (projectID == null)) {
			return;
		}
		createCProject(projectHandle.getDescription(), projectHandle, monitor, projectID);
		// now add C++ nature
		convertProjectFromCtoCC(projectHandle, monitor);
	}

	public void convertProjectToNewCC(IProject projectHandle, String bsId, IProgressMonitor monitor)
		throws CoreException {
		if ((projectHandle == null) || (monitor == null) || (bsId == null)) {
			throw new NullPointerException();
		}
		createCDTProject(projectHandle.getDescription(), projectHandle, bsId, monitor);
		// now add C++ nature
		convertProjectFromCtoCC(projectHandle, monitor);
	}

	/**
	 * Get the IProcessList contributed interface for the platform.
	 * @return IProcessList
	 */
	public IProcessList getProcessList() throws CoreException {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "ProcessList"); //$NON-NLS-1$
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			IConfigurationElement defaultContributor = null;
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					if (configElement.getName().equals("processList")) { //$NON-NLS-1$
						String platform = configElement.getAttribute("platform"); //$NON-NLS-1$
						if (platform == null ) { // first contributor found with not platform will be default.
							if (defaultContributor == null) {
								defaultContributor = configElement;
							}
						} else if (platform.equals(Platform.getOS())) {
							// found explicit contributor for this platform.
							return (IProcessList) configElement.createExecutableExtension("class"); //$NON-NLS-1$
						}
					}
				}
			}
			if ( defaultContributor != null) { 
				return (IProcessList) defaultContributor.createExecutableExtension("class"); //$NON-NLS-1$
			}
		}
		return null;
		
	}
	
	/**
	 * @deprecated since CDT 6.1. Use {@link ErrorParserManager#getErrorParserAvailableIds()} instead
	 * @return array of error parsers ids
	 */
	@Deprecated
	public String[] getAllErrorParsersIDs() {
		ErrorParserExtensionManager.loadErrorParserExtensions();
		return ErrorParserExtensionManager.getErrorParserAvailableIds();
	}
	
	/**
	 * @deprecated since CDT 6.1. Use {@link ErrorParserManager#getErrorParserCopy(String)} instead
	 * @param id - id of error parser
	 * @return array of error parsers
	 */
	@Deprecated
	public IErrorParser[] getErrorParser(String id) {
		ErrorParserExtensionManager.loadErrorParserExtensions();
		IErrorParser errorParser = ErrorParserExtensionManager.getErrorParserInternal(id);
		if (errorParser == null) {
			return new IErrorParser[] {};
		} else {
			return new IErrorParser[] { errorParser };
		}
	}

	public IScannerInfoProvider getScannerInfoProvider(IProject project) {
		IScannerInfoProvider provider = null;
		try {
			// Look up in session property for previously created provider
			QualifiedName scannerInfoProviderName = new QualifiedName(PLUGIN_ID, SCANNER_INFO_PROVIDER2_NAME);
			provider = (IScannerInfoProvider)project.getSessionProperty(scannerInfoProviderName);
			if (provider != null)
				return provider;
			
			// Next search the extension registry to see if a provider is registered with a build command
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry.getExtensionPoint(SCANNER_INFO_PROVIDER2);
			if (point != null) {
				IExtension[] exts = point.getExtensions();
				for (IExtension ext : exts) {
					IConfigurationElement[] elems = ext.getConfigurationElements();
					for (IConfigurationElement elem : elems) {
						String builder = elem.getAttribute("builder"); //$NON-NLS-1$
						if (builder != null) {
							IProjectDescription desc = project.getDescription();
							ICommand[] commands = desc.getBuildSpec();
							for (ICommand command : commands)
								if (builder.equals(command.getBuilderName()))
									provider = (IScannerInfoProvider)elem.createExecutableExtension("class"); //$NON-NLS-1$
						}
					}
				}
			}
			
			// Default to the proxy
			if (provider == null)
				provider = fNewCProjectDescriptionManager.getScannerInfoProviderProxy(project);
			project.setSessionProperty(scannerInfoProviderName, provider);
		} catch (CoreException e) {
			// Bug 313725: When project is being closed, don't report an error.
			if (!project.isOpen())
				return null;
			
			log(e);
 		}
		
		return provider;
	}
	
	/**
	 * Helper function, returning the content type for a filename
	 * Same as: <pre>
	 * 	getContentType(null, filename)
	 * </pre>
	 * @param filename
	 * @return the content type found, or <code>null</code>
	 */
	public static IContentType getContentType(String filename) {
		return CContentTypes.getContentType(null, filename);
	}
	
	/**
	 * Returns the content type for a filename. The method respects
	 * project specific content type definitions. The lookup prefers case-
	 * sensitive matches over the others.
	 * @param project a project with possible project specific settings. Can be <code>null</code>
	 * @param filename a filename to compute the content type for
	 * @return the content type found or <code>null</code>
	 */
	public static IContentType getContentType(IProject project, String filename) {
		return CContentTypes.getContentType(project, filename);
	}
	
	/**
	 * Tests whether the given project uses its project specific content types.
	 */
	public static boolean usesProjectSpecificContentTypes(IProject project) {
		return CContentTypes.usesProjectSpecificContentTypes(project);
	}

	/**
	 * Enables or disables the project specific content types.
	 */
	public static void setUseProjectSpecificContentTypes(IProject project, boolean val) {
		CContentTypes.setUseProjectSpecificContentTypes(project, val);
	}

	

	private static final String MODEL = CCorePlugin.PLUGIN_ID + "/debug/model" ; //$NON-NLS-1$
	private static final String PARSER = CCorePlugin.PLUGIN_ID + "/debug/parser" ; //$NON-NLS-1$
	private static final String PARSER_EXCEPTIONS = CCorePlugin.PLUGIN_ID + "/debug/parser/exceptions" ; //$NON-NLS-1$
	private static final String SCANNER = CCorePlugin.PLUGIN_ID + "/debug/scanner"; //$NON-NLS-1$
	private static final String DELTA = CCorePlugin.PLUGIN_ID + "/debug/deltaprocessor" ; //$NON-NLS-1$
	//private static final String CONTENTASSIST = CCorePlugin.PLUGIN_ID + "/debug/contentassist" ; //$NON-NLS-1$

	/**
	 * Configure the plug-in with respect to option settings defined in ".options" file
	 */
	public void configurePluginDebugOptions() {
		
		if(CCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(PARSER);
			if(option != null) Util.VERBOSE_PARSER = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(PARSER_EXCEPTIONS);
			if( option != null ) Util.PARSER_EXCEPTIONS = option.equalsIgnoreCase("true"); //$NON-NLS-1$

			option = Platform.getDebugOption(SCANNER);
			if( option != null ) Util.VERBOSE_SCANNER = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			
			option = Platform.getDebugOption(MODEL);
			if(option != null) Util.VERBOSE_MODEL = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(DELTA);
			if(option != null) Util.VERBOSE_DELTA= option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
		}
	}

	// Preference to turn on/off the use of structural parse mode to build the CModel
	public void setStructuralParseMode(boolean useNewParser) {
		getPluginPreferences().setValue(PREF_USE_STRUCTURAL_PARSE_MODE, useNewParser);
		savePluginPreferences();
	}

	public boolean useStructuralParseMode() {
		return getPluginPreferences().getBoolean(PREF_USE_STRUCTURAL_PARSE_MODE);
	}
	
	/**
	 * @deprecated use {@link ITranslationUnit} or {@link ILanguage} to construct ASTs, instead.
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.CDOM getDOM() {
	    return org.eclipse.cdt.core.dom.CDOM.getInstance();
	}

	public ICdtVariableManager getCdtVariableManager(){
		return CdtVariableManager.getDefault();
	}
	
	public IEnvironmentVariableManager getBuildEnvironmentManager(){
		return EnvironmentVariableManager.getDefault();
	}
	
	public ICConfigurationDescription getPreferenceConfiguration(String buildSystemId) throws CoreException{
		return fNewCProjectDescriptionManager.getPreferenceConfiguration(buildSystemId);
	}

	public ICConfigurationDescription getPreferenceConfiguration(String buildSystemId, boolean write) throws CoreException{
		return fNewCProjectDescriptionManager.getPreferenceConfiguration(buildSystemId, write);
	}
	
	public void setPreferenceConfiguration(String buildSystemId, ICConfigurationDescription des) throws CoreException {
		fNewCProjectDescriptionManager.setPreferenceConfiguration(buildSystemId, des);
	}
	
	/**
	 * this method is a full equivalent to <code>createProjectDescription(IProject, boolean, false)</code>.
	 * 
	 * @see #createProjectDescription(IProject, boolean, boolean)
	 */
	public ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists) throws CoreException{
		return fNewCProjectDescriptionManager.createProjectDescription(project, loadIfExists);
	}

	/**
	 * the method creates and returns a writable project description
	 * 
	 * @param project project for which the project description is requested
	 * @param loadIfExists if true the method first tries to load and return the project description
	 * from the settings file (.cproject)
	 * if false, the stored settings are ignored and the new (empty) project description is created
	 * @param creating if true the created project description will be contain the true "isCdtProjectCreating" state.
	 * NOTE: in case the project already contains the project description AND its "isCdtProjectCreating" is false
	 * the resulting description will be created with the false "isCdtProjectCreating" state
	 * 
	 * NOTE: changes made to the returned project description will not be applied until the {@link #setProjectDescription(IProject, ICProjectDescription)} is called 
	 * @return {@link ICProjectDescription}
	 * @throws CoreException
	 */
	public ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists, boolean creating) throws CoreException{
		return fNewCProjectDescriptionManager.createProjectDescription(project, loadIfExists, creating);
	}
	
	/**
	 * returns the project description associated with this project or null if the project does not contain the
	 * CDT data associated with it. 
	 * 
	 * this is a convenience method fully equivalent to getProjectDescription(project, true)
	 * see {@link #getProjectDescription(IProject, boolean)} for more detail
	 * @param project
	 * @return a writable copy of the ICProjectDescription or null if the project does not contain the
	 * CDT data associated with it. 
	 * Note: changes to the project description will not be reflected/used by the core
	 * until the {@link #setProjectDescription(IProject, ICProjectDescription)} is called
	 * 
	 * @see #getProjectDescription(IProject, boolean)
	 */
	public ICProjectDescription getProjectDescription(IProject project){
		return fNewCProjectDescriptionManager.getProjectDescription(project);
	}
	
	/**
	 * this method is called to save/apply the project description
	 * the method should be called to apply changes made to the project description
	 * returned by the {@link #getProjectDescription(IProject, boolean)} or {@link #createProjectDescription(IProject, boolean)} 
	 * 
	 * @param project
	 * @param des
	 * @throws CoreException
	 * 
	 * @see #getProjectDescription(IProject, boolean)
	 * @see #createProjectDescription(IProject, boolean)
	 */
	public void setProjectDescription(IProject project, ICProjectDescription des) throws CoreException {
		fNewCProjectDescriptionManager.setProjectDescription(project, des);
	}

	public void setProjectDescription(IProject project, ICProjectDescription des, boolean force, IProgressMonitor monitor) throws CoreException {
		fNewCProjectDescriptionManager.setProjectDescription(project, des, force, monitor);
	}

	/**
	 * returns the project description associated with this project or null if the project does not contain the
	 * CDT data associated with it. 
	 * 
	 * @param project project for which the description is requested
	 * @param write if true, the writable description copy is returned. 
	 * If false the cached read-only description is returned.
	 * 
	 * CDT core maintains the cached project description settings. If only read access is needed to description,
	 * then the read-only project description should be obtained.
	 * This description always operates with cached data and thus it is better to use it for performance reasons
	 * All set* calls to the read-only description result in the {@link WriteAccessException}
	 * 
	 * When the writable description is requested, the description copy is created.
	 * Changes to this description will not be reflected/used by the core and Build System untill the
	 * {@link #setProjectDescription(IProject, ICProjectDescription)} is called
	 *
	 * Each getProjectDescription(project, true) returns a new copy of the project description 
	 * 
	 * The writable description uses the cached data untill the first set call
	 * after that the description communicates directly to the Build System
	 * i.e. the implementer of the org.eclipse.cdt.core.CConfigurationDataProvider extension
	 * This ensures the Core<->Build System settings integrity
	 * 
	 * @return {@link ICProjectDescription} or null if the project does not contain the
	 * CDT data associated with it. 
	 */
	public ICProjectDescription getProjectDescription(IProject project, boolean write){
		return fNewCProjectDescriptionManager.getProjectDescription(project, write);
	}

	/**
	 * forces the cached data of the specified projects to be re-calculated.
	 * if the <code>projects</code> argument is <code>null</code> al projects 
	 * within the workspace are updated
	 * 
	 * @param projects
	 * @param monitor
	 * @throws CoreException 
	 */
	public void updateProjectDescriptions(IProject projects[], IProgressMonitor monitor) throws CoreException{
		fNewCProjectDescriptionManager.updateProjectDescriptions(projects, monitor);
	}
	
	/**
	 * Answers whether the given project is a new-style project, i.e. CConfigurationDataProvider-driven
	 */
	public boolean isNewStyleProject(IProject project){
		return fNewCProjectDescriptionManager.isNewStyleProject(project);
	}

	/**
	 * Answers whether the given project is a new-style project, i.e. CConfigurationDataProvider-driven
	 */
	public boolean isNewStyleProject(ICProjectDescription des){
		return fNewCProjectDescriptionManager.isNewStyleProject(des);
	}
	
	public ICProjectDescriptionManager getProjectDescriptionManager(){
		return fNewCProjectDescriptionManager;
	}
	
	/**
	 * @return editable User-variable's supplier 
	 */
	public static IUserVarSupplier getUserVarSupplier() {
		return UserVarSupplier.getInstance();
	}
	
	// NON-API

	/**
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public CCorePlugin() {
		super();
		fgCPlugin = this;
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String e) {
		log(createStatus(e));
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException 
				&& (nestedException = ((CModelException)e).getException()) != null) {
			e = nestedException;
		}
		log(createStatus(message, e));
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * @deprecated use getIndexManager().
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static IPDOMManager getPDOMManager() {
		return getDefault().pdomManager;
	}

	/**
	 * Returns preference controlling whether source roots are shown at the top of projects
	 * or embedded within the resource tree of projects when they are not top level folders.
	 * 
	 * @return boolean preference value
	 * @since 5.2
	 */
	public static boolean showSourceRootsAtTopOfProject() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID)
			.getBoolean(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT, true);
	}
}