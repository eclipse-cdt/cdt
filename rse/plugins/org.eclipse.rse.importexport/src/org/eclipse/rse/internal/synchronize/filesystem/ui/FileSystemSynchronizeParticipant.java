/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemSynchronizeParticipant
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemPlugin;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemMergeContext;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.MergeContext;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipantActionGroup;

/**
 * This is an example synchronize participant for the file system provider. It
 * will allow showing synchronization state for local resources mapped to a
 * remote file system location.
 * 
 * @since 3.0
 */
public class FileSystemSynchronizeParticipant extends ModelSynchronizeParticipant {

	/**
	 * The participant id for the org.eclipse.team.ui.synchronizeParticipant
	 * extension point.
	 */
	public static final String ID = "org.eclipse.rse.internal.synchronize.filesystem.participant"; //$NON-NLS-1$

	/**
	 * The viewer id for the org.eclipse.ui.navigator.viewer extension point.
	 */
	public static final String VIEWER_ID = "org.eclipse.rse.internal.synchronize.filesystem.syncViewer"; //$NON-NLS-1$

	/**
	 * Custom menu groups included in the viewer definition in the plugin.xml.
	 */
	public static final String CONTEXT_MENU_MERGE_GROUP_1 = "merge"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_PUT_GROUP_1 = "put"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_GET_GROUP_1 = "get"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_OVERWRITE_GROUP_1 = "overwrite"; //$NON-NLS-1$

	/**
	 * A custom label decorator that will show the remote mapped path for each
	 * file.
	 */
	public class FileSystemParticipantLabelDecorator extends LabelProvider implements ILabelDecorator {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse
		 * .swt.graphics.Image, java.lang.Object)
		 */
		public Image decorateImage(Image image, Object element) {
			return image;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.
		 * String, java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			try {
				if (element instanceof ISynchronizeModelElement) {
					IResource resource = ((ISynchronizeModelElement) element).getResource();
					if (resource != null && resource.getType() == IResource.FILE) {
						SyncInfo info = FileSystemSubscriber.getInstance().getSyncInfo(resource);
						IResourceVariant variant = info.getRemote();
						if (variant != null) {
							return text + " (" + variant.getContentIdentifier() + ")";
						}
					}
				}
			} catch (TeamException e) {
				FileSystemPlugin.log(e);
			}
			return null;
		}
	}

	/**
	 * Action group that contributes the get an put menus to the context menu in
	 * the synchronize view
	 */
	public class FileSystemParticipantActionGroup extends ModelSynchronizeParticipantActionGroup {
		private ModelPutAction putAction;
		private ModelGetAction getAction;
		private ModelMergeAction mergeAction;
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#initialize
		 * (org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
		 */
		@Override
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			putAction = new ModelPutAction("", configuration);
			getAction = new ModelGetAction("", configuration);
			mergeAction = new ModelMergeAction("", configuration);
		}
		
		

		@Override
		public void fillContextMenu(IMenuManager menu) {
			//menu.remove(CONTEXT_MENU_GET_GROUP_1);
			//menu.remove(CONTEXT_MENU_PUT_GROUP_1);
			
			try {
				//super.fillContextMenu(menu);
				TreeSelection selection = (TreeSelection)getConfiguration().getSite().getSelectionProvider().getSelection();
				boolean hasOutgoingChange = false;
				boolean hasIncomingChange = false;
				boolean hasConflictingChange = false;
				boolean hasSingleResource = selection.size() == 1?true:false;
				
				List<IResource> resources = new ArrayList<IResource>();
				for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
					resources.add(Utils.getResource(iterator.next()));
				}
				
				for (Iterator<IResource> iterator = resources.iterator(); iterator.hasNext();) {
					
					IResource resource = (IResource) iterator.next();
					SyncInfo info =  FileSystemSubscriber.getInstance().getSyncInfo(resource);						
					
					int kind = info.getKind();
					
					if(SyncInfo.getDirection(kind) == SyncInfo.OUTGOING){
						hasOutgoingChange = true;
					} else if (SyncInfo.getDirection(kind) == SyncInfo.INCOMING){
						hasIncomingChange = true;
					}
					else if (SyncInfo.getDirection(kind) == SyncInfo.CONFLICTING){
						hasConflictingChange = true;
					}
				}
				
				if (hasConflictingChange){
					if(hasSingleResource){
						mergeAction.setText(SystemImportExportResources.RESID_SYNCHRONIZE_ACTIONS_MERGE_LABEL);
					}else{
						mergeAction.setText(SystemImportExportResources.RESID_SYNCHRONIZE_ACTIONS_MERGE_ALL_LABEL);
					}
					appendToGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTEXT_MENU_MERGE_GROUP_1, putAction);
					menu.appendToGroup(CONTEXT_MENU_MERGE_GROUP_1, mergeAction);										
				}
				
				if(hasOutgoingChange){
					if(hasSingleResource){
						putAction.setText(SystemImportExportResources.RESID_SYNCHRONIZE_ACTIONS_PUT_LABEL);
					}else{
						putAction.setText(SystemImportExportResources.RESID_SYNCHRONIZE_ACTIONS_PUT_ALL_LABEL);
					}
					appendToGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTEXT_MENU_PUT_GROUP_1, putAction);
					menu.appendToGroup(CONTEXT_MENU_PUT_GROUP_1, putAction);
				}
				
				if(hasIncomingChange){
					if(hasSingleResource){
						getAction.setText(SystemImportExportResources.RESID_SYNCHRONIZE_ACTIONS_GET_LABEL);
					}else{
						getAction.setText(SystemImportExportResources.RESID_SYNCHRONIZE_ACTIONS_GET_ALL_LABEL);
					}
					appendToGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, CONTEXT_MENU_GET_GROUP_1, getAction);
					menu.appendToGroup(CONTEXT_MENU_GET_GROUP_1, getAction);
				}
					
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	/**
	 * Create a file system participant. This method is invoked by the
	 * Synchronize view when a persisted participant is being restored.
	 * Participants that are persisted must override the {@link
	 * #restoreContext(ISynchronizationScopeManager)} method to recreate the
	 * context and may also need to override the {@link
	 * #createScopeManager(ResourceMapping[])} method if they require a custom
	 * scope manager.
	 */
	public FileSystemSynchronizeParticipant() {
		super();
	}

	/**
	 * Create the participant for the given context. This method is used by the
	 * file system plugin to create a participant and then add it to the sync
	 * view (or show it is some other container).
	 * 
	 * @param context
	 * 		the synchronization context
	 */
	public FileSystemSynchronizeParticipant(SynchronizationContext context) {
		super(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor(ID));
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#
	 * initializeConfiguration
	 * (org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	@Override
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration.setProperty(ISynchronizePageConfiguration.P_VIEWER_ID, VIEWER_ID);

		// Add the label decorator
		configuration.addLabelDecorator(new FileSystemParticipantLabelDecorator());
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.team.ui.synchronize.ModelSynchronizeParticipant#
	 * createMergeActionGroup()
	 */
	@Override
	protected ModelSynchronizeParticipantActionGroup createMergeActionGroup() {
		return new FileSystemParticipantActionGroup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant#restoreContext
	 * (org.eclipse.team.core.mapping.ISynchronizationScopeManager)
	 */
	@Override
	protected MergeContext restoreContext(ISynchronizationScopeManager manager) {
		return new FileSystemMergeContext(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.team.ui.synchronize.ModelSynchronizeParticipant#
	 * createScopeManager(org.eclipse.core.resources.mapping.ResourceMapping[])
	 */
	@Override
	protected ISynchronizationScopeManager createScopeManager(ResourceMapping[] mappings) {
		return FileSystemOperation.createScopeManager(getName(), mappings);
	}
}
