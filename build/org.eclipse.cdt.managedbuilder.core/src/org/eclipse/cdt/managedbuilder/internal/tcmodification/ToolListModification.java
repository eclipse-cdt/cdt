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
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceInfo;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChainModificationHelper;
import org.eclipse.cdt.managedbuilder.internal.core.ToolListModificationInfo;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatch;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatchSet;
import org.eclipse.cdt.managedbuilder.tcmodification.CompatibilityStatus;
import org.eclipse.cdt.managedbuilder.tcmodification.IModificationOperation;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolListModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolModification;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public abstract class ToolListModification implements IToolListModification {
	//	private Tool []fTools;
	private HashSet<String> fInputExtsSet = new HashSet<>();
	private ResourceInfo fRcInfo;
	private LinkedHashMap<Tool, IToolModification> fProjCompInfoMap = new LinkedHashMap<>();
	private HashMap<Tool, IToolModification> fSysCompInfoMap = new HashMap<>();
	private Tool[] fAllSysTools;
	private HashSet<ITool> fFilteredOutSysTools;
	//	private LinkedHashMap fRealToToolMap = new LinkedHashMap();
	//	private boolean fSysInfoMapInited;
	private PerTypeMapStorage<IRealBuildObjectAssociation, Set<IPath>> fCompleteObjectStorage;
	protected TreeMap<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> fCompletePathMapStorage;
	private HashSet<Tool> fAddCapableTools;
	private Map<Tool, Tool> fFilteredOutTools;

	private ToolListModificationInfo fModificationInfo;

	private class ModificationOperation implements IModificationOperation {
		private ITool fReplacement;
		private IToolModification fModification;

		ModificationOperation(IToolModification modification, ITool tool) {
			fModification = modification;
			fReplacement = tool;
		}

		@Override
		public ITool getReplacementTool() {
			return fReplacement;
		}

		@Override
		public IToolModification getToolModification() {
			return fModification;
		}
	}

	private class FilteredTool implements IToolModification {
		private ITool fTool;
		private boolean fIsProject;

		public FilteredTool(ITool tool, boolean isProject) {
			fTool = tool;
			fIsProject = isProject;
		}

		@Override
		public CompatibilityStatus getCompatibilityStatus() {
			return fIsProject ? CompatibilityStatus.OK_COMPATIBILITY_STATUS
					: new CompatibilityStatus(IStatus.ERROR,
							Messages.getString("ToolListModification.ToolIsIncompatible"), null); //$NON-NLS-1$
		}

		@Override
		public IModificationOperation[] getSupportedOperations() {
			return new IModificationOperation[0];
		}

		@Override
		public ITool getTool() {
			return fTool;
		}

		@Override
		public boolean isCompatible() {
			return fIsProject;
		}

		@Override
		public boolean isProjectTool() {
			return fIsProject;
		}

		@Override
		public IResourceInfo getResourceInfo() {
			return ToolListModification.this.getResourceInfo();
		}

	}

	public class ProjToolCompatibilityStatusInfo implements IToolModification {
		private ToolCompatibilityInfoElement fCurrentElement;
		private Map<Tool, ToolCompatibilityInfoElement> fCompatibleTools;
		private Map<Tool, ToolCompatibilityInfoElement> fInCompatibleTools;
		private IModificationOperation[] fOperations;
		private Tool fSelectedTool;
		private Tool fRealTool;
		private boolean fInited;
		private Set<Tool> fExtConflictTools;

		ProjToolCompatibilityStatusInfo(Tool tool) {
			fSelectedTool = tool;
			fRealTool = (Tool) ManagedBuildManager.getRealTool(tool);
		}

		private ToolCompatibilityInfoElement getCurrentElement() {
			checkInitCompatibleTools();
			return fCurrentElement;
		}

		@Override
		public CompatibilityStatus getCompatibilityStatus() {
			return getCurrentElement().getCompatibilityStatus();
		}

		@Override
		public boolean isCompatible() {
			return getCurrentElement().isCompatible();
		}

		@Override
		public boolean isProjectTool() {
			return true;
		}

		public Map<Tool, ToolCompatibilityInfoElement> getCompatibleTools() {
			checkInitCompatibleTools();
			return fCompatibleTools;
		}

		private void checkInitCompatibleTools() {
			if (fInited)
				return;

			if (DbgTcmUtil.DEBUG)
				DbgTcmUtil.println("calculating compatibility for tool " + fRealTool.getUniqueRealName()); //$NON-NLS-1$

			PerTypeMapStorage<? extends IRealBuildObjectAssociation, Set<IPath>> storage = getCompleteObjectStore();
			Tool tool = fRealTool;
			Set<IPath> rmSet = getToolApplicabilityPathSet(tool, true);
			@SuppressWarnings("unchecked")
			Map<Tool, Set<IPath>> toolMap = (Map<Tool, Set<IPath>>) storage
					.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, false);
			try {
				if (rmSet != null && rmSet.size() != 0)
					TcModificationUtil.removePaths(toolMap, tool, rmSet);

				if (DbgTcmUtil.DEBUG)
					DbgTcmUtil.dumpStorage(storage);

				@SuppressWarnings("unchecked")
				ConflictMatchSet conflicts = ToolChainModificationManager.getInstance().getConflictInfo(
						IRealBuildObjectAssociation.OBJECT_TOOL,
						(PerTypeMapStorage<IRealBuildObjectAssociation, Set<IPath>>) storage);

				fCompatibleTools = new HashMap<>();
				fInCompatibleTools = new HashMap<>();
				Tool sysTools[] = getTools(false, true);
				@SuppressWarnings("unchecked")
				Map<Tool, List<ConflictMatch>> conflictMap = (Map<Tool, List<ConflictMatch>>) conflicts.fObjToConflictListMap;
				for (int i = 0; i < sysTools.length; i++) {
					Tool t = sysTools[i];
					List<ConflictMatch> l = conflictMap.get(t);
					ToolCompatibilityInfoElement el = new ToolCompatibilityInfoElement(this, t, l);
					if (el.isCompatible()) {
						fCompatibleTools.put(t, el);
					} else {
						fInCompatibleTools.put(t, el);
					}
				}

				Tool t = fRealTool;
				List<ConflictMatch> l = conflictMap.get(t);
				fCurrentElement = new ToolCompatibilityInfoElement(this, t, l);
			} finally {
				if (rmSet != null && rmSet.size() != 0)
					TcModificationUtil.addPaths(toolMap, tool, rmSet);
			}
			fInited = true;
		}

		private Set<Tool> getConflictingTools() {
			if (fExtConflictTools == null) {
				Tool[] tmp = new Tool[1];
				tmp[0] = fSelectedTool;
				tmp = filterTools(tmp);
				if (tmp.length == 0)
					fExtConflictTools = Collections.emptySet();
				else
					fExtConflictTools = getExtensionConflictToolSet(fSelectedTool, filterTools(getTools(true, false)));
			}
			return fExtConflictTools;
		}

		@Override
		public IModificationOperation[] getSupportedOperations() {
			if (fOperations == null) {
				checkInitCompatibleTools();
				if (fFilteredOutTools.containsKey(fRealTool)) {
					if (canRemove(fRealTool)) {
						fOperations = new ModificationOperation[] { new ModificationOperation(this, null) };
					} else {
						fOperations = new ModificationOperation[0];
					}
				} else {
					List<ModificationOperation> opList = new ArrayList<>(fCompatibleTools.size() + 1);
					Set<Tool> keySet = fCompatibleTools.keySet();
					for (Tool tool : keySet) {
						if (tool == fRealTool)
							continue;

						if (canReplace(fRealTool, tool))
							opList.add(new ModificationOperation(this, tool));
					}

					if (fCompatibleTools.size() == 0 || canRemove(fRealTool)) {
						opList.add(new ModificationOperation(this, null));
					}

					fOperations = opList.toArray(new ModificationOperation[opList.size()]);
				}
			}
			return fOperations.clone();
		}

		@Override
		public ITool getTool() {
			return fSelectedTool;
		}

		public ITool getRealTool() {
			return fRealTool;
		}

		@Override
		public IResourceInfo getResourceInfo() {
			return fRcInfo;
		}

		public void clearCompatibilityInfo() {
			fInited = false;
			fCompatibleTools = null;
			fInCompatibleTools = null;
			fOperations = null;
			fCurrentElement = null;
			fExtConflictTools = null;
		}

	}

	protected Set<String> getInputExtsSet() {
		return fInputExtsSet;
	}

	public class SysToolCompatibilityStatusInfo implements IToolModification {
		private IModificationOperation[] fOperations;
		private Tool fSelectedTool;
		private Tool fRealTool;
		private CompatibilityStatus fStatus;

		SysToolCompatibilityStatusInfo(Tool tool) {
			fSelectedTool = tool;
			fRealTool = (Tool) ManagedBuildManager.getRealTool(tool);
		}

		//		private ToolCompatibilityInfoElement getCurrentElement(){
		//			if(fCurrentElement == null){
		//				checkInitCompatibleTools();
		//				ToolCompatibilityInfoElement info = (ToolCompatibilityInfoElement)fCompatibleTools.get(fRealTool);
		//				if(info == null)
		//					info = (ToolCompatibilityInfoElement)fInCompatibleTools.get(fRealTool);
		//				fCurrentElement = info;
		//			}
		//			return fCurrentElement;
		//		}

		@Override
		public CompatibilityStatus getCompatibilityStatus() {
			if (fStatus == null) {
				int severity;
				String message;
				if (!isCompatible()) {
					severity = IStatus.ERROR;
					message = Messages.getString("ToolListModification.ToolIsIncompatible"); //$NON-NLS-1$
				} else {
					severity = IStatus.OK;
					message = ""; //$NON-NLS-1$
				}
				fStatus = new CompatibilityStatus(severity, message, null);
			}
			return fStatus;
		}

		@Override
		public boolean isCompatible() {
			return getSupportedOperationsArray().length > 0;
		}

		@Override
		public boolean isProjectTool() {
			return false;
		}

		public IModificationOperation[] getSupportedOperationsArray() {
			if (fOperations == null) {
				Set<Tool> addCompatibleSysToolsSet = getAddCompatibleSysTools();
				if (addCompatibleSysToolsSet.contains(fRealTool) && canAdd(fRealTool)) {
					fOperations = new ModificationOperation[] { new ModificationOperation(this, null) };
				} else {
					Map<Tool, IToolModification> projMap = getMap(true);
					List<ModificationOperation> opList = new ArrayList<>(projMap.size());
					for (IToolModification tm : projMap.values()) {
						ProjToolCompatibilityStatusInfo info = (ProjToolCompatibilityStatusInfo) tm;
						if (info.getCompatibleTools().containsKey(fRealTool)
								&& !fFilteredOutTools.containsKey(info.fRealTool)
								&& canReplace(info.fSelectedTool, this.fSelectedTool)) {
							opList.add(new ModificationOperation(this, info.fSelectedTool));
						}
					}
					fOperations = opList.toArray(new ModificationOperation[opList.size()]);
				}
			}
			return fOperations;

		}

		@Override
		public IModificationOperation[] getSupportedOperations() {
			return getSupportedOperationsArray().clone();
		}

		@Override
		public ITool getTool() {
			return fSelectedTool;
		}

		public ITool getRealTool() {
			return fRealTool;
		}

		@Override
		public IResourceInfo getResourceInfo() {
			return fRcInfo;
		}

		public void clearCompatibilityInfo() {
			fOperations = null;
			fStatus = null;
		}
	}

	private Set<Tool> getAddCompatibleSysTools() {
		if (fAddCapableTools == null) {
			fAddCapableTools = new HashSet<>(Arrays.asList(getAllSysTools()));
			PerTypeMapStorage<IRealBuildObjectAssociation, Set<IPath>> storage = getCompleteObjectStore();
			ConflictMatchSet conflicts = ToolChainModificationManager.getInstance()
					.getConflictInfo(IRealBuildObjectAssociation.OBJECT_TOOL, storage);
			fAddCapableTools.removeAll(conflicts.fObjToConflictListMap.keySet());
		}
		return fAddCapableTools;
	}

	public ToolListModification(ResourceInfo rcInfo, ITool[] tools) {
		fRcInfo = rcInfo;
		clearToolInfo(tools);
	}

	public ToolListModification(ResourceInfo rcInfo, ToolListModification base) {
		fRcInfo = rcInfo;
		Tool[] initialTools = (Tool[]) rcInfo.getTools();
		@SuppressWarnings("unchecked")
		Map<Tool, Tool> initRealToToolMap = (Map<Tool, Tool>) TcModificationUtil.getRealToObjectsMap(initialTools,
				null);
		Tool[] updatedTools = base.getTools(true, false);
		@SuppressWarnings("unchecked")
		Map<Tool, Tool> updatedRealToToolMap = (Map<Tool, Tool>) TcModificationUtil.getRealToObjectsMap(updatedTools,
				null);
		Set<Entry<Tool, Tool>> entrySet = updatedRealToToolMap.entrySet();
		for (Entry<Tool, Tool> entry : entrySet) {
			Tool real = entry.getKey();
			Tool initial = initRealToToolMap.get(real);
			if (initial != null) {
				entry.setValue(initial);
			} else {
				Tool updated = entry.getValue();
				if (!updated.isExtensionBuildObject()) {
					updated = (Tool) updated.getExtensionObject();
					entry.setValue(updated);
				}
			}
		}

		updatedRealToToolMap.values().toArray(updatedTools);
		clearToolInfo(updatedTools);

		//		if(base.fInputExtsSet != null)
		//			fInputExtsSet = (HashSet)base.fInputExtsSet.clone();
		//		private LinkedHashMap fProjCompInfoMap = new LinkedHashMap();
		//		private HashMap fSysCompInfoMap = new HashMap();
		if (base.fAllSysTools != null)
			fAllSysTools = base.fAllSysTools.clone();

		if (base.fFilteredOutSysTools != null) {
			@SuppressWarnings("unchecked")
			HashSet<ITool> clone = (HashSet<ITool>) base.fFilteredOutSysTools.clone();
			fFilteredOutSysTools = clone;
		}

		if (base.fCompleteObjectStorage != null) {
			fCompleteObjectStorage = TcModificationUtil.cloneRealToolToPathSet(base.fCompleteObjectStorage);
		}

		if (base.fCompletePathMapStorage != null) {
			fCompletePathMapStorage = base.fCompletePathMapStorage;
		}
		//		if(base.fAddCapableTools)
		//			fAddCapableTools = (HashSet)base.fAddCapableTools.clone();
		//private Map fFilteredOutTools;

		//		private ToolListModificationInfo fModificationInfo;
	}

	@Override
	public IResourceInfo getResourceInfo() {
		return fRcInfo;
	}

	protected abstract boolean canRemove(ITool realTool);

	protected abstract boolean canReplace(Tool fromTool, Tool toTool);

	protected abstract boolean canAdd(Tool tool);

	protected abstract Set<IPath> getToolApplicabilityPathSet(Tool realTool, boolean isProject);

	protected abstract Set<Tool> getExtensionConflictToolSet(Tool tool, Tool[] toos);

	protected abstract Tool[] filterTools(Tool[] tools);

	public class ToolCompatibilityInfoElement {
		private Tool fRealTool;
		private List<ConflictMatch> fErrComflictMatchList;
		private CompatibilityStatus fStatus;
		private ProjToolCompatibilityStatusInfo fStatusInfo;

		ToolCompatibilityInfoElement(ProjToolCompatibilityStatusInfo statusInfo, Tool realTool,
				List<ConflictMatch> errConflictList) {
			fStatusInfo = statusInfo;
			fRealTool = realTool;
			if (errConflictList != null && errConflictList.size() != 0)
				fErrComflictMatchList = errConflictList;
		}

		public CompatibilityStatus getCompatibilityStatus() {
			if (fStatus == null) {
				int severity;
				String message;
				ConflictSet conflicts;
				if (!isCompatible()) {
					severity = IStatus.ERROR;
					message = Messages.getString("ToolListModification.ToolIsIncompatible"); //$NON-NLS-1$
					conflicts = new ConflictSet(fRealTool, fErrComflictMatchList, fStatusInfo.getConflictingTools());
				} else {
					severity = IStatus.OK;
					message = ""; //$NON-NLS-1$
					conflicts = null;
				}
				fStatus = new CompatibilityStatus(severity, message, conflicts);
			}
			return fStatus;
		}

		public boolean isCompatible() {
			return noCompatibilityConflicts()
					&& (fRealTool != fStatusInfo.getRealTool() || fStatusInfo.getConflictingTools().size() == 0);
		}

		public boolean noCompatibilityConflicts() {
			return fErrComflictMatchList == null;
		}
	}

	private Tool[] getAllSysTools() {
		if (fAllSysTools == null) {
			ITool[] allSys = ManagedBuildManager.getRealTools();
			fAllSysTools = filterTools((Tool[]) allSys);
			HashSet<ITool> set = new HashSet<>(Arrays.asList(allSys));
			set.removeAll(Arrays.asList(fAllSysTools));
			fFilteredOutSysTools = set;
		}
		return fAllSysTools;
	}

	@Override
	public final void apply() throws CoreException {
		TreeMap<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> initialMap = TcModificationUtil
				.createPathMap(fRcInfo.getParent());
		TreeMap<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> cur = getCompletePathMapStorage();
		TreeMap<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> result = TcModificationUtil
				.createResultingChangesMap(cur, initialMap);
		apply(result);
	}

	private void apply(TreeMap<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> resultingChangeMap)
			throws CoreException {
		//the order matters here: we first should process tool-chain than a builder and then tools
		int types[] = new int[] { IRealBuildObjectAssociation.OBJECT_TOOLCHAIN,
				IRealBuildObjectAssociation.OBJECT_BUILDER, IRealBuildObjectAssociation.OBJECT_TOOL, };

		int type;
		IConfiguration cfg = fRcInfo.getParent();
		Set<Entry<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>>> entrySet = resultingChangeMap.entrySet();
		for (Entry<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> entry : entrySet) {
			IPath path = entry.getKey();
			ResourceInfo rcInfo = (ResourceInfo) cfg.getResourceInfo(path, true);
			if (rcInfo == null) {
				rcInfo = (FolderInfo) cfg.createFolderInfo(path);
			}
			PerTypeSetStorage<IRealBuildObjectAssociation> storage = entry.getValue();
			for (int i = 0; i < types.length; i++) {
				type = types[i];
				Set<IRealBuildObjectAssociation> set = storage.getSet(type, false);
				if (set != null) {
					apply(rcInfo, type, set);
				}
			}
		}
	}

	private void apply(ResourceInfo rcInfo, int type, Set<? extends IRealBuildObjectAssociation> set)
			throws CoreException {
		switch (type) {
		case IRealBuildObjectAssociation.OBJECT_TOOL:
			ToolListModificationInfo info = rcInfo == fRcInfo ? getModificationInfo()
					: ToolChainModificationHelper.getModificationInfo(rcInfo, rcInfo.getTools(),
							set.toArray(new Tool[set.size()]));
			info.apply();
			break;
		case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
			if (rcInfo.isFolderInfo()) {
				if (set.size() != 0) {
					ToolChain tc = (ToolChain) set.iterator().next();
					try {
						((FolderInfo) rcInfo).changeToolChain(tc, CDataUtil.genId(tc.getId()), null);
					} catch (BuildException e) {
						throw new CoreException(new Status(IStatus.ERROR,
								ManagedBuilderCorePlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e));
					}
				}
			}
			break;
		case IRealBuildObjectAssociation.OBJECT_BUILDER:
			if (rcInfo.isRoot()) {
				if (set.size() != 0) {
					Builder b = (Builder) set.iterator().next();
					rcInfo.getParent().changeBuilder(b, CDataUtil.genId(b.getId()), null);
				}
			}
			break;
		}
	}

	private ToolListModificationInfo getModificationInfo() {
		if (fModificationInfo == null) {
			fModificationInfo = ToolChainModificationHelper.getModificationInfo(fRcInfo, fRcInfo.getTools(),
					getTools(true, false));
		}
		return fModificationInfo;
	}

	@Override
	public IToolModification[] getProjectToolModifications() {
		Map<Tool, IToolModification> map = getMap(true);
		return map.values().toArray(new ProjToolCompatibilityStatusInfo[map.size()]);
	}

	@Override
	public ITool[] getProjectTools() {
		return getTools(true, false);
	}

	@Override
	public IToolModification[] getSystemToolModifications() {
		Map<Tool, IToolModification> map = getMap(false);
		return map.values().toArray(new SysToolCompatibilityStatusInfo[map.size()]);
	}

	@Override
	public IToolModification getToolModification(ITool tool) {
		Tool rt = (Tool) ManagedBuildManager.getRealTool(tool);
		boolean isProj = isProjectTool(rt);
		Map<Tool, IToolModification> map = getMap(isProj);
		IToolModification m = map.get(rt);
		if (m == null) {
			ITool realTool = ManagedBuildManager.getRealTool(tool);
			boolean projFiltered = fFilteredOutTools.keySet().contains(realTool);
			m = new FilteredTool(tool, projFiltered);
		}
		return m;
	}

	//	private Map<Tool, IToolModification> getMap(Tool tool){
	//		return getMap(isProjectTool(tool));
	//	}

	private Map<Tool, IToolModification> getMap(boolean proj) {
		return proj ? fProjCompInfoMap : fSysCompInfoMap;
	}

	private Tool[] getTools(boolean proj, boolean real) {
		if (proj) {
			if (real)
				return fProjCompInfoMap.keySet().toArray(new Tool[fProjCompInfoMap.size()]);

			Tool[] tools = new Tool[fProjCompInfoMap.size()];
			int i = 0;
			Collection<IToolModification> infos = fProjCompInfoMap.values();
			for (IToolModification info : infos) {
				tools[i++] = ((ProjToolCompatibilityStatusInfo) info).fSelectedTool;
			}
			return tools;
		}

		return fSysCompInfoMap.keySet().toArray(new Tool[fSysCompInfoMap.size()]);
	}

	private boolean isProjectTool(Tool tool) {
		return fProjCompInfoMap.containsKey(tool.getRealBuildObject());
	}

	@Override
	public void changeProjectTools(ITool removeTool, ITool addTool) {
		Map<ITool, ITool> map = createRealToToolMap();
		Tool realAdded = (Tool) ManagedBuildManager.getRealTool(addTool);
		Tool realRemoved = (Tool) ManagedBuildManager.getRealTool(removeTool);
		boolean removed = realRemoved != null ? map.remove(realRemoved) != null : false;
		boolean added = realAdded != null ? map.put(realAdded, addTool) == null : false;
		if (!added && !removed)
			return;

		Set<IPath> rmSet = null;
		Set<IPath> addSet = null;
		if (removed) {
			rmSet = getToolApplicabilityPathSet(realRemoved, true);
		}
		if (added) {
			if (rmSet == null)
				addSet = getToolApplicabilityPathSet(realAdded, false);
			else
				addSet = rmSet;
		}

		List<ITool> list = new ArrayList<>();
		list.addAll(map.values());
		clearToolInfo(map.values().toArray(new Tool[map.size()]));

		PerTypeMapStorage<? extends IRealBuildObjectAssociation, Set<IPath>> storage = getCompleteObjectStore();
		@SuppressWarnings("unchecked")
		Map<Tool, Set<IPath>> toolMap = (Map<Tool, Set<IPath>>) storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL,
				true);
		if (rmSet != null)
			TcModificationUtil.removePaths(toolMap, realRemoved, rmSet);
		if (addSet != null)
			TcModificationUtil.addPaths(toolMap, realAdded, addSet);
	}

	private HashMap<ITool, ITool> createRealToToolMap(/*boolean includeFilteredOut*/) {
		HashMap<ITool, ITool> map = new HashMap<>();
		Set<Entry<Tool, IToolModification>> entries = fProjCompInfoMap.entrySet();
		for (Entry<Tool, IToolModification> entry : entries) {
			map.put(entry.getKey(), entry.getValue().getTool());
		}
		//		if(includeFilteredOut){
		//			for(Iterator iter = fFilteredOutTools.entrySet().iterator(); iter.hasNext(); ){
		//				Map.Entry entry = (Map.Entry)iter.next();
		//				map.put(entry.getKey(), entry.getValue());
		//			}
		//		}

		return map;
	}

	protected void clearToolCompatibilityInfo() {
		for (IToolModification info : fProjCompInfoMap.values()) {
			((ProjToolCompatibilityStatusInfo) info).clearCompatibilityInfo();
		}

		for (IToolModification info : fSysCompInfoMap.values()) {
			((SysToolCompatibilityStatusInfo) info).clearCompatibilityInfo();
		}
	}

	protected void clearToolInfo(ITool[] tools) {
		Tool[] filteredTools;
		Tool[] allTools;
		if (tools instanceof Tool[]) {
			allTools = (Tool[]) tools;
		} else {
			allTools = new Tool[tools.length];
			System.arraycopy(tools, 0, allTools, 0, tools.length);
		}

		filteredTools = filterTools(allTools);
		@SuppressWarnings("unchecked")
		Map<Tool, Tool> filteredMap = (Map<Tool, Tool>) TcModificationUtil.getRealToObjectsMap(filteredTools, null);
		@SuppressWarnings("unchecked")
		Map<Tool, Tool> allMap = (Map<Tool, Tool>) TcModificationUtil.getRealToObjectsMap(allTools, null);
		allMap.keySet().removeAll(filteredMap.keySet());
		fFilteredOutTools = allMap;
		//		tools = filteredTools;

		fModificationInfo = null;
		fInputExtsSet.clear();
		fProjCompInfoMap.clear();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			Tool realTool = (Tool) ManagedBuildManager.getRealTool(tool);
			fProjCompInfoMap.put(realTool, new ProjToolCompatibilityStatusInfo((Tool) tool));
			if (!fFilteredOutTools.containsKey(realTool))
				fInputExtsSet.addAll(Arrays.asList(tool.getPrimaryInputExtensions()));
		}
		fSysCompInfoMap.clear();
		Tool[] sysTools = getAllSysTools();
		for (int i = 0; i < sysTools.length; i++) {
			Tool sysTool = sysTools[i];
			if (!fProjCompInfoMap.containsKey(sysTool)) {
				fSysCompInfoMap.put(sysTool, new SysToolCompatibilityStatusInfo(sysTool));
			}
		}
		fAddCapableTools = null;
		fCompletePathMapStorage = null;
	}

	protected PerTypeMapStorage<IRealBuildObjectAssociation, Set<IPath>> getCompleteObjectStore() {
		if (fCompleteObjectStorage == null) {
			fCompleteObjectStorage = TcModificationUtil.createRealToolToPathSet(fRcInfo.getParent(), null, false);
			if (DbgTcmUtil.DEBUG) {
				DbgTcmUtil.println("dumping complete storage"); //$NON-NLS-1$
				DbgTcmUtil.dumpStorage(fCompleteObjectStorage);
			}
		}
		return fCompleteObjectStorage;
	}

	protected TreeMap<IPath, PerTypeSetStorage<IRealBuildObjectAssociation>> getCompletePathMapStorage() {
		if (fCompletePathMapStorage == null) {
			fCompletePathMapStorage = TcModificationUtil.createPathMap(getCompleteObjectStore());
		}
		return fCompletePathMapStorage;
	}

}
