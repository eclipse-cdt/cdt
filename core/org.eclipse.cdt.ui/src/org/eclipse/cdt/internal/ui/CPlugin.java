package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.ui.cview.CView;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextTools;
import org.eclipse.cdt.internal.ui.preferences.CEditorPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.util.ProblemMarkerManager;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class CPlugin extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID= "org.eclipse.cdt.ui";
	public static final String PLUGIN_CORE_ID= "org.eclipse.cdt.core";
	public static final String EDITOR_ID= PLUGIN_ID + ".editor.CEditor";
	public static final String CONSOLE_ID= PLUGIN_ID + ".BuildConsoleView";
	public static final String CVIEW_ID= PLUGIN_ID + ".CView";
	public static final String MAKEVIEW_ID= PLUGIN_ID + ".MakeView";
	public static final String C_PROBLEMMARKER= PLUGIN_CORE_ID + ".problem";

	public static final String C_PROJECT_WIZARD_ID= PLUGIN_ID + ".wizards.StdCWizard";
	public static final String CPP_PROJECT_WIZARD_ID= PLUGIN_ID + ".wizards.StdCCWizard";

	public static final String FILE_WIZARD_ID= PLUGIN_ID + ".wizards.new.BasicNewFileResourceWizard";
	public static final String FOLDER_WIZARD_ID= PLUGIN_ID + ".wizards.new.BasicNewFolderResourceWizard";

	public static final String FOLDER_ACTION_SET_ID= PLUGIN_ID + ".CFolderActionSet";
	public static final String BUILDER_ID= PLUGIN_CORE_ID + ".cbuilder";
		
	private static CPlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;
	private ImageDescriptorRegistry fImageDescriptorRegistry;

	static String SEPARATOR = System.getProperty("file.separator");

	// -------- static methods --------
	
	static {
		try {
			fgResourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.internal.ui.CPluginResources");
		} catch (MissingResourceException x) {
			fgResourceBundle= null;
		}
	}

	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		} catch (NullPointerException e) {
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
		
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}	
	
	public static CPlugin getDefault() {
		return fgCPlugin;
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}	
	
	// ------ CPlugin
	
	private ConsoleDocument fConsoleDocument;
	private CoreModel fCoreModel;
	private CDocumentProvider fDocumentProvider;
	private CTextTools fTextTools;
	private AsmTextTools fAsmTextTools;
	private ProblemMarkerManager fProblemMarkerManager;

	static class ConsoleDocument extends ConsoleOutputStream implements IConsole {
		private IDocument fDocument;
    	
		public ConsoleDocument() {
			fDocument = new Document();
		}
    	
    	public void start(IProject project) {
    	}
    	
		public void clear() {
			super.clear();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					fDocument.set("");
					fContent.setLength(0);
				}
			});
		}
			
		public ConsoleOutputStream getOutputStream() {
			return this;
		}

		public IDocument getDocument() {
			return fDocument;
		}
	    
		public synchronized void flush() throws IOException {
			super.flush();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (CPluginPreferencePage.isConsoleOnTop())
						bringConsoleOnTop();
					try {
						int len = fDocument.getLength ();
						fDocument.replace(len, 0, getContent(len));
					} catch (BadLocationException x) {
					}
				}
			});
		}

		void bringConsoleOnTop () {
			IWorkbenchWindow window = getActiveWorkbenchWindow();
			if ( window == null )
				return;
			IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					try {
						// show the build console
						IViewPart cBuild = page.findView(CPlugin.CONSOLE_ID);
						if(cBuild == null) {
							if(CPluginPreferencePage.isAutoOpenConsole()) {
								IWorkbenchPart activePart = page.getActivePart();
								cBuild = page.showView(CPlugin.CONSOLE_ID);
								//restore focus
								page.activate(activePart);
							}
						} else {
							page.bringToTop(cBuild);
						}
					} catch (PartInitException pie) {
					}
				}
		}
	}
    
	public CPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgCPlugin= this;
/*		
		fModel = new ACDebugModel() {
    		public Object createPresentation() {
    			return new CDebugModelPresentation();
    		}
		
    		public String getIdentifier() {
    			return PLUGIN_ID;
    		}
    
			public IMarker createBreakpoint( final IResource resource, 
											final Map attributes,
											final String markerType ) throws CoreException {

				class BreakpointRunnable implements IWorkspaceRunnable {
					IMarker fBreakpoint = null;
		
					public void run( IProgressMonitor monitor ) throws CoreException 
					{
						fBreakpoint = resource.createMarker( markerType );
						fBreakpoint.setAttributes( attributes );
					}
				};
				BreakpointRunnable r = new BreakpointRunnable();
			
				resource.getWorkspace().run( r, null );
		
				return r.fBreakpoint;
			}
		};
*/		
		fConsoleDocument= new ConsoleDocument();
		fDocumentProvider= null;
		fTextTools= null;
	}

	/**
	 * Returns the used document provider
	 */	
	public CDocumentProvider getDocumentProvider() {
		if (fDocumentProvider == null) {
			fDocumentProvider= new CDocumentProvider();
		}
		return fDocumentProvider;
	}

	/**
	 * Returns the shared text tools
	 */		
	public CTextTools getTextTools() {
		if (fTextTools == null)
			fTextTools= new CTextTools();
		return fTextTools;
	}

	/**
	 * Returns the shared assembly text tools
	 */		
	public AsmTextTools getAsmTextTools() {
		if (fAsmTextTools == null)
			fAsmTextTools= new AsmTextTools();
		return fAsmTextTools;
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
		super.shutdown();
	}		
	
	private void runUI(Runnable run) {
		Display display;
		display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
			display.asyncExec(run);
		} else {
			run.run();
	}
	}

	/**
	 * @see Plugin#startup
	 */
	public void startup() throws CoreException {
		super.startup();
		IAdapterManager manager= Platform.getAdapterManager();
		manager.registerAdapters(new ResourceAdapterFactory(), IResource.class);
		manager.registerAdapters(new CElementAdapterFactory(), ICElement.class);
		runUI(new Runnable() {
				public void run() {
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
	}
		});
	}
	
	public IConsole getConsole() {
		return fConsoleDocument;
	}
	
	public IDocument getConsoleDocument() {
		return fConsoleDocument.getDocument();
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
			fImageDescriptorRegistry= new ImageDescriptorRegistry();
		return fImageDescriptorRegistry;
	}
	
	/**
	 * Returns the problem marker manager
	 */		
	public ProblemMarkerManager getProblemMarkerManager() {
		if (fProblemMarkerManager == null)
			fProblemMarkerManager= new ProblemMarkerManager();
		return fProblemMarkerManager;
	}	
}
