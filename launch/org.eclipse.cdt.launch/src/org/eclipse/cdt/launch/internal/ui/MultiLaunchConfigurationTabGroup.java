/*******************************************************************************
 *  Copyright (c) 2009, 2010 QNX Software Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *      Freescale Semiconductor
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate;
import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate.LaunchElement;
import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate.LaunchElement.EPostLaunchAction;
import org.eclipse.cdt.launch.ui.CommonTabLite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Tab group for Launch Group.
 */
public class MultiLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	static class ContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		protected List<LaunchElement> input;

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
			input = null;
		}

		@SuppressWarnings("unchecked") // nothing we can do about this
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof List<?>)
				input = (List<LaunchElement>) newInput;
		}

		public Object[] getChildren(Object parentElement) {
			return (parentElement == input) ? input.toArray() : null;
		}

		public Object getParent(Object element) {
			return (element == input) ? null : input;
		}

		public boolean hasChildren(Object element) {
			return (element == input) ? (input.size() > 0) : false;
		}
	}
	static class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (!(element instanceof MultiLaunchConfigurationDelegate.LaunchElement))
				return null;
			if (columnIndex == 0) {
				MultiLaunchConfigurationDelegate.LaunchElement el = (MultiLaunchConfigurationDelegate.LaunchElement) element;
				if (el.data == null || !MultiLaunchConfigurationDelegate.isValidLaunchReference(el.data)) {
					Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
					return errorImage;
				}
				
				try {
	                String key = el.data.getType().getIdentifier();
	                return DebugPluginImages.getImage(key);
                } catch (CoreException e) {
                	Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
					return errorImage;
                }
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof MultiLaunchConfigurationDelegate.LaunchElement))
				return null;
			MultiLaunchConfigurationDelegate.LaunchElement el = (MultiLaunchConfigurationDelegate.LaunchElement) element;
			
			// launch name
			if (columnIndex == 0) {
				try {
					return (el.data != null) ? el.data.getType().getName() + "::" + el.name : el.name; //$NON-NLS-1$
				} catch (CoreException e) {
					return el.name;
				}
			}

			// launch mode 
			if (columnIndex == 1)
				return el.mode;
			
			// launch post action
			if (columnIndex == 2) {
				EPostLaunchAction action = el.action;
				switch (action) {
				case NONE:
					return ""; //$NON-NLS-1$
				case WAIT_FOR_TERMINATION:
					return LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.WaitUntilTerminated"); //$NON-NLS-1$
				case DELAY:
					final Object actionParam = el.actionParam;
					return LaunchMessages.getFormattedString("MultiLaunchConfigurationTabGroup.13", //$NON-NLS-1$
							actionParam instanceof Integer ? Integer.toString((Integer)actionParam) : "?"); //$NON-NLS-1$
				default:
					assert false : "new post launch action missing logic here"; //$NON-NLS-1$
					return ""; //$NON-NLS-1$
				}
			}
			return null;
		}
	}
	
	static class CheckStateProvider implements ICheckStateProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
		 */
		public boolean isChecked(Object element) {
			if (element instanceof LaunchElement) {
				return ((LaunchElement)element).enabled;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
		 */
		public boolean isGrayed(Object element) {
			return false;
		}
	}
	static abstract class ButtonComposite extends Composite implements SelectionListener {
		Button upButton;
		Button downButton;
		Button addButton;
		Button deleteButton;
		Button editButton;

		public ButtonComposite(Composite parent, int style) {
			super(parent, style);
			setLayout(new GridLayout());
			upButton = createPushButton(this, LaunchMessages.getString("MultiLaunchConfigurationTabGroup.1")); //$NON-NLS-1$
			downButton = createPushButton(this, LaunchMessages.getString("MultiLaunchConfigurationTabGroup.2")); //$NON-NLS-1$
			editButton = createPushButton(this, LaunchMessages.getString("MultiLaunchConfigurationTabGroup.3")); //$NON-NLS-1$
			addButton = createPushButton(this, LaunchMessages.getString("MultiLaunchConfigurationTabGroup.4")); //$NON-NLS-1$
			deleteButton = createPushButton(this, LaunchMessages.getString("MultiLaunchConfigurationTabGroup.5")); //$NON-NLS-1$
		
		}

		protected void updateWidgetEnablement(){
			
		}
		/**
		 * Helper method to create a push button.
		 * 
		 * @param parent
		 *            the parent control
		 * @param key
		 *            the resource name used to supply the button's label text
		 * @return Button
		 */
		protected Button createPushButton(Composite parent, String key) {
			Button button = new Button(parent, SWT.PUSH);
			button.setText(key);
			button.setFont(parent.getFont());
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			button.setLayoutData(data);
			button.addSelectionListener(this);
			return button;
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// nothing
		}

		public void widgetSelected(SelectionEvent e) {
			Widget widget = e.widget;
			if (widget == upButton) {
				upPressed();
			} else if (widget == downButton) {
				downPressed();
			} else if (widget == addButton) {
				addPressed();
			} else if (widget == deleteButton) {
				deletePressed();
			} else if (widget == editButton) {
				editPressed();
			}
		}

		protected abstract void addPressed();

		protected abstract void editPressed();
		
		protected abstract void deletePressed();

		protected abstract void downPressed();

		protected abstract void upPressed();
	}
	static class GroupLaunchTab extends AbstractLaunchConfigurationTab {
		protected CheckboxTreeViewer treeViewer;
		protected List<LaunchElement> input = new ArrayList<LaunchElement>();
		private String mode;

		public GroupLaunchTab(String mode) {
	        this.mode = mode;
        }

		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			//comp.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
			comp.setLayout(new GridLayout(2, false));
			treeViewer = new CheckboxTreeViewer(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			Tree table = treeViewer.getTree();
			table.setFont(parent.getFont());
			treeViewer.setContentProvider(new ContentProvider());
			treeViewer.setLabelProvider(new LabelProvider());
			treeViewer.setCheckStateProvider(new CheckStateProvider());
			table.setHeaderVisible(true);
			table.setLayoutData(new GridData(GridData.FILL_BOTH));
			TreeColumn col1 = new TreeColumn(table, SWT.NONE);
			col1.setText(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.6")); //$NON-NLS-1$
			col1.setWidth(300);
			TreeColumn col2 = new TreeColumn(table, SWT.NONE);
			col2.setText(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.7")); //$NON-NLS-1$
			col2.setWidth(100);
			TreeColumn col3 = new TreeColumn(table, SWT.NONE);
			col3.setText(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.12")); //$NON-NLS-1$
			col3.setWidth(100);
		
			treeViewer.setInput(input);
			final ButtonComposite buts = new ButtonComposite(comp, SWT.NONE) {
				protected void addPressed() {
					MultiLaunchConfigurationSelectionDialog dialog = 
						MultiLaunchConfigurationSelectionDialog.createDialog(
								treeViewer.getControl().getShell(), mode, false);
					if (dialog.open() == Dialog.OK) {
						ILaunchConfiguration[] configs = dialog.getSelectedLaunchConfigurations();
						if (configs.length < 1) 
							return;
						for (ILaunchConfiguration config : configs) {
							MultiLaunchConfigurationDelegate.LaunchElement el = new MultiLaunchConfigurationDelegate.LaunchElement();
							input.add(el);
							el.index = input.size() - 1;
							el.enabled = true;
							el.name = config.getName();
							el.data = config;
							el.mode = dialog.getMode();
							el.action = dialog.getAction();
							el.actionParam = dialog.getActionParam();
							treeViewer.refresh(true);
							treeViewer.setChecked(el, el.enabled);
						}
						updateWidgetEnablement();
						updateLaunchConfigurationDialog();
					}
				}
				protected void updateWidgetEnablement(){
					downButton.setEnabled(isDownEnabled());
					upButton.setEnabled(isUpEnabled());

					int selectionCount = getSelectionCount();
					editButton.setEnabled(selectionCount == 1);
					deleteButton.setEnabled(selectionCount > 0);
				}
				

				protected void editPressed() {
					int index = getSingleSelectionIndex();
					if (index < 0)
						return;
					MultiLaunchConfigurationDelegate.LaunchElement el = input.get(index);
					MultiLaunchConfigurationSelectionDialog dialog = 
						MultiLaunchConfigurationSelectionDialog.createDialog(
								treeViewer.getControl().getShell(), el.mode, true);
					if (MultiLaunchConfigurationDelegate.isValidLaunchReference(el.data)) {
						dialog.setInitialSelection(el);
					}
					if (dialog.open() == Dialog.OK) {
						ILaunchConfiguration[] confs = dialog.getSelectedLaunchConfigurations();
						if (confs.length < 0) 
							return;
						assert confs.length == 1 : "invocation of the dialog for editing an entry sholdn't allow OK to be hit if the user chooses multiple launch configs in the dialog"; //$NON-NLS-1$
						el.name = confs[0].getName();
						el.data = confs[0];
						el.mode = dialog.getMode();
						el.action = dialog.getAction();
						el.actionParam = dialog.getActionParam();
						treeViewer.refresh(true);
						updateWidgetEnablement();
						updateLaunchConfigurationDialog();
					}
				}
				protected void deletePressed() {
					int[] indices = getMultiSelectionIndices();
					if (indices.length < 1)
						return;
					// need to delete from high to low
					for (int i = indices.length - 1; i >= 0; i--) {
						input.remove(indices[i]);
					}
					treeViewer.refresh(true);
					updateWidgetEnablement();
					updateLaunchConfigurationDialog();
				}

				/**
				 * @return the index of the selection if a single item is
				 *         selected. If zero or multiple are selected, -1 is
				 *         returned
				 */
				private int getSingleSelectionIndex() {
					StructuredSelection sel = (StructuredSelection) treeViewer.getSelection();
					if (sel.size() != 1)
						return -1;
					MultiLaunchConfigurationDelegate.LaunchElement el = ((MultiLaunchConfigurationDelegate.LaunchElement) sel
					        .getFirstElement());
					return input.indexOf(el);
				}

				/**
				 * @return the indices of one or more selected items. Indices
				 *         are always returned in ascending order
				 */
				private int[] getMultiSelectionIndices() {
					StructuredSelection sel = (StructuredSelection) treeViewer.getSelection();
					List<Integer> indices = new ArrayList<Integer>();
					
					for (Iterator<?> iter = sel.iterator(); iter.hasNext(); ) {
						MultiLaunchConfigurationDelegate.LaunchElement el = (MultiLaunchConfigurationDelegate.LaunchElement)iter.next();
						indices.add(input.indexOf(el));
						
					}
					int[] result = new int[indices.size()];
					for (int i = 0; i < result.length; i++) {
						result[i] = indices.get(i);
					}
					return result;
				}

				private int getSelectionCount() {
					return ((StructuredSelection)treeViewer.getSelection()).size();
				}
				

				protected void downPressed() {
					if (!isDownEnabled()) return;
					int index = getSingleSelectionIndex();
					
					MultiLaunchConfigurationDelegate.LaunchElement x = input.get(index);
					input.set(index, input.get(index + 1));
					input.set(index + 1, x);
					treeViewer.refresh(true);
					updateWidgetEnablement();
					updateLaunchConfigurationDialog();
				}
				
				protected boolean isDownEnabled() {
					final int index = getSingleSelectionIndex();
	                return (index >= 0) && (index != input.size() - 1);
                }
				
				protected boolean isUpEnabled(){
					return getSingleSelectionIndex() > 0;
				}

				protected void upPressed() {
					if (!isUpEnabled()) return;
					int index = getSingleSelectionIndex();
					MultiLaunchConfigurationDelegate.LaunchElement x = input.get(index);
					input.set(index, input.get(index - 1));
					input.set(index - 1, x);
					treeViewer.refresh(true);
					updateWidgetEnablement();
					updateLaunchConfigurationDialog();
				}
			};
			treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					buts.updateWidgetEnablement();
				}
			});
			
			treeViewer.getTree().addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					buts.editPressed();
				}
			});
			
			treeViewer.addCheckStateListener(new ICheckStateListener(){
				public void checkStateChanged(CheckStateChangedEvent event) {
					((LaunchElement)event.getElement()).enabled = event.getChecked();
					updateLaunchConfigurationDialog();
				}
			});
			buts.updateWidgetEnablement();
			GridData layoutData = new GridData(GridData.GRAB_VERTICAL);
			layoutData.verticalAlignment = SWT.BEGINNING;
			buts.setLayoutData(layoutData);
		}

		public String getName() {
			return LaunchMessages.getString("MultiLaunchConfigurationTabGroup.10"); //$NON-NLS-1$
		}

		public void initializeFrom(ILaunchConfiguration configuration) {
			MultiLaunchConfigurationDelegate.createLaunchElements(configuration, input);
			if (treeViewer != null) {
				treeViewer.refresh(true);
			}
		}

		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			MultiLaunchConfigurationDelegate.storeLaunchElements(configuration, input);
		}

		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
			// defaults is empty list
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
		 */
		@Override
		public boolean isValid(ILaunchConfiguration launchConfig) {
			setMessage(null);
			setErrorMessage(null);
			int validLaunches = 0;
			// test if each launch is valid
			for (LaunchElement element : input) {
				if (element.enabled) { 
					if ( element.data == null) {
						// error referencing invalid launch
						setErrorMessage(MessageFormat.format(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.14"), //$NON-NLS-1$
								element.name));
						return false;
					} else if (!MultiLaunchConfigurationDelegate.isValidLaunchReference(element.data)) {
						// error referencing invalid launch
						setErrorMessage(MessageFormat.format(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.15"), //$NON-NLS-1$
								element.name));
						return false;
					}
					validLaunches++;
				} 
			}
			if (validLaunches < 1) {
				// must have at least one valid and enabled launch
				setErrorMessage(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.16")); //$NON-NLS-1$
				return false;				
			}
			return true;
		}
	}

	public MultiLaunchConfigurationTabGroup() {
		// nothing
	}

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {//
				new GroupLaunchTab(mode), //
				new CommonTabLite() //
		};
		setTabs(tabs);
	}
}
