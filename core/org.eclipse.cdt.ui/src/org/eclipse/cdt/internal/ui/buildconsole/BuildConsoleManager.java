/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Red Hat Inc. - multiple build console support
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.osgi.service.prefs.Preferences;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleEvent;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.cdt.ui.IBuildConsoleManager;

import org.eclipse.cdt.internal.core.LocalProjectScope;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;

public class BuildConsoleManager implements IBuildConsoleManager, IResourceChangeListener, IPropertyChangeListener {
	private static final String QUALIFIER = CUIPlugin.PLUGIN_ID;
	private static final String BUILD_CONSOLE_NODE = "buildConsole"; //$NON-NLS-1$
	public static final String KEY_KEEP_LOG = "keepLog"; //$NON-NLS-1$
	public static final String KEY_LOG_LOCATION = "logLocation"; //$NON-NLS-1$
	public static final boolean CONSOLE_KEEP_LOG_DEFAULT = true;

	private ListenerList listeners = new ListenerList();
	private BuildConsole fConsole;
	private Map<IProject, BuildConsolePartitioner> fConsoleMap = new HashMap<IProject, BuildConsolePartitioner>();
	private Color infoColor;
	private Color outputColor;
	private Color errorColor;
	private Color backgroundColor;
	private Color problemHighlightedColor;
	private Color problemErrorBackgroundColor;
	private Color problemInfoBackgroundColor;
	private Color problemWarningBackgroundColor;

	public Color getProblemHighlightedColor() {
		return problemHighlightedColor;
	}

	/**
	 * This function returns background color for errors only now
	 */
	public Color getProblemBackgroundColor() {
		return problemErrorBackgroundColor;
	}

	public Color getWarningBackgroundColor() {
		return problemWarningBackgroundColor;
	}
	
	public Color getInfoBackgroundColor() {
		return problemInfoBackgroundColor;
	}
	
	private BuildConsoleStreamDecorator infoStream;
	private BuildConsoleStreamDecorator outputStream;
	private BuildConsoleStreamDecorator errorStream;
	
	private String fName;
	private String fContextMenuId;

	static public final int BUILD_STREAM_TYPE_INFO = 0;
	static public final int BUILD_STREAM_TYPE_OUTPUT = 1;
	static public final int BUILD_STREAM_TYPE_ERROR = 2;
	
	static public final String DEFAULT_CONTEXT_MENU_ID = CUIPlugin.PLUGIN_ID + ".CBuildConole"; //$NON-NLS-1$

	private IProject fLastProject;

	public BuildConsoleManager() {
	}

	/**
	 * Notifies the console manager that console activity has started on the
	 * project The manager will open the console if the preference is set to
	 * show the console, and notify listeners
	 */
	protected void startConsoleActivity(IProject project) {
		Object[] list = listeners.getListeners();
		if (list.length > 0) {
			for (int i = 0; i < list.length; i++) {
				IBuildConsoleListener listener = (IBuildConsoleListener)list[i];
				ConsoleEvent event = new ConsoleEvent(BuildConsoleManager.this, project, IBuildConsoleEvent.CONSOLE_START);
				listener.consoleChange(event);
			}
		}
		showConsole();
	}

	/**
	 * Opens the console view. If the view is already open, it is brought to the
	 * front.
	 */
	protected void showConsole() {
		IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IViewPart consoleView = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
				if (consoleView == null && BuildConsolePreferencePage.isAutoOpenConsole()) {
					IWorkbenchPart activePart = page.getActivePart();
					try {
						consoleView = page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					} catch (PartInitException pie) {
						CUIPlugin.log(pie);
					}
					//restore focus stolen by the creation of the
					// console
					page.activate(activePart);
				} else {
					boolean bringToTop = shouldBringToTop(consoleView);
					if (bringToTop) {
						page.bringToTop(consoleView);
					}
				}
				if (consoleView instanceof IConsoleView) {
					((IConsoleView)consoleView).display(fConsole);
				}
			}
		}
	}

	boolean shouldBringToTop(IViewPart consoleView) {
		boolean bringToTop = false;
		if (consoleView instanceof IConsoleView) {
			IConsoleView cView = (IConsoleView)consoleView;
			return !cView.isPinned() && BuildConsolePreferencePage.isConsoleOnTop();
		}
		return bringToTop;
	}

	/**
	 * Traverses the delta looking for added/removed/changed launch
	 * configuration files.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResource resource = event.getResource();
		if (resource != null && resource.getType() == IResource.PROJECT) {
			if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
				IDocumentPartitioner partioner = fConsoleMap.remove(resource);
				if (partioner != null) {
					partioner.disconnect();
					Object[] list = listeners.getListeners();
					if (list.length > 0) {
						for (int i = 0; i < list.length; i++) {
							IBuildConsoleListener listener = (IBuildConsoleListener)list[i];
							ConsoleEvent consoleEvent = new ConsoleEvent(this, (IProject)resource, IBuildConsoleEvent.CONSOLE_CLOSE);
							listener.consoleChange(consoleEvent);
						}
					}
				}
			}
		}
	}

	public void shutdown() {
		if (infoColor != null) {
			infoColor.dispose();
			outputColor.dispose();
			errorColor.dispose();
			backgroundColor.dispose();
			problemErrorBackgroundColor.dispose();
			problemWarningBackgroundColor.dispose();
			problemInfoBackgroundColor.dispose();
			problemHighlightedColor.dispose();
		}
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new org.eclipse.ui.console.IConsole[]{fConsole});
		CUIPlugin.getWorkspace().removeResourceChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	private void runUI(Runnable run) {
		Display display;
		display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
			display.asyncExec(run);
		} else {
			run.run();
		}
	}

	public void startup(String name, String id) {
		infoStream = new BuildConsoleStreamDecorator();
		outputStream = new BuildConsoleStreamDecorator();
		errorStream = new BuildConsoleStreamDecorator();
		fName = name;
		fContextMenuId = id;

		runUI(new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				// install colors
				fConsole = new BuildConsole(BuildConsoleManager.this, fName, fContextMenuId);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new org.eclipse.ui.console.IConsole[]{fConsole});
				infoStream.setConsole(fConsole);
				infoColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_INFO_COLOR);
				infoStream.setColor(infoColor);
				outputStream.setConsole(fConsole);
				outputColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_OUTPUT_COLOR);
				outputStream.setColor(outputColor);
				errorStream.setConsole(fConsole);
				errorColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_ERROR_COLOR);
				errorStream.setColor(errorColor);
				backgroundColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_BACKGROUND_COLOR);
				fConsole.setBackground(backgroundColor);
				problemHighlightedColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR);
				problemErrorBackgroundColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR);
				problemWarningBackgroundColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR);
				problemInfoBackgroundColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR);
			}
		});
		CUIPlugin.getWorkspace().addResourceChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		// colors
		if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_INFO_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_INFO_COLOR);
			infoStream.setColor(newColor);
			infoColor.dispose();
			infoColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_OUTPUT_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_OUTPUT_COLOR);
			outputStream.setColor(newColor);
			outputColor.dispose();
			outputColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_ERROR_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_ERROR_COLOR);
			errorStream.setColor(newColor);
			errorColor.dispose();
			errorColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_BACKGROUND_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_BACKGROUND_COLOR);
			fConsole.setBackground(newColor);
			backgroundColor.dispose();
			backgroundColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR);
			problemHighlightedColor.dispose();
			problemHighlightedColor = newColor;
			redrawTextViewer();
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR);
			problemErrorBackgroundColor.dispose();
			problemErrorBackgroundColor = newColor;
			redrawTextViewer();
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR);
			problemWarningBackgroundColor.dispose();
			problemWarningBackgroundColor = newColor;
			redrawTextViewer();
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR);
			problemInfoBackgroundColor.dispose();
			problemInfoBackgroundColor = newColor;
			redrawTextViewer();
		}
	}

	private void redrawTextViewer() {
		final BuildConsolePage p = BuildConsole.getPage(); 
		if ( p == null ) return;
		final BuildConsoleViewer v = p.getViewer();
		if ( v  == null ) return;		
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			public void run() {
				v.getTextWidget().redraw();
			}
		});
	}

	public BuildConsoleStreamDecorator getStreamDecorator(int type) throws CoreException {
		switch (type) {
			case BUILD_STREAM_TYPE_ERROR :
				return errorStream;
			case BUILD_STREAM_TYPE_INFO :
				return infoStream;
			case BUILD_STREAM_TYPE_OUTPUT :
				return outputStream;
		}
		throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, -1, "No Such Console", null)); //$NON-NLS-1$
	}

	/**
	 * Returns a color instance based on data from a preference field.
	 */
	Color createColor(Display display, String preference) {
		RGB rgb = PreferenceConverter.getColor(CUIPlugin.getDefault().getPreferenceStore(), preference);
		return new Color(display, rgb);
	}
	/**
	 * Returns the console for the project, or <code>null</code> if none.
	 */

	public IConsole getConsole(IProject project) {
		Assert.isNotNull(project);
		fLastProject = project;
		return getConsolePartioner(project).getConsole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.IBuildConsoleManager#getLastBuiltProject()
	 */
	public IProject getLastBuiltProject() {
		return fLastProject;
	}
	
	private BuildConsolePartitioner getConsolePartioner(IProject project) {
		BuildConsolePartitioner partioner = fConsoleMap.get(project);
		if (partioner == null) {
			partioner = new BuildConsolePartitioner(project, this);
			fConsoleMap.put(project, partioner);
		}
		return partioner;
	}

	/**
	 * Returns the document for the projects console, or <code>null</code> if
	 * none.
	 */
	public IDocument getConsoleDocument(IProject project) {
		Assert.isNotNull(project);
		return getConsolePartioner(project).getDocument();
	}

	public void addConsoleListener(IBuildConsoleListener listener) {
		listeners.add(listener);
	}

	public void removeConsoleListener(IBuildConsoleListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return logging preferences for a given project. 
	 * @param project to get logging preferences for.
	 */
	public static Preferences getBuildLogPreferences(IProject project) {
		return new LocalProjectScope(project).getNode(QUALIFIER).node(BUILD_CONSOLE_NODE);
	}

	/**
	 * @return default location of logs for a project.
	 * @param project to get default log location for.
	 */
	public static String getDefaultConsoleLogLocation(IProject project) {
		IPath defaultLogLocation = CUIPlugin.getDefault().getStateLocation().append(project.getName()+".build.log"); //$NON-NLS-1$
		return defaultLogLocation.toOSString();
	}

	/**
	 * Refresh output file when it happens to belong to Workspace. There could
	 * be multiple workspace {@link IFile} associated with one URI.
	 *
	 * @param uri - URI of the file.
	 */
	static void refreshWorkspaceFiles(URI uri) {
		if (uri!=null) {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			for (IFile file : files) {
				try {
					file.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		}
	}

}
