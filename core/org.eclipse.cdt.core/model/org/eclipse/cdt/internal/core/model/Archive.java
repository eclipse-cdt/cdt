/*******************************************************************************
 * Copyright (c) 2000, 2023 QNX Software Systems and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *     John Dallaway - Adapt for IBinaryFile (#413)
 *     John Dallaway - Fix object path processing (#630)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class Archive extends Openable implements IArchive {

	IBinaryArchive binaryArchive;

	public Archive(ICElement parent, IFile file, IBinaryArchive ar) {
		super(parent, file, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	public Archive(ICElement parent, IPath path, IBinaryArchive ar) {
		super(parent, path, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	@Override
	public IBinary[] getBinaries() throws CModelException {
		ICElement[] e = getChildren();
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() throws CModelException {
		return (ArchiveInfo) getElementInfo();
	}

	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements,
			IResource underlyingResource) throws CModelException {
		return computeChildren(info, underlyingResource);
	}

	public boolean computeChildren(OpenableInfo info, IResource res) {
		IBinaryArchive ar = getBinaryArchive();
		IPath location = res.getLocation();
		if (ar != null && location != null) {
			// find the build CWD for the archive file
			IPath buildCWD = Optional.ofNullable(findBuildConfiguration(res)).map(Archive::getBuildCWD)
					.orElse(location.removeLastSegments(1));
			for (IBinaryObject obj : ar.getObjects()) {
				// assume object names are paths as specified on the archiver command line ("ar -P")
				IPath objPath = new Path(obj.getName());
				if (!objPath.isAbsolute()) {
					// assume path is relative to the build CWD
					objPath = buildCWD.append(objPath);
				}
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(objPath);
				if (file == null) { // if object path is external to the workspace
					// fallback to legacy behaviour
					// TODO: support external paths in Binary class as we do in TranslationUnit
					objPath = ar.getPath().append(objPath.lastSegment());
				}
				Binary binary = new Binary(this, objPath, obj);
				info.addChild(binary);
			}
			return true;
		}
		return false;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IBinaryArchive.class)) {
			return adapter.cast(getBinaryArchive());
		}
		return super.getAdapter(adapter);
	}

	IBinaryArchive getBinaryArchive() {
		return binaryArchive;
	}

	@Override
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}

	@Override
	protected void closing(Object info) throws CModelException {
		ICProject cproject = getCProject();
		CProjectInfo pinfo = (CProjectInfo) CModelManager.getDefault().peekAtInfo(cproject);
		if (pinfo != null && pinfo.vLib != null) {
			pinfo.vLib.removeChild(this);
		}
		super.closing(info);
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		return null;
	}

	@Override
	public String getHandleMemento() {
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
		return 0;
	}

	private static ICConfigurationDescription findBuildConfiguration(IResource resource) {
		IPath location = resource.getLocation();
		IProject project = resource.getProject();
		ICProjectDescription projectDesc = CoreModel.getDefault().getProjectDescription(project, false);
		if (projectDesc == null) {
			return null; // not a CDT project
		}
		// for each build configuration of the project
		for (ICConfigurationDescription configDesc : projectDesc.getConfigurations()) {
			CConfigurationData configData = configDesc.getConfigurationData();
			if (configData == null) {
				continue; // no configuration data
			}
			CBuildData buildData = configData.getBuildData();
			if (buildData == null) {
				continue; // no build data
			}
			// for each build output directory of the build configuration
			for (ICOutputEntry dir : buildData.getOutputDirectories()) {
				IPath dirLocation = CDataUtil.makeAbsolute(project, dir).getLocation();
				// if the build output directory is an ancestor of the resource
				if ((dirLocation != null) && dirLocation.isPrefixOf(location)) {
					return configDesc; // build configuration found
				}
			}
		}
		return null;
	}

	private static IPath getBuildCWD(ICConfigurationDescription configDesc) {
		IPath builderCWD = configDesc.getBuildSetting().getBuilderCWD();
		if (builderCWD != null) {
			ICdtVariableManager manager = CCorePlugin.getDefault().getCdtVariableManager();
			try {
				String cwd = builderCWD.toString();
				cwd = manager.resolveValue(cwd, "", null, configDesc); //$NON-NLS-1$
				if (!cwd.isEmpty()) {
					return new Path(cwd);
				}
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}

}
