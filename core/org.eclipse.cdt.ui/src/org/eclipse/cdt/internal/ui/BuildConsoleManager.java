/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.ui;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.ui.*;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

public class BuildConsoleManager implements IBuildConsoleManager, IResourceChangeListener {
	private HashMap fConsoleDocumentMap;
	ListenerList listeners = new ListenerList(1);

	private class BuildConsole extends ConsoleOutputStream implements IConsole {
		protected IDocument fDocument;

		public BuildConsole() {
			fDocument = new Document();
		}

		public void start(IProject project) {
			if (CPluginPreferencePage.isClearBuildConsole() ) {
				clear();
			}
			Object[] list =  listeners.getListeners();
			if ( list .length > 0 ) {
				for ( int i = 0; i < list.length; i++ ) {
					IBuildConsoleListener listener = (IBuildConsoleListener)list[i];
					ConsoleEvent event = new ConsoleEvent(project, ConsoleEvent.CONSOLE_START);
					listener.consoleChange(event);
				}
			}
		}

		public void clear() {
			super.clear();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					fDocument.set("");
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
						int len = fDocument.getLength();
						fDocument.replace(len, 0, readBuffer());
					}
					catch (BadLocationException x) {
					}
				}
			});
		}

		void bringConsoleOnTop() {
			IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
			if (window == null)
				return;
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					// show the build console
					IViewPart cBuild = page.findView(CUIPlugin.CONSOLE_ID);
					if (cBuild == null) {
						if (CPluginPreferencePage.isAutoOpenConsole()) {
							IWorkbenchPart activePart = page.getActivePart();
							cBuild = page.showView(CUIPlugin.CONSOLE_ID);
							//restore focus
							page.activate(activePart);
						}
					}
					else {
						page.bringToTop(cBuild);
					}
				}
				catch (PartInitException pie) {
				}
			}
		}
	}
	
	public BuildConsoleManager() {
		fConsoleDocumentMap = new HashMap();
	}

	/**
	 * Traverses the delta looking for added/removed/changed launch
	 * configuration files.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if ( fConsoleDocumentMap == null ) {
			return;
		}
		IResource resource = event.getResource();
		if ( event.getType() == IResourceChangeEvent.PRE_DELETE ) {
			if(resource.getType() == IResource.PROJECT ) {
				fConsoleDocumentMap.remove(resource);
			}
		}
	}
	
	public void shutdown() {
		CUIPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	public void startup() {
		CUIPlugin.getWorkspace().addResourceChangeListener(this);
	}

	private BuildConsole getBuildConsole(IProject project) {
		BuildConsole console = (BuildConsole) fConsoleDocumentMap.get(project);
		if (console == null) {
			console = new BuildConsole();
			fConsoleDocumentMap.put(project, console);
		}
		return console;
	}

	public IConsole getConsole(IProject project) {
		return getBuildConsole(project);
	}

	public IDocument getConsoleDocument(IProject project) {
		return getBuildConsole(project).getDocument();
	}

	public void addConsoleListener(IBuildConsoleListener listener) {
		listeners.add(listener);
	}

	public void removeConsoleListener(IBuildConsoleListener listener) {
		listeners.remove(listener);
	}

}
