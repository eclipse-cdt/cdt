/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core.index;

import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.cdt.qt.internal.core.index.QtFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * The public interface to the Qt index.  The Qt index is a small wrapper around the
 * core CDT's CIndex that adds Qt specific information.  The Qt index is designed to
 * interpret multiple versions of Qt, however only 4.8 has been implemented for now.
 *
 * @see #getIndex(IProject)
 */
public abstract class QtIndex {
	/**
	 * Return an instance of the Qt index for the argument project.  The CDT index is
	 * examined to discover appropriate version of Qt (using the value of QT_VERSION).
	 * Returns null if the Qt index cannot be created.  This could happen if the argument
	 * project does not have the qtnature, or if the value of QT_VERSION is not supported
	 * by this implementation.
	 *
	 * @param  project A Qt enabled project that should be indexed with Qt-specific information.
	 * @return The Qt index or null if the index cannot be created.
	 */
	public static QtIndex getIndex(IProject project) {

		if (project == null)
			return null;

		try {
			Object index = project.getSessionProperty(QtPlugin.QTINDEX_PROP_NAME);
			if (index instanceof QtIndex)
				return (QtIndex)index;
		} catch(CoreException e) {
			QtPlugin.log(e);
		}

		// create and store a new instance when needed
		QtIndex index = QtFactory.create(project);
		if (index == null)
			return null;

		try {
			project.setSessionProperty(QtPlugin.QTINDEX_PROP_NAME, index);
		} catch( CoreException e ) {
			QtPlugin.log(e);
		}

		return index;
	}

	/**
	 * Find and return a subclass of QObject with the given qualified name.  Returns null if
	 * the index does not have a subclass of QObject with the given name.
	 */
	public abstract IQObject findQObject(String[] qualifiedName);
}
