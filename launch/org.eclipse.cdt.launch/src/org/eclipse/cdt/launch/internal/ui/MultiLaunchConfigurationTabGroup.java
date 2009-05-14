package org.eclipse.cdt.launch.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate;
import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate.LaunchElement;
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
 * Tab group for Launch Group. Only one tab.
 */
public class MultiLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	static class ContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		protected ArrayList<LaunchElement> input;

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
			input = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof ArrayList)
				input = (ArrayList) newInput;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement == input)
				return input.toArray();
			else
				return null;
		}

		public Object getParent(Object element) {
			if (element == input)
				return null;
			return input;
		}

		public boolean hasChildren(Object element) {
			if (element == input)
				return input.size() > 0;
			return false;
		}
	}
	static class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (!(element instanceof MultiLaunchConfigurationDelegate.LaunchElement))
				return null;
			if (columnIndex == 0) {
				MultiLaunchConfigurationDelegate.LaunchElement el = (MultiLaunchConfigurationDelegate.LaunchElement) element;
				if (el.getData() == null) {
					Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
					return errorImage;
				}
				
				try {
	                String key = el.getData().getType().getIdentifier();
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
			if (columnIndex == 0)
				try {
					return (el.getData() != null) ? el.getData().getType().getName() + "::" + el.getName() : el.getName(); //$NON-NLS-1$
				} catch (CoreException e) {
					return el.getName();
				}
			if (columnIndex == 1)
				return el.getMode();
			if (columnIndex == 2)
				return el.getAction();
			return null;
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
		protected ArrayList input = new ArrayList();
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
			table.setHeaderVisible(true);
			table.setLayoutData(new GridData(GridData.FILL_BOTH));
			TreeColumn col1 = new TreeColumn(table, SWT.NONE);
			col1.setText(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.6")); //$NON-NLS-1$
			col1.setWidth(300);
			TreeColumn col2 = new TreeColumn(table, SWT.NONE);
			col2.setText(LaunchMessages.getString("MultiLaunchConfigurationTabGroup.7")); //$NON-NLS-1$
			col2.setWidth(100);
			TreeColumn col3 = new TreeColumn(table, SWT.NONE);
			col3.setText("Action");
			col3.setWidth(100);
		
			treeViewer.setInput(input);
			final ButtonComposite buts = new ButtonComposite(comp, SWT.NONE) {
				protected void addPressed() {
					MultiLaunchConfigurationSelectionDialog dialog = MultiLaunchConfigurationSelectionDialog.createDialog(treeViewer
					        .getControl().getShell(), LaunchMessages.getString("MultiLaunchConfigurationTabGroup.8"),  //$NON-NLS-1$
					        mode
					        );
					if (dialog.open() == Dialog.OK) {
						ILaunchConfiguration conf = dialog.getSelectedLaunchConfiguration();
						if (conf==null) return;
						MultiLaunchConfigurationDelegate.LaunchElement el = new MultiLaunchConfigurationDelegate.LaunchElement();
						input.add(el);
						el.setIndex(input.size() - 1);
						el.setEnabled(true);
						el.setName(conf.getName());
						el.setData(conf);
						el.setMode(dialog.getMode());
						el.setAction(dialog.getAction());
						treeViewer.refresh(true);
						treeViewer.setChecked(el, el.isEnabled());
						updateWidgetEnablement();
						updateLaunchConfigurationDialog();
					}
				}
				protected void updateWidgetEnablement(){
					downButton.setEnabled(isDownEnabled());
					upButton.setEnabled(isUpEnabled());
					int index = getSelIndex();
					deleteButton.setEnabled(index>=0);
					editButton.setEnabled(index>=0);
				}
				

				protected void editPressed() {
					int index = getSelIndex();
					if (index < 0)
						return;
					MultiLaunchConfigurationDelegate.LaunchElement el = (MultiLaunchConfigurationDelegate.LaunchElement) input
					        .get(index);
					MultiLaunchConfigurationSelectionDialog dialog = MultiLaunchConfigurationSelectionDialog.createDialog(treeViewer
					        .getControl().getShell(), LaunchMessages.getString("MultiLaunchConfigurationTabGroup.9"),  //$NON-NLS-1$
					        el.getMode()
					        );
					dialog.setInitialSelection(el);
					if (dialog.open() == Dialog.OK) {
						ILaunchConfiguration conf = dialog.getSelectedLaunchConfiguration();
						if (conf==null) return;
						el.setName(conf.getName());
						el.setData(conf);
						el.setMode(dialog.getMode());
						el.setAction(dialog.getAction());
						treeViewer.refresh(true);
						updateWidgetEnablement();
						updateLaunchConfigurationDialog();
					}
				}
				protected void deletePressed() {
					int index = getSelIndex();
					if (index < 0)
						return;
					input.remove(index);
					treeViewer.refresh(true);
					updateWidgetEnablement();
					updateLaunchConfigurationDialog();
				}

				private int getSelIndex() {
					StructuredSelection sel = (StructuredSelection) treeViewer.getSelection();
					if (sel.isEmpty())
						return -1;
					MultiLaunchConfigurationDelegate.LaunchElement el = ((MultiLaunchConfigurationDelegate.LaunchElement) sel
					        .getFirstElement());
					return input.indexOf(el);
				}

				protected void downPressed() {
					if (!isDownEnabled()) return;
					int index = getSelIndex();
					
					MultiLaunchConfigurationDelegate.LaunchElement x = (MultiLaunchConfigurationDelegate.LaunchElement) input
					        .get(index);
					input.set(index, input.get(index + 1));
					input.set(index + 1, x);
					treeViewer.refresh(true);
					updateWidgetEnablement();
					updateLaunchConfigurationDialog();
				}
				
				protected boolean isDownEnabled() {
					int index = getSelIndex();
	                if (index < 0 || index == input.size() - 1)
						return false;
	                return true;
                }
				
				protected boolean isUpEnabled(){
					int index = getSelIndex();
					if (index <= 0)
						return false;
					return true;
				}

				protected void upPressed() {
					if (!isUpEnabled()) return;
					int index = getSelIndex();
					MultiLaunchConfigurationDelegate.LaunchElement x = (MultiLaunchConfigurationDelegate.LaunchElement) input
					        .get(index);
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
					((LaunchElement)event.getElement()).setEnabled(event.getChecked());
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
				for (Iterator iterator = input.iterator(); iterator.hasNext();) {
					MultiLaunchConfigurationDelegate.LaunchElement el = (MultiLaunchConfigurationDelegate.LaunchElement) iterator
					        .next();
					treeViewer.setChecked(el, el.isEnabled());
				}
				treeViewer.refresh(true);
			}
		}

		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			MultiLaunchConfigurationDelegate.storeLaunchElements(configuration, input);
		}

		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
			// defaults is empty list
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
