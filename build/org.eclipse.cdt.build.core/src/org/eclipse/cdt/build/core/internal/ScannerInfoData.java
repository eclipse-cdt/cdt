/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.build.core.CBuildConfiguration;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;

import com.google.gson.Gson;

/**
 * Class representing scanner info data for a project as stored in the metadata
 * area.
 */
public class ScannerInfoData {

	private Set<ToolChainScannerInfo> perResourceInfo;
	private Map<String, ToolChainScannerInfo> perLanguageInfo;

	private transient Path savePath;
	private transient Map<ToolChainScannerInfo, ToolChainScannerInfo> infoCache;
	private transient Map<String, ToolChainScannerInfo> resourceCache;

	public void createCache() {
		infoCache = new HashMap<>();
		resourceCache = new HashMap<>();
		if (perResourceInfo != null) {
			for (ToolChainScannerInfo info : perResourceInfo) {
				infoCache.put(info, info);
				for (String path : info.getResourcePaths()) {
					resourceCache.put(path, info);
				}
			}
		}
	}

	private boolean perResource() {
		return perResourceInfo != null && !perResourceInfo.isEmpty();
	}

	private boolean perLanguage() {
		return perLanguageInfo != null && !perLanguageInfo.isEmpty();
	}

	public IScannerInfo getScannerInfo(IResource resource) {
		if (perResource()) {
			ToolChainScannerInfo info = resourceCache.get(resource.getFullPath().toString());
			if (info != null) {
				return info.getScannerInfo();
			}
		}

		// Else try language
		if (perLanguage()) {
			IProject project = resource.getProject();
			IContentType contentType = CCorePlugin.getContentType(project, resource.getName());
			if (contentType != null) {
				ILanguage language = LanguageManager.getInstance().getLanguage(contentType, project);
				ToolChainScannerInfo info = perLanguageInfo.get(language.getId());
				if (info != null) {
					return info.getScannerInfo();
				}
			}
		}

		return null;
	}

	public IScannerInfo getScannerInfo(ILanguage language) {
		if (perLanguage()) {
			ToolChainScannerInfo info = perLanguageInfo.get(language.getId());
			if (info != null) {
				return info.getScannerInfo();
			}
		}
		return null;
	}

	public void putScannerInfo(IResource resource, ToolChainScannerInfo info) {
		if (perResourceInfo == null) {
			perResourceInfo = new HashSet<>();
			infoCache = new HashMap<>();
			infoCache.put(info, info);
		} else {
			ToolChainScannerInfo existing = infoCache.get(info);
			if (existing != null) {
				info = existing;
			} else {
				perResourceInfo.add(info);
				infoCache.put(info, info);
			}
		}

		info.addResource(resource);
		resourceCache.put(resource.getFullPath().toString(), info);
		queueSave();
	}

	public void putScannerInfo(ILanguage language, IExtendedScannerInfo info) {
		if (perLanguageInfo == null) {
			perLanguageInfo = new HashMap<>();
		}
		perLanguageInfo.put(language.getId(), new ToolChainScannerInfo(info));
		queueSave();
	}

	public static ScannerInfoData load(CBuildConfiguration config) {
		IPath stateLoc = Activator.getDefault().getStateLocation();
		IPath scannerInfoPath = stateLoc.append(config.getProject().getName()).append(config.getName() + ".scInfo"); //$NON-NLS-1$
		File scannerInfoFile = scannerInfoPath.toFile();
		ScannerInfoData info = null;
		if (scannerInfoFile.canRead()) {
			try (Reader reader = new FileReader(scannerInfoFile)) {
				info = new Gson().fromJson(reader, ScannerInfoData.class);
			} catch (Exception e) {
				CCorePlugin.log(e);
			}
		}

		if (info == null) {
			info = new ScannerInfoData();
		}

		info.savePath = scannerInfoFile.toPath();
		info.createCache();
		return info;
	}

	public void save() {
		try {
			String json = new Gson().toJson(this);
			Files.createDirectories(savePath.getParent());
			Files.write(savePath, json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	}

	public void queueSave() {
		ScannerInfoSaveParticipant.getInstance().save(this);
	}

	public void clear() {
		perLanguageInfo = null;
		perResourceInfo = null;
		createCache();
		queueSave();
	}

}
