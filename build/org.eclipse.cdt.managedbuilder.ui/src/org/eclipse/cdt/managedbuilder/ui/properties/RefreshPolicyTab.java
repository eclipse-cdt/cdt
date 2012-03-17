/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.ExclusionType;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.internal.ide.misc.ContainerContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The RefreshPolicyTab allows users to modify a project's refresh settings for each build.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 *
 * @author vkong
 * @since 8.0
 */
@SuppressWarnings("restriction")
public class RefreshPolicyTab extends AbstractCBuildPropertyTab {

	private final Image IMG_FOLDER = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FOLDER);
	private final Image IMG_FILE = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_FILE_OBJ);
	private final Image IMG_RESOURCE = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_FILE_FOLDER_OBJ);
	private final Image IMG_EXCEPTION = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_REFACTORING_ERROR);


	private final static int IDX_ADD_RESOURCE = 0;
	private final static int IDX_ADD_EXCEPTION = 1;
	private final static int IDX_EDIT_EXCEPTION = 2;
	private final static int IDX_DELETE = 3;

	private TreeViewer fTree;
	private RefreshScopeManager fManager;
	private IProject fProject;
	private ArrayList<_Entry> fSrc;
	private HashMap<String, HashMap<IResource, List<RefreshExclusion>>> fConfigurationToResourcesToExclusionsMap;

	public RefreshPolicyTab() {
		fManager = RefreshScopeManager.getInstance();
	}

	private  HashMap<IResource, List<RefreshExclusion>> getResourcesToExclusionsMap(String configName) {
		 HashMap<IResource, List<RefreshExclusion>> resourceMap = fConfigurationToResourcesToExclusionsMap.get(configName);
		 if (resourceMap == null) {
			 resourceMap = new HashMap<IResource, List<RefreshExclusion>>();
			 fConfigurationToResourcesToExclusionsMap.put(configName, resourceMap);
		 }
		 
		 return resourceMap;
	}

	private String getConfigName() {
		return this.getCfg().getName();
	}
	
	private HashMap<String, HashMap<IResource, List<RefreshExclusion>>> copyHashMap(HashMap<String, HashMap<IResource, List<RefreshExclusion>>> source) {
		
		HashMap<String, HashMap<IResource, List<RefreshExclusion>>> target = new HashMap<String, HashMap<IResource, List<RefreshExclusion>>>();
		Iterator<String> config_iterator = source.keySet().iterator();
		// for each Configuration ...
		while (config_iterator.hasNext()) {
			String configName = config_iterator.next();
			
			HashMap<IResource, List<RefreshExclusion>> source_resourceMap = source.get(configName);
			HashMap<IResource, List<RefreshExclusion>> target_resourceMap = new HashMap<IResource, List<RefreshExclusion>>();
			
			Iterator<IResource> resource_iterator = source_resourceMap.keySet().iterator();
			while (resource_iterator.hasNext()) {
				IResource source_resource = resource_iterator.next();
				List<RefreshExclusion> source_exclusions = source_resourceMap.get(source_resource);	
				List<RefreshExclusion> target_exclusions = new LinkedList<RefreshExclusion>();
				for (RefreshExclusion exclusion : source_exclusions) {
					// ADD each exclusion to the target exclusion list.
					RefreshExclusion target_exclusion = (RefreshExclusion) exclusion.clone();
					target_exclusions.add(target_exclusion);
				}

				// ADD the exclusion list for this resource
				target_resourceMap.put(source_resource, target_exclusions);
			}
			
			// ADD each resource.
			target.put(configName, target_resourceMap);
		}
		return target;
	}
	
	private void loadInfo() {
			HashMap<String, HashMap<IResource, List<RefreshExclusion>>> configMap = fManager.getConfigurationToResourcesMap(fProject);
			fConfigurationToResourcesToExclusionsMap = copyHashMap(configMap);
	}

	private List<RefreshExclusion> getExclusions(String configName, IResource resource) {
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(configName);
		List<RefreshExclusion> exclusions = resourceMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			resourceMap.put(resource, exclusions);
		}
		return resourceMap.get(resource);
	}

	/**
	 * Wrapper for IResource/RefreshExclusion
	 */
	class _Entry {
		//if this is a refresh exclusion, resourceToRefresh will be null
		IResource resourceToRefresh = null;

		//if this is a resource to refresh, exclusion will be null
		RefreshExclusion exclusion = null;

		//if this is a refresh exclusion, parent is the Exceptions node this is a child of
		_Exception_Node parent = null;

		// exceptions_node is the Exceptions node under this Entry, there should be a exceptions_node if this resource/refresh exclusion has nested exclusions
		_Exception_Node exceptions_node = null;

		// if this is a refresh exclusion, exclusion_instances is a list of exclusion instances associated with this exclusion
		List<_Exclusion_Instance> exclusion_instances = new ArrayList<_Exclusion_Instance>();

		_Entry(IResource _ent) {
			resourceToRefresh = _ent;
			if (getExclusions(getConfigName(),resourceToRefresh) != null && getExclusions(getConfigName(),resourceToRefresh).size() > 0)
				exceptions_node = new _Exception_Node(this);
		}

		_Entry(RefreshExclusion _ent, _Exception_Node parent) {
			exclusion = _ent;
			this.parent = parent;
			if (exclusion.getNestedExclusions() != null && exclusion.getNestedExclusions().size() > 0) {
				exceptions_node = new _Exception_Node(this);
			}
			if (exclusion.getExclusionInstances() != null && exclusion.getExclusionInstances().size() > 0) {
				Iterator<ExclusionInstance> iterator = exclusion.getExclusionInstances().iterator();
				while (iterator.hasNext()) {
					exclusion_instances.add(new _Exclusion_Instance(iterator.next(), this));
				}
			}
		}


		@Override
		public String toString() {
			if (isExclusion())
				return exclusion.getName();

			return resourceToRefresh.getFullPath().makeRelative().toString();
		}

		public Object[] getChildren() {
			if (isExclusion()) {
				List<Object> children = new ArrayList<Object>(exclusion_instances);
				if (exceptions_node != null)
					children.add(exceptions_node);
				return children.toArray();
			}

			if (exceptions_node != null)
				return new Object[] {exceptions_node};

			return null;
		}

		public boolean isExclusion() {
			return parent != null;
		}

		public void addException(RefreshExclusion exclusion) {
			if (exceptions_node == null) {
				exceptions_node = new _Exception_Node(this);
			}
			exceptions_node.addException(exclusion);
		}

		public void updateException(RefreshExclusion exclusion) {
			List<ExclusionInstance> exclusionInstancesToAdd = exclusion.getExclusionInstances();
			Iterator<ExclusionInstance> iterator = exclusionInstancesToAdd.iterator();
			exclusion_instances.clear();

			while (iterator.hasNext()) {
				ExclusionInstance instanceToAdd = iterator.next();
				exclusion_instances.add(new _Exclusion_Instance(instanceToAdd, this));
			}
		}

		public void remove() {
			if (isExclusion()) {
				RefreshExclusion exclusionToRemove = exclusion;

				_Entry parentEntry = parent.parent;
				if (parentEntry.isExclusion()) {
					parentEntry.exclusion.removeNestedExclusion(exclusionToRemove);
				} else {
					List<RefreshExclusion> exceptions = getExclusions(getConfigName(), parentEntry.resourceToRefresh);
					exceptions.remove(exclusionToRemove);
				}

				//update tree
				if (parent.exceptions.size() > 1) {
					parent.exceptions.remove(this);
				} else {
					parentEntry.exceptions_node = null;
				}
			} else { //this is a resource to refresh
				getResourcesToExclusionsMap(getConfigName()).remove(resourceToRefresh);
				fSrc.remove(this);
			}
		}

	}

	class _Exception_Node {
		_Entry parent;	//can be IResource or RefreshExclusion - must not be null

		//list of refresh exclusions under this Exceptions node
		List <_Entry> exceptions = new ArrayList<_Entry>();

		_Exception_Node(_Entry ent) {
			parent = ent;
			Iterator<RefreshExclusion> iterator = null;

			if (parent.isExclusion()) {
				if (parent.exclusion.getNestedExclusions() != null)
					iterator = parent.exclusion.getNestedExclusions().iterator();
			} else {
				if (getExclusions(getConfigName(),parent.resourceToRefresh) != null)
					iterator = getExclusions(getConfigName(),parent.resourceToRefresh).iterator();
			}

			if (iterator != null) {
				while (iterator.hasNext()) {
					exceptions.add(new _Entry(iterator.next(), this));
				}
			}

		}

		public void addException(RefreshExclusion exclusion) {
			exceptions.add(new _Entry(exclusion, this));
			if (parent.isExclusion()) {
				parent.exclusion.addNestedExclusion(exclusion);
			} else {
				List<RefreshExclusion> exclusions = getExclusions(getConfigName(),parent.resourceToRefresh);
				if (exclusions == null) {
					exclusions = new LinkedList<RefreshExclusion>();
					getResourcesToExclusionsMap(getConfigName()).put(parent.resourceToRefresh, exclusions);
				}
				exclusions.add(exclusion);
			}
		}

		public Object[] getChildren() {
			return exceptions.toArray();
		}

		@Override
		public String toString() {
			return Messages.RefreshPolicyTab_exceptionsLabel;
		}
	}

	/**
	 * Wrapper for ExclusionInstance
	 */
	class _Exclusion_Instance {
		_Entry parent; //the parent refresh exclusion
		ExclusionInstance instance = null;

		_Exclusion_Instance(ExclusionInstance instance, _Entry parent) {
			this.parent = parent;
			this.instance = instance;
		}

		public Object[] getChildren() {
			return null;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return instance.getDisplayString();
		}

		public void remove() {
			parent.exclusion.removeExclusionInstance(instance);
			parent.exclusion_instances.remove(this);

			if (parent.exclusion_instances.size() < 1 && parent.exclusion.supportsExclusionInstances()) {
				parent.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		fProject = page.getProject();
		loadInfo();
		initButtons(new String[] {
				Messages.RefreshPolicyTab_addResourceButtonLabel,
				Messages.RefreshPolicyTab_addExceptionButtonLabel,
				Messages.RefreshPolicyTab_editExceptionButtonLabel,
				Messages.RefreshPolicyTab_deleteButtonLabel}, 120);
		usercomp.setLayout(new GridLayout(1, false));


		Label topLabel = new Label(usercomp, SWT.NONE);
		topLabel.setText(Messages.RefreshPolicyTab_tabLabel);
		Group g1 = setupGroup(usercomp, Messages.RefreshPolicyTab_resourcesGroupLabel, 2, GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);

		fSrc = new ArrayList<_Entry>();
		generateTreeContent();

		fTree = new TreeViewer(g1);
		fTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fTree.getTree().getAccessible().addAccessibleListener(
            new AccessibleAdapter() {
                @Override
				public void getName(AccessibleEvent e) {
                	e.result = Messages.RefreshPolicyTab_resourcesTreeLabel;
                }
            }
        );

		fTree.setContentProvider(new ITreeContentProvider() {
			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof _Entry) {
					return ((_Entry) parentElement).getChildren();
				}
				if (parentElement instanceof _Exception_Node) {
					return ((_Exception_Node)parentElement).getChildren();
				}
				return null;
			}
			@Override
			public Object getParent(Object element) {
				if (element instanceof _Entry)
					return ((_Entry)element).parent;
				if (element instanceof _Exception_Node)
					return ((_Exception_Node)element).parent;
				if (element instanceof _Exclusion_Instance)
					return ((_Exclusion_Instance)element).parent;
				return null;
			}
			@Override
			public boolean hasChildren(Object element) {
				return (element instanceof _Entry || element instanceof _Exception_Node);
			}
			@Override
			public Object[] getElements(Object inputElement) {
				return fSrc.toArray(new _Entry[fSrc.size()]);
			}
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}});

		fTree.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof _Exception_Node)
					return IMG_EXCEPTION;
				else if (element instanceof _Entry) {
					_Entry entry = (_Entry) element;
					if (entry.isExclusion()) {
						return getImageForExclusionType(entry.exclusion.getExclusionType());
					}
					return getImageForResource(entry.resourceToRefresh);
				}

				else if (element instanceof _Exclusion_Instance){
					return getImageForExclusionType(((_Exclusion_Instance) element).instance.getExclusionType());
				}
				else
					return null;
			}
		});

		fTree.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof TreeSelection) {
					Object sel = ((TreeSelection)selection).getFirstElement();
					if ( sel != null && sel instanceof _Exception_Node) {
						fTree.setSelection(null);
					}
				}
				updateButtons();
			}
		});

		fTree.setInput(fSrc);
		fTree.expandAll();
		updateButtons();

	}

	private Image getImageForResource(IResource resource) {
		switch (resource.getType()) {
		case IResource.FILE:
			return IMG_FILE;
		case IResource.FOLDER:
		case IResource.PROJECT:
			return IMG_FOLDER;
		default:
			return IMG_RESOURCE;
		}
	}

	private Image getImageForExclusionType(ExclusionType exclusionType) {
		switch (exclusionType) {
		case FILE:
			return IMG_FILE;
		case FOLDER:
			return IMG_FOLDER;
		case RESOURCE:
		default:
			return IMG_RESOURCE;
		}
	}

	private void generateTreeContent() {
		Iterator<IResource> iterator = getResourcesToExclusionsMap(getConfigName()).keySet().iterator();
		while (iterator.hasNext()) {
			_Entry top = new _Entry(iterator.next());
			fSrc.add(top);
		}
	}

	private void clearTreeContent() {
		// Just clear the fSrc.
		fSrc.clear();
	}
	
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
		// only expand on first update.
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return;
		} else {
			clearTreeContent();
			generateTreeContent();
			fTree.refresh();
			fTree.expandAll();
		}
	}

	@Override
	protected void updateButtons() {
		TreeItem[] sel = fTree.getTree().getSelection();
		buttonSetEnabled(IDX_ADD_RESOURCE, true);
    	buttonSetEnabled(IDX_ADD_EXCEPTION, sel.length == 1 && sel[0].getData() instanceof _Entry);
    	buttonSetEnabled(IDX_EDIT_EXCEPTION, sel.length == 1 && sel[0].getData() instanceof _Entry && ((_Entry) sel[0].getData()).isExclusion());
    	buttonSetEnabled(IDX_DELETE, sel.length == 1 && (sel[0].getData() instanceof _Entry || sel[0].getData() instanceof _Exclusion_Instance));
	}

	class FilteredContainerContentProvider extends ContainerContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.ide.misc.ContainerContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object element) {
			ArrayList<Object> filteredChildren = new ArrayList<Object>(Arrays.asList(super.getChildren(element)));
			Iterator<IResource> iterator = getResourcesToExclusionsMap(getConfigName()).keySet().iterator(); //fResourcesToRefresh.iterator();
			
			while (iterator.hasNext()) {
				filteredChildren.remove(iterator.next());
			}
			return filteredChildren.toArray();
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int x) {
		Shell shell = usercomp.getShell();
		TreeSelection selection = (TreeSelection) fTree.getSelection();

		switch (x) {
		case IDX_ADD_RESOURCE:
			//TODO: Phase one implementation - folders only - need to change this for Phase two
			
			CheckedTreeSelectionDialog addResourceDialog = new CheckedTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
					new FilteredContainerContentProvider());
			addResourceDialog.setInput(ResourcesPlugin.getWorkspace());
			addResourceDialog.setTitle(Messages.RefreshPolicyTab_addResourceDialogTitle);
			addResourceDialog.setMessage(Messages.RefreshPolicyTab_addResourceDialogDescription);
			if (addResourceDialog.open() == Window.OK) {
				Object[] result = addResourceDialog.getResult();
				for (int i = 0; i < result.length; i++) {
					 IResource resource = (IResource) result[i];
					_Entry newResource = new _Entry(resource);
					//update the model element in this tab
					getResourcesToExclusionsMap(getConfigName()).put(resource,new LinkedList<RefreshExclusion>());
					
					//update tree
					fSrc.add(newResource);
				}
				fTree.refresh();
				fTree.expandAll();
			}
			break;

		case IDX_ADD_EXCEPTION:
			if (selection == null)
				break;
			_Entry sel = (_Entry) selection.getFirstElement();
			RefreshPolicyExceptionDialog addExceptionDialog;
			if (sel.isExclusion()) {
				addExceptionDialog = new RefreshPolicyExceptionDialog(shell, sel.exclusion, true);
			} else {
				addExceptionDialog = new RefreshPolicyExceptionDialog(shell, sel.resourceToRefresh, getExclusions(getConfigName(),sel.resourceToRefresh), true);
			}
			if (addExceptionDialog.open() == Window.OK) {
				RefreshExclusion newExclusion = addExceptionDialog.getResult();
				if (newExclusion != null)
					//update tree & the working copy of the model elements in this tab
					sel.addException(newExclusion);
				fTree.refresh();
				fTree.expandAll();
			}
			fTree.refresh();
			fTree.expandAll();
			break;

		case IDX_EDIT_EXCEPTION:	//can only edit a refresh exclusion
			if (selection == null)
				break;
			_Entry selectedExclusion = (_Entry) selection.getFirstElement();
			RefreshPolicyExceptionDialog editExceptionDialog;

			editExceptionDialog = new RefreshPolicyExceptionDialog(shell, selectedExclusion.exclusion, false);
			if (editExceptionDialog.open() == Window.OK) {
				RefreshExclusion updatedExclusion = editExceptionDialog.getResult();

				//update tree
				selectedExclusion.updateException(updatedExclusion);
				fTree.refresh();
				fTree.expandAll();
			}
			fTree.refresh();
			fTree.expandAll();
			break;

		case IDX_DELETE:
			if (selection == null)
				break;
			if (selection.getFirstElement() instanceof _Entry) {
				_Entry sel1 = (_Entry) selection.getFirstElement();
				boolean remove = false;
				if (sel1.exceptions_node != null) {
					String question;
					if (sel1.isExclusion()) {
						question = Messages.RefreshPolicyTab_deleteConfirmationDialog_question_exception;
					} else {
						question = Messages.RefreshPolicyTab_deleteConfirmationDialog_question_resource;
					}
					if (MessageDialog.openQuestion(shell, Messages.RefreshPolicyTab_deleteConfirmationDialog_title, question)) {
						remove = true;
					}
				} else {
					remove = true;
				}
				if (remove) {
					//update tree & the working copy of the model elements in this tab
					sel1.remove();
					fTree.refresh();
					fTree.expandAll();
				}
			} else { //exclusion instance
				_Exclusion_Instance sel1 = (_Exclusion_Instance) selection.getFirstElement();
				boolean remove = false;
				if (sel1.parent.exclusion.supportsExclusionInstances() && sel1.parent.exclusion_instances.size() == 1 && sel1.parent.exceptions_node != null) {
					//this is the only exclusion instance for an exclusion and the exclusion has nested exclusions
					if (MessageDialog.openQuestion(shell, Messages.RefreshPolicyTab_deleteConfirmationDialog_title, Messages.RefreshPolicyTab_deleteConfirmationDialog_question_exception)) {
						remove = true;
					}
				} else
					remove = true;
				if (remove) {
					//update tree & the working copy of the model elements in this tab
					sel1.remove();
					fTree.refresh();
					fTree.expandAll();
				}
			}
			break;

		default:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performOK()
	 */
	@Override
	protected void performOK() {
		Iterator<String> config_iterator = fConfigurationToResourcesToExclusionsMap.keySet().iterator();
		
		while (config_iterator.hasNext()) {
			String configName = config_iterator.next();
		
			fManager.setResourcesToExclusionsMap(fProject, configName, getResourcesToExclusionsMap(configName));
		}
		try {
			fManager.persistSettings(getResDesc().getConfiguration().getProjectDescription());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
