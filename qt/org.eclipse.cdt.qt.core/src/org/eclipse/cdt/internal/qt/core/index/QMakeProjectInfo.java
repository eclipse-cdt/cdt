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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.qt.core.index.IQMakeEnvProvider.IController;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Represents a QMake project information that is based on an activate project configuration of a specified related IProject.
 * Allows to resolve actual information and listen its change.
 */
public final class QMakeProjectInfo implements IQMakeProjectInfo {

	private final State STATE_FREEZE = new State();
	private final State STATE_INVALID = new State();

	// listeners
	private final List<IQMakeProjectInfoListener> listeners = new CopyOnWriteArrayList<>();

	private final IProject project;

	private final Object stateSync = new Object();

	// represents a current state of QMakeProjectInfo
	private State state = STATE_INVALID;

	QMakeProjectInfo(IProject project) {
		this.project = project;
	}

	void destroy() {
		setState(STATE_FREEZE);
	}

	// must not be called under any QMake-related sync-lock, except for workspace lock
	private void updateStateFrom(State fromState) {
		synchronized (stateSync) {
			if (state != fromState) {
				return;
			}
		}
		updateState();
	}

	// must not be called under any QMake-related sync-lock, except for workspace lock
	// we are running outside of synchronized block to prevent deadlock involving workspace lock
	// this means that theoretically there might be multiple thread calculating the same results but only the last one wins
	State updateState() {
		// note that getProjectDescription might acquire workspace lock
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescriptionManager()
				.getProjectDescription(project);
		ICConfigurationDescription configuration = projectDescription != null
				? projectDescription.getActiveConfiguration()
				: null;
		State newState = configuration != null ? new State(configuration) : STATE_INVALID;
		setState(newState);
		return newState;
	}

	private void setState(State newState) {
		State oldState = null;
		synchronized (stateSync) {
			if (newState == null || state == newState) {
				return;
			}
			if (state == STATE_FREEZE) {
				newState.destroyBeforeInit();
				return;
			}
			oldState = state;
			state = newState;
			if (oldState != null) {
				oldState.destroy();
			}
			newState.init();
		}
		for (IQMakeProjectInfoListener listener : listeners) {
			listener.qmakeInfoChanged();
		}
	}

	@Override
	public void addListener(IQMakeProjectInfoListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IQMakeProjectInfoListener listener) {
		listeners.remove(listener);
	}

	IProject getProject() {
		return project;
	}

	@Override
	public IQMakeInfo getActualInfo() {
		synchronized (stateSync) {
			return state.getQMakeInfo();
		}
	}

	@Override
	public IQMakeInfo updateActualInfo() {
		return updateState().getQMakeInfo();
	}

	// converts IFile to absolute path
	private static String toFilePath(IFile file) {
		if (file != null) {
			IPath rawLocation = file.getRawLocation();
			if (rawLocation != null) {
				rawLocation = rawLocation.makeAbsolute();
				if (rawLocation != null) {
					File f = rawLocation.toFile();
					if (f != null) {
						return f.getAbsolutePath();
					}
				}
			}
		}
		return null;
	}

	// checks if any of the specified files is a sensitive file
	boolean containsAnySensitiveFile(Set<IPath> files) {
		synchronized (stateSync) {
			return state.containsAnySensitiveFile(files);
		}
	}

	/**
	 * Represents a state of QMakeInfo for a specific QMakeProjectInfo.
	 */
	private final class State implements IController {

		// an active project configuration
		private final ICConfigurationDescription configuration;
		// an active project qmake env
		private final IQMakeEnv qmakeEnv;
		// the last calculated QMake info; null if not calculated
		private final IQMakeInfo qmakeInfo;
		// a set of sensitive files that might affects actual QMake information
		private final SensitiveSet sensitiveFilePathSet;

		State() {
			configuration = null;
			qmakeEnv = null;
			qmakeInfo = QMakeInfo.INVALID;
			sensitiveFilePathSet = new SensitiveSet();
		}

		State(ICConfigurationDescription configuration) {
			this.configuration = configuration;
			// qmakeEnv created from registry of qmakeEnvProvider extensions
			this.qmakeEnv = QMakeEnvProviderManager.getInstance().createEnv(this);

			// retrieves IQMakeEnvInfo from IQMakeEnv
			QMakeEnvInfo qmakeEnvInfo = qmakeEnv.getQMakeEnvInfo();

			// retrieves .pro file path
			String proFilePath = toFilePath(qmakeEnvInfo != null ? qmakeEnvInfo.getProFile() : null);
			// retrieves qmake executable path
			String qmakeFilePath = qmakeEnvInfo != null ? qmakeEnvInfo.getQMakeFilePath() : null;
			// retries environment
			List<String> envList = new ArrayList<>();
			Map<String, String> envMap = qmakeEnvInfo != null ? qmakeEnvInfo.getEnvironment()
					: Collections.<String, String>emptyMap();
			for (Map.Entry<String, String> entry : envMap.entrySet()) {
				envList.add(entry.getKey() + "=" + entry.getValue());
			}

			// calculates actual QMake info
			qmakeInfo = QMakeInfo.create(proFilePath, qmakeFilePath, envList.toArray(new String[envList.size()]));

			// calculates a new set of sensitive file paths
			sensitiveFilePathSet = new SensitiveSet();
			Set<IFile> envSensFiles = qmakeEnvInfo != null ? qmakeEnvInfo.getSensitiveFiles()
					: Collections.<IFile>emptySet();
			for (IFile sensitiveFile : envSensFiles) {
				if (sensitiveFile != null) {
					sensitiveFilePathSet.addSensitiveFile(sensitiveFile);
				}
			}
			if (proFilePath != null) {
				sensitiveFilePathSet.addSensitiveFile(proFilePath);
			}
			List<String> sensitiveFiles = qmakeInfo.getInvolvedQMakeFiles();
			if (sensitiveFiles != null) {
				for (String sensitiveFile : sensitiveFiles) {
					sensitiveFilePathSet.addSensitiveFile(sensitiveFile);
				}
			}
		}

		private boolean containsAnySensitiveFile(Set<IPath> files) {
			for (Iterator<IPath> iterator = files.iterator(); iterator.hasNext();) {
				IPath path = iterator.next();
				if (sensitiveFilePathSet.contains(path)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public ICConfigurationDescription getConfiguration() {
			return configuration;
		}

		public void destroyBeforeInit() {
			// see IQMakeEnv JavaDoc for details
			if (qmakeEnv != null && !(qmakeEnv instanceof IQMakeEnv2)) {
				qmakeEnv.destroy();
			}
		}

		public void init() {
			if (qmakeEnv instanceof IQMakeEnv2) {
				((IQMakeEnv2) qmakeEnv).init();
			}
		}

		public void destroy() {
			if (qmakeEnv != null) {
				qmakeEnv.destroy();
			}
		}

		@Override
		public void scheduleUpdate() {
			updateStateFrom(this);
		}

		IQMakeInfo getQMakeInfo() {
			return qmakeInfo;
		}

	}

	private static final class SensitiveSet extends HashSet<IPath> {

		private static final long serialVersionUID = 2684086006933209512L;

		// adds a sensitive file in form of a specified absolute path
		private void addSensitiveFile(String sensitiveFile) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] files = root
					.findFilesForLocationURI(URIUtil.toURI(Path.fromOSString(sensitiveFile).makeAbsolute()));
			if (files != null && files.length > 0) {
				IFile file = files[0];
				addSensitiveFile(file);
			}
		}

		// adds a sensitive file in form of a IFile
		private void addSensitiveFile(IFile file) {
			IPath fullPath = file.getFullPath();
			if (fullPath != null) {
				add(fullPath);
			}
		}

	}

}
