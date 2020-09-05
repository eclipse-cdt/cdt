/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * Default implementation of {@code ICMakeExecutionMarkerFactory}.
 *
 * @author Martin Weber
 */
public class CMakeExecutionMarkerFactory implements ICMakeExecutionMarkerFactory {

	private final IContainer srcFolder;

	/**
	 * @param srcFolder
	 * 		source-folder ofthe project currently being build.
	 *
	 */
	public CMakeExecutionMarkerFactory(IContainer srcFolder) {
		this.srcFolder = Objects.requireNonNull(srcFolder);
	}

	@Override
	public final IMarker createMarker(String message, int severity, String filePath) throws CoreException {
		IMarker marker;
		if (filePath == null) {
			marker = srcFolder.createMarker(CMAKE_PROBLEM_MARKER_ID);
		} else {
			// NOTE normally, cmake reports the file name relative to source root.
			// BUT some messages give an absolute file-system path which is problematic when the build
			// runs in a docker container
			// So we do some heuristics here...
			IPath path = new Path(filePath);
			try {
				// normal case: file is rel. to source root
				marker = srcFolder.getFile(path).createMarker(CMAKE_PROBLEM_MARKER_ID);
			} catch (CoreException ign) {
				// try abs. path
				IPath srcLocation = srcFolder.getLocation();
				if (srcLocation.isPrefixOf(path)) {
					// can resolve the cmake file
					int segmentsToRemove = srcLocation.segmentCount();
					path = path.removeFirstSegments(segmentsToRemove);
					marker = srcFolder.getFile(path).createMarker(CMAKE_PROBLEM_MARKER_ID);
				} else {
					// possibly a build in docker container. we would reach this if the source-dir path inside
					// the container is NOT the same as the one in the host/IDE.
					// for now, just add the markers to the source dir and lets users file issues:-)
					marker = srcFolder.createMarker(CMAKE_PROBLEM_MARKER_ID);
					Activator.getPlugin().getLog().log(new Status(IStatus.INFO, Activator.getId(),
							String.format(Messages.CMakeErrorParser_NotAWorkspaceResource, filePath)));
					// Extra case: IDE runs on Linux, build runs on Windows, or vice versa...
				}
			}
		}
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		return marker;
	}

}
