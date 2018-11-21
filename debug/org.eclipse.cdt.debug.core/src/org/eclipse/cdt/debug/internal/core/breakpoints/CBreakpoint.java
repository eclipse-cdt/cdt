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
 * QNX Software Systems - Initial API and implementation
 * Marc Khouzam (Ericsson) - Must synchronized creation of breakpoint creation (Bug 444395)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICBreakpoint2;
import org.eclipse.cdt.debug.core.model.ICBreakpointExtension;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.Breakpoint;

import com.ibm.icu.text.MessageFormat;

/**
 * The base class for all C/C++ specific breakpoints.
 */
public abstract class CBreakpoint extends Breakpoint
		implements ICBreakpoint2, ICBreakpointType, IDebugEventSetListener {

	/**
	 * Map of breakpoint extensions.  The keys to the map are debug model IDs
	 * and values are arrays of breakpoint extensions.
	 *
	 * This map is sorted to allow consistent iteration order so that extension
	 * message does not unexpectedly change order
	 */
	private SortedMap<String, ICBreakpointExtension[]> fExtensions = new TreeMap<>();

	/**
	 * The number of debug targets the breakpoint is installed in. We don't use
	 * the INSTALL_COUNT attribute to manage this property (see bugzilla 218194)
	 *
	 */
	private int fInstallCount = 0;

	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint() {
	}

	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint(final IResource resource, final Map<String, Object> attributes, final boolean add)
			throws CoreException {
		this();
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(getMarkerType()));
				// set attributes
				ensureMarker().setAttributes(attributes);
				//set the marker message
				setAttribute(IMarker.MESSAGE, getMarkerMessage());
				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(wr);
	}

	@Override
	public String getModelIdentifier() {
		return CDIDebugModel.getPluginIdentifier();
	}

	@Override
	public boolean isInstalled() throws CoreException {
		return fInstallCount > 0;
	}

	@Override
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, ""); //$NON-NLS-1$
	}

	@Override
	public void setCondition(String condition) throws CoreException {
		setAttribute(CONDITION, condition);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	@Override
	public int getIgnoreCount() throws CoreException {
		return ensureMarker().getAttribute(IGNORE_COUNT, 0);
	}

	@Override
	public void setIgnoreCount(int ignoreCount) throws CoreException {
		setAttribute(IGNORE_COUNT, ignoreCount);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	@Override
	public int getType() throws CoreException {
		return ensureMarker().getAttribute(TYPE, 0);
	}

	@Override
	public void setType(int type) throws CoreException {
		setAttribute(TYPE, type);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	@Override
	public String getThreadId() throws CoreException {
		return ensureMarker().getAttribute(THREAD_ID, null);
	}

	@Override
	public void setThreadId(String threadId) throws CoreException {
		setAttribute(THREAD_ID, threadId);
	}

	@Override
	public String getSourceHandle() throws CoreException {
		return ensureMarker().getAttribute(SOURCE_HANDLE, null);
	}

	@Override
	public void setSourceHandle(String sourceHandle) throws CoreException {
		setAttribute(SOURCE_HANDLE, sourceHandle);
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
	}

	/**
	 * Execute the given workspace runnable
	 */
	protected void run(IWorkspaceRunnable wr) throws DebugException {
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}

	/**
	 * Add this breakpoint to the breakpoint manager, or sets it as
	 * unregistered.
	 */
	public void register(boolean register) throws CoreException {
		if (register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		}
		/*
		 * else { setRegistered( false ); }
		 */
	}

	abstract protected String getMarkerMessage() throws CoreException;

	@Override
	public synchronized int incrementInstallCount() throws CoreException {
		++fInstallCount;

		// cause the marker to update; will ultimately result in a blue checkmark
		// when install count > 0
		setAttribute(INSTALL_COUNT, fInstallCount);

		return fInstallCount;
	}

	/**
	 * Returns the <code>INSTALL_COUNT</code> attribute of this breakpoint or
	 * 0 if the attribute is not set.
	 */
	public int getInstallCount() throws CoreException {
		return fInstallCount;
	}

	@Override
	public synchronized int decrementInstallCount() throws CoreException {
		fInstallCount--;

		// cause the marker to update; will ultimately remove blue checkmark
		// when install count == 0
		setAttribute(INSTALL_COUNT, fInstallCount);

		return fInstallCount;
	}

	@Override
	public synchronized void resetInstallCount() throws CoreException {
		if (fInstallCount != 0) {
			fInstallCount = 0;
			setAttribute(INSTALL_COUNT, fInstallCount);
		}
	}

	@Override
	public boolean isConditional() throws CoreException {
		return ((getCondition() != null && getCondition().trim().length() > 0) || getIgnoreCount() > 0);
	}

	protected String getConditionText() throws CoreException {
		StringBuilder sb = new StringBuilder();
		int ignoreCount = getIgnoreCount();
		if (ignoreCount > 0) {
			sb.append(
					MessageFormat.format(BreakpointMessages.getString("CBreakpoint.1"), new Object[] { ignoreCount })); //$NON-NLS-1$
		}
		String condition = getCondition();
		if (condition != null && condition.length() > 0) {
			sb.append(MessageFormat.format(BreakpointMessages.getString("CBreakpoint.2"), new Object[] { condition })); //$NON-NLS-1$
		}
		return sb.toString();
	}

	/**
	 * Change notification when there are no marker changes. If the marker
	 * does not exist, do not fire a change notificaiton (the marker may not
	 * exist if the associated project was closed).
	 */
	public void fireChanged() {
		if (markerExists()) {
			DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
		}
	}

	@Override
	public String getModule() throws CoreException {
		return ensureMarker().getAttribute(MODULE, null);
	}

	@Override
	public void setModule(String module) throws CoreException {
		setAttribute(MODULE, module);
	}

	@Override
	public <V extends ICBreakpointExtension> V getExtension(String debugModelId, Class<V> extensionType)
			throws CoreException {
		ICBreakpointExtension[] extensions = getExtensionsForModelId(debugModelId);
		for (int i = 0; i < extensions.length; i++) {
			if (extensionType.isAssignableFrom(extensions[i].getClass())) {
				@SuppressWarnings("unchecked")
				V retVal = (V) extensions[i];
				return retVal;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugPlugin.ERROR,
				"Extension " + extensionType + " not defined for breakpoint " + this, null)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Reads platform extension registry for breakpoint extensions registered
	 * for the given debug model.
	 * @param debugModelId Requested debug model that the extensions were
	 * registerd for.
	 * @return Breakpoint extensions.
	 * @throws CoreException Throws exception in case the breakpoint marker
	 * cannot be accessed.
	 */
	private ICBreakpointExtension[] getExtensionsForModelId(String debugModelId) throws CoreException {
		// Check outside the synchronized lock for efficiency
		if (!fExtensions.containsKey(debugModelId)) {
			synchronized (fExtensions) {
				// Must check again within the synchronized block, in case things were changed by another thread.
				if (!fExtensions.containsKey(debugModelId)) {
					// Check to make sure that a marker is present.  Extensions can only be created
					// once the marker type is known.
					IMarker marker = ensureMarker();

					// Read the extension registry and create applicable extensions.
					List<ICBreakpointExtension> extensions = new ArrayList<>(4);
					IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(
							CDebugCorePlugin.getUniqueIdentifier(),
							CDebugCorePlugin.BREAKPOINT_EXTENSION_EXTENSION_POINT_ID);
					IConfigurationElement[] elements = ep.getConfigurationElements();
					for (int i = 0; i < elements.length; i++) {
						if (elements[i].getName().equals(CDebugCorePlugin.BREAKPOINT_EXTENSION_ELEMENT)) {
							String elementDebugModelId = elements[i].getAttribute("debugModelId"); //$NON-NLS-1$
							String elementMarkerType = elements[i].getAttribute("markerType"); //$NON-NLS-1$
							if (elementDebugModelId == null) {
								CDebugCorePlugin.log(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(),
										DebugPlugin.INTERNAL_ERROR,
										"Extension " + elements[i].getDeclaringExtension().getUniqueIdentifier() //$NON-NLS-1$
												+ " missing required attribute: markerType", //$NON-NLS-1$
										null));
							} else if (elementMarkerType == null) {
								CDebugCorePlugin.log(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(),
										DebugPlugin.INTERNAL_ERROR,
										"Extension " + elements[i].getDeclaringExtension().getUniqueIdentifier() //$NON-NLS-1$
												+ " missing required attribute: debugModelId", //$NON-NLS-1$
										null));
							} else if (debugModelId.equals(elementDebugModelId)
									&& marker.isSubtypeOf(elementMarkerType)) {
								String className = elements[i].getAttribute("class"); //$NON-NLS-1$
								if (className == null) {
									CDebugCorePlugin.log(new Status(IStatus.ERROR,
											CDebugCorePlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR,
											"Extension " + elements[i].getDeclaringExtension().getUniqueIdentifier() //$NON-NLS-1$
													+ " missing required attribute: className", //$NON-NLS-1$
											null));
								} else {
									ICBreakpointExtension extension;
									try {
										extension = (ICBreakpointExtension) elements[i]
												.createExecutableExtension("class"); //$NON-NLS-1$
										extension.initialize(this);
										extensions.add(extension);
									} catch (CoreException e) {
										CDebugCorePlugin.log(new Status(IStatus.ERROR,
												CDebugCorePlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR,
												"Extension " + elements[i].getDeclaringExtension().getUniqueIdentifier() //$NON-NLS-1$
														+ " contains an invalid value for attribute: className", //$NON-NLS-1$
												e));
									}
								}
							}
						}
					}
					fExtensions.put(debugModelId, extensions.toArray(new ICBreakpointExtension[extensions.size()]));
				}
			}
		}
		return fExtensions.get(debugModelId);
	}

	@Override
	public void refreshMessage() throws CoreException {
		IMarker marker = ensureMarker();
		marker.setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	@Override
	public String getExtensionMessage() {
		Collection<ICBreakpointExtension[]> extensionLists;
		synchronized (fExtensions) {
			extensionLists = new ArrayList<>(fExtensions.values());
		}

		StringBuilder sb = new StringBuilder();
		for (ICBreakpointExtension[] extensions : extensionLists) {
			for (ICBreakpointExtension extension : extensions) {
				SafeRunner.run(() -> {
					String message = extension.getExtensionMessage();
					if (message != null && !message.isEmpty()) {
						if (sb.length() > 0) {
							sb.append(' ');
						}
						sb.append(message);
					}
				});
			}
		}

		return sb.toString();
	}
}
