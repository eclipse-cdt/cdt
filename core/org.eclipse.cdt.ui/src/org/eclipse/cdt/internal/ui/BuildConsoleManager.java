/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

public class BuildConsoleManager implements IBuildConsoleManager, IResourceChangeListener, IPropertyChangeListener {
	private HashMap fConsoleDocumentMap;
	ListenerList listeners = new ListenerList(1);

	private class BuildConsoleDocument extends Document {

		private int fMaxLines;

		public BuildConsoleDocument(int nLines) {
			super();
			fMaxLines = nLines;
		}

		public void setDocumentSize(int nLines) {
			fMaxLines = nLines;
			nLines = getNumberOfLines();
			if (nLines > fMaxLines) {
				try {
					int start = getLineOffset(nLines - fMaxLines);
					String part = get(start, getLength() - start);
					set(part);
				} catch (BadLocationException e) {
				}
			}
		}

		public BuildConsoleDocument(String initialContent) {
			super(initialContent);
		}

		public void replace(int offset, int length, String text) throws BadLocationException {
			super.replace(offset, length, text);
			int nLines = getNumberOfLines();
			if (nLines > fMaxLines) {
				int start = getLineOffset(nLines - fMaxLines);
				String part = get(start, getLength() - start);
				set(part);
			}
		}
	}

	private class BuildConsole extends ConsoleOutputStream implements IConsole {
		protected BuildConsoleDocument fDocument;

		public BuildConsole() {
			fDocument = new BuildConsoleDocument(BuildConsolePreferencePage.buildConsoleLines());
		}

		public void setConsoleSize(int nLines) {
			fDocument.setDocumentSize(nLines);
		}

		public void start(IProject project) {
			if (BuildConsolePreferencePage.isClearBuildConsole()) {
				clear();
			}
			Object[] list = listeners.getListeners();
			if (list.length > 0) {
				for (int i = 0; i < list.length; i++) {
					IBuildConsoleListener listener = (IBuildConsoleListener) list[i];
					ConsoleEvent event = new ConsoleEvent(BuildConsoleManager.this, project, ConsoleEvent.CONSOLE_START);
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

		public void flush() throws IOException {
			flush(false);
		}

		public void flush(boolean force) throws IOException {
			if (force || fBuffer.length() > 512) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (BuildConsolePreferencePage.isConsoleOnTop())
							bringConsoleOnTop();
						try {
							int len = fDocument.getLength();
							fDocument.replace(len, 0, readBuffer());
						} catch (BadLocationException x) {
						}
					}
				});
			}
		}

		void bringConsoleOnTop() {
			IWorkbenchWindow window = CUIPlugin.getDefault().getActiveWorkbenchWindow();
			if (window == null)
				return;
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					// show the build console
					IViewPart cBuild = page.findView(CUIPlugin.CONSOLE_ID);
					if (cBuild == null) {
						if (BuildConsolePreferencePage.isAutoOpenConsole()) {
							IWorkbenchPart activePart = page.getActivePart();
							cBuild = page.showView(CUIPlugin.CONSOLE_ID);
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

		public void close() throws IOException {
			flush(true);
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
		if (fConsoleDocumentMap == null) {
			return;
		}
		IResource resource = event.getResource();
		if (resource != null && resource.getType() == IResource.PROJECT) {
			if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
				fConsoleDocumentMap.remove(resource);
				Object[] list = listeners.getListeners();
				if (list.length > 0) {
					for (int i = 0; i < list.length; i++) {
						IBuildConsoleListener listener = (IBuildConsoleListener) list[i];
						ConsoleEvent consoleEvent = new ConsoleEvent(this, (IProject) resource, ConsoleEvent.CONSOLE_CLOSE);
						listener.consoleChange(consoleEvent);
					}
				}
			}
		}
	}

	public void shutdown() {
		CUIPlugin.getWorkspace().removeResourceChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	public void startup() {
		CUIPlugin.getWorkspace().addResourceChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
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

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() == BuildConsolePreferencePage.PREF_BUILDCONSOLE_LINES) {
			Collection consoles = fConsoleDocumentMap.values();
			Iterator iter = consoles.iterator();
			while (iter.hasNext()) {
				BuildConsole console = (BuildConsole) iter.next();
				console.setConsoleSize(BuildConsolePreferencePage.buildConsoleLines());
			}
		}
	}

}
