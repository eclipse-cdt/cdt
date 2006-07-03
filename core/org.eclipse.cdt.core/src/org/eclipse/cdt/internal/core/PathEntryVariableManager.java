/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IPathEntryVariableChangeListener;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.cdt.core.resources.PathEntryVariableChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SafeRunner;



/**
 * Core's implementation of IPathEntryVariableManager. 
 */
public class PathEntryVariableManager implements IPathEntryVariableManager {

	private Set listeners;
	private Preferences preferences;

	static final String VARIABLE_PREFIX = "pathEntryVariable."; //$NON-NLS-1$

	/**
	 * Constructor for the class.
	 */
	public PathEntryVariableManager() {
		this.listeners = Collections.synchronizedSet(new HashSet());
		this.preferences = CCorePlugin.getDefault().getPluginPreferences();
	}

	/**
	 * Note that if a user changes the key in the preferences file to be invalid
	 * and then calls #getValue using that key, they will get the value back for
	 * that. But then if they try and call #setValue using the same key it will throw
	 * an exception. We may want to revisit this behaviour in the future.
	 * 
	 * @see org.eclipse.core.resources.IPathEntryVariableManager#getValue(String)
	 */
	public IPath getValue(String varName) {
		String key = getKeyForName(varName);
		String value = preferences.getString(key);
		return value.length() == 0 ? null : Path.fromPortableString(value);
	}

	/**
	 * @see org.eclipse.core.resources.IPathEntryVariableManager#setValue(String, IPath)
	 */
	public void setValue(String varName, IPath newValue) throws CoreException {
		//if the location doesn't have a device, see if the OS will assign one
		if (newValue != null && newValue.isAbsolute() && newValue.getDevice() == null) {
			newValue = new Path(newValue.toFile().getAbsolutePath());
		}
		int eventType;
		// read previous value and set new value atomically in order to generate the right event		
		synchronized (this) {
			IPath currentValue = getValue(varName);
			boolean variableExists = currentValue != null;
			if (!variableExists && newValue == null) {
				return;
			}
			if (variableExists && currentValue.equals(newValue)) {
				return;
			}
			if (newValue == null) {
				preferences.setToDefault(getKeyForName(varName));
				eventType = PathEntryVariableChangeEvent.VARIABLE_DELETED;
			} else {
				preferences.setValue(getKeyForName(varName), newValue.toPortableString());
				eventType = variableExists ? PathEntryVariableChangeEvent.VARIABLE_CHANGED : PathEntryVariableChangeEvent.VARIABLE_CREATED;
			}
		}
		// notify listeners from outside the synchronized block to avoid deadlocks
		fireVariableChangeEvent(varName, newValue, eventType);
	}

	/**
	 * Return a key to use in the Preferences.
	 */
	private String getKeyForName(String varName) {
		return VARIABLE_PREFIX + varName;
	}

	/**
	 * @see org.eclipse.core.resources.IPathEntryVariableManager#resolvePath(IPath)
	 */
	public IPath resolvePath(IPath path) {
		if (path == null || path.segmentCount() == 0) {
			return path;
		}
		String variable = path.toPortableString();
		if (variable.indexOf('$') == -1) {
			return path;
		}
		String value = expandVariable(variable);
		return (value == null || value.length() == 0) ? Path.EMPTY : new Path(value);
	}

	/**
	 * Fires a property change event corresponding to a change to the
	 * current value of the variable with the given name.
	 * 
	 * @param name the name of the variable, to be used as the variable
	 *      in the event object
	 * @param value the current value of the path variable or <code>null</code> if
	 *      the variable was deleted
	 * @param type one of <code>IPathVariableChangeEvent.VARIABLE_CREATED</code>,
	 *      <code>PathEntryVariableChangeEvent.VARIABLE_CHANGED</code>, or
	 *      <code>PathEntryVariableChangeEvent.VARIABLE_DELETED</code>
	 * @see PathEntryVariableChangeEvent
	 * @see PathEntryVariableChangeEvent#VARIABLE_CREATED
	 * @see PathEntryVariableChangeEvent#VARIABLE_CHANGED
	 * @see PathEntryVariableChangeEvent#VARIABLE_DELETED
	 */
	private void fireVariableChangeEvent(String name, IPath value, int type) {
		if (this.listeners.size() == 0)
			return;
		// use a separate collection to avoid interference of simultaneous additions/removals 
		Object[] listenerArray = this.listeners.toArray();
		final PathEntryVariableChangeEvent pve = new PathEntryVariableChangeEvent(this, name, value, type);
		for (int i = 0; i < listenerArray.length; ++i) {
			final IPathEntryVariableChangeListener l = (IPathEntryVariableChangeListener) listenerArray[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already being logged in Platform#run()
				}

				public void run() throws Exception {
					l.pathVariableChanged(pve);
				}
			};
			SafeRunner.run(job);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#getPathVariableNames()
	 */
	public String[] getVariableNames() {
		List result = new LinkedList();
		String[] names = preferences.propertyNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(VARIABLE_PREFIX)) {
				String key = names[i].substring(VARIABLE_PREFIX.length());
				result.add(key);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * @see org.eclipse.core.resources.
	 * IPathEntryVariableManager#addChangeListener(IPathEntryVariableChangeListener)
	 */
	public void addChangeListener(IPathEntryVariableChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.core.resources.
	 * IPathEntryVariableManager#removeChangeListener(IPathEntryVariableChangeListener)
	 */
	public void removeChangeListener(IPathEntryVariableChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableManager#isDefined(String)
	 */
	public boolean isDefined(String varName) {
		return getValue(varName) != null;
	}

	public void startup() {
	}

	public void shutdown() {
	}

	/**
	 * Expand the variable with the format ${key}. example:
	 * with variable HOME=/foobar
	 * ${HOME}/project
	 * The the return value will be /foobar/project.
	 */
	protected String expandVariable(String variable) {
		StringBuffer sb = new StringBuffer();
		StringBuffer param = new StringBuffer();
		char prev = '\n';
		char ch = prev;
		boolean inMacro = false;
		boolean inSingleQuote = false;
		
		for (int i = 0; i < variable.length(); i++) {
			ch = variable.charAt(i);
			switch (ch) {
			case '\'':
				if (prev != '\\') {
					inSingleQuote = !inSingleQuote;
				}
				break;
				
			case '$' :
				if (!inSingleQuote && prev != '\\') {
					if (i < variable.length() && variable.indexOf('}', i) > 0) {
						char c = variable.charAt(i + 1);
						if (c == '{') {
							param.setLength(0);
							inMacro = true;
							prev = ch;
							continue;
						}
					}
				}
				break;
				
			case '}' :
				if (inMacro) {
					inMacro = false;
					String p = param.toString();
					IPath path = getValue(p);
					if (path != null) {
						String v = path.toPortableString();
						if (v != null) {
							sb.append(v);
						}
					}
					param.setLength(0);
					/* Skip the trailing } */
					prev = ch;
					continue;
				}
				break;
			} /* switch */
			
			if (!inMacro) {
				sb.append(ch);
			} else {
				/* Do not had the '{' */
				if (!(ch == '{' && prev == '$')) {
					param.append(ch);
				}
			}
			prev = (ch == '\\' && prev == '\\') ? '\n' : ch;
		} /* for */
		return sb.toString();
	}


}
