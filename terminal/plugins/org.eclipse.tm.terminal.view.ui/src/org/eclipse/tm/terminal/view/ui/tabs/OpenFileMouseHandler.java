/*******************************************************************************
 * Copyright (c) 2021 Fabrizio Iannetti.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.tabs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.tm.internal.terminal.control.ITerminalMouseListener2;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.dialogs.OpenResourceDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class OpenFileMouseHandler implements ITerminalMouseListener2 {
	private static final boolean DEBUG_HOVER = UIPlugin.isOptionEnabled(Logger.TRACE_DEBUG_LOG_HOVER);
	private static final List<String> NEEDED_BUNDLES = //
			List.of("org.eclipse.core.resources", //$NON-NLS-1$
					"org.eclipse.ui.ide", //$NON-NLS-1$
					"org.eclipse.ui.editors", //$NON-NLS-1$
					"org.eclipse.text"); //$NON-NLS-1$

	private final ITerminalViewControl terminal;
	private Pattern regex = Pattern.compile("(\\d*)(:(\\d*))?.*"); //$NON-NLS-1$
	private IWorkbenchPartSite site;

	/**
	 * Check if we have the bundles needed.
	 */
	private boolean neededBundlesAvailable;

	OpenFileMouseHandler(IWorkbenchPartSite site, ITerminalViewControl terminal) {
		this.site = site;
		this.terminal = terminal;
		neededBundlesAvailable = true;
		for (String bundleName : NEEDED_BUNDLES) {
			if (!bundleAvailable(bundleName)) {
				this.neededBundlesAvailable = false;
				if (DEBUG_HOVER) {
					System.out.format(
							"hover: the %s bundle is not present, therefore full ctrl-click functionality is not available\n", //$NON-NLS-1$
							bundleName);
				}
			}
		}
		if (neededBundlesAvailable && DEBUG_HOVER) {
			System.out.format("hover: the bundles needed for full ctrl-click functionality are available\n"); //$NON-NLS-1$
		}
	}

	@Override
	public void mouseUp(ITerminalTextDataReadOnly terminalText, int line, int column, int button, int stateMask) {
		if ((stateMask & SWT.MODIFIER_MASK) != SWT.MOD1) {
			// Only handle Ctrl-click
			return;
		}
		String textToOpen = terminal.getHoverSelection();
		String lineAndCol = null;
		if (textToOpen.length() > 0) {
			try {
				// if the selection looks like a web URL, open using the browser
				if (textToOpen.startsWith("http://") || textToOpen.startsWith("https://")) { //$NON-NLS-1$//$NON-NLS-2$
					try {
						PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null).openURL(new URL(textToOpen));
						return;
					} catch (MalformedURLException e) {
						// not a valid URL, continue
					}
				}

				// After this we need Eclipse IDE features. If we don't have them then we stop here.
				if (!neededBundlesAvailable) {
					return;
				}

				// extract the path from file:// URLs
				if (textToOpen.startsWith("file://")) { //$NON-NLS-1$
					textToOpen = textToOpen.substring(7);
				}
				// remove optional position info name:[row[:col]]
				{
					int startOfRowCol = textToOpen.indexOf(':');
					if (startOfRowCol == 1 && textToOpen.length() > 2) {
						// assume this is the device separator on Windows
						startOfRowCol = textToOpen.indexOf(':', startOfRowCol + 1);
					}
					if (startOfRowCol >= 0) {
						lineAndCol = textToOpen.substring(startOfRowCol + 1);
						textToOpen = textToOpen.substring(0, startOfRowCol);
					}
				}
				Optional<String> fullPath = Optional.empty();
				if (!textToOpen.startsWith("/")) { //$NON-NLS-1$
					// relative path: try to append to the working directory
					Optional<String> workingDirectory = terminal.getTerminalConnector().getWorkingDirectory();
					if (workingDirectory.isPresent()) {
						fullPath = Optional.of(workingDirectory.get() + "/" + textToOpen);
					}
				}
				// if the selection is a file location that maps to a resource
				// open the resource
				IFile fileForLocation = ResourcesPlugin.getWorkspace().getRoot()
						.getFileForLocation(new Path(fullPath.orElse(textToOpen)));
				if (fileForLocation != null && fileForLocation.exists()) {
					IEditorPart editor = IDE.openEditor(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileForLocation,
							true);
					goToLine(lineAndCol, editor);
					return;
				}
				// try an external file, if it exists
				File file = new File(fullPath.orElse(textToOpen));
				if (file.exists() && !file.isDirectory()) {
					try {
						IEditorPart editor = IDE.openEditor(site.getPage(), file.toURI(),
								IDE.getEditorDescriptor(file.getName(), true, true).getId(), true);
						goToLine(lineAndCol, editor);
						return;
					} catch (Exception e) {
						// continue
					}
				}
				OpenResourceDialog openResourceDialog = new OpenResourceDialog(site.getShell(),
						ResourcesPlugin.getPlugin().getWorkspace().getRoot(), IResource.FILE);
				openResourceDialog.setInitialPattern(textToOpen);
				if (openResourceDialog.open() != Window.OK)
					return;
				Object[] results = openResourceDialog.getResult();
				List<IFile> files = new ArrayList<>();
				for (Object result : results) {
					if (result instanceof IFile) {
						files.add((IFile) result);
					}
				}
				if (files.size() > 0) {

					final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						throw new ExecutionException("no active workbench window"); //$NON-NLS-1$
					}

					final IWorkbenchPage page = window.getActivePage();
					if (page == null) {
						throw new ExecutionException("no active workbench page"); //$NON-NLS-1$
					}

					try {
						for (IFile iFile : files) {
							IEditorPart editor = IDE.openEditor(page, iFile, true);
							goToLine(lineAndCol, editor);
						}
					} catch (final PartInitException e) {
						throw new ExecutionException("error opening file in editor", e); //$NON-NLS-1$
					}
				}
			} catch (IllegalArgumentException | NullPointerException | ExecutionException | PartInitException e) {
				UIPlugin.log("Failed to activate OpenResourceDialog", e); //$NON-NLS-1$
			}

		}

	}

	private boolean bundleAvailable(String symbolicName) {
		Bundle bundle = Platform.getBundle(symbolicName);
		boolean available = bundle != null && bundle.getState() != Bundle.UNINSTALLED
				&& bundle.getState() != Bundle.STOPPING;
		return available;
	}

	private void goToLine(String lineAndCol, IEditorPart editor) {
		ITextEditor textEditor = Adapters.adapt(editor, ITextEditor.class);
		if (textEditor != null) {
			Optional<Integer> optionalOffset = getRegionFromLineAndCol(textEditor, lineAndCol);
			optionalOffset.ifPresent(offset -> textEditor.selectAndReveal(offset, 0));
		}
	}

	/**
	 * Returns the line information for the given line in the given editor
	 */
	private Optional<Integer> getRegionFromLineAndCol(ITextEditor editor, String lineAndCol) {
		if (lineAndCol == null) {
			return Optional.empty();
		}
		Matcher matcher = regex.matcher(lineAndCol);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		String lineStr = matcher.group(1);
		String colStr = matcher.group(3);
		int line;
		int col = 0;
		try {
			line = Integer.parseInt(lineStr);
		} catch (NumberFormatException e1) {
			return Optional.empty();
		}
		try {
			col = Integer.parseInt(colStr);
		} catch (NumberFormatException e1) {
			// if we can't get a column, go to the line alone
		}
		IDocumentProvider provider = editor.getDocumentProvider();
		IEditorInput input = editor.getEditorInput();
		try {
			provider.connect(input);
		} catch (CoreException e) {
			return null;
		}
		try {
			IDocument document = provider.getDocument(input);
			if (document != null && line > 0) {
				// document's lines are 0-offset
				line = line - 1;
				int lineOffset = document.getLineOffset(line);
				if (col > 0) {
					int lineLength = document.getLineLength(line);
					if (col < lineLength) {
						lineOffset += col;
					}
				}
				return Optional.of(lineOffset);
			}
		} catch (BadLocationException e) {
		} finally {
			provider.disconnect(input);
		}
		return Optional.empty();
	}
}