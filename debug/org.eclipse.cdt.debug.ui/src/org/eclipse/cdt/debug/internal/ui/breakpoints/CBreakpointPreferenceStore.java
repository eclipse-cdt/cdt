/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
 *     QNX Software Systems - Refactored to use platform implementation
 *     Marc Khouzam (Ericsson) - Added support for Tracepoints (bug 376116)
 *     Marc Khouzam (Ericsson) - Added support for Dynamic-Printf (bug 400628)
 *     Jonah Graham - Set REQUESTED_* fields when creating from dialog (bug 46026)
 *     Jonah Graham (Kichwa Coders) - Create "Add Line Breakpoint (C/C++)" action (Bug 464917)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint2;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A preference store that presents the state of the properties of a C/C++ breakpoint,
 * tracepoint or dynamic-printf.
 */
public class CBreakpointPreferenceStore implements IPersistentPreferenceStore {

	// This map is the current properties/values being maintained/manipulated
	private HashMap<String, Object> fProperties = new HashMap<>();

	// Original set of values. So we can see what has really changed on the save and
	// perform appropriate change operations. We only really want to operate on changed
	// values, to avoid generating churn.
	private HashMap<String, Object> fOriginalValues = new HashMap<>();
	private boolean fIsDirty = false;
	private boolean fIsCanceled = false;
	private ListenerList<IPropertyChangeListener> fListeners;
	private final CBreakpointContext fContext;

	public CBreakpointPreferenceStore() {
		this(null, null);
	}

	public CBreakpointPreferenceStore(CBreakpointContext context, Map<String, Object> attributes) {
		fListeners = new ListenerList<>(org.eclipse.core.runtime.ListenerList.IDENTITY);
		fContext = context;

		fOriginalValues.clear();
		fProperties.clear();
		if (context != null) {
			IMarker marker = context.getBreakpoint().getMarker();
			if (marker != null) {
				Map<String, Object> bpAttrs = Collections.emptyMap();
				try {
					bpAttrs = marker.getAttributes();
					fOriginalValues.putAll(bpAttrs);
					fProperties.putAll(bpAttrs);
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
		}
		if (attributes != null) {
			fProperties.putAll(attributes);
			fIsDirty = true;
		}
	}

	public Map<String, Object> getAttributes() {
		return fProperties;
	}

	public void setCanceled(boolean canceled) {
		fIsCanceled = canceled;
	}

	@Override
	public void save() throws IOException {
		if (!fIsCanceled && fContext != null && fContext.getBreakpoint() != null) {
			ICBreakpoint bp = fContext.getBreakpoint();
			if (bp.getMarker() != null && fIsDirty) {
				saveToExistingMarker(bp, bp.getMarker());
			} else {
				IResource resolved = getResource(fContext.getResource());
				if (resolved != null) {
					saveToNewMarker(bp, resolved);
				} else {
					throw new IOException("Unable to create breakpoint: no resource specified."); //$NON-NLS-1$
				}
			}
		}

	}

	/**
	 * Get the resource to apply the marker against. This may not be the same
	 * resource the dialog was created for if the user has selected a different
	 * resource.
	 * <p>
	 * If the {@link ICBreakpoint#SOURCE_HANDLE} resolves to the same file on
	 * the filesystem as the preferred resource the preferred resource is used.
	 *
	 * @param preferred
	 *            resource to use if it matches the SOURCE_HANDLE
	 * @return Resource to install marker on, or <code>null</code> for not
	 *         available.
	 */
	private IResource getResource(IResource preferred) {
		IResource resolved = null;
		String source = getString(ICBreakpoint.SOURCE_HANDLE);
		if (!"".equals(source)) { //$NON-NLS-1$
			IPath rawLocation = preferred.getRawLocation();
			if (rawLocation != null) {
				File file = rawLocation.toFile();
				File sourceFile = new File(source);
				if (file.getAbsoluteFile().equals(sourceFile.getAbsoluteFile())) {
					resolved = preferred;
				}
			}

			if (resolved == null) {
				IPath path = Path.fromOSString(source);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IFile file = root.getFileForLocation(path);
				if (file == null) {
					resolved = root;
				} else {
					resolved = file;
				}
			}
		}
		if (resolved == null) {
			resolved = preferred;
		}
		return resolved;
	}

	private void saveToExistingMarker(final ICBreakpoint breakpoint, final IMarker marker) throws IOException {
		final List<String> changedProperties = new ArrayList<>(5);
		Set<String> valueNames = fProperties.keySet();
		for (String name : valueNames) {
			if (fProperties.containsKey(name)) {
				Object originalObject = fOriginalValues.get(name);
				Object currentObject = fProperties.get(name);
				if (originalObject == null) {
					changedProperties.add(name);
				} else if (!originalObject.equals(currentObject)) {
					changedProperties.add(name);
				}
			}
		}
		if (!changedProperties.isEmpty()) {
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					Iterator<String> changed = changedProperties.iterator();
					while (changed.hasNext()) {
						String property = changed.next();
						if (property.equals(ICBreakpoint.ENABLED)) {
							breakpoint.setEnabled(getBoolean(ICBreakpoint.ENABLED));
						} else if (property.equals(ICBreakpoint.IGNORE_COUNT)) {
							breakpoint.setIgnoreCount(getInt(ICBreakpoint.IGNORE_COUNT));
						} else if (breakpoint instanceof ICTracepoint && property.equals(ICTracepoint.PASS_COUNT)) {
							((ICTracepoint) breakpoint).setPassCount(getInt(ICTracepoint.PASS_COUNT));
						} else if (breakpoint instanceof ICDynamicPrintf
								&& property.equals(ICDynamicPrintf.PRINTF_STRING)) {
							((ICDynamicPrintf) breakpoint).setPrintfString(getString(ICDynamicPrintf.PRINTF_STRING));
						} else if (property.equals(ICBreakpoint.CONDITION)) {
							breakpoint.setCondition(getString(ICBreakpoint.CONDITION));
						} else if (property.equals(IMarker.LINE_NUMBER)) {
							if (breakpoint instanceof ICLineBreakpoint2) {
								// refresh message and line number
								// Note there are no API methods to set the line number of a Line Breakpoint, so we
								// replicate what is done in CDIDebugModel.setLineBreakpointAttributes()
								// to set the line number fields properly and then refresh the message if possible
								((ICLineBreakpoint2) breakpoint).setRequestedLine(getInt(IMarker.LINE_NUMBER));
								breakpoint.getMarker().setAttribute(IMarker.LINE_NUMBER, getInt(IMarker.LINE_NUMBER));
								((ICBreakpoint2) breakpoint).refreshMessage();
							} else {
								// already workspace runnable, setting markers are safe
								breakpoint.getMarker().setAttribute(IMarker.LINE_NUMBER, getInt(IMarker.LINE_NUMBER));
								breakpoint.getMarker().setAttribute(ICLineBreakpoint2.REQUESTED_LINE,
										getInt(IMarker.LINE_NUMBER));
							}
						} else {
							// this allow set attributes contributed by other plugins
							Object value = fProperties.get(property);
							if (value != null) {
								marker.setAttribute(property, value);
								if (breakpoint instanceof ICBreakpoint2) {
									// To be safe, refresh the breakpoint message as the property
									// change might affect it.
									((ICBreakpoint2) breakpoint).refreshMessage();
								}
							}
						}
					}
				}
			};
			try {
				ResourcesPlugin.getWorkspace().run(wr, null);
			} catch (CoreException ce) {
				throw new IOException("Cannot save properties to breakpoint.", ce); //$NON-NLS-1$
			}
		}
	}

	private void saveToNewMarker(final ICBreakpoint breakpoint, final IResource resource) throws IOException {
		try {
			// On initial creation of BP, make sure that requested values of breakpoint
			// match the current values (i.e. make sure it starts as a not-relocated breakpoint)
			// See CDIDebugModel.setLineBreakpointAttributes
			if (fProperties.containsKey(ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE)) {
				fProperties.put(ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE, fProperties.get(ICBreakpoint.SOURCE_HANDLE));
			}
			if (fProperties.containsKey(ICLineBreakpoint2.REQUESTED_LINE)) {
				fProperties.put(ICLineBreakpoint2.REQUESTED_LINE, fProperties.get(IMarker.LINE_NUMBER));
			}
			if (fProperties.containsKey(ICLineBreakpoint2.REQUESTED_CHAR_START)) {
				fProperties.put(ICLineBreakpoint2.REQUESTED_CHAR_START, fProperties.get(IMarker.CHAR_START));
			}
			if (fProperties.containsKey(ICLineBreakpoint2.REQUESTED_CHAR_END)) {
				fProperties.put(ICLineBreakpoint2.REQUESTED_CHAR_END, fProperties.get(IMarker.CHAR_END));
			}

			CDIDebugModel.createBreakpointMarker(breakpoint, resource, fProperties, true);
		} catch (CoreException ce) {
			throw new IOException("Cannot save properties to new breakpoint.", ce); //$NON-NLS-1$
		}
	}

	///////////////////////////////////////////////////////////////////////
	// IPreferenceStore

	@Override
	public boolean needsSaving() {
		return fIsDirty && !fIsCanceled;
	}

	@Override
	public boolean contains(String name) {
		return fProperties.containsKey(name);
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		Object[] listeners = fListeners.getListeners();
		// Do we need to fire an event.
		if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
			PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) {
				IPropertyChangeListener l = (IPropertyChangeListener) listeners[i];
				l.propertyChange(pe);
			}
		}
	}

	@Override
	public boolean getBoolean(String name) {
		boolean retVal = false;
		Object o = fProperties.get(name);
		if (o instanceof Boolean) {
			retVal = ((Boolean) o).booleanValue();
		}
		return retVal;
	}

	@Override
	public int getInt(String name) {
		int retVal = 0;
		Object o = fProperties.get(name);
		if (o instanceof Integer) {
			retVal = ((Integer) o).intValue();
		}
		return retVal;
	}

	@Override
	public String getString(String name) {
		String retVal = ""; //$NON-NLS-1$
		Object o = fProperties.get(name);
		if (o instanceof String) {
			retVal = (String) o;
		}
		return retVal;
	}

	@Override
	public double getDouble(String name) {
		return 0;
	}

	@Override
	public float getFloat(String name) {
		return 0;
	}

	@Override
	public long getLong(String name) {
		return 0;
	}

	@Override
	public boolean isDefault(String name) {
		return false;
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		return false;
	}

	@Override
	public double getDefaultDouble(String name) {
		return 0;
	}

	@Override
	public float getDefaultFloat(String name) {
		return 0;
	}

	@Override
	public int getDefaultInt(String name) {
		return 0;
	}

	@Override
	public long getDefaultLong(String name) {
		return 0;
	}

	@Override
	public String getDefaultString(String name) {
		return null;
	}

	@Override
	public void putValue(String name, String value) {
		Object oldValue = fProperties.get(name);
		if (oldValue == null || !oldValue.equals(value)) {
			fProperties.put(name, value);
			setDirty(true);
		}
	}

	@Override
	public void setDefault(String name, double value) {
	}

	@Override
	public void setDefault(String name, float value) {
	}

	@Override
	public void setDefault(String name, int value) {
	}

	@Override
	public void setDefault(String name, long value) {
	}

	@Override
	public void setDefault(String name, String defaultObject) {
	}

	@Override
	public void setDefault(String name, boolean value) {
	}

	@Override
	public void setToDefault(String name) {
	}

	@Override
	public void setValue(String name, boolean value) {
		boolean oldValue = getBoolean(name);
		if (oldValue != value) {
			fProperties.put(name, Boolean.valueOf(value));
			setDirty(true);
			firePropertyChangeEvent(name, Boolean.valueOf(oldValue), Boolean.valueOf(value));
		}
	}

	@Override
	public void setValue(String name, int value) {
		int oldValue = getInt(name);
		if (oldValue != value) {
			fProperties.put(name, Integer.valueOf(value));
			setDirty(true);
			firePropertyChangeEvent(name, Integer.valueOf(oldValue), Integer.valueOf(value));
		}
	}

	@Override
	public void setValue(String name, String value) {
		Object oldValue = fProperties.get(name);
		if ((oldValue == null && value != null) || (oldValue != null && !oldValue.equals(value))) {
			fProperties.put(name, value);
			setDirty(true);
			firePropertyChangeEvent(name, oldValue, value);
		}
	}

	@Override
	public void setValue(String name, float value) {
	}

	@Override
	public void setValue(String name, double value) {
	}

	@Override
	public void setValue(String name, long value) {
	}

	// IPreferenceStore
	///////////////////////////////////////////////////////////////////////

	private void setDirty(boolean isDirty) {
		fIsDirty = isDirty;
	}
}
