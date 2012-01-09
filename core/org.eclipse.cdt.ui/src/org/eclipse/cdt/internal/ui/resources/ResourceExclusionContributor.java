/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.ExclusionType;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.ui.resources.Messages;
import org.eclipse.cdt.ui.resources.RefreshExclusionContributor;

import org.eclipse.cdt.internal.core.resources.ResourceExclusion;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * 
 * @author vkong
 * @since 5.3
 * 
 */
public class ResourceExclusionContributor extends RefreshExclusionContributor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.resources.RefreshExclusionContributor#createExclusion()
	 */
	@Override
	public RefreshExclusion createExclusion() {
		ResourceExclusion newExclusion = new ResourceExclusion();
		newExclusion.setContributorId(getID());

		// TODO change this for Phase 2
		newExclusion.setExclusionType(ExclusionType.FOLDER);
		return newExclusion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.resources.RefreshExclusionContributor#createProperiesUI(org.eclipse.swt.widgets.
	 * Composite, org.eclipse.cdt.core.resources.RefreshExclusion)
	 */
	@Override
	public void createProperiesUI(Composite parent, final RefreshExclusion exclusion) {
		final Shell shell = parent.getShell();

		Group g = new Group(parent, SWT.NONE);
		g.setText(Messages.RefreshPolicyExceptionDialog_exceptionTypeResources);
		g.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 2;
		g.setLayoutData(gridData);

		final List exceptionsList = new List(g, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.minimumHeight = 250;
		gridData.minimumWidth = 275;
		exceptionsList.setLayoutData(gridData);

		final HashMap<String, ExclusionInstance> exclusionInstanceStrings = new LinkedHashMap<String, ExclusionInstance>();
		final HashMap<String, Object> exclusionInstanceResources = new LinkedHashMap<String, Object>();

		java.util.List<ExclusionInstance> exclusionInstances = exclusion.getExclusionInstances();

		// populate exclusion instance list
		if (exclusionInstances != null) {
			Iterator<ExclusionInstance> iterator = exclusionInstances.iterator();
			while (iterator.hasNext()) {
				ExclusionInstance exclusionInstance = iterator.next();
				String name = exclusionInstance.getDisplayString();
				exceptionsList.add(name);
				exclusionInstanceStrings.put(name, exclusionInstance);
				exclusionInstanceResources.put(name, exclusionInstance.getResource());
			}
		}

		Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, false));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.minimumWidth = 100;
		buttonComp.setLayoutData(gridData);

		Button addButton = new Button(buttonComp, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		addButton.setLayoutData(gridData);
		addButton.setText(Messages.RefreshPolicyExceptionDialog_addButtonLabel);

		addButton.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(shell,
						WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
						new ITreeContentProvider() {

							@Override
							public void dispose() {
							}

							@Override
							public Object[] getChildren(Object parentElement) {
								if (parentElement instanceof IContainer) {
									IContainer container = (IContainer) parentElement;
									if (container.isAccessible()) {
										try {
											java.util.List<IResource> children = new ArrayList<IResource>();
											IResource[] members = container.members();
											for (int i = 0; i < members.length; i++) {
												if (members[i].getType() == IResource.FOLDER) {
													children.add(members[i]);
												}
											}
											return children.toArray();
										} catch (CoreException e) {
											// this should never happen because we call #isAccessible before
											// invoking #members
										}
									}
								}
								return new Object[0];
							}

							@Override
							public Object[] getElements(Object inputElement) {
								return getChildren(inputElement);
							}

							@Override
							public Object getParent(Object element) {
								if (element instanceof IResource) {
									return ((IResource) element).getParent();
								}
								return null;
							}

							@Override
							public boolean hasChildren(Object element) {
								return getChildren(element).length > 0;
							}

							@Override
							public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
							}
						});

				dialog.setInput(getResourceRoot(exclusion));

				if (exclusionInstanceResources.values().size() > 0) {
					dialog.setInitialElementSelections(Arrays.asList(exclusionInstanceResources.values()
							.toArray()));
				}
				dialog.setMessage(Messages.RefreshPolicyExceptionDialog_SelectResourceDialogMessage);
				dialog.setTitle(Messages.RefreshPolicyExceptionDialog_SelectResourceDialogTitle);

				if (dialog.open() == Window.OK) {
					Object[] selection = dialog.getResult();
					exceptionsList.removeAll();
					exclusionInstanceResources.clear();
					final HashMap<String, ExclusionInstance> oldExclusionInstanceStrings = new LinkedHashMap<String, ExclusionInstance>(
							exclusionInstanceStrings);
					exclusionInstanceStrings.clear();

					for (int i = 0; i < selection.length; i++) {
						Object selected = selection[i];
						if (selected instanceof IFolder) {
							IPath path = ((IFolder) selected).getFullPath();
							IPath relativePath = path
									.makeRelativeTo(getResourceRoot(exclusion).getFullPath());

							exceptionsList.add(relativePath.toString());
							ExclusionInstance instance = oldExclusionInstanceStrings.get(relativePath
									.toString());
							if (instance == null) {
								instance = new ExclusionInstance();
								instance.setExclusionType(ExclusionType.FOLDER);
								instance.setParentExclusion(exclusion);
								instance.setResource((IResource) selected);
								instance.setDisplayString(relativePath.toString());
								exclusion.addExclusionInstance(instance);
							} else {
								oldExclusionInstanceStrings.remove(relativePath.toString());
							}

							exclusionInstanceStrings.put(instance.getDisplayString(), instance);
							exclusionInstanceResources.put(instance.getDisplayString(), selected);
						}
					}
					// remove deprecated exclusion instances
					oldExclusionInstanceStrings.keySet();
					Iterator<String> iterator = oldExclusionInstanceStrings.keySet().iterator();
					while (iterator.hasNext()) {
						String toRemove = iterator.next();
						ExclusionInstance instanceToRemove = oldExclusionInstanceStrings.get(toRemove);
						exclusion.removeExclusionInstance(instanceToRemove);
						iterator.remove();
					}
				}
			}

		});

		Button deleteButton = new Button(buttonComp, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		deleteButton.setLayoutData(gridData);
		deleteButton.setText(Messages.RefreshPolicyExceptionDialog_deleteButtonLabel);

		deleteButton.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selected = exceptionsList.getSelection();
				if (selected.length < 1)
					return;

				for (int i = 0; i < selected.length; i++) {
					String folderToRemove = selected[i];
					ExclusionInstance instanceToRemove = exclusionInstanceStrings.get(folderToRemove);

					// Iterator<Object> iterator = selectedFolders.iterator();

					// while (iterator.hasNext()) {
					// IPath path = ((IFolder)iterator.next()).getFullPath();
					// IPath relativePath = path.makeRelativeTo(getResourceRoot(exclusionRoot).getFullPath());
					// if (relativePath.toString().compareTo(folderToRemove) == 0) {
					// iterator.remove();
					// break;
					// }
					// }
					exclusion.removeExclusionInstance(instanceToRemove);
					exclusionInstanceStrings.remove(folderToRemove);
					exclusionInstanceResources.remove(folderToRemove);
				}
				exceptionsList.remove(exceptionsList.getSelectionIndices());
			}

		});
	}

	private IResource getResourceRoot(RefreshExclusion exclusion) {
		if (exclusion.getParentExclusion() != null) {
			return getResourceRoot(exclusion.getParentExclusion());
		}
		return exclusion.getParentResource();
	}
}
