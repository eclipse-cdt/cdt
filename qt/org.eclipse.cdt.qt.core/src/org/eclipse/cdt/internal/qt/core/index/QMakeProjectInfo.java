/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.qt.core.index.IQMakeEnv;
import org.eclipse.cdt.qt.core.index.IQMakeEnvProvider;
import org.eclipse.cdt.qt.core.index.IQMakeProjectInfo;
import org.eclipse.cdt.qt.core.index.IQMakeProjectInfoListener;
import org.eclipse.cdt.qt.core.index.QMakeEnvInfo;
import org.eclipse.cdt.qt.core.index.IQMakeInfo;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Represents a QMake project information that is based on an activate project configuration of a specified related IProject.
 * Allows to resolve actual information and listen its change.
 * Manages life-cycle of all QMakeProjectInfo instances.
 */
public final class QMakeProjectInfo implements IQMakeProjectInfo, ICProjectDescriptionListener {

	private static final RCListener LISTENER = new RCListener();
	// sync object for CACHE field
	private static final Object SYNC = new Object();
	// a map of all QMakeProjectInfo instances
	private static final Map<IProject,QMakeProjectInfo> CACHE = new HashMap<IProject,QMakeProjectInfo>();

	// called by QtPlugin activator to setup this class
	public static final void start() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(LISTENER, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
	}

	// called by QtPlugin activator to clean up this class
	public static final void stop() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(LISTENER);
		List<QMakeProjectInfo> infos;
		synchronized (SYNC) {
			infos = new ArrayList<QMakeProjectInfo>(CACHE.values());
			CACHE.clear();
		}
		for (QMakeProjectInfo info : infos) {
			if (info != null) {
				info.destroy();
			}
		}
	}

	/**
	 * Returns a QMakeProjectInfo for an active project configuration of a specified project.
	 *
	 * @param project the project
	 * @return the QMakeProjectInfo; or null if the project does not have QtNature
	 */
	public static QMakeProjectInfo getQMakeProjectInfoFor(IProject project) {
		QMakeProjectInfo info;
		synchronized (SYNC) {
			info = CACHE.get(project);
			if (info != null) {
				return info;
			}
			info = new QMakeProjectInfo(project);
			CACHE.put(project,info);
		}
		info.updateActiveConfiguration();
		return info;
	}

	// removes the project from the CACHE
	private static void removeProjectFromCache(IResource project) {
		QMakeProjectInfo info;
		synchronized (SYNC) {
			info = CACHE.remove(project);
		}
		if (info != null) {
			info.destroy();
		}
	}

	private final IProject project;

	// sync object for all mutable fields
	private final Object sync = new Object();
	// true if this instance still registered in CACHE
	private boolean live = true;
	// a set of sensitive files that might affects actual QMake information
	private final SensitiveSet sensitiveFilePathSet = new SensitiveSet();
	// an active project configuration
	private ControllerImpl activeController;
	// the last calculated QMake info; null if not calculated
	private IQMakeInfo qmakeInfo = null;
	// listeners
	private final List<IQMakeProjectInfoListener> listeners = new CopyOnWriteArrayList<IQMakeProjectInfoListener>();

	private QMakeProjectInfo(IProject project) {
		this.project = project;
		CoreModel.getDefault().addCProjectDescriptionListener(this, ICDescriptionDelta.ACTIVE_CFG);
	}

	// called from removeProjectFromCache only
	private void destroy() {
		synchronized (sync) {
			if (! live) {
				return;
			}
			live = false;
			CoreModel.getDefault().removeCProjectDescriptionListener(this);
			setActiveConfiguration(null);
			qmakeInfo = QMakeInfo.INVALID;
		}
	}

	// must not be called under synchronized (SYNC) or synchronized (sync)
	private void updateActiveConfiguration() {
		synchronized (sync) {
			if (! live) {
				return;
			}
			ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescriptionManager().getProjectDescription(project);
			setActiveConfiguration(projectDescription != null ? projectDescription.getActiveConfiguration() : null);
			qmakeInfo = null;
		}
		notifyListeners();
	}

	// called under synchronized (sync)
	private void setActiveConfiguration(ICConfigurationDescription configuration) {
		ControllerImpl previous = activeController;
		activeController = configuration != null ? new ControllerImpl(configuration) : null;
		if (previous != null) {
			previous.destroy();
		}
	}

	// called on active project configuration change
	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		if (event.getProject() != project) {
			return;
		}
		updateActiveConfiguration();
	}

	@Override
	public void addListener(IQMakeProjectInfoListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IQMakeProjectInfoListener listener) {
		listeners.remove(listener);
	}

	private IProject getProject() {
		return project;
	}

	// calculates (if does not exist) and returns actual QMake info
	@Override
	public IQMakeInfo getActualInfo() {
		synchronized (sync) {
			if (! live) {
				return QMakeInfo.INVALID;
			}
			if (qmakeInfo == null) {
				fetchQMakeInfo();
			}
			return qmakeInfo;
		}
	}

	// calculates actual QMake info
	private void fetchQMakeInfo() {
		// retrieves IQMakeEnvInfo from IQMakeEnv
		IQMakeEnv qmakeEnv = activeController != null ? activeController.getQMakeEnv() : null;
		QMakeEnvInfo qmakeEnvInfo = qmakeEnv != null ? qmakeEnv.getQMakeEnvInfo() : null;

		// retrieves .pro file path
		String proFilePath = toFilePath(qmakeEnvInfo != null ? qmakeEnvInfo.getProFile() : null);
		// retrieves qmake executable path
		String qmakeFilePath = qmakeEnvInfo != null ? qmakeEnvInfo.getQMakeFilePath() : null;
		// retries environment
		List<String> envList = new ArrayList<String>();
		Map<String, String> envMap = qmakeEnvInfo != null ? qmakeEnvInfo.getEnvironment() : Collections.<String,String>emptyMap();
		for (Map.Entry<String,String> entry : envMap.entrySet()) {
			envList.add(entry.getKey() + "=" + entry.getValue());
		}

		// calculates actual QMake info
		qmakeInfo = QMakeInfo.create(proFilePath, qmakeFilePath, envList.toArray(new String[envList.size()]));

		// calculates a new set of sensitive file paths
		sensitiveFilePathSet.clear();
		Set<IFile> envSensFiles = qmakeEnvInfo != null ? qmakeEnvInfo.getSensitiveFiles() : Collections.<IFile>emptySet();
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
	private boolean containsAnySensitiveFile(Set<IPath> files) {
		synchronized (sync) {
			if (live) {
				for (Iterator<IPath> iterator = files.iterator(); iterator.hasNext();) {
					IPath path = iterator.next();
					if (sensitiveFilePathSet.contains(path)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	// resets actual QMake info and notifies all listeners that QMake information has changes
	private void scheduleFetchQMakeInfo() {
		synchronized (sync) {
			if (! live) {
				return;
			}
			qmakeInfo = null;
		}
		notifyListeners();
	}

	private void notifyListeners() {
		for (IQMakeProjectInfoListener listener : listeners) {
			listener.qmakeInfoChanged();
		}
	}

	/**
	 * Represents a project configuration.
	 */
	private final class ControllerImpl implements IQMakeEnvProvider.IController {

		private final ICConfigurationDescription configuration;
		private final IQMakeEnv qmakeEnv;

		public ControllerImpl(ICConfigurationDescription configuration) {
			this.configuration = configuration;
			// qmakeEnv created from registry of qmakeEnvProvider extensions
			this.qmakeEnv = QMakeEnvProviderManager.getInstance().createEnv(this);
		}

		public void destroy() {
			qmakeEnv.destroy();
		}

		public IQMakeEnv getQMakeEnv() {
			return qmakeEnv;
		}

		@Override
		public ICConfigurationDescription getConfiguration() {
			return configuration;
		}

		@Override
		public void scheduleUpdate() {
			scheduleFetchQMakeInfo();
		}

	}

	/**
	 * Listens on Eclipse file system changes.
	 */
	private static final class RCListener implements IResourceChangeListener {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			RDVisitor visitor = new RDVisitor();

			// collect project to delete and changed files
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_CLOSE:
			case IResourceChangeEvent.PRE_DELETE:
				IResource project = event.getResource();
				if (project != null && project.getType() == IResource.PROJECT) {
					visitor.addProjectToDelete(project);
				}
				break;
			case IResourceChangeEvent.POST_CHANGE:
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					try {
						delta.accept(visitor);
					} catch (CoreException e) {
						// empty
					}
				}
				break;
			}

			// process collected data
			visitor.process();
		}

	}

	private static final class RDVisitor implements IResourceDeltaVisitor {

		private final Set<IResource> projectsToDelete = new HashSet<IResource>();
		private final Set<IResource> projectsToUpdate = new HashSet<IResource>();
		private final Set<IPath> changedFiles = new HashSet<IPath>();

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource != null) {
				switch (resource.getType()) {
				case IResource.FILE:
					addChangedFile(resource);
					return false;
				case IResource.PROJECT:
					switch (delta.getKind()) {
					case IResourceDelta.CHANGED:
						if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
							addProjectToUpdate(resource);
						}
						return true;
					case IResourceDelta.REMOVED:
						addProjectToDelete(resource);
						return false;
					}
					break;
				}
			}
			return true;
		}

		private void addProjectToUpdate(IResource project) {
			projectsToUpdate.add(project);
		}

		private void addProjectToDelete(IResource project) {
			projectsToDelete.add(project);
		}

		private void addChangedFile(IResource file) {
			IPath fullPath = file.getFullPath();
			if (fullPath != null) {
				changedFiles.add(fullPath);
			}
		}

		public void process() {
			// removing projects from CACHE
			for(IResource project : projectsToDelete) {
				removeProjectFromCache(project);
			}

			List<QMakeProjectInfo> infos;
			synchronized (SYNC) {
				infos = new ArrayList<QMakeProjectInfo>(CACHE.values());
			}
			for (QMakeProjectInfo info : infos) {
				// checking if any project description change affects QMakeProjectInfo
				if (projectsToUpdate.contains(info.getProject())) {
					info.updateActiveConfiguration();
				}
				// checking if any of the changed files affects QMakeProjectInfo
				if (info.containsAnySensitiveFile(changedFiles)) {
					// if so then scheduling update
					info.scheduleFetchQMakeInfo();
				}
			}
		}

	}

	private static final class SensitiveSet extends HashSet<IPath> {

		private static final long serialVersionUID = 2684086006933209512L;

		// adds a sensitive file in form of a specified absolute path
		private void addSensitiveFile(String sensitiveFile) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] files = root.findFilesForLocationURI(URIUtil.toURI(Path.fromOSString(sensitiveFile).makeAbsolute()));
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
