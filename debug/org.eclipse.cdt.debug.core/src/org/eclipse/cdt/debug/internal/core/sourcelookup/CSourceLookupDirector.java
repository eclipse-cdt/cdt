/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - Added support for AbsoluteSourceContainer(159833)
 *     Texas Instruments - added extension point for source container type (279473)
 *     Sergey Prigogin (Google)
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * C/C++ source lookup director.
 *
 * Most instantiations of this class are transient, created through
 * {@link ILaunchManager#newSourceLocator(String)}. A singleton is also created
 * to represent the global source locators.
 *
 * An instance is either associated with a particular launch configuration or it
 * has no association (global).
 *
 * This class is created by the {@link ILaunchManager#newSourceLocator(String)}
 * (e.g. DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type)) and
 * must have a no-arguments constructor.
 */
public class CSourceLookupDirector extends AbstractSourceLookupDirector {
	private static Set<String> fSupportedTypes;
	private static Object fSupportedTypesLock = new Object();

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new CSourceLookupParticipant() });
	}

	@Override
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		readSupportedContainerTypes();
		return fSupportedTypes.contains(type.getId());
	}

	/**
	 * Translate a local file name to a name understood by the backend.
	 *
	 * This method is used when CDT needs to send a command to the backend
	 * containing a file name. For example, inserting a breakpoint. The platform
	 * breakpoint's file name is a path of a file on the user's machine, but GDB
	 * needs the path that corresponds to the debug information.
	 *
	 * @param sourceName
	 *            file name of a local file
	 * @return file name as understood by the debugger backend
	 */
	public IPath getCompilationPath(String sourceName) {
		for (ISourceContainer container : getSourceContainers()) {
			IPath path = SourceUtils.getCompilationPath(container, sourceName);
			if (path != null) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Load and cache the source container types which are supported for CDT
	 * debugging.
	 *
	 * See Bug 279473 for more information.
	 */
	private void readSupportedContainerTypes() {
		synchronized (fSupportedTypesLock) {
			if (fSupportedTypes == null) {
				fSupportedTypes = new HashSet<>();
				String name = CDebugCorePlugin.PLUGIN_ID + ".supportedSourceContainerTypes"; //$NON-NLS-1$;
				IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(name);
				if (extensionPoint != null) {
					for (IExtension extension : extensionPoint.getExtensions()) {
						for (IConfigurationElement configurationElements : extension.getConfigurationElements()) {
							String id = configurationElements.getAttribute("id"); //$NON-NLS-1$;
							if (id != null)
								fSupportedTypes.add(id);
						}
					}
				}
			}
		}
	}
}
