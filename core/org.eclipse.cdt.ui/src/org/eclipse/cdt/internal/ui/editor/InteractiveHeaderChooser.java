/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.ui.refactoring.includes.IHeaderChooser;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Dialog-based header chooser.
 */
public class InteractiveHeaderChooser implements IHeaderChooser {
	private final String title;
	private final Shell shell;
	private final Map<Collection<IPath>, IPath> userChoiceCache;

	public InteractiveHeaderChooser(String title, Shell shell) {
		this.title = title;
		this.shell = shell;
		userChoiceCache = new HashMap<>();
	}

	@Override
	public IPath chooseHeader(final String bindingName, Collection<IPath> headers) {
		if (headers.isEmpty())
			return null;
		if (headers.size() == 1)
			return headers.iterator().next();

		Set<IPath> cacheKey = new HashSet<>(headers);
		// Check the decision cache. If the cache doesn't help, ask the user.
		// Query the cache.
		if (userChoiceCache.containsKey(cacheKey)) {
			return userChoiceCache.get(cacheKey);
		}

		// Ask the user.
		final IPath[] elemArray = headers.toArray(new IPath[headers.size()]);
		final IPath[] selectedElement = new IPath[1];
		runInUIThread(() -> {
			if (!shell.isDisposed()) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
				dialog.setElements(elemArray);
				dialog.setTitle(title);
				dialog.setMessage(NLS.bind(CEditorMessages.OrganizeIncludes_choose_header, bindingName));
				if (dialog.open() == Window.OK) {
					selectedElement[0] = (IPath) dialog.getFirstResult();
				}
			}
		});

		IPath selectedHeader = selectedElement[0];
		if (selectedHeader != null)
			userChoiceCache.put(headers, selectedHeader); // Remember user's choice.
		return selectedHeader;
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}
}
