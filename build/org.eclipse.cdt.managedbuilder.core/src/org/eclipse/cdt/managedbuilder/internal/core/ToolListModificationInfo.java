/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

public class ToolListModificationInfo {
	private ToolInfo[] fResultingTools;
	private ToolInfo[] fAddedTools;
	private ToolInfo[] fRemovedTools;
	private IResourceInfo fRcInfo;

	ToolListModificationInfo(IResourceInfo rcInfo, ToolInfo[] resultingTools, ToolInfo[] added, ToolInfo[] removed,
			ToolInfo[] remaining) {
		fResultingTools = resultingTools;
		fRemovedTools = removed;
		fAddedTools = added;
		fRcInfo = rcInfo;
	}

	public IResourceInfo getResourceInfo() {
		return fRcInfo;
	}

	public List<ITool> getResultingToolList(List<ITool> list) {
		if (list == null)
			list = new ArrayList<>(fResultingTools.length);

		for (int i = 0; i < fResultingTools.length; i++) {
			list.add(fResultingTools[i].getResultingTool());
		}

		return list;
	}

	public ITool[] getResultingTools() {
		ITool[] tools = new ITool[fResultingTools.length];

		for (int i = 0; i < fResultingTools.length; i++) {
			tools[i] = fResultingTools[i].getResultingTool();
		}

		return tools;
	}

	public ITool[] getRemovedTools() {
		return toToolArray(fRemovedTools, true);
	}

	public ITool[] getAddedTools(boolean resulting) {
		return toToolArray(fAddedTools, !resulting);
	}

	public ITool[] getRemainedTools() {
		return toToolArray(fAddedTools, true);
	}

	private static ITool[] toToolArray(ToolInfo[] infos, boolean initialTools) {
		ITool[] tools = new ITool[infos.length];

		for (int i = 0; i < infos.length; i++) {
			tools[i] = initialTools ? infos[i].getInitialTool() : infos[i].getResultingTool();
		}

		return tools;
	}

	private static ITool[][] toToolArray(ToolInfo[][] infos, boolean initialTools) {
		ITool[][] tools = new ITool[infos.length][];

		for (int i = 0; i < infos.length; i++) {
			tools[i] = toToolArray(infos[i], initialTools);
		}

		return tools;
	}

	public MultiStatus getModificationStatus() {
		List<IModificationStatus> statusList = new ArrayList<>();

		ToolInfo[][] conflictInfos = calculateConflictingTools(fResultingTools);
		ITool[][] conflicting = toToolArray(conflictInfos, true);

		Map<String, String> unspecifiedRequiredProps = new HashMap<>();
		Map<String, String> unspecifiedProps = new HashMap<>();
		Set<String> undefinedSet = new HashSet<>();
		IConfiguration cfg = fRcInfo.getParent();
		ITool[] nonManagedTools = null;
		if (cfg.isManagedBuildOn() && cfg.supportsBuild(true)) {
			List<ITool> list = new ArrayList<>();
			for (int i = 0; i < fResultingTools.length; i++) {
				if (!fResultingTools[i].getInitialTool().supportsBuild(true)) {
					list.add(fResultingTools[i].getInitialTool());
				}
			}
			if (list.size() != 0) {
				nonManagedTools = list.toArray(new Tool[list.size()]);
			}
		}

		IModificationStatus status = new ModificationStatus(unspecifiedRequiredProps, unspecifiedProps, undefinedSet,
				conflicting, nonManagedTools);

		if (status.getSeverity() != IStatus.OK)
			statusList.add(status);

		for (int i = 0; i < fResultingTools.length; i++) {
			status = fResultingTools[i].getModificationStatus();
			if (status.getSeverity() != IStatus.OK)
				statusList.add(status);
		}

		if (statusList.size() != 0)
			return new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(), IStatus.INFO, "", null); //$NON-NLS-1$
		return new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "", null); //$NON-NLS-1$
	}

	private ToolInfo[][] calculateConflictingTools(ToolInfo[] infos) {
		infos = filterInfos(infos);

		return doCalculateConflictingTools(infos);
	}

	private ToolInfo[] filterInfos(ToolInfo[] infos) {
		if (fRcInfo instanceof FolderInfo) {
			Map<ITool, ToolInfo> map = createInitialToolToToolInfoMap(infos);
			ITool[] tools = new ArrayList<>(map.keySet()).toArray(new ITool[map.size()]);

			tools = ((FolderInfo) fRcInfo).filterTools(tools, fRcInfo.getParent().getManagedProject());

			if (tools.length < infos.length) {
				infos = new ToolInfo[tools.length];
				for (int i = 0; i < infos.length; i++) {
					infos[i] = map.get(tools[i]);
				}
			}
		}

		return infos;
	}

	private static Map<ITool, ToolInfo> createInitialToolToToolInfoMap(ToolInfo[] infos) {
		Map<ITool, ToolInfo> map = new LinkedHashMap<>();
		for (int i = 0; i < infos.length; i++) {
			map.put(infos[i].getInitialTool(), infos[i]);
		}

		return map;
	}

	private ToolInfo[][] doCalculateConflictingTools(ToolInfo[] infos) {
		HashSet<ToolInfo> set = new HashSet<>();
		set.addAll(Arrays.asList(infos));
		List<ToolInfo[]> result = new ArrayList<>();
		for (Iterator<ToolInfo> iter = set.iterator(); iter.hasNext();) {
			ToolInfo ti = iter.next();
			ITool t = ti.getInitialTool();
			iter.remove();
			@SuppressWarnings("unchecked")
			HashSet<ToolInfo> tmp = (HashSet<ToolInfo>) set.clone();
			List<ITool> list = new ArrayList<>();
			for (Iterator<ToolInfo> tmpIt = tmp.iterator(); tmpIt.hasNext();) {
				ToolInfo otherTi = tmpIt.next();
				ITool other = otherTi.getInitialTool();
				String conflicts[] = getConflictingInputExts(t, other);
				if (conflicts.length != 0) {
					list.add(other);
					tmpIt.remove();
				}
			}

			if (list.size() != 0) {
				list.add(t);
				ToolInfo[] arr = list.toArray(new ToolInfo[list.size()]);
				result.add(arr);
			}
			set = tmp;
			iter = set.iterator();
		}

		return result.toArray(new ToolInfo[result.size()][]);
	}

	private String[] getConflictingInputExts(ITool tool1, ITool tool2) {
		IProject project = fRcInfo.getParent().getOwner().getProject();
		String ext1[] = ((Tool) tool1).getAllInputExtensions(project);
		String ext2[] = ((Tool) tool2).getAllInputExtensions(project);
		Set<String> set1 = new HashSet<>(Arrays.asList(ext1));
		Set<String> result = new HashSet<>();
		for (int i = 0; i < ext2.length; i++) {
			if (set1.remove(ext2[i]))
				result.add(ext2[i]);
		}
		return result.toArray(new String[result.size()]);
	}

	public void apply() {
		((ResourceInfo) fRcInfo).doApply(this);
	}
}
