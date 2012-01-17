/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Freescale Semiconductor (stripped out functionality from platform debug version) 
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * This class was taken from org.eclipse.debug.ui. We expose a Common tab for
 * Multilaunch that has only a subset of the standard tab's properties.
 * 
 * Launch configuration tab used to specify the location a launch configuration
 * is stored in, whether it should appear in the favorites list, and perspective
 * switching behavior for an associated launch.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * 
 * @since 6.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CommonTabLite extends AbstractLaunchConfigurationTab {
	
	/**
	 * Provides a persistible dialog for selecting the shared project location
	 * @since 3.2
	 */
	class SharedLocationSelectionDialog extends ContainerSelectionDialog {
		private final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SHARED_LAUNCH_CONFIGURATON_DIALOG"; //$NON-NLS-1$
		
		public SharedLocationSelectionDialog(Shell parentShell, IContainer initialRoot, boolean allowNewContainerName, String message) {
			super(parentShell, initialRoot, allowNewContainerName, message);
		}

		@Override
		protected IDialogSettings getDialogBoundsSettings() {
			IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section = settings.getSection(SETTINGS_ID);
			if (section == null) {
				section = settings.addNewSection(SETTINGS_ID);
			} 
			return section;
		}
	}
	
	/**
	 * This attribute exists solely for the purpose of making sure that invalid shared locations
	 * can be revertible. This attribute is not saveable and will never appear in a saved
	 * launch configuration.
	 * @since 3.3
	 */
	private static final String BAD_CONTAINER = "bad_container_name"; //$NON-NLS-1$
	
	// Local/shared UI widgets
	private Button fLocalRadioButton;
	private Button fSharedRadioButton;
	private Text fSharedLocationText;
	private Button fSharedLocationButton;
	
	/**
	 * Check box list for specifying favorites
	 */
	private CheckboxTableViewer fFavoritesTable;
			
	/**
	 * Modify listener that simply updates the owning launch configuration dialog.
	 */
	private ModifyListener fBasicModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent evt) {
			updateLaunchConfigurationDialog();
		}
	};
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_COMMON_TAB);
		comp.setLayout(new GridLayout(2, true));
		comp.setFont(parent.getFont());
		
		createSharedConfigComponent(comp);
		createFavoritesComponent(comp);
	}
	
	/**
	 * Creates the favorites control
	 * @param parent the parent composite to add this one to
	 * @since 3.2
	 */
	private void createFavoritesComponent(Composite parent) {
		Group favComp = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.CommonTab_Display_in_favorites_menu__10, 1, 1, GridData.FILL_BOTH);
		fFavoritesTable = CheckboxTableViewer.newCheckList(favComp, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Control table = fFavoritesTable.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		table.setFont(parent.getFont());
		fFavoritesTable.setContentProvider(new FavoritesContentProvider());
		fFavoritesTable.setLabelProvider(new FavoritesLabelProvider());
		fFavoritesTable.addCheckStateListener(new ICheckStateListener() {
				@Override
				public void checkStateChanged(CheckStateChangedEvent event) {
					updateLaunchConfigurationDialog();
				}
			});
	}
	
	/**
	 * Creates the shared config component
	 * @param parent the parent composite to add this component to
	 * @since 3.2
	 */
	private void createSharedConfigComponent(Composite parent) {
		Group group = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.CommonTab_0, 3, 2, GridData.FILL_HORIZONTAL);
		Composite comp = SWTFactory.createComposite(group, parent.getFont(), 3, 3, GridData.FILL_BOTH, 0, 0);
		fLocalRadioButton = createRadioButton(comp, LaunchConfigurationsMessages.CommonTab_L_ocal_3);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		fLocalRadioButton.setLayoutData(gd);
		fSharedRadioButton = createRadioButton(comp, LaunchConfigurationsMessages.CommonTab_S_hared_4);
		fSharedRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSharedRadioButtonSelected();
			}
		});
		fSharedLocationText = SWTFactory.createSingleText(comp, 1);
		fSharedLocationText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result =  LaunchConfigurationsMessages.CommonTab_S_hared_4;
			}
		});
		fSharedLocationText.addModifyListener(fBasicModifyListener);
		fSharedLocationButton = createPushButton(comp, LaunchConfigurationsMessages.CommonTab__Browse_6, null);	 
		fSharedLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSharedLocationButtonSelected();
			}
		});	

		fLocalRadioButton.setSelection(true);
		setSharedEnabled(false);	
	}
	
	/**
	 * handles the shared radio button being selected
	 */
	private void handleSharedRadioButtonSelected() {
		setSharedEnabled(isShared());
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Sets the widgets for specifying that a launch configuration is to be shared to the enable value
	 * @param enable the enabled value for 
	 */
	private void setSharedEnabled(boolean enable) {
		fSharedLocationText.setEnabled(enable);
		fSharedLocationButton.setEnabled(enable);
	}
	
	private String getDefaultSharedConfigLocation(ILaunchConfiguration config) {
		String path = IInternalDebugCoreConstants.EMPTY_STRING;
		try {
			IResource[] res = config.getMappedResources();
			if(res != null) {
				IProject  proj;
				for (int i = 0; i < res.length; i++) {
					proj = res[i].getProject();
					if(proj != null && proj.isAccessible()) {
						return proj.getFullPath().toOSString();
					}
				}
			}
		} 
		catch (CoreException e) {DebugUIPlugin.log(e);}
		return path;
	}
	
	/**
	 * if the shared radio button is selected, indicating that the launch configuration is to be shared
	 * @return true if the radio button is selected, false otherwise
	 */
	private boolean isShared() {
		return fSharedRadioButton.getSelection();
	}
	
	/**
	 * Handles the shared location button being selected
	 */
	private void handleSharedLocationButtonSelected() { 
		String currentContainerString = fSharedLocationText.getText();
		IContainer currentContainer = getContainer(currentContainerString);
		SharedLocationSelectionDialog dialog = new SharedLocationSelectionDialog(getShell(),
				   currentContainer,
				   false,
				   LaunchConfigurationsMessages.CommonTab_Select_a_location_for_the_launch_configuration_13);
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] results = dialog.getResult();	
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			String containerName = path.toOSString();
			fSharedLocationText.setText(containerName);
		}		
	}
	
	/**
	 * gets the container form the specified path
	 * @param path the path to get the container from
	 * @return the container for the specified path or null if one cannot be determined
	 */
	private IContainer getContainer(String path) {
		Path containerPath = new Path(path);
		return (IContainer) getWorkspaceRoot().findMember(containerPath);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean isShared = !configuration.isLocal();
		fSharedRadioButton.setSelection(isShared);
		fLocalRadioButton.setSelection(!isShared);
		setSharedEnabled(isShared);
		fSharedLocationText.setText(getDefaultSharedConfigLocation(configuration));
		if(isShared) {
			String containerName = IInternalDebugCoreConstants.EMPTY_STRING;
			IFile file = configuration.getFile();
			if (file != null) {
				IContainer parent = file.getParent();
				if (parent != null) {
					containerName = parent.getFullPath().toOSString();
				}
			}
			fSharedLocationText.setText(containerName);
		}
		updateFavoritesFromConfig(configuration);
	}
	

	/**
	 * Updates the favorites selections from the local configuration
	 * @param config the local configuration
	 */
	private void updateFavoritesFromConfig(ILaunchConfiguration config) {
		fFavoritesTable.setInput(config);
		fFavoritesTable.setCheckedElements(new Object[]{});
		try {
			List groups = config.getAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, new ArrayList());
			if (groups.isEmpty()) {
				// check old attributes for backwards compatible
				if (config.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false)) {
					groups.add(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
				}
				if (config.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false)) {
					groups.add(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
				}
			}
			if (!groups.isEmpty()) {
				List list = new ArrayList();
				Iterator iterator = groups.iterator();
				while (iterator.hasNext()) {
					String id = (String)iterator.next();
					LaunchGroupExtension extension = getLaunchConfigurationManager().getLaunchGroup(id);
					if (extension != null) {
						list.add(extension);
					}
				}
				fFavoritesTable.setCheckedElements(list.toArray());
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
	}

	/**
	 * Updates the configuration form the local shared config working copy
	 * @param config the local shared config working copy
	 */
	private void updateConfigFromLocalShared(ILaunchConfigurationWorkingCopy config) {
		if (isShared()) {
			String containerPathString = fSharedLocationText.getText();
			IContainer container = getContainer(containerPathString);
			if(container == null) {
				//we need to force an attribute to allow the invalid container path to be revertable
				config.setAttribute(BAD_CONTAINER, containerPathString);
			}
			else {
				config.setContainer(container);
			}
		} else {
			config.setContainer(null);
		}
	}
	
	/**
	 * Convenience accessor
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
	
	/**
	 * Update the favorite settings.
	 * 
	 * NOTE: set to <code>null</code> instead of <code>false</code> for backwards compatibility
	 *  when comparing if content is equal, since 'false' is default
	 * 	and will be missing for older configurations.
	 */
	private void updateConfigFromFavorites(ILaunchConfigurationWorkingCopy config) {
		try {
			Object[] checked = fFavoritesTable.getCheckedElements();
			boolean debug = config.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false);
			boolean run = config.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false);
			if (debug || run) {
				// old attributes
				List groups = new ArrayList();
				int num = 0;
				if (debug) {
					groups.add(getLaunchConfigurationManager().getLaunchGroup(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP));
					num++;
				}
				if (run) {
					num++;
					groups.add(getLaunchConfigurationManager().getLaunchGroup(IDebugUIConstants.ID_RUN_LAUNCH_GROUP));
				}
				// see if there are any changes
				if (num == checked.length) {
					boolean different = false;
					for (int i = 0; i < checked.length; i++) {
						if (!groups.contains(checked[i])) {
							different = true;
							break;
						}
					}
					if (!different) {
						return;
					}
				}
			} 
			config.setAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, (String)null);
			config.setAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, (String)null);
			List groups = null;
			for (int i = 0; i < checked.length; i++) {
				LaunchGroupExtension group = (LaunchGroupExtension)checked[i];
				if (groups == null) {
					groups = new ArrayList();
				}
				groups.add(group.getIdentifier());
			}
			config.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, groups);
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}		
	}	
	
	/**
	 * Convenience method for getting the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setMessage(null);
		setErrorMessage(null);
		
		return validateLocalShared();
	}
	
    /**
     * validates the local shared config file location
     * @return true if the local shared file exists, false otherwise
     */
    private boolean validateLocalShared() {
		if (isShared()) {
			String path = fSharedLocationText.getText().trim();
			IContainer container = getContainer(path);
			if (container == null || container.equals(ResourcesPlugin.getWorkspace().getRoot())) {
				setErrorMessage(LaunchConfigurationsMessages.CommonTab_Invalid_shared_configuration_location_14); 
				return false;
			} else if (!container.getProject().isOpen()) {
				setErrorMessage(LaunchConfigurationsMessages.CommonTab_Cannot_save_launch_configuration_in_a_closed_project__1); 
				return false;				
			}
		}
		return true;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setContainer(null);
		setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, config, true, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		updateConfigFromLocalShared(configuration);
		updateConfigFromFavorites(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return LaunchConfigurationsMessages.CommonTab__Common_15; 
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	@Override
	public String getId() {
		return "org.eclipse.debug.ui.commonTab"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		return validateLocalShared();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return DebugUITools.getImage(IInternalDebugUIConstants.IMG_OBJS_COMMON_TAB);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {}

	/**
	 * Content provider for the favorites table
	 */
	class FavoritesContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
			List possibleGroups = new ArrayList();
			ILaunchConfiguration configuration = (ILaunchConfiguration)inputElement;
			for (int i = 0; i < groups.length; i++) {
				ILaunchGroup extension = groups[i];
				LaunchHistory history = getLaunchConfigurationManager().getLaunchHistory(extension.getIdentifier());
				if (history != null && history.accepts(configuration)) {
					possibleGroups.add(extension);
				} 
			}
			return possibleGroups.toArray();
		}

		@Override
		public void dispose() {}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	}
	
	/**
	 * Provides the labels for the favorites table
	 *
	 */
	class FavoritesLabelProvider implements ITableLabelProvider {
		
		private Map fImages = new HashMap();

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			Image image = (Image)fImages.get(element);
			if (image == null) {
				ImageDescriptor descriptor = ((LaunchGroupExtension)element).getImageDescriptor();
				if (descriptor != null) {
					image = descriptor.createImage();
					fImages.put(element, image);
				}
			}
			return image;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			String label = ((LaunchGroupExtension)element).getLabel();
			return DebugUIPlugin.removeAccelerators(label);
		}

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {
			Iterator images = fImages.values().iterator();
			while (images.hasNext()) {
				Image image = (Image)images.next();
				image.dispose();
			}
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {return false;}

		@Override
		public void removeListener(ILabelProviderListener listener) {}		
	}

}
