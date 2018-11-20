/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class QtFactory {

	private static final char[] QT_VERSION = "QT_VERSION".toCharArray();

	public static QtIndex create(IProject project) {
		CDTIndex cdtIndex = getCDTIndex(project);
		if (cdtIndex == null) {
			Activator.log("could not get CDT index from project " + project.getName());
			return null;
		}

		QtVersion qtVersion = cdtIndex.get(QtVersionAccessor);
		if (qtVersion == null) {
			Activator.log("could not find Qt version in CDT index from project " + project.getName());
			return null;
		}

		if (qtVersion.major == 4 && qtVersion.minor == 8)
			return new QtIndexImpl(cdtIndex);

		// Qt 4.8 is the default implementation, 5.0 support will need to come soon
		return new QtIndexImpl(cdtIndex);
	}

	private static CDTIndex getCDTIndex(IProject project) {
		if (project == null)
			return null;

		ICProject cProject = CoreModel.getDefault().create(project);
		if (cProject == null)
			return null;

		IIndex index = null;
		try {
			index = CCorePlugin.getIndexManager().getIndex(cProject);
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}

		return index == null ? null : new CDTIndex(index);
	}

	/**
	 * A small wrapper to hold the result of index lookups for the Qt version.
	 */
	private static class QtVersion {
		public final int major;
		public final int minor;
		@SuppressWarnings("unused")
		public final int patch;

		// QT_VERSION looks like 0x040805
		private static final Pattern Version_regex = Pattern
				.compile("0x([a-fA-F\\d]{1,2})([a-fA-F\\d]{2})([a-fA-F\\d]{2})");

		public static QtVersion create(String version) {
			Matcher m = Version_regex.matcher(version);
			if (!m.matches())
				return null;

			try {
				int major = Integer.parseInt(m.group(1), 16);
				int minor = Integer.parseInt(m.group(2), 16);
				int patch = Integer.parseInt(m.group(3), 16);
				return new QtVersion(major, minor, patch);
			} catch (NumberFormatException e) {
				Activator.log(e);
			}
			return null;
		}

		private QtVersion(int major, int minor, int patch) {
			this.major = major;
			this.minor = minor;
			this.patch = patch;
		}
	}

	private static final CDTIndex.Accessor<QtVersion> QtVersionAccessor = new CDTIndex.Accessor<QtVersion>() {
		@Override
		public QtVersion access(IIndex index) throws CoreException {
			// Multiple macros might be found, sort the values and choose the highest version.
			SortedSet<String> versions = new TreeSet<>();
			try {
				for (IIndexMacro macro : index.findMacros(QT_VERSION, IndexFilter.ALL, null))
					versions.add(new String(macro.getExpansion()).toLowerCase());
			} catch (CoreException e) {
			}

			// don't create the Qt index if there is no Qt information in the CDT index
			if (versions.size() <= 0)
				return null;

			// the highest version has been sorted to the last position
			return QtVersion.create(versions.last());
		}
	};
}
