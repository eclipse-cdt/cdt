/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.srcfinder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.ISourceFinder;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ProgramRelativePathSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class CSourceFinder implements ISourceFinder, ILaunchConfigurationListener, ILaunchesListener {

	/**
	 * The binary we are searching files for. We need this reference to find a
	 * matching ILaunch or ILaunchConfiguration if the caller needs to search
	 * for files when it doesn't have a debug context.
	 */
	private IBinary fBinary;

	/**
	 * The locator tied to an ILaunch or an ILaunchConfiguration that is
	 * associated with the binary. Used for searching when the caller has no
	 * debug context. See {@link ISourceFinder#toLocalPath(String)} for
	 * performance considerations that dictate how the locator is chosen. Access
	 * this only from synchronized blocks as the field is subject to be changed
	 * by listener invocations.
	 */
	private ISourceLookupDirector fLaunchLocator;

	/**
	 * A launch configuration doesn't have a source locator instance tied to it.
	 * Instead, one is created on the fly as needed from attributes in the
	 * launch config. This is a heavy operation. As an optimization, we cache
	 * the locators we create and discard when the launch config changes or is
	 * disposed. Collection is subject to be changed by listener invocations.
	 * Map key is the launch configuration name.
	 * 
	 * @see CSourceFinder#getLocator(ILaunchConfiguration)
	 */
	private Map<String, ISourceLocator> fConfigLocators = Collections.synchronizedMap(new HashMap<String, ISourceLocator>());

	/**
	 * We use this when we don't have an ILaunch or ILaunchConfiguration
	 * locator. A program relative container instance is automatically added to
	 * every CDT launch configuration. So when we lack a configuration context,
	 * we rely on this container to help us resolve relative paths.
	 */
	private ProgramRelativePathSourceContainer fRelativePathContainer;

	/**
	 * Constructor.
	 * 
	 * @param binary
	 *            the executable whose source files we will be asked to find
	 *            locally
	 */
	public CSourceFinder(IBinary binary) {
		assert(binary != null);
		fBinary = binary;
		
		fRelativePathContainer = new ProgramRelativePathSourceContainer(binary); 
		
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchConfigurationListener(this);
		lmgr.addLaunchListener(this);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ISourceFinder#toLocalPath(java.lang.String)
	 */
	@Override
	synchronized public String toLocalPath(String compilationPath) {
		try {
			Object foundElement = null;
			
			// Find a suitable launch/config locator if we haven't found one yet
			if (fLaunchLocator == null) {
				ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
			
				// See if there are any active debug sessions (running, or
				// terminated but still in the Debug view) that are targeting
				// our executable. If there are then use the first one to
				// provide a locator
				ILaunch[] launches = lmgr.getLaunches();
				for (ILaunch launch : launches) {
					ILaunchConfiguration config = launch.getLaunchConfiguration();
					if (config != null && isMatch(config)) {
						ISourceLocator launchLocator = launch.getSourceLocator();
						// in practice, a launch locator is always an ISourceLookupDirector
						if (launchLocator instanceof ISourceLookupDirector) {
							fLaunchLocator = (ISourceLookupDirector)launchLocator;
							break;
						}
					}
				}
				
				// If there were no matching launches or none of them
				// provided a locator, search the launch configurations
				if (fLaunchLocator == null) {
					for (ILaunchConfiguration config : lmgr.getLaunchConfigurations()) {
						if (isMatch(config)) {
							String configName = config.getName();
							
							// Search our cache of locators that we
							// instantiate for configurations. Create one if
							// not found
							ISourceLocator configLocator = fConfigLocators.get(configName);
							if (configLocator == null) {
								configLocator = getLocator(config);	// heavy operation
								fConfigLocators.put(configName, configLocator);	// cache to avoid next time
							}
							// In practice, a config's locator is always an ISourceLookupDirector
							if (configLocator instanceof ISourceLookupDirector) {
								fLaunchLocator = (ISourceLookupDirector)configLocator;
								break;
							}
						}
					}
				}
			}
			
			// Search for the file using the launch/config locator
			if (fLaunchLocator != null) {
				foundElement = fLaunchLocator.getSourceElement(compilationPath);
			}
			else {
				// If there isn't a launch/config locator, we need to explicitly
				// try to resolve relative paths...relative to the binary
				// location.
				Object[] elements = fRelativePathContainer.findSourceElements(compilationPath);
				if (elements.length > 0) {
					assert elements.length == 1; // relative path container should return at most one element
					foundElement = elements[0];
				}
			}

			// If we didn't search using the locator of an ILaunch or
			// ILaunchConfiguration, look in the global (common) locator. The
			// common locator is automatically included in every
			// launch/configuration locator, which is why we skip looking
			// through the common one again here.
			if ((fLaunchLocator == null) && (foundElement == null)) {
				CSourceLookupDirector locator = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector();
				foundElement = locator.getSourceElement(compilationPath);
			}

			return foundElementToPath(foundElement);
		}
		catch (CoreException exc) {
			CDebugCorePlugin.log(exc);			
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ISourceFinder#toLocalPath(org.eclipse.core.runtime.IAdaptable, java.lang.String)
	 */
	@Override
	public String toLocalPath(IAdaptable _launch, String compilationPath) {
		Object foundElement = null;
		
		ILaunch launch = (ILaunch)_launch.getAdapter(ILaunch.class);
		if (launch != null) {
			ISourceLocator locator = launch.getSourceLocator();
			// in practice, a launch locator is always an ISourceLookupDirector
			if (locator instanceof ISourceLookupDirector) {
				foundElement = ((ISourceLookupDirector)locator).getSourceElement(compilationPath);
			}
		}

		// If not found, look in the global (common) locator
		if (foundElement == null) {
			CSourceLookupDirector locator = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector();
			foundElement = locator.getSourceElement(compilationPath);
		}
		
		return foundElementToPath(foundElement);
	}

	/**
	 * Utility method to convert the element found by the source locators to a
	 * canonical file path
	 * 
	 * @param foundElement
	 *            the element found by the source locator, or null if not found
	 * @return the canonical file path of the element
	 */
	private static String foundElementToPath(Object foundElement) {
		if (foundElement != null) {
			try {
				if (foundElement instanceof IFile) {
					IPath path = ((IFile)foundElement).getLocation();
					if (path != null) {
						File file = path.toFile();
						if (file != null) {
								return file.getCanonicalPath();
						}
					}
					
				}
				else if (foundElement instanceof LocalFileStorage) {
					File file = ((LocalFileStorage)foundElement).getFile();
					if (file != null) {
						return file.getCanonicalPath();
					}
				}
				else if (foundElement instanceof ExternalTranslationUnit) {
					URI uri = ((ExternalTranslationUnit)foundElement).getLocationURI();
					if (uri != null) {
						IPath path = URIUtil.toPath(uri);
						if (path != null) {
							File file = path.toFile();
							if (file != null) {
								return file.getCanonicalPath();
							}
						}
					}
				}
			} catch (IOException e) {
				CDebugCorePlugin.log(e);
			}
		}
		
		return null;
	}
	
	/**
	 * Utility method to determine if the given launch configuration targets the Binary we are associated with
	 * @param config
	 * @return true if the launch config targets our binary, false otherwise
	 */
	private boolean isMatch(ILaunchConfiguration config) {
		IResource resource = (IResource)fBinary.getAdapter(IResource.class);
		if (resource != null) {
			String binaryPath = resource.getFullPath().toString();
			try {
				String projectNameConfig = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
				String programNameConfig = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
				IProject project = resource.getProject();
				if (project != null && project.getName().equals(projectNameConfig)) {
					Path path = new Path(programNameConfig);
					if (!path.isEmpty()) {
						IFile file = project.getFile(path);
						if (file != null) {
							String fullPath = file.getFullPath().toString();
							return fullPath.equals(binaryPath);
						}
					}
				}
			} catch (CoreException e) {
				// Problem getting attribute from launch config? Not expecting that.
				CDebugCorePlugin.log(e);
			}
		}
		
		return false;
	}

	/**
	 * Utility method to instantiate a source locator for a launch
	 * configuration. A launch configuration doesn't have a source locator
	 * instance tied to it. Transient instances are created as needed. from
	 * attributes in the launch config. This is a heavy operation.
	 * 
	 * @param config
	 *            the launch configuration to create the locator for
	 * @return the source locator
	 * @throws CoreException
	 */
	static private ISourceLocator getLocator(ILaunchConfiguration config) throws CoreException {
		String type = config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
		if (type == null) {
			type = config.getType().getSourceLocatorId();
		}
		if (type != null) {
			IPersistableSourceLocator locator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type);
			String memento = config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
			if (memento == null) {
				locator.initializeDefaults(config);
			} else {
				if(locator instanceof IPersistableSourceLocator2)
					((IPersistableSourceLocator2)locator).initializeFromMemento(memento, config);
				else
					locator.initializeFromMemento(memento);
			}
			return locator;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationAdded(ILaunchConfiguration config) {
		// Don't care.  
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	synchronized public void launchConfigurationChanged(ILaunchConfiguration config) {
		// We don't care if it's a working copy.
		if (config.isWorkingCopy()) {
			return;
		}
		
		// the source locator attribute may have changed
		fConfigLocators.remove(config.getName());
		if ((fLaunchLocator != null) && (fLaunchLocator.getLaunchConfiguration().getName() == config.getName())) {
			fLaunchLocator = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	synchronized public void launchConfigurationRemoved(ILaunchConfiguration config) {
		// We don't care if it's a working copy.
		if (config.isWorkingCopy()) {
			return;
		}
		
		fConfigLocators.remove(config.getName());
		if ((fLaunchLocator != null) && (fLaunchLocator.getLaunchConfiguration().getName() == config.getName())) {
			fLaunchLocator = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	synchronized public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			if (launch.getSourceLocator() == fLaunchLocator) {
				fLaunchLocator = null;
				return;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public void launchesAdded(ILaunch[] launches) {
		// If there's a new launch in town, we need to take it into
		// consideration. E.g., if it targets our binary, and we're currently
		// searching using an inactive launch configuration's locator, then the
		// new launch's locator should take precedence
		for (ILaunch launch : launches) {
			ILaunchConfiguration config = launch.getLaunchConfiguration();
			if (config != null && isMatch(config)) {
				synchronized(this) {
					fLaunchLocator = null;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public void launchesChanged(ILaunch[] launches) {
		// don't care. I don't think setting a new locator in a launch would result in us getting notified
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ISourceFinder#dispose()
	 */
	@Override
	public void dispose() {
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.removeLaunchConfigurationListener(this);
		lmgr.removeLaunchListener(this);
	}
}
