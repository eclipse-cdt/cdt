package org.eclipse.cdt.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.BuildConsoleManager;
import org.eclipse.cdt.internal.ui.CElementAdapterFactory;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ResourceAdapterFactory;
import org.eclipse.cdt.internal.ui.cview.CView;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.WorkingCopyManager;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextTools;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CEditorPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.util.ProblemMarkerManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CUIPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.ui";
	public static final String PLUGIN_CORE_ID = "org.eclipse.cdt.core";
	public static final String EDITOR_ID = PLUGIN_ID + ".editor.CEditor";
	public static final String CONSOLE_ID = PLUGIN_ID + ".BuildConsoleView";
	public static final String CVIEW_ID = PLUGIN_ID + ".CView";
	public static final String MAKEVIEW_ID = PLUGIN_ID + ".MakeView";
	public static final String C_PROBLEMMARKER = PLUGIN_CORE_ID + ".problem";

	public static final String C_PROJECT_WIZARD_ID = PLUGIN_ID + ".wizards.StdCWizard";
	public static final String CPP_PROJECT_WIZARD_ID = PLUGIN_ID + ".wizards.StdCCWizard";

	public static final String FILE_WIZARD_ID = "org.eclipse.ui.wizards.new.file";
	public static final String FOLDER_WIZARD_ID = "org.eclipse.ui.wizards.new.folder";
	public static final String CLASS_WIZARD_ID = "org.eclipse.cdt.ui.wizards.NewClassWizard";
	public static final String FOLDER_ACTION_SET_ID = PLUGIN_ID + ".CFolderActionSet";
	public static final String BUILDER_ID = PLUGIN_CORE_ID + ".cbuilder";

	private static CUIPlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;
	private ImageDescriptorRegistry fImageDescriptorRegistry;

	static String SEPARATOR = System.getProperty("file.separator");

	// -------- static methods --------

	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.internal.ui.CPluginResources");
		}
		catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		}
		catch (MissingResourceException e) {
			return "!" + key + "!";
		}
		catch (NullPointerException e) {
			return "#" + key + "#";
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

	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * @return
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getDefault().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	public Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static CUIPlugin getDefault() {
		return fgCPlugin;
	}

	public void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
	}

	public void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	// ------ CUIPlugin

	private CoreModel fCoreModel;
	private CDocumentProvider fDocumentProvider;
	private WorkingCopyManager fWorkingCopyManager;
	private CTextTools fTextTools;
	private AsmTextTools fAsmTextTools;
	private ProblemMarkerManager fProblemMarkerManager;
	private BuildConsoleManager fBuildConsoleManager;
	private ResourceAdapterFactory fResourceAdapterFactory;
	private CElementAdapterFactory fCElementAdapterFactory;


	public CUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgCPlugin = this;
		fDocumentProvider = null;
		fTextTools = null;
	}
	
	/**
	 * Returns the used document provider
	 */
	public synchronized CDocumentProvider getDocumentProvider() {
		if (fDocumentProvider == null) {
			fDocumentProvider = new CDocumentProvider();
		}
		return fDocumentProvider;
	}

	/**
	 * Returns the working copy manager
	 * @return IWorkingCopyManager
	 */
	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (fWorkingCopyManager == null) {
			CDocumentProvider provider = getDocumentProvider();
			fWorkingCopyManager = new WorkingCopyManager(provider);
		}
		return fWorkingCopyManager;
	}

	/**
	 * Returns the shared text tools
	 */
	public CTextTools getTextTools() {
		if (fTextTools == null)
			fTextTools = new CTextTools();
		return fTextTools;
	}

	/**
	 * Returns the shared assembly text tools
	 */
	public AsmTextTools getAsmTextTools() {
		if (fAsmTextTools == null)
			fAsmTextTools = new AsmTextTools();
		return fAsmTextTools;
	}

	public IBuildConsoleManager getConsoleManager() {
		if ( fBuildConsoleManager == null ) {
			fBuildConsoleManager = new BuildConsoleManager();
			fBuildConsoleManager.startup();
		}
		return fBuildConsoleManager;
	}

	/**
	 * @see Plugin#shutdown
	 */
	public void shutdown() throws CoreException {
		if (fTextTools != null) {
			fTextTools.dispose();
		}
		if (fImageDescriptorRegistry != null)
			fImageDescriptorRegistry.dispose();
		if ( fBuildConsoleManager != null ) {
			fBuildConsoleManager.shutdown();
			fBuildConsoleManager = null;
		}

		unregisterAdapters();

		super.shutdown();

		if (fWorkingCopyManager != null) {
			fWorkingCopyManager.shutdown();
			fWorkingCopyManager= null;
		}
                
		if (fDocumentProvider != null) {
			fDocumentProvider.shutdown();
			fDocumentProvider= null;
		}
	}

	private void runUI(Runnable run) {
		Display display;
		display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
			display.asyncExec(run);
		}
		else {
			run.run();
		}
	}

	/**
	 * @see Plugin#startup
	 */
	public void startup() throws CoreException {
		super.startup();
		runUI(new Runnable() {
			public void run() {
				registerAdapters();
				CPluginImages.initialize();
			}
		});
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(final IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		runUI(new Runnable() {
			public void run() {
				CPluginPreferencePage.initDefaults(store);
				CEditorPreferencePage.initDefaults(store);
				CView.initDefaults(store);
				BuildConsolePreferencePage.initDefaults(store);
			}
		});
	}

	public CoreModel getCoreModel() {
		return fCoreModel;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null)
			fImageDescriptorRegistry = new ImageDescriptorRegistry();
		return fImageDescriptorRegistry;
	}

	/**
	 * Returns the problem marker manager
	 */
	public ProblemMarkerManager getProblemMarkerManager() {
		if (fProblemMarkerManager == null)
			fProblemMarkerManager = new ProblemMarkerManager();
		return fProblemMarkerManager;
	}

	private void registerAdapters() {
		fResourceAdapterFactory = new ResourceAdapterFactory();
		fCElementAdapterFactory = new CElementAdapterFactory();

		IAdapterManager manager = Platform.getAdapterManager();
		manager.registerAdapters(fResourceAdapterFactory, IResource.class);
		manager.registerAdapters(fCElementAdapterFactory, ICElement.class);
	}
        
	private void unregisterAdapters() {
		IAdapterManager manager = Platform.getAdapterManager();
		manager.unregisterAdapters(fResourceAdapterFactory);
		manager.unregisterAdapters(fCElementAdapterFactory);
	}

}
