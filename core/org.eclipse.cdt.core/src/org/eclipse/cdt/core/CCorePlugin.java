package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.core.internal.filetype.ResolverModel;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.cdt.core.resources.ScannerProvider;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.CDTLogWriter;
import org.eclipse.cdt.internal.core.CDescriptorManager;
import org.eclipse.cdt.internal.core.PathEntryVariableManager;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexerRunner;
import org.eclipse.cdt.internal.core.model.BufferManager;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.DeltaProcessor;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.BundleContext;

public class CCorePlugin extends Plugin {

	public static final int STATUS_CDTPROJECT_EXISTS = 1;
	public static final int STATUS_CDTPROJECT_MISMATCH = 2;
	public static final int CDT_PROJECT_NATURE_ID_MISMATCH = 3;

	public static final String PLUGIN_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$

	public static final String BUILDER_MODEL_ID = PLUGIN_ID + ".CBuildModel"; //$NON-NLS-1$
	public static final String BINARY_PARSER_SIMPLE_ID = "BinaryParser"; //$NON-NLS-1$
	public final static String BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + BINARY_PARSER_SIMPLE_ID; //$NON-NLS-1$
	public final static String PREF_BINARY_PARSER = "binaryparser"; //$NON-NLS-1$
	public final static String DEFAULT_BINARY_PARSER_SIMPLE_ID = "ELF"; //$NON-NLS-1$
	public final static String DEFAULT_BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + DEFAULT_BINARY_PARSER_SIMPLE_ID; //$NON-NLS-1$
	public final static String PREF_USE_STRUCTURAL_PARSE_MODE = "useStructualParseMode"; //$NON-NLS-1$
	
	public final static String ERROR_PARSER_SIMPLE_ID = "ErrorParser"; //$NON-NLS-1$

	// default store for pathentry
	public final static String DEFAULT_PATHENTRY_STORE_ID = PLUGIN_ID + ".cdtPathEntryStore"; //$NON-NLS-1$

	// Build Model Interface Discovery
	public final static String BUILD_SCANNER_INFO_SIMPLE_ID = "ScannerInfoProvider"; //$NON-NLS-1$
	public final static String BUILD_SCANNER_INFO_UNIQ_ID = PLUGIN_ID + "." + BUILD_SCANNER_INFO_SIMPLE_ID; //$NON-NLS-1$

	/**
	 * Name of the extension point for contributing a source code formatter
	 */
	public static final String FORMATTER_EXTPOINT_ID = "CodeFormatter" ; //$NON-NLS-1$

	/**
	 * Possible configurable option value for TRANSLATION_TASK_PRIORITIES.
	 * @see #getDefaultOptions
	 */
	public static final String TRANSLATION_TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$	    
    /**
     * Possible configurable option value for TRANSLATION_TASK_PRIORITIES.
     * @see #getDefaultOptions
     */
    public static final String TRANSLATION_TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$
    /**
     * Possible configurable option value for TRANSLATION_TASK_PRIORITIES.
     * @see #getDefaultOptions
     */
    public static final String TRANSLATION_TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see #getDefaultOptions
     */
    public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$
	public CDTLogWriter cdtLog = null;

	private static CCorePlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;

	private CDescriptorManager fDescriptorManager = new CDescriptorManager();

	private CoreModel fCoreModel;

	private PathEntryVariableManager fPathEntryVariableManager;

	// -------- static methods --------

	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.internal.core.CCorePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	/**
	 * Answers the shared working copies currently registered for this buffer factory. 
	 * Working copies can be shared by several clients using the same buffer factory,see 
	 * <code>IWorkingCopy.getSharedWorkingCopy</code>.
	 * 
	 * @param factory the given buffer factory
	 * @return the list of shared working copies for a given buffer factory
	 * @see IWorkingCopy
	 */
	public static IWorkingCopy[] getSharedWorkingCopies(IBufferFactory factory){
		
		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager().getDefaultBufferFactory();
		Map sharedWorkingCopies = CModelManager.getDefault().sharedWorkingCopies;
		
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null) return CModelManager.NoWorkingCopy;
		Collection copies = perFactoryWorkingCopies.values();
		IWorkingCopy[] result = new IWorkingCopy[copies.size()];
		copies.toArray(result);
		return result;
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
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	public static CCorePlugin getDefault() {
		return fgCPlugin;
	}

	public static void log(Throwable e) {
		if ( e instanceof CoreException ) {
			log(((CoreException)e).getStatus());
		} else {
			log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
		}
	}

	public static void log(IStatus status) {
		((Plugin) getDefault()).getLog().log(status);
	}

	// ------ CPlugin

	public CCorePlugin() {
		super();
		fgCPlugin = this;
	}

	/**
	 * @see Plugin#shutdown
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			if (fDescriptorManager != null) {
				fDescriptorManager.shutdown();
			}
			
			if (fCoreModel != null) {
				fCoreModel.shutdown();
			}
			
			if (cdtLog != null) {
				cdtLog.shutdown();
			}

			if (fPathEntryVariableManager != null) {
				fPathEntryVariableManager.shutdown();
			}

			savePluginPreferences();
		} finally {
			super.stop(context);
		}
	}

	/**
	 * @see Plugin#startup
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		cdtLog = new CDTLogWriter(CCorePlugin.getDefault().getStateLocation().append(".log").toFile()); //$NON-NLS-1$
		
		//Set debug tracing options
		CCorePlugin.getDefault().configurePluginDebugOptions();
		
		fDescriptorManager.startup();

		// Fired up the model.
		fCoreModel = CoreModel.getDefault();
		fCoreModel.startup();

		//Fired up the indexer
		fCoreModel.startIndexing();

		// Set the default for using the structual parse mode to build the CModel
		getPluginPreferences().setDefault(PREF_USE_STRUCTURAL_PARSE_MODE, false);

		// Start file type manager
		fPathEntryVariableManager = new PathEntryVariableManager();
		fPathEntryVariableManager.startup();

	}
    
    
    /**
     * TODO: Add all options here
     * Returns a table of all known configurable options with their default values.
     * These options allow to configure the behaviour of the underlying components.
     * The client may safely use the result as a template that they can modify and
     * then pass to <code>setOptions</code>.
     * 
     * Helper constants have been defined on CCorePlugin for each of the option ID and 
     * their possible constant values.
     * 
     * Note: more options might be added in further releases.
     * <pre>
     * RECOGNIZED OPTIONS:
     * TRANSLATION / Define the Automatic Task Tags
     *    When the tag list is not empty, translation will issue a task marker whenever it encounters
     *    one of the corresponding tags inside any comment in C/C++ source code.
     *    Generated task messages will include the tag, and range until the next line separator or comment ending.
     *    Note that tasks messages are trimmed. If a tag is starting with a letter or digit, then it cannot be leaded by
     *    another letter or digit to be recognized ("fooToDo" will not be recognized as a task for tag "ToDo", but "foo#ToDo"
     *    will be detected for either tag "ToDo" or "#ToDo"). Respectively, a tag ending with a letter or digit cannot be followed
     *    by a letter or digit to be recognized ("ToDofoo" will not be recognized as a task for tag "ToDo", but "ToDo:foo" will
     *    be detected either for tag "ToDo" or "ToDo:").
     *     - option id:         "org.eclipse.cdt.core.translation.taskTags"
     *     - possible values:   { "<tag>[,<tag>]*" } where <tag> is a String without any wild-card or leading/trailing spaces 
     *     - default:           ""
     * 
     * TRANSLATION / Define the Automatic Task Priorities
     *    In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
     *    of the task markers issued by the translation.
     *    If the default is specified, the priority of each task marker is "NORMAL".
     *     - option id:         "org.eclipse.cdt.core.transltaion.taskPriorities"
     *     - possible values:   { "<priority>[,<priority>]*" } where <priority> is one of "HIGH", "NORMAL" or "LOW"
     *     - default:           ""
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
    
    public static HashMap getDefaultOptions()
    {
        HashMap defaultOptions = new HashMap(10);

        // see #initializeDefaultPluginPreferences() for changing default settings
        Preferences preferences = getDefault().getPluginPreferences();
        HashSet optionNames = CModelManager.OptionNames;
        
        // get preferences set to their default
        String[] defaultPropertyNames = preferences.defaultPropertyNames();
        for (int i = 0; i < defaultPropertyNames.length; i++){
            String propertyName = defaultPropertyNames[i];
            if (optionNames.contains(propertyName)) {
                defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
            }
        }       
        // get preferences not set to their default
        String[] propertyNames = preferences.propertyNames();
        for (int i = 0; i < propertyNames.length; i++){
            String propertyName = propertyNames[i];
            if (optionNames.contains(propertyName)) {
                defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
            }
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
    public static HashMap getOptions() {
        
        HashMap options = new HashMap(10);

        // see #initializeDefaultPluginPreferences() for changing default settings
        Plugin plugin = getDefault();
        if (plugin != null) {
            Preferences preferences = plugin.getPluginPreferences();
            HashSet optionNames = CModelManager.OptionNames;
            
            // get preferences set to their default
            String[] defaultPropertyNames = preferences.defaultPropertyNames();
            for (int i = 0; i < defaultPropertyNames.length; i++){
                String propertyName = defaultPropertyNames[i];
                if (optionNames.contains(propertyName)){
                    options.put(propertyName, preferences.getDefaultString(propertyName));
                }
            }       
            // get preferences not set to their default
            String[] propertyNames = preferences.propertyNames();
            for (int i = 0; i < propertyNames.length; i++){
                String propertyName = propertyNames[i];
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
    public static void setOptions(HashMap newOptions) {
    
        // see #initializeDefaultPluginPreferences() for changing default settings
        Preferences preferences = getDefault().getPluginPreferences();

        if (newOptions == null){
            newOptions = getDefaultOptions();
        }
        Iterator keys = newOptions.keySet().iterator();
        while (keys.hasNext()){
            String key = (String)keys.next();
            if (!CModelManager.OptionNames.contains(key)) continue; // unrecognized option
            if (key.equals(CORE_ENCODING)) continue; // skipped, contributed by resource prefs
            String value = (String)newOptions.get(key);
            preferences.setValue(key, value);
        }
    
        // persist options
        getDefault().savePluginPreferences();
    }    
    

	public IConsole getConsole(String id) {
		try {
	        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "CBuildConsole"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String builderID = configElements[j].getAttribute("id"); //$NON-NLS-1$
						if ((id == null && builderID == null) || (id != null && id.equals(builderID))) {
							return (IConsole) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (CoreException e) {
			log(e);
		}
		return new IConsole() { // return a null console
			private ConsoleOutputStream nullStream = new ConsoleOutputStream() {
			    public void write(byte[] b) throws IOException {
			    }			    
				public void write(byte[] b, int off, int len) throws IOException {
				}					
				public void write(int c) throws IOException {
				}
			};
			
			public void start(IProject project) {
			}
		    // this can be a null console....
			public ConsoleOutputStream getOutputStream() {
				return nullStream;
			}
			public ConsoleOutputStream getInfoStream() {
				return nullStream; 
			}
			public ConsoleOutputStream getErrorStream() {
				return nullStream;
			}
		};
	}

	public IConsole getConsole() {
		return getConsole(null);
	}

	public ICExtensionReference[] getBinaryParserExtensions(IProject project) throws CoreException {
		ICExtensionReference ext[] = new ICExtensionReference[0];
		if (project != null) {
			try {
				ICDescriptor cdesc = getCProjectDescription(project);
				ICExtensionReference[] cextensions = cdesc.get(BINARY_PARSER_UNIQ_ID, true);
				if (cextensions.length > 0) {
					ArrayList list = new ArrayList(cextensions.length);
					for (int i = 0; i < cextensions.length; i++) {
						list.add(cextensions[i]);
					}
					ext = (ICExtensionReference[])list.toArray(ext);
				}
			} catch (CoreException e) {
				log(e);
			}
		}
		return ext;
	}

	/**
	 * @param project
	 * @return
	 * @throws CoreException
	 * @deprecated - use getBinaryParserExtensions(IProject project)
	 */
	public IBinaryParser[] getBinaryParser(IProject project) throws CoreException {
		IBinaryParser parsers[] = null;
		if (project != null) {
			try {
				ICDescriptor cdesc = getCProjectDescription(project);
				ICExtensionReference[] cextensions = cdesc.get(BINARY_PARSER_UNIQ_ID, true);
				if (cextensions.length > 0) {
					ArrayList list = new ArrayList(cextensions.length);
					for (int i = 0; i < cextensions.length; i++) {
						IBinaryParser parser = null;
						try {
							parser = (IBinaryParser) cextensions[i].createExtension();
						} catch (ClassCastException e) {
							//
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
		if (id == null || id.length() == 0) {
			id = DEFAULT_BINARY_PARSER_UNIQ_ID;
		}
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, BINARY_PARSER_SIMPLE_ID);
		IExtension extension = extensionPoint.getExtension(id);
		if (extension != null) {
			IConfigurationElement element[] = extension.getConfigurationElements();
			for (int i = 0; i < element.length; i++) {
				if (element[i].getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
					parser = (IBinaryParser) element[i].createExecutableExtension("run"); //$NON-NLS-1$
					break;
				}
			}
		} else {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("CCorePlugin.exception.noBinaryFormat"), null); //$NON-NLS-1$
			throw new CoreException(s);
		}
		return parser;
	}

	/**
	 * Returns the file type object corresponding to the provided
	 * file name.
	 * 
	 * If no file type object exists, a default file type object is
	 * returned.
	 * 
	 * @param project Project to resolve type info for.
	 * @param fileName Name of the file to resolve type info for.
	 * 
	 * @return File type object for the provided file name, in the context
	 * of the given project (or the workspace, if project is null)
	 */
	public ICFileType getFileType(IProject project, String fileName) {	
		return getFileTypeResolver(project).getFileType(fileName);
	}

	/**
	 * Return the file type resolver for the specified project.
	 * Specifying a null project returns the file type resolver
	 * for the workspace.
	 * 
	 * @param project Project to get file type resolver for.
	 * 	 * 
	 * @return File type resolver for the project.
	 */
	public ICFileTypeResolver getFileTypeResolver(IProject project) {	
		if (null == project) {
			return getResolverModel().getResolver();
		}
		return getResolverModel().getResolver(project);
	}

	public IResolverModel getResolverModel() {	
		return ResolverModel.getDefault();
	}
	
	public CoreModel getCoreModel() {
		return fCoreModel;
	}

	public IPathEntryVariableManager getPathEntryVariableManager() {
		return fPathEntryVariableManager;
	}

	/**
	 * @param project
	 * @return
	 * @throws CoreException
	 * @deprecated use getCProjetDescription(IProject project, boolean create)
	 */
	public ICDescriptor getCProjectDescription(IProject project) throws CoreException {
		return fDescriptorManager.getDescriptor(project);
	}

	/**
	 * Get the ICDescriptor for the given project, if <b>create</b> is <b>true</b> then a descriptor will be created
	 * if one does not exist.
	 * 
	 * @param project
	 * @param create
	 * @return ICDescriptor or <b>null</b> if <b>create</b> is <b>false</b> and no .cdtproject file exists on disk.
	 * @throws CoreException
	 */
	public ICDescriptor getCProjectDescription(IProject project, boolean create) throws CoreException {
		return fDescriptorManager.getDescriptor(project, create);
	}

	public void mapCProjectOwner(IProject project, String id, boolean override) throws CoreException {
		if (!override) {
			fDescriptorManager.configure(project, id);
		} else {
			fDescriptorManager.convert(project, id);
		}
	}
	
	public ICDescriptorManager getCDescriptorManager() {
		return fDescriptorManager;
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
					projectHandle.open(new SubProgressMonitor(monitor, 1));

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
	 * 
	 * @param project
	 * @param String targetNature
	 * @param monitor
	 * @param projectID
	 * @exception CoreException
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

	/**
	 * Method to convert a project to a C++ nature 
	 * 
	 * @param project
	 * @param String targetNature
	 * @param monitor
	 * @param projectID
	 * @exception CoreException
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

	/**
	 * Get the IProcessList contributed interface for the platform.
	 * @return IProcessList
	 */
	public IProcessList getProcessList() throws CoreException {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "ProcessList"); //$NON-NLS-1$
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			IConfigurationElement defaultContributor = null;
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					if (configElements[j].getName().equals("processList")) { //$NON-NLS-1$
						String platform = configElements[j].getAttribute("platform"); //$NON-NLS-1$
						if (platform == null ) { // first contrbutor found with not platform will be default.
							if (defaultContributor == null) {
								defaultContributor = configElements[j];
							}
						} else if (platform.equals(Platform.getOS())) {
							// found explicit contributor for this platform.
							return (IProcessList) configElements[0].createExecutableExtension("class"); //$NON-NLS-1$
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
	 * Array of error parsers ids.
	 * @return
	 */
	public String[] getAllErrorParsersIDs() {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, ERROR_PARSER_SIMPLE_ID);
		String[] empty = new String[0];
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			ArrayList list = new ArrayList(extensions.length);
			for (int i = 0; i < extensions.length; i++) {
				list.add(extensions[i].getUniqueIdentifier());
			}
			return (String[]) list.toArray(empty);
		}
		return empty;
	}

	public IErrorParser[] getErrorParser(String id) {
		IErrorParser[] empty = new IErrorParser[0];
		try {
	        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, ERROR_PARSER_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				List list = new ArrayList(extensions.length);
				for (int i = 0; i < extensions.length; i++) {
					String parserID = extensions[i].getUniqueIdentifier();
					if ((id == null && parserID != null) || (id != null && id.equals(parserID))) {
						IConfigurationElement[] configElements = extensions[i]. getConfigurationElements();
						for (int j = 0; j < configElements.length; j++) {
							IErrorParser parser = (IErrorParser)configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							list.add(parser);
						}
					}
				}
				return (IErrorParser[]) list.toArray(empty);
			}
		} catch (CoreException e) {
			log(e);
		}
		return empty;
	}

	public IScannerInfoProvider getScannerInfoProvider(IProject project) {
		IScannerInfoProvider provider = null;
		if (project != null) {
			try {
				ICDescriptor desc = getCProjectDescription(project);
				ICExtensionReference[] extensions = desc.get(BUILD_SCANNER_INFO_UNIQ_ID, true);
				if (extensions.length > 0)
					provider = (IScannerInfoProvider) extensions[0].createExtension();
			} catch (CoreException e) {
				// log(e);
			}
			if ( provider == null) {
				return ScannerProvider.getInstance();
			}
		}
		return provider;
	}

	private static final String MODEL = CCorePlugin.PLUGIN_ID + "/debug/model" ; //$NON-NLS-1$
	private static final String INDEXER = CCorePlugin.PLUGIN_ID + "/debug/indexer"; //$NON-NLS-1$
	private static final String INDEX_MANAGER = CCorePlugin.PLUGIN_ID + "/debug/indexmanager"; //$NON-NLS-1$
	private static final String SEARCH  = CCorePlugin.PLUGIN_ID + "/debug/search" ; //$NON-NLS-1$
	private static final String MATCH_LOCATOR  = CCorePlugin.PLUGIN_ID + "/debug/matchlocator" ; //$NON-NLS-1$
	private static final String PARSER = CCorePlugin.PLUGIN_ID + "/debug/parser" ; //$NON-NLS-1$
	private static final String SCANNER = CCorePlugin.PLUGIN_ID + "/debug/scanner"; //$NON-NLS-1$
	private static final String DELTA = CCorePlugin.PLUGIN_ID + "/debug/deltaprocessor" ; //$NON-NLS-1$
	private static final String RESOLVER = CCorePlugin.PLUGIN_ID + "/debug/typeresolver" ; //$NON-NLS-1$
	//private static final String CONTENTASSIST = CCorePlugin.PLUGIN_ID + "/debug/contentassist" ; //$NON-NLS-1$

	/**
	 * Configure the plugin with respect to option settings defined in ".options" file
	 */
	public void configurePluginDebugOptions() {
		
		if(CCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(PARSER);
			if(option != null) Util.VERBOSE_PARSER = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
		
			option = Platform.getDebugOption(SCANNER);
			if( option != null ) Util.VERBOSE_SCANNER = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			
			option = Platform.getDebugOption(MODEL);
			if(option != null) Util.VERBOSE_MODEL = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			boolean indexFlag = false;
			option = Platform.getDebugOption(INDEX_MANAGER);
			if(option != null) {
				indexFlag = option.equalsIgnoreCase("true"); //$NON-NLS-1$
				IndexManager.VERBOSE = indexFlag;
			} //$NON-NLS-1$
			
			option = Platform.getDebugOption(INDEXER);
			if(option != null) SourceIndexerRunner.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
		
			option = Platform.getDebugOption(SEARCH);
			if(option != null) SearchEngine.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(DELTA);
			if(option != null) DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(MATCH_LOCATOR);
			if(option != null) MatchLocator.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(RESOLVER);
			if(option != null) ResolverModel.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			if (indexFlag == true){
			   JobManager.VERBOSE = true; 	
			}
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
	
	public CDOM getDOM() {
	    return CDOM.getInstance();
	}

	
}