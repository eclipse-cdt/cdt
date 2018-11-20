/*******************************************************************************
 * Copyright (c) 2010, 2016 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.File;

import org.eclipse.cdt.internal.core.model.Binary;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This interface is available for a {@link Binary} via the adapter mechanism. It is used to translate the
 * file specification of a source file that was used to produce the executable to its local counterpart. In
 * other words, the debug information in the binary tells us what source files where involved in building it.
 * Such a file specification may be a simple file name, a relative path, or an absolute path that might be
 * invalid on the local machine (the executable may have been built on another machine). In all cases, the
 * file is found on the local machine by using source locators (see ISourceLocator). ISourceFinder is a front
 * end to that search capability.
 *
 * <p>
 * CDT has:
 * <ul>
 * <li>A global (common) source locator. Its containers are defined via Window > Preferences > C/C++ > Debug >
 * Source Lookup Path
 * <li>Launch configuration source locators. The containers of such a locator are defined via the 'Source' tab
 * in a CDT launch configuration. The common source containers are automatically added to this locator.
 * <li>Launch source locators. Typically, a launch's locator is the one defined in the launch configuration
 * that spawned the launch, but technically, they could be different. The ILaunch API allows any source
 * locator to be associated with a launch.
 * </ul>
 *
 * <p>
 * So, when trying to translate a source file specification in the debug information to a local file, there
 * are a variety of locators that need to be considered. An ISourceFinder shields client code from having to
 * worry about those details. A client simply wants to find a file locally.
 *
 * <p>
 * This interface provides two choices for searching. One caters to logic involved in actively debugging a
 * binary (e.g., a breakpoint is hit). The other is for use when there is no debug-session context (double
 * clicking on a child file element of a Binary object in the Projects view). The former will search using
 * only the locator associated with the ILaunch. The latter will use the locator of any relevant launch or
 * launch configuration. In all cases, the global locator is consulted if no other locator has converted the
 * file.
 *
 * <p>
 * A new instance is created every time a Binary object is queried for this interface. Clients must call
 * {@link #dispose()} when it is done with the object.
 *
 * @since 5.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceFinder {
	/**
	 * Use this method to find a file if you do not have a debug context. The implementation will consult
	 * locators in the following order:
	 * <ol>
	 * <li>If there is an ongoing debug session on that binary, use that's ILaunch's locator. If there are
	 * multiple such debug sessions going on, the first one encountered is used.
	 * <li>If there are no ongoing debug sessions on that binary that provide a locator, search for a CDT
	 * launch configuration that references the binary. Use the locator of the first matching configuration.
	 * <li>If neither of the above result in a locator, explicitly consult the common (global) locator. Note
	 * that the common locator's containers are automatically added to every launch configuration locator. So,
	 * in effect, the common locator is always searched, and always last.
	 * </ol>
	 *
	 * In the first two cases, only the first locator of the first matching launch or launch configuration is
	 * used, even if that locator doesn't find the file. Potentially, another matching one could find the
	 * file, but it could easily get very expensive to iterate through numerous matches that way. Searching
	 * for a file using a single launch/config's locator is expensive enough (it has numerous source
	 * containers). Searching through multiple ones could make this method unbearably slow. And because
	 * finding a matching launch/config can itself be a bit expensive, once a match has been found, its
	 * locator is used from that point on in all subsequent calls to this method. The implementation listens
	 * for launch and configurations changes, though, so we find a new locator when the active locator is no
	 * longer relevant. Note that calls to {@link #toLocalPath(IAdaptable, String)} have no effect on the
	 * active locator we use in this method.
	 *
	 * @param compilationPath
	 *            the path of a file as found in the debug information
	 * @return if we are able to find the file, the location on the host machine, otherwise null. The result
	 *         is in OS specific format, specifically what {@link File#getCanonicalPath()} returns. Note that
	 *         by "on the host machine", we simply mean a specification that is <i>accessible by the host
	 *         machine</i>. The file may be on a network drive, e.g., and thus not really be "local".
	 */
	public String toLocalPath(String compilationPath);

	/**
	 * @deprecated This method is unused in CDT code base. Use {@link #toLocalPath(String)} or request
	 * undeprecating on the cdt-dev mailing list.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public String toLocalPath(IAdaptable launch, String compilationPath);

	/**
	 * Clients must call this to ensure that the object properly cleans up. E.g., a source finder may register
	 * itself as a listener for changes that would effect how it searches for files. Calling this method will
	 * allow it to unregister itself.
	 */
	public void dispose();
}
