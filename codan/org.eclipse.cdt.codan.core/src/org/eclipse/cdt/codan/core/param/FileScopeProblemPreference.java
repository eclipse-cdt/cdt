/*******************************************************************************
 * Copyright (c) 2009, 2015 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.codan.internal.core.CharOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Custom preference for resource scope
 *
 * @noextend This class is not intended to be extended by clients.
 * @since 1.0
 */
public class FileScopeProblemPreference extends AbstractProblemPreference {
	/**
	 * Key for the scope preference
	 */
	public static final String KEY = "fileScope"; //$NON-NLS-1$
	/**
	 * Exclusion attribute
	 */
	public static final String EXCLUSION = "exclusion"; //$NON-NLS-1$
	/**
	 * Inclusion attribute
	 */
	public static final String INCLUSION = "inclusion"; //$NON-NLS-1$

	private IResource resource;
	private IPath[] inclusion = new IPath[0];
	private IPath[] exclusion = new IPath[0];

	/**
	 * Default constructor
	 */
	public FileScopeProblemPreference() {
		setKey(KEY);
		setLabel(Messages.FileScopeProblemPreference_Label);
	}

	@Override
	public PreferenceType getType() {
		return PreferenceType.TYPE_CUSTOM;
	}

	/**
	 * Get attribute. Possible keys are EXCUSION and INCLUSION
	 *
	 * @param key
	 * @return class attribute for given key
	 */
	public IPath[] getAttribute(String key) {
		if (key == EXCLUSION)
			return exclusion;
		if (key == INCLUSION)
			return inclusion;
		return null;
	}

	/**
	 * Set attribute to a value. Possible keys are EXCUSION and INCLUSION
	 *
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, IPath[] value) {
		if (key == EXCLUSION)
			exclusion = value.clone();
		if (key == INCLUSION)
			inclusion = value.clone();
	}

	/**
	 * @return null for workspace, or project of the resource it is applicable
	 *         for
	 */
	public IProject getProject() {
		if (resource != null)
			return resource.getProject();
		return null;
	}

	/**
	 * @return path of the resource it is applicable to
	 */
	public IPath getPath() {
		if (resource != null)
			return resource.getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		return root.getFullPath();
	}

	/**
	 * @param resource
	 *        the resource to set
	 */
	public void setResource(IResource resource) {
		this.resource = resource;
	}

	/**
	 * @return the resource for which scope is define. Null if workspace.
	 */
	public IResource getResource() {
		return resource;
	}

	@Override
	public boolean isDefault() {
		if (inclusion.length == 0 && exclusion.length == 0)
			return true;
		return false;
	}

	@Override
	public String exportValue() {
		return exportPathList(INCLUSION, inclusion) + "," //$NON-NLS-1$
				+ exportPathList(EXCLUSION, exclusion);
	}

	protected String exportPathList(String key, IPath[] arr) {
		String res = key + "=>("; //$NON-NLS-1$
		for (int i = 0; i < arr.length; i++) {
			if (i != 0)
				res += ","; //$NON-NLS-1$
			res += escape(arr[i].toPortableString());
		}
		return res + ")"; //$NON-NLS-1$
	}

	@Override
	public void importValue(StreamTokenizer tokenizer) throws IOException {
		List<IPath> inc = importPathList(tokenizer, INCLUSION);
		inclusion = inc.toArray(new IPath[inc.size()]);
		checkChar(tokenizer, ',');
		List<IPath> exc = importPathList(tokenizer, EXCLUSION);
		exclusion = exc.toArray(new IPath[exc.size()]);
	}

	private void checkChar(StreamTokenizer tokenizer, char c) throws IOException {
		tokenizer.nextToken();
		if (tokenizer.ttype != c)
			throw new IllegalArgumentException("Expected " + c); //$NON-NLS-1$
	}

	private void checkKeyword(StreamTokenizer tokenizer, String keyword) throws IOException {
		tokenizer.nextToken();
		if (tokenizer.sval == null || !tokenizer.sval.equals(keyword))
			throw new IllegalArgumentException("Expected " + keyword); //$NON-NLS-1$
	}

	protected List<IPath> importPathList(StreamTokenizer tokenizer, String keyword) throws IOException {
		checkKeyword(tokenizer, keyword);
		checkChar(tokenizer, '=');
		checkChar(tokenizer, '>');
		ArrayList<IPath> list = new ArrayList<>();
		int token;
		try {
			checkChar(tokenizer, '(');
			token = tokenizer.nextToken();
			if (token != ')') {
				tokenizer.pushBack();
			} else {
				return Collections.emptyList();
			}
			while (true) {
				token = tokenizer.nextToken();
				if (tokenizer.sval == null)
					throw new IllegalArgumentException();
				list.add(new Path(tokenizer.sval));
				token = tokenizer.nextToken();
				if (token == ')')
					break;
				tokenizer.pushBack();
				checkChar(tokenizer, ',');
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return list;
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public void setValue(Object value) {
		if (this == value)
			return;
		FileScopeProblemPreference scope = (FileScopeProblemPreference) value;
		setAttribute(INCLUSION, scope.getAttribute(INCLUSION));
		setAttribute(EXCLUSION, scope.getAttribute(EXCLUSION));
		this.resource = scope.getResource();
	}

	@Override
	public Object clone() {
		FileScopeProblemPreference scope = (FileScopeProblemPreference) super.clone();
		scope.setValue(this);
		return scope;
	}

	/**
	 * Checks that resource denotated by the given path is in scope (defined by
	 * exclusion/inclusion settings of this class). In inclusion list is defined
	 * check first if it belongs to it, returns false if not.
	 * Then checks if it belongs to exclusion list and return false if it is.
	 *
	 * @param path
	 *        - resource path
	 * @return true is given path is in scope
	 */
	public boolean isInScope(IPath path) {
		//System.err.println("test " + file + " " + exportValue());
		if (inclusion.length > 0) {
			if (!matchesFilter(path, inclusion))
				return false;
		}
		if (exclusion.length > 0) {
			if (matchesFilter(path, exclusion))
				return false;
		}
		return true;
	}

	/**
	 * Checks that given path matches on the paths provided as second argument
	 *
	 * @param resourcePath - resource path
	 * @param paths - array of path patterns, for pattern see
	 *        {@link CharOperation#pathMatch}
	 * @return true if matches with at least one pattern in the array
	 */
	public boolean matchesFilter(IPath resourcePath, IPath[] paths) {
		String path = resourcePath.makeRelative().toString();
		for (int i = 0, length = paths.length; i < length; i++) {
			String pattern = paths[i].toString();
			if (CharOperation.pathMatch(pattern, path, true, '/')) {
				return true;
			}
		}
		return false;
	}
}
