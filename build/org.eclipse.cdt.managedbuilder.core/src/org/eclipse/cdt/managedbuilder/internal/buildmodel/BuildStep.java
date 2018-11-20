/*******************************************************************************
 * Copyright (c) 2006, 2016 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IFileContextData;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BuildStep implements IBuildStep {
	/**
	 * When an argument of a command is joined into a String for preparation for running with exec, the
	 * argument may need surrounding with quotes and have spaces between each argument. This padding allows
	 * for that when constructing long commands.
	 */
	private static final int PER_ARGUMENT_PADDING = 3;
	/**
	 * On Windows XP and above, the maximum command line length is 8191, on Linux it is at least 131072, but
	 * that includes the environment. We want to limit the invocation of a single command to this number of
	 * characters, and we want to ensure that the number isn't so low as to slow down operation.
	 *
	 * Doing each rm in its own command would be very slow, especially on Windows.
	 */
	private static final int MAX_CLEAN_LENGTH = 6000;

	private List<BuildIOType> fInputTypes = new ArrayList<>();
	private List<BuildIOType> fOutputTypes = new ArrayList<>();
	private ITool fTool;
	private BuildGroup fBuildGroup;
	private boolean fNeedsRebuild;
	private boolean fIsRemoved;
	private BuildDescription fBuildDescription;
	private IInputType fInputType;
	private ITool fLibTool;
	private boolean fAssignToCalculated;

	protected BuildStep(BuildDescription des, ITool tool, IInputType inputType) {
		fTool = tool;
		fInputType = inputType;
		fBuildDescription = des;

		if (DbgUtil.DEBUG)
			DbgUtil.trace("step " + DbgUtil.stepName(this) + " created"); //$NON-NLS-1$ //$NON-NLS-2$

		des.stepCreated(this);
	}

	@Override
	public IBuildIOType[] getInputIOTypes() {
		return fInputTypes.toArray(new BuildIOType[fInputTypes.size()]);
	}

	@Override
	public IBuildIOType[] getOutputIOTypes() {
		return fOutputTypes.toArray(new BuildIOType[fOutputTypes.size()]);
	}

	@Override
	public boolean needsRebuild() {
		if (fNeedsRebuild || (fTool != null && fTool.needsRebuild()) || (fLibTool != null && fLibTool.needsRebuild()))
			return true;

		if (fBuildGroup != null && fBuildGroup.needsRebuild())
			return true;

		return false;
	}

	public void setRebuildState(boolean rebuild) {
		fNeedsRebuild = rebuild;
	}

	public BuildResource[] removeIOType(BuildIOType type) {

		BuildResource rcs[] = type.remove();

		if (type.isInput())
			fInputTypes.remove(type);
		else
			fOutputTypes.remove(type);

		return rcs;
	}

	BuildResource[][] remove() {
		BuildResource[][] rcs = clear();

		if (DbgUtil.DEBUG)
			DbgUtil.trace("step  " + DbgUtil.stepName(this) + " removed"); //$NON-NLS-1$ //$NON-NLS-2$

		fBuildDescription.stepRemoved(this);
		fBuildDescription = null;

		return rcs;
	}

	BuildResource[][] clear() {
		BuildResource[][] rcs = new BuildResource[2][];

		rcs[0] = (BuildResource[]) getInputResources();
		rcs[1] = (BuildResource[]) getOutputResources();

		BuildIOType types[] = (BuildIOType[]) getInputIOTypes();
		for (int i = 0; i < types.length; i++) {
			removeIOType(types[i]);
		}

		types = (BuildIOType[]) getOutputIOTypes();
		for (int i = 0; i < types.length; i++) {
			removeIOType(types[i]);
		}

		return rcs;
	}

	public void removeResource(BuildIOType type, BuildResource rc, boolean rmTypeIfEmpty) {
		type.removeResource(rc);
		if (rmTypeIfEmpty && type.getResources().length == 0) {
			removeIOType(type);
		}
	}

	public BuildIOType createIOType(boolean input, boolean primary, /* String ext, */ IBuildObject ioType) {
		if (input) {
			if (fBuildDescription.getInputStep() == this)
				throw new IllegalArgumentException("input step can not have inputs"); //$NON-NLS-1$
		} else {
			if (fBuildDescription.getOutputStep() == this)
				throw new IllegalArgumentException("input step can not have outputs"); //$NON-NLS-1$
		}

		BuildIOType arg = new BuildIOType(this, input, primary, /* ext, */ ioType);
		if (input)
			fInputTypes.add(arg);
		else
			fOutputTypes.add(arg);

		return arg;
	}

	public void setTool(ITool tool) {
		fTool = tool;
	}

	public ITool getTool() {
		return fTool;
	}

	public BuildIOType[] getPrimaryTypes(boolean input) {
		List<BuildIOType> types = input ? fInputTypes : fOutputTypes;

		List<BuildIOType> list = new ArrayList<>();
		for (BuildIOType arg : types) {
			if (arg.isPrimary())
				list.add(arg);
		}
		return list.toArray(new BuildIOType[list.size()]);
	}

	public BuildIOType getIOTypeForType(IBuildObject ioType, boolean input) {
		List<BuildIOType> list = input ? fInputTypes : fOutputTypes;

		if (ioType != null) {
			for (BuildIOType arg : list) {
				if (arg.getIoType() == ioType)
					return arg;
			}
		} else {
			if (list.size() > 0)
				return list.get(0);
		}
		return null;
	}

	protected void setGroup(BuildGroup group) {
		fBuildGroup = group;
	}

	@Override
	public IBuildResource[] getInputResources() {
		return getResources(true);
	}

	@Override
	public IBuildResource[] getOutputResources() {
		return getResources(false);
	}

	public IBuildResource[] getResources(boolean input) {
		List<BuildIOType> list = input ? fInputTypes : fOutputTypes;
		Set<IBuildResource> set = new HashSet<>();

		for (BuildIOType arg : list) {
			IBuildResource rcs[] = arg.getResources();
			for (int j = 0; j < rcs.length; j++) {
				set.add(rcs[j]);
			}
		}
		return set.toArray(new BuildResource[set.size()]);
	}

	@Override
	public IBuildCommand[] getCommands(IPath cwd, Map inputArgValues, Map outputArgValues, boolean resolveAll) {
		if (cwd == null)
			cwd = calcCWD();

		if (fTool == null) {
			if (this == fBuildDescription.getCleanStep()) {

				String cleanCmd = fBuildDescription.getConfiguration().getCleanCommand();
				if (cleanCmd != null && (cleanCmd = cleanCmd.trim()).length() > 0) {
					List<IBuildCommand> list = new ArrayList<>();
					cleanCmd = resolveMacros(cleanCmd, resolveAll);
					String commands[] = cleanCmd.split(";"); //$NON-NLS-1$
					for (int i = 0; i < commands.length - 1; i++) {
						list.add(createCommandFromString(commands[0], cwd, getEnvironment()));
					}

					List<String> cleanCmdArgs = convertStringToArguments(commands[commands.length - 1]);
					final int initialLen = cleanCmdArgs.stream().mapToInt(w -> w.length() + PER_ARGUMENT_PADDING).sum();
					IPath cleanCmdPath = new Path(cleanCmdArgs.get(0));
					Map<String, String> env = getEnvironment();

					IBuildResource[] resources = fBuildDescription.getResources(true);

					List<String> args = new ArrayList<>();
					args.addAll(cleanCmdArgs.subList(1, cleanCmdArgs.size()));
					int totalLen = initialLen;
					for (IBuildResource resource : resources) {
						IPath resLoc = BuildDescriptionManager.getRelPath(cwd, resource.getLocation());
						String path = resLoc.toString();
						int pathLen = path.length() + PER_ARGUMENT_PADDING;

						if (totalLen + pathLen > MAX_CLEAN_LENGTH && totalLen != initialLen) {
							// adding new path takes us over limit, emit what we have...
							BuildCommand buildCommand = new BuildCommand(cleanCmdPath,
									args.toArray(new String[args.size()]), env, cwd, this);
							list.add(buildCommand);

							// ...and restart
							totalLen = initialLen;
							args.clear();
							args.addAll(cleanCmdArgs.subList(1, cleanCmdArgs.size()));
						}

						args.add(path);
						totalLen += pathLen;
					}

					// add remaining files
					BuildCommand buildCommand = new BuildCommand(cleanCmdPath, args.toArray(new String[args.size()]),
							env, cwd, this);
					list.add(buildCommand);

					return list.toArray(new BuildCommand[list.size()]);
				}

			} else {
				String step = null;
				if (this == fBuildDescription.getInputStep()) {
					step = fBuildDescription.getConfiguration().getPrebuildStep();
				} else if (this == fBuildDescription.getOutputStep()) {
					step = fBuildDescription.getConfiguration().getPostbuildStep();
				}

				if (step != null && (step = step.trim()).length() > 0) {
					step = resolveMacros(step, resolveAll);
					if (step != null && (step = step.trim()).length() > 0) {
						String commands[] = step.split(";"); //$NON-NLS-1$

						List<IBuildCommand> list = new ArrayList<>();
						for (int i = 0; i < commands.length; i++) {
							IBuildCommand cmds[] = createCommandsFromString(commands[i], cwd, getEnvironment());
							for (int j = 0; j < cmds.length; j++) {
								list.add(cmds[j]);
							}
						}
						return list.toArray(new BuildCommand[list.size()]);
					}
				}
			}
			return new IBuildCommand[0];
		}

		performAsignToOption(cwd);

		BuildResource inRc = getRcForMacros(true);
		BuildResource outRc = getRcForMacros(false);
		IPath inRcPath = inRc != null ? BuildDescriptionManager.getRelPath(cwd, inRc.getLocation()) : null;
		IPath outRcPath = outRc != null ? BuildDescriptionManager.getRelPath(cwd, outRc.getLocation()) : null;

		IManagedCommandLineGenerator gen = fTool.getCommandLineGenerator();
		FileContextData data = new FileContextData(inRcPath, outRcPath, null, fTool);
		String outPrefix = fTool.getOutputPrefix();
		outPrefix = resolveMacros(outPrefix, data, true);
		outRcPath = rmNamePrefix(outRcPath, outPrefix);

		IManagedCommandLineInfo info = gen.generateCommandLineInfo(fTool, fTool.getToolCommand(),
				getCommandFlags(inRcPath, outRcPath, resolveAll), fTool.getOutputFlag(), outPrefix,
				listToString(resourcesToStrings(cwd, getPrimaryResources(false), outPrefix), " "), //$NON-NLS-1$
				getInputResources(cwd, getPrimaryResources(true)), fTool.getCommandLinePattern());

		return createCommandsFromString(resolveMacros(info.getCommandLine(), data, true), cwd, getEnvironment());
	}

	private IPath rmNamePrefix(IPath path, String prefix) {
		if (prefix != null && prefix.length() != 0) {
			String name = path.lastSegment();
			if (name.startsWith(prefix)) {
				name = name.substring(prefix.length());
				path = path.removeLastSegments(1).append(name);
			}
		}
		return path;
	}

	private String[] getInputResources(IPath cwd, BuildResource[] rcs) {
		String[] resources = resourcesToStrings(cwd, rcs, null);

		// also need to get libraries
		String[] libs = null;
		IOption[] opts = fTool.getOptions();
		for (int i = 0; i < opts.length; ++i) {
			try {
				IOption opt = opts[i];
				if (opt.getValueType() == IOption.LIBRARIES) {
					String[] l = opts[i].getLibraries();
					if (l != null) {
						libs = new String[l.length];
						for (int j = 0; j < l.length; ++j) {
							libs[j] = opt.getCommand() + l[j];
						}
					}
				}
			} catch (BuildException e) {
			}
		}

		if (libs != null) {
			String[] irs = new String[resources.length + libs.length];
			System.arraycopy(resources, 0, irs, 0, resources.length);
			System.arraycopy(libs, 0, irs, resources.length, libs.length);
			return irs;
		} else {
			return resources;
		}
	}

	private IPath calcCWD() {
		IPath cwd = fBuildDescription.getDefaultBuildDirLocation();

		if (!cwd.isAbsolute())
			cwd = fBuildDescription.getConfiguration().getOwner().getProject().getLocation().append(cwd);

		return cwd;
	}

	protected Map<String, String> getEnvironment() {
		return fBuildDescription.getEnvironment();
	}

	protected IBuildCommand[] createCommandsFromString(String cmd, IPath cwd, Map<String, String> env) {
		IBuildCommand buildCommand = createCommandFromString(cmd, cwd, env);
		return new IBuildCommand[] { buildCommand };
	}

	protected IBuildCommand createCommandFromString(String cmd, IPath cwd, Map<String, String> env) {
		List<String> list = convertStringToArguments(cmd);

		IPath c = new Path(list.remove(0));
		String[] args = list.toArray(new String[list.size()]);

		BuildCommand buildCommand = new BuildCommand(c, args, env, cwd, this);
		return buildCommand;
	}

	/**
	 * Convert string to arguments, first argument is command
	 *
	 * @param commandLine
	 *            to parse
	 * @return arguments as a list
	 */
	protected List<String> convertStringToArguments(String commandLine) {
		char arr[] = commandLine.toCharArray();
		char expect = 0;
		char prev = 0;
		// int start = 0;
		List<String> list = new ArrayList<>();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			char ch = arr[i];
			switch (ch) {
			case '\'':
			case '"':
				if (expect == ch) {
					// String s = cmd.substring(start, i);
					// list.add(s);
					expect = 0;
					// start = i + 1;
				} else if (expect == 0) {
					// String s = cmd.substring(start, i);
					// list.add(s);
					expect = ch;
					// start = i + 1;
				} else {
					buf.append(ch);
				}
				break;
			case ' ':
				if (expect == 0) {
					if (prev != ' ') {
						list.add(buf.toString());
						buf.delete(0, buf.length());
					}
					// start = i + 1;
				} else {
					buf.append(ch);
				}
				break;
			default:
				buf.append(ch);
				break;

			}
			prev = ch;
		}

		if (buf.length() > 0)
			list.add(buf.toString());
		return list;
	}

	private BuildResource[] getPrimaryResources(boolean input) {
		BuildIOType[] types = getPrimaryTypes(input);
		if (types.length == 0)
			types = input ? (BuildIOType[]) getInputIOTypes() : (BuildIOType[]) getOutputIOTypes();
		List<BuildResource> list = new ArrayList<>();

		for (int i = 0; i < types.length; i++) {
			BuildResource[] rcs = (BuildResource[]) types[i].getResources();

			for (int j = 0; j < rcs.length; j++) {
				list.add(rcs[j]);
			}
		}

		return list.toArray(new BuildResource[list.size()]);
	}

	private String[] resourcesToStrings(IPath cwd, BuildResource rcs[], String prefixToRm) {
		List<String> list = new ArrayList<>(rcs.length);

		for (int i = 0; i < rcs.length; i++) {
			IPath path = BuildDescriptionManager.getRelPath(cwd, rcs[i].getLocation());
			path = rmNamePrefix(path, prefixToRm);
			list.add(path.toOSString());
		}
		return list.toArray(new String[list.size()]);
	}

	private String resolveMacros(String str, IFileContextData fileData, boolean resolveAll) {
		String result = str;
		try {
			if (resolveAll) {
				IConfiguration cfg = getBuildDescription().getConfiguration();
				IBuilder builder = cfg.getBuilder();
				SupplierBasedCdtVariableSubstitutor sub = createSubstitutor(cfg, builder, fileData);
				result = CdtVariableResolver.resolveToString(str, sub);
			} else {
				result = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(str, "", //$NON-NLS-1$
						" ", IBuildMacroProvider.CONTEXT_FILE, fileData); //$NON-NLS-1$
			}
		} catch (CdtVariableException e) {
		}

		return result;
	}

	private String resolveMacros(String str, boolean resolveAll) {
		String result = str;
		try {
			IConfiguration cfg = getBuildDescription().getConfiguration();
			if (resolveAll) {
				result = ManagedBuildManager.getBuildMacroProvider().resolveValue(str, "", " ", //$NON-NLS-1$ //$NON-NLS-2$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
			} else {
				result = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(str, "", //$NON-NLS-1$
						" ", IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg); //$NON-NLS-1$
			}
		} catch (CdtVariableException e) {
		}

		return result;
	}

	private SupplierBasedCdtVariableSubstitutor createSubstitutor(IConfiguration cfg, IBuilder builder,
			IFileContextData fileData) {
		BuildMacroProvider prov = (BuildMacroProvider) ManagedBuildManager.getBuildMacroProvider();
		IMacroContextInfo info = prov.getMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE, fileData);
		FileMacroExplicitSubstitutor sub = new FileMacroExplicitSubstitutor(info, cfg, builder, "", " "); //$NON-NLS-1$ //$NON-NLS-2$

		return sub;
	}

	private String[] getCommandFlags(IPath inRcPath, IPath outRcPath, boolean resolveAll) {
		try {
			if (resolveAll) {
				IConfiguration cfg = getBuildDescription().getConfiguration();
				IBuilder builder = cfg.getBuilder();
				return ((Tool) fTool).getToolCommandFlags(inRcPath, outRcPath,
						createSubstitutor(cfg, builder, new FileContextData(inRcPath, outRcPath, null, fTool)),
						BuildMacroProvider.getDefault());
			}
			return fTool.getToolCommandFlags(inRcPath, outRcPath);
		} catch (BuildException e) {
		}
		return new String[0];
	}

	private String listToString(String[] list, String delimiter) {
		if (list == null || list.length == 0)
			return ""; //$NON-NLS-1$

		StringBuilder buf = new StringBuilder(list[0]);

		for (int i = 1; i < list.length; i++) {
			buf.append(delimiter).append(list[i]);
		}

		return buf.toString();
	}

	private BuildResource getRcForMacros(boolean input) {
		IBuildIOType types[] = getPrimaryTypes(input);
		if (types.length != 0) {
			for (int i = 0; i < types.length; i++) {
				IBuildResource rcs[] = types[i].getResources();
				if (rcs.length != 0)
					return (BuildResource) rcs[0];
			}
		}

		types = input ? getInputIOTypes() : getOutputIOTypes();
		if (types.length != 0) {
			for (int i = 0; i < types.length; i++) {
				IBuildResource rcs[] = types[i].getResources();
				if (rcs.length != 0)
					return (BuildResource) rcs[0];
			}
		}

		return null;
	}

	@Override
	public boolean isRemoved() {
		return fIsRemoved;
	}

	public void setRemoved() {
		fIsRemoved = true;
		fNeedsRebuild = false;
	}

	@Override
	public IBuildDescription getBuildDescription() {
		return fBuildDescription;
	}

	boolean isMultiAction() {
		BuildIOType args[] = getPrimaryTypes(true);
		BuildIOType arg = args.length > 0 ? args[0] : null;

		if (arg != null) {
			if (arg.getIoType() != null)
				return ((IInputType) arg.getIoType()).getMultipleOfType();
			return fTool != null
					&& fTool == ((Configuration) fBuildDescription.getConfiguration()).calculateTargetTool();
		}
		return false;
	}

	public IInputType getInputType() {
		return fInputType;
	}

	public void setLibTool(ITool libTool) {
		fLibTool = libTool;
	}

	public ITool getLibTool() {
		return fLibTool;
	}

	protected void performAsignToOption(IPath cwd) {
		if (fTool == null && !fAssignToCalculated)
			return;

		fAssignToCalculated = true;

		IConfiguration cfg = fBuildDescription.getConfiguration();

		for (BuildIOType bType : fInputTypes) {
			IInputType type = (IInputType) bType.getIoType();

			if (type == null)
				continue;

			IOption option = fTool.getOptionBySuperClassId(type.getOptionId());
			IOption assignToOption = fTool.getOptionBySuperClassId(type.getAssignToOptionId());
			if (assignToOption != null && option == null) {
				try {
					BuildResource bRcs[] = (BuildResource[]) bType.getResources();
					int optType = assignToOption.getValueType();
					if (optType == IOption.STRING) {
						String optVal = ""; //$NON-NLS-1$
						for (int j = 0; j < bRcs.length; j++) {
							if (j != 0) {
								optVal += " "; //$NON-NLS-1$
							}
							optVal += BuildDescriptionManager.getRelPath(cwd, bRcs[j].getLocation()).toOSString();
						}
						ManagedBuildManager.setOption(cfg, fTool, assignToOption, optVal);
					} else if (optType == IOption.STRING_LIST || optType == IOption.LIBRARIES
							|| optType == IOption.OBJECTS || optType == IOption.INCLUDE_PATH
							|| optType == IOption.PREPROCESSOR_SYMBOLS || optType == IOption.INCLUDE_FILES
							|| optType == IOption.LIBRARY_PATHS || optType == IOption.LIBRARY_FILES
							|| optType == IOption.MACRO_FILES || optType == IOption.UNDEF_INCLUDE_PATH
							|| optType == IOption.UNDEF_PREPROCESSOR_SYMBOLS || optType == IOption.UNDEF_INCLUDE_FILES
							|| optType == IOption.UNDEF_LIBRARY_PATHS || optType == IOption.UNDEF_LIBRARY_FILES
							|| optType == IOption.UNDEF_MACRO_FILES) {
						// Mote that when using the enumerated inputs, the path(s) must be translated from
						// project relative
						// to top build directory relative
						String[] paths = new String[bRcs.length];
						for (int j = 0; j < bRcs.length; j++) {
							paths[j] = BuildDescriptionManager.getRelPath(cwd, bRcs[j].getLocation()).toOSString();
						}
						ManagedBuildManager.setOption(cfg, fTool, assignToOption, paths);
					} else if (optType == IOption.BOOLEAN) {
						if (bRcs.length > 0) {
							ManagedBuildManager.setOption(cfg, fTool, assignToOption, true);
						} else {
							ManagedBuildManager.setOption(cfg, fTool, assignToOption, false);
						}
					} else if (optType == IOption.ENUMERATED || optType == IOption.TREE) {
						if (bRcs.length > 0) {
							ManagedBuildManager.setOption(cfg, fTool, assignToOption,
									BuildDescriptionManager.getRelPath(cwd, bRcs[0].getLocation()).toOSString());
						}
					}
				} catch (BuildException ex) {
				}
			}
		}
	}
}
