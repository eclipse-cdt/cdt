/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator.ReferenceSettingsInfo;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables;
import org.eclipse.cdt.internal.core.cdtvariables.UserDefinedVariableSupplier;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.BuilderFactory;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class ProjectConverter implements ICProjectConverter {
	private final static String OLD_MAKE_BUILDER_ID = "org.eclipse.cdt.make.core.makeBuilder";	//$NON-NLS-1$
	private final static String OLD_MAKE_NATURE_ID = "org.eclipse.cdt.make.core.makeNature";	//$NON-NLS-1$
	private final static String OLD_MNG_BUILDER_ID = "org.eclipse.cdt.managedbuilder.core.genmakebuilder";	//$NON-NLS-1$
	private final static String OLD_MNG_NATURE_ID = "org.eclipse.cdt.managedbuilder.core.managedBuildNature";	//$NON-NLS-1$
	private final static String OLD_DISCOVERY_MODULE_ID = "scannerConfiguration";	//$NON-NLS-1$
	private final static String OLD_BINARY_PARSER_ID = "org.eclipse.cdt.core.BinaryParser";	//$NON-NLS-1$
	private final static String OLD_ERROR_PARSER_ID = "org.eclipse.cdt.core.ErrorParser";	//$NON-NLS-1$
	private final static String OLD_PATH_ENTRY_ID = "org.eclipse.cdt.core.pathentry"; //$NON-NLS-1$
	private final static String OLD_DISCOVERY_NATURE_ID = "org.eclipse.cdt.make.core.ScannerConfigNature"; //$NON-NLS-1$
	private final static String OLD_DISCOVERY_BUILDER_ID = "org.eclipse.cdt.make.core.ScannerConfigBuilder"; //$NON-NLS-1$
	private final static String OLD_MAKE_TARGET_BUIDER_ID = "org.eclipse.cdt.make.MakeTargetBuilder"; //$NON-NLS-1$
	private final static String NEW_MAKE_TARGET_BUIDER_ID = "org.eclipse.cdt.build.MakeTargetBuilder"; //$NON-NLS-1$

	private static ResourcePropertyHolder PROPS = new ResourcePropertyHolder(true);
	
	private static String CONVERSION_FAILED_MSG_ID = "conversionFailed"; //$NON-NLS-1$
	
	public boolean canConvertProject(IProject project, String oldOwnerId, ICProjectDescription oldDes) {
		try {
			if(oldOwnerId == null || oldDes == null)
				return false;
		
			IProjectDescription eDes = project.getDescription();
			Set<String> natureSet = new HashSet<String>(Arrays.asList(eDes.getNatureIds()));
			if(natureSet.contains(OLD_MAKE_NATURE_ID))
				return true;
			
			if(natureSet.contains(OLD_MNG_NATURE_ID))
				return true;
			
		} catch (CoreException e) {
		}
		
		return false;
//		return ManagedBuildManager.canGetBuildInfo(project);
	}

	public ICProjectDescription convertProject(IProject project, IProjectDescription eDes, String oldOwnerId, ICProjectDescription oldDes)
			throws CoreException {
		Set<String> natureSet = new HashSet<String>(Arrays.asList(eDes.getNatureIds()));
		CoreModel model = CoreModel.getDefault();
		ICProjectDescription newDes = null;
		IManagedBuildInfo info = null;
		String[] binErrParserIds = null;
//		boolean convertMakeTargetInfo = false;

		if(natureSet.contains(OLD_MAKE_NATURE_ID)){
			newDes = oldDes;
			ICConfigurationDescription des = newDes.getConfigurations()[0];
			ICConfigExtensionReference refs[] = des.get(OLD_BINARY_PARSER_ID);
			if(refs.length != 0){
				binErrParserIds = new String[refs.length];
				for(int i = 0; i < refs.length; i++){
					binErrParserIds[i] = refs[i].getID();
				}
			}
			info = ManagedBuildManager.createBuildInfo(project);
			ManagedProject mProj = new ManagedProject(newDes);
			info.setManagedProject(mProj);

			Configuration cfg = ConfigurationDataProvider.getClearPreference(des.getId());
			cfg.applyToManagedProject(mProj);
			cfg.setConfigurationDescription(des);

			des.setConfigurationData(ManagedBuildManager.CFG_DATA_PROVIDER_ID, cfg.getConfigurationData());
		} else if(natureSet.contains(OLD_MNG_NATURE_ID)){
			try {
				if(PROPS.getProperty(project, CONVERSION_FAILED_MSG_ID) != null)
					throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), DataProviderMessages.getString("ProjectConverter.0"))); //$NON-NLS-1$

				newDes = model.createProjectDescription(project, false);
				info = convertManagedBuildInfo(project, newDes);
			} catch (CoreException e) {
				displayInfo(project, CONVERSION_FAILED_MSG_ID, DataProviderMessages.getString("ProjectConverter.10"), DataProviderMessages.getFormattedString("ProjectConverter.11", new String[]{project.getName(), e.getLocalizedMessage()})); //$NON-NLS-1$ //$NON-NLS-2$
				throw e;
			}
		} 

		if(newDes == null || !newDes.isValid() || newDes.getConfigurations().length == 0){
			newDes = null;
		} else {
			boolean changeEDes = false;
			if(natureSet.remove(OLD_MAKE_NATURE_ID))
				changeEDes = true;
			if(natureSet.remove(OLD_DISCOVERY_NATURE_ID))
				changeEDes = true;
				
			if(changeEDes)
				eDes.setNatureIds(natureSet.toArray(new String[natureSet.size()]));
			
			changeEDes = false;
			ICommand[] cmds = eDes.getBuildSpec();
			List<ICommand> list = new ArrayList<ICommand>(Arrays.asList(cmds));
			ICommand makeBuilderCmd = null;
			for(Iterator<ICommand> iter = list.iterator(); iter.hasNext();){
				ICommand cmd = iter.next();
				if(OLD_MAKE_BUILDER_ID.equals(cmd.getBuilderName())){
					makeBuilderCmd = cmd;
					iter.remove();
					changeEDes = true;
//					convertMakeTargetInfo = true;
				} else if(OLD_DISCOVERY_BUILDER_ID.equals(cmd.getBuilderName())){
					iter.remove();
					changeEDes = true;
				}
			}
			
			ICConfigurationDescription cfgDess[] = newDes.getConfigurations();
			for(int i = 0; i < cfgDess.length; i++){
				ICConfigurationDescription cfgDes = cfgDess[i];
				BuildConfigurationData data = (BuildConfigurationData)cfgDes.getConfigurationData();
				IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

				if(makeBuilderCmd != null)
					loadBuilderSettings(cfg, makeBuilderCmd);
				
//				loadDiscoveryOptions(cfgDes, cfg);
				
				loadPathEntryInfo(project, cfgDes, data);
				
				if(binErrParserIds != null){
					data.getTargetPlatformData().setBinaryParserIds(binErrParserIds);
					cfgDes.get(OLD_BINARY_PARSER_ID);
//					ICConfigExtensionReference refs[] = cfgDes.get(OLD_BINARY_PARSER_ID);
//					String ids[] = idsFromRefs(refs);
//					data.getTargetPlatformData().setBinaryParserIds(ids);
//					
//					refs = cfgDes.get(OLD_ERROR_PARSER_ID);
//					ids = idsFromRefs(refs);
//					data.getBuildData().setErrorParserIDs(ids);
				}
				
				try {
					ConfigurationDataProvider.writeConfiguration(cfgDes, data);
				} catch (CoreException e){
				}
			}
			
//			if(convertMakeTargetInfo){
//				try {
//					convertMakeTargetInfo(project, newDes, null);
//				} catch (CoreException e){
//					ManagedBuilderCorePlugin.log(e);
//				}
//			}
			
			if(changeEDes){
				cmds = list.toArray(new ICommand[list.size()]);
				eDes.setBuildSpec(cmds);
			}
			
			info.setValid(true);
			
			
			try {
				ManagedBuildManager.setLoaddedBuildInfo(project, info);
			} catch (Exception e) {
			}
		}

		return newDes;
	}
	
	static void displayInfo(IProject proj, String id, String title, String message){
		if(PROPS.getProperty(proj, id) == null){
			openInformation(proj, id, title, message, false);
			PROPS.setProperty(proj, id, Boolean.TRUE);
		}
	}
	
	public static boolean getBooleanFromQueryAnswer(String answer){
		if(IOverwriteQuery.ALL.equalsIgnoreCase(answer) ||
				IOverwriteQuery.YES.equalsIgnoreCase(answer))
			return true;
		return false;
	}
	
	static public boolean openQuestion(final IResource rc, final String id, final String title, final String message, IOverwriteQuery query, final boolean multiple){
		if(query != null)
			return getBooleanFromQueryAnswer(query.queryOverwrite(message));

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window == null){
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			window = windows[0];
		}

		final Shell shell = window.getShell();
		final boolean [] answer = new boolean[1];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				Object ob = PROPS.getProperty(rc, id);
				if(multiple || ob == null){
					PROPS.setProperty(rc, id, Boolean.TRUE);
					answer[0] = MessageDialog.openQuestion(shell,title,message);
					PROPS.setProperty(rc, id, answer[0] ? Boolean.TRUE : Boolean.FALSE);
				} else {
					answer[0] = ((Boolean)ob).booleanValue();
				}
			}
		});	
		return answer[0];
	}
	
	static private void openInformation(final IResource rc, final String id, final String title, final String message, final boolean multiple){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window == null){
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			window = windows[0];
		}

		final Shell shell = window.getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				if(multiple || PROPS.getProperty(rc, id) == null){
					PROPS.setProperty(rc, id, Boolean.TRUE);
					MessageDialog.openInformation(shell,title,message);
				}
			}
		});	
	}
	
	private static void convertMakeTargetInfo(final IProject project, ICProjectDescription des, IProgressMonitor monitor) throws CoreException{
		if(monitor == null)
			monitor = new NullProgressMonitor();
		
		CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(project, des, new ICDescriptorOperation(){

			public void execute(ICDescriptor descriptor,
					IProgressMonitor monitor) throws CoreException {
				final IMakeTargetManager mngr = MakeCorePlugin.getDefault().getTargetManager();
				
				project.accept(new IResourceVisitor(){

					public boolean visit(IResource resource)
							throws CoreException {
						if(resource.getType() == IResource.FILE)
							return false;
						
						try {
							IContainer cr = (IContainer)resource;
							IMakeTarget targets[] = mngr.getTargets(cr);
							for(int i = 0; i < targets.length; i++){
								IMakeTarget t = targets[i];
								if(!OLD_MAKE_TARGET_BUIDER_ID.equals(t.getTargetBuilderID()))
									continue;
								
								IMakeTarget newT = mngr.createTarget(project, t.getName(), NEW_MAKE_TARGET_BUIDER_ID);
								copySettings(t, newT);
								mngr.removeTarget(t);
								mngr.addTarget(cr, newT);
							}
						} catch ( CoreException e){
							ManagedBuilderCorePlugin.log(e);
						}
						return true;
					}
					
				});
			}
			
		}, monitor);
	}
	
	private static void copySettings(IMakeTarget fromTarget, IMakeTarget toTarget) throws CoreException{
			toTarget.setAppendEnvironment(fromTarget.appendEnvironment());
			toTarget.setAppendProjectEnvironment(fromTarget.appendProjectEnvironment());

			toTarget.setBuildAttribute(IMakeTarget.BUILD_LOCATION, fromTarget.getBuildAttribute(IMakeTarget.BUILD_LOCATION, null));
			toTarget.setBuildAttribute(IMakeTarget.BUILD_COMMAND, fromTarget.getBuildAttribute(IMakeTarget.BUILD_COMMAND, null));
			toTarget.setBuildAttribute(IMakeTarget.BUILD_ARGUMENTS, fromTarget.getBuildAttribute(IMakeTarget.BUILD_ARGUMENTS, null));
			toTarget.setBuildAttribute(IMakeTarget.BUILD_TARGET, fromTarget.getBuildAttribute(IMakeTarget.BUILD_TARGET, null));
			
			Map<String, String> fromMap = fromTarget.getEnvironment();
			if(fromMap != null)
				toTarget.setEnvironment(new HashMap<String, String>(fromMap));
			
//			toTarget.setErrorParsers(fromTarget.getErrorParsers());
			
			toTarget.setRunAllBuilders(fromTarget.runAllBuilders());
			
			toTarget.setStopOnError(fromTarget.isStopOnError());
			
			toTarget.setUseDefaultBuildCmd(fromTarget.isDefaultBuildCmd());
			
			toTarget.setContainer(fromTarget.getContainer());

	}
	
	private void loadPathEntryInfo(IProject project, ICConfigurationDescription des, CConfigurationData data){
		try {
			ICStorageElement el = des.getStorage(OLD_PATH_ENTRY_ID, false);
			if(el != null){
				IPathEntry[] entries = PathEntryTranslator.decodePathEntries(project, el);
				if(entries.length != 0){
					List<IPathEntry> list = new ArrayList<IPathEntry>(Arrays.asList(entries));
					for(Iterator<IPathEntry> iter = list.iterator(); iter.hasNext();){
						IPathEntry entry = iter.next();
						if(entry.getEntryKind() == IPathEntry.CDT_CONTAINER){
							iter.remove();
							continue;
						}
					}
					
					if(list.size() != 0){
						PathEntryTranslator tr = new PathEntryTranslator(project, data);
						entries = list.toArray(new IPathEntry[list.size()]);
						ReferenceSettingsInfo refInfo = tr.applyPathEntries(entries, null, PathEntryTranslator.OP_REPLACE);
						ICExternalSetting extSettings[] = refInfo.getExternalSettings();
						des.removeExternalSettings();
						if(extSettings.length != 0){
							ICExternalSetting setting;
							for(int i = 0; i < extSettings.length; i++){
								setting = extSettings[i];
								des.createExternalSetting(setting.getCompatibleLanguageIds(), 
										setting.getCompatibleContentTypeIds(), 
										setting.getCompatibleExtensions(), 
										setting.getEntries());
							}
						}

						IPath projPaths[] = refInfo.getReferencedProjectsPaths();
						if(projPaths.length != 0){
							Map<String, String> map = new HashMap<String, String>(projPaths.length);
							for(int i = 0; i < projPaths.length; i++){
								map.put(projPaths[i].segment(0), "");	//$NON-NLS-1$
							}
							des.setReferenceInfo(map);
						}
					}
				}
				des.removeStorage(OLD_PATH_ENTRY_ID);
			}
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}
	
//	private String[] idsFromRefs(ICConfigExtensionReference refs[]){
//		String ids[] = new String[refs.length];
//		for(int i = 0; i < ids.length; i++){
//			ids[i] = refs[i].getID();
//		}
//		return ids;
//	}
	
//	private void loadDiscoveryOptions(ICConfigurationDescription des, IConfiguration cfg){
//		try {
//			ICStorageElement discoveryStorage = des.getStorage(OLD_DISCOVERY_MODULE_ID, false);
//			if(discoveryStorage != null){
//				Configuration config = (Configuration)cfg;
//				IScannerConfigBuilderInfo2 scannerConfigInfo = ScannerConfigInfoFactory2.create(new CfgInfoContext(cfg), discoveryStorage, ScannerConfigProfileManager.NULL_PROFILE_ID);
//				config.setPerRcTypeDiscovery(false);
//				config.setScannerConfigInfo(scannerConfigInfo);
//				des.removeStorage(OLD_DISCOVERY_MODULE_ID);
//			}
//		} catch (CoreException e) {
//			ManagedBuilderCorePlugin.log(e);
//		}
//		
//		
//	}
	
	private void loadBuilderSettings(IConfiguration cfg, ICommand cmd){
		Builder builder = (Builder)BuilderFactory.createBuilderFromCommand(cfg, cmd);
		if(builder.getCommand() != null && builder.getCommand().length() != 0){
			String[] errParserIds = builder.getCustomizedErrorParserIds();
			builder.setCustomizedErrorParserIds(null);
			((ToolChain)cfg.getToolChain()).setBuilder(builder);
			if(errParserIds != null && errParserIds.length != 0){
				cfg.setErrorParserList(errParserIds);
			}
		}
	}
	
	private static boolean convertOldStdMakeToNewStyle(final IProject project, boolean checkOnly, IProgressMonitor monitor, boolean throwExceptions) throws CoreException {
		try {
//			ICDescriptor dr = CCorePlugin.getDefault().getCProjectDescription(project, false);
//			if(dr == null){
//				if(throwExceptions)
//					throw new CoreException(new Status(IStatus.ERROR,
//							ManagedBuilderCorePlugin.getUniqueIdentifier(),
//							DataProviderMessages.getString("ProjectConverter.0"))); //$NON-NLS-1$
//				return false;
//			}
//
//			if(!MakeCorePlugin.MAKE_PROJECT_ID.equals(dr.getProjectOwner().getID())){
//				if(throwExceptions)
//					throw new CoreException(new Status(IStatus.ERROR,
//							ManagedBuilderCorePlugin.getUniqueIdentifier(),
//							DataProviderMessages.getString("ProjectConverter.1") + dr.getProjectOwner().getID())); //$NON-NLS-1$
//				return false;
//			}
			
			ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
			
			if(des == null){
				if(throwExceptions)
					throw new CoreException(new Status(IStatus.ERROR,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							DataProviderMessages.getString("ProjectConverter.9")));  //$NON-NLS-1$
				return false;
			}

			ICConfigurationDescription cfgs[] = des.getConfigurations();
			if(cfgs.length != 1){
				if(throwExceptions)
					throw new CoreException(new Status(IStatus.ERROR,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							DataProviderMessages.getString("ProjectConverter.2") + cfgs.length)); //$NON-NLS-1$
				return false;
			}
			
			if(!CCorePlugin.DEFAULT_PROVIDER_ID.equals(cfgs[0].getBuildSystemId())){
				if(throwExceptions)
					throw new CoreException(new Status(IStatus.ERROR,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							DataProviderMessages.getString("ProjectConverter.3") + cfgs.length)); //$NON-NLS-1$
				return false;
			}
			
			final IProjectDescription eDes = project.getDescription();
			String natureIds[] = eDes.getNatureIds();
			Set<String> set = new HashSet<String>(Arrays.asList(natureIds));
			if(!set.contains(OLD_MAKE_NATURE_ID)){
				if(throwExceptions)
					throw new CoreException(new Status(IStatus.ERROR,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							DataProviderMessages.getString("ProjectConverter.4") + natureIds.toString())); //$NON-NLS-1$
				return false;
			}
			
			if(!checkOnly){
				ProjectConverter instance = new ProjectConverter();
				ICProjectDescription oldDes = CCorePlugin.getDefault().getProjectDescription(project);
				if(!instance.canConvertProject(project, MakeCorePlugin.MAKE_PROJECT_ID, oldDes)){
					if(throwExceptions)
						throw new CoreException(new Status(IStatus.ERROR,
								ManagedBuilderCorePlugin.getUniqueIdentifier(),
								DataProviderMessages.getString("ProjectConverter.5"))); //$NON-NLS-1$
					return false;
				}
				
				final ICProjectDescription newDes = instance.convertProject(project, eDes, MakeCorePlugin.MAKE_PROJECT_ID, oldDes);
				if(newDes == null){
					if(throwExceptions)
						throw new CoreException(new Status(IStatus.ERROR,
								ManagedBuilderCorePlugin.getUniqueIdentifier(),
								DataProviderMessages.getString("ProjectConverter.6"))); //$NON-NLS-1$
					return false;
				}
				
				final IWorkspace wsp = ResourcesPlugin.getWorkspace();
				wsp.run(new IWorkspaceRunnable(){

					public void run(IProgressMonitor monitor)
							throws CoreException {
						project.setDescription(eDes, monitor);
						CCorePlugin.getDefault().setProjectDescription(project, newDes);
						Job job = new Job(DataProviderMessages.getString("ProjectConverter.7")){ //$NON-NLS-1$

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								try {
									ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project);
									convertMakeTargetInfo(project, des, monitor);
									CCorePlugin.getDefault().setProjectDescription(project, des);
								} catch (CoreException e) {
									return e.getStatus();
								}
								return Status.OK_STATUS;
							}
							
						};
						
						job.setRule(wsp.getRoot());
						job.schedule();
					}
					
				}, wsp.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
			}
			return true;
		} catch (CoreException e) {
			if(throwExceptions)
				throw e;
			ManagedBuilderCorePlugin.log(e);
		}
		if(throwExceptions)
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					DataProviderMessages.getString("ProjectConverter.8"))); //$NON-NLS-1$
		return false;
	}
	
	public static boolean isOldStyleMakeProject(IProject project){
		try {
			return convertOldStdMakeToNewStyle(project, true, null, false);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		return false;
	}

	public static void convertOldStdMakeToNewStyle(IProject project, IProgressMonitor monitor) throws CoreException{
		convertOldStdMakeToNewStyle(project, false, monitor, true);
	}

	private IManagedBuildInfo convertManagedBuildInfo(IProject project, ICProjectDescription newDes) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getOldStyleBuildInfo(project);
		
		synchronized(PROPS){
			if(info != null && info.isValid()){
				IManagedProject mProj = info.getManagedProject();
				IConfiguration cfgs[] = mProj.getConfigurations();
				if(cfgs.length != 0){
					Configuration cfg;
					CConfigurationData data;

					UserDefinedVariableSupplier usrSupplier = UserDefinedVariableSupplier.getInstance();

					for(int i = 0; i < cfgs.length; i++){
						cfg = (Configuration)cfgs[i];
						data = cfg.getConfigurationData();
//						try {
							ICConfigurationDescription cfgDes = newDes.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
							if(cfg.getConfigurationDescription() != null) {
								//copy cfg to avoid raise conditions
								cfg = ConfigurationDataProvider.copyCfg(cfg, cfgDes);
								cfgDes.setConfigurationData(ManagedBuildManager.CFG_DATA_PROVIDER_ID, cfg.getConfigurationData());
							}
							cfg.setConfigurationDescription(cfgDes);
							
							StorableCdtVariables vars = ((ToolChain)cfg.getToolChain()).getResetOldStyleProjectVariables();
							if(vars != null){
								ICdtVariable vs[] = vars.getMacros();
								for(int k = 0; k < vs.length; k++){
									usrSupplier.createMacro(vs[k], ICoreVariableContextInfo.CONTEXT_CONFIGURATION, cfgDes);
								}
							}
//						} catch (WriteAccessException e) {
//							ManagedBuilderCorePlugin.log(e);
//						} catch (CoreException e) {
//							ManagedBuilderCorePlugin.log(e);
//						}
						cfg.exportArtifactInfo();
					}
				}
			} else {
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), DataProviderMessages.getString("ProjectConverter.13"))); //$NON-NLS-1$
			}
		}
		return info;
	}

}
