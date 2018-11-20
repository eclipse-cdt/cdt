/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractMesonCommandHandler extends AbstractHandler {

	private IContainer fContainer;

	protected abstract void run(Shell activeShell);

	protected Object execute1(ExecutionEvent event) {
		ISelection k = HandlerUtil.getCurrentSelection(event);
		if (!k.isEmpty() && k instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) k).getFirstElement();
			IContainer container = getContainer(obj);
			if (container != null) {
				setSelectedContainer(container);
				run(HandlerUtil.getActiveShell(event));
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected IContainer getContainer(Object obj) {
		IContainer fContainer = null;

		if (obj instanceof Collection) {
			Collection<Object> c = (Collection<Object>) obj;
			Object[] objArray = c.toArray();
			if (objArray.length > 0) {
				obj = objArray[0];
			}
		}
		if (obj instanceof ICElement) {
			if (obj instanceof ICContainer || obj instanceof ICProject) {
				fContainer = (IContainer) ((ICElement) obj).getUnderlyingResource();
			} else {
				obj = ((ICElement) obj).getResource();
				if (obj != null) {
					fContainer = ((IResource) obj).getParent();
				}
			}
		} else if (obj instanceof IResource) {
			if (obj instanceof IContainer) {
				fContainer = (IContainer) obj;
			} else {
				fContainer = ((IResource) obj).getParent();
			}
		} else {
			fContainer = null;
		}
		return fContainer;
	}

	protected void showError(String title, String content) {
		MessageDialog.openError(new Shell(), title, content);
	}

	/**
	 * Separate targets to array from a string.
	 *
	 * @param rawArgList
	 * @return targets in string[] array. if targets are not formatted properly,
	 *         returns null
	 */
	protected List<String> separateTargets(String rawArgList) {

		StringTokenizer st = new StringTokenizer(rawArgList, " "); //$NON-NLS-1$
		List<String> targetList = new ArrayList<>();

		while (st.hasMoreTokens()) {
			String currentWord = st.nextToken().trim();

			if (currentWord.startsWith("'")) { //$NON-NLS-1$
				StringBuilder tmpTarget = new StringBuilder();
				while (!currentWord.endsWith("'")) { //$NON-NLS-1$
					tmpTarget.append(currentWord).append(' ');
					if (!st.hasMoreTokens()) {
						// quote not closed properly, so return null
						return null;
					}
					currentWord = st.nextToken().trim();
				}

				tmpTarget.append(currentWord);
				targetList.add(tmpTarget.toString());
				continue;
			}

			if (currentWord.startsWith("\"")) { //$NON-NLS-1$
				StringBuilder tmpTarget = new StringBuilder();
				while (!currentWord.endsWith("\"")) { //$NON-NLS-1$
					tmpTarget.append(currentWord).append(' ');
					if (!st.hasMoreTokens()) {
						// double quote not closed properly, so return null
						return null;
					}
					currentWord = st.nextToken().trim();
				}

				tmpTarget.append(currentWord);
				targetList.add(tmpTarget.toString());
				continue;
			}

			// for targets without quote/double quotes.
			targetList.add(currentWord);

		}

		return targetList;
	}

	protected List<String> separateOptions(String rawArgList) {
		List<String> argList = new ArrayList<>();
		// May be multiple user-specified options in which case we
		// need to split them up into individual options
		rawArgList = rawArgList.trim();
		boolean finished = false;
		int lastIndex = rawArgList.indexOf("--"); //$NON-NLS-1$
		if (lastIndex != -1) {
			while (!finished) {
				int index = rawArgList.indexOf("--", lastIndex + 2); //$NON-NLS-1$
				if (index != -1) {
					String previous = rawArgList.substring(lastIndex, index).trim();
					argList.add(previous);
					rawArgList = rawArgList.substring(index);
				} else {
					argList.add(rawArgList);
					finished = true;
				}
			}
		}

		return argList;

	}

	protected List<String> simpleParseOptions(String rawArgList) {
		List<String> argList = new ArrayList<>();
		int lastArgIndex = -1;
		int i = 0;
		while (i < rawArgList.length()) {
			char ch = rawArgList.charAt(i);
			// Skip white-space
			while (Character.isWhitespace(ch)) {
				++i;
				if (i < rawArgList.length()) {
					ch = rawArgList.charAt(i);
				} else { // Otherwise we are done
					return argList;
				}
			}

			// Simplistic parser. We break up into strings delimited
			// by blanks. If quotes are used, we ignore blanks within.
			// If a backslash is used, we ignore the next character and
			// pass it through.
			lastArgIndex = i;
			boolean inString = false;
			while (i < rawArgList.length()) {
				ch = rawArgList.charAt(i);
				if (ch == '\\') { // escape character
					++i; // skip over the next character
				} else if (ch == '\"') { // double quotes
					inString = !inString;
				} else if (Character.isWhitespace(ch) && !inString) {
					argList.add(rawArgList.substring(lastArgIndex, i));
					break;
				}
				++i;
			}
			// Look for the case where we ran out of chars for the last
			// token.
			if (i >= rawArgList.length()) {
				argList.add(rawArgList.substring(lastArgIndex));
			}
			++i;
		}
		return argList;
	}

	protected IPath getExecDir(IContainer container) {
		int type = container.getType();
		IPath execDir = null;
		if (type == IResource.FILE) {
			execDir = container.getLocation().removeLastSegments(1);
		} else {
			execDir = container.getLocation();
		}
		return execDir;
	}

	protected IPath getCWD(IContainer container) {
		int type = container.getType();
		IPath cwd = null;
		if (type == IResource.FILE) {
			cwd = container.getFullPath().removeLastSegments(1);
		} else {
			cwd = container.getFullPath();
		}
		return cwd;
	}

	protected IContainer getSelectedContainer() {
		return fContainer;
	}

	public void setSelectedContainer(IContainer container) {
		fContainer = container;
	}

}
