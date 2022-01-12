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

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/** Responsible for creating problem marker objects related to errors in cmake's output for the project currently being
 * build.
 *
 * @author Martin Weber
 */
@FunctionalInterface
interface ICMakeExecutionMarkerFactory {

	/** ID for error markers related to execution of the cmake tool.
	 * @see IResource#createMarker(String)
	 */
	String CMAKE_PROBLEM_MARKER_ID = Activator.getId() + ".cmakeproblem"; //$NON-NLS-1$

	/**
	 * Creates a problem marker and/or text marker object related to errors in cmake's output.
	 *
	 * @param message
	 *          the complete error message, including the string the message starts with
	 * @param severity
	 *          the severity of the problem, see {@link IMarker} for acceptable severity values
	 * @param filePath
	 *          the name of the file where the problem occurred, extracted from the error message.
	 *          This is a String denoting a location in the file system.<br>
	 *          If <code>null</code>, no file information is present in the error message; the marker should be created
	 *          then for the project being build.<br>
	 *          If it is relative, it is relative to the source-root of the project being build (and thus can be
	 *          converted to an {@code IResource}).<br>
	 *          If absolute, the file might not be a workspace resource (e.g. one of cmake's module files).<br>
	 * @param mandatoryAttributes
	 * 			mandatory attributes to add to the marker. Attribute keys must be one of those defined in {@link IMarker}
	 *
	 * @throws CoreException
	 */
	// TODO pass in the extra attributes here then return void
	void createMarker(String message, int severity, String filePath, Map<String, Object> mandatoryAttributes)
			throws CoreException;
}