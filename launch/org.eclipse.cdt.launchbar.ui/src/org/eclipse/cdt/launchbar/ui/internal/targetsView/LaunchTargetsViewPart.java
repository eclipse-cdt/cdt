package org.eclipse.cdt.launchbar.ui.internal.targetsView;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.cdt.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;

public class LaunchTargetsViewPart extends ViewPart {

	private TreeViewer treeViewer;
	private final LaunchBarUIManager uiManager;

	public LaunchTargetsViewPart() {
		ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
		uiManager = (LaunchBarUIManager) manager.getAdapter(LaunchBarUIManager.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.NONE);
		treeViewer.setContentProvider(new ITreeContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof LaunchBarManager)
					return true;
				else if (element instanceof ILaunchTargetType)
					return true;
				else if (element instanceof ILaunchTarget)
					return false;
				return false;
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof ILaunchTarget)
					return ((ILaunchTarget) element).getType();
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				// TODO optionally categorize by launch type, maybe
				// return ((LaunchBarManager) inputElement).getLaunchTargetTypes();
				return ((LaunchBarManager) inputElement).getAllLaunchTargets();
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof ILaunchTargetType)
					return ((ILaunchTargetType) parentElement).getTargets();
				return new Object[0];
			}
		});

		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public void sort(Viewer viewer, Object[] elements) {
				if (elements instanceof ILaunchTarget[]) {
					Arrays.sort((ILaunchTarget[]) elements, new Comparator<ILaunchTarget>() {
						@Override
						public int compare(ILaunchTarget o1, ILaunchTarget o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				}
			}
		});

		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchTargetType) {
					return element.getClass().getSimpleName();
				} else if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider targetLabelProvider = uiManager.getLabelProvider(target);
					if (targetLabelProvider != null) {
						return targetLabelProvider.getText(element);
					} else {
						return target.getName();
					}
				} else {
					return super.getText(element);
				}
			}
		});

		final ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class); 
		treeViewer.setInput(launchBarManager);
		launchBarManager.addListener(new ILaunchBarManager.Listener() {
			@Override
			public void launchTargetsChanged() {
				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						treeViewer.setInput(launchBarManager);
					}
				});
			}
			@Override
			public void launchDescriptorRemoved(ILaunchDescriptor descriptor) {
			}
			@Override
			public void activeLaunchTargetChanged() {
			}
			@Override
			public void activeLaunchModeChanged() {
			}
			@Override
			public void activeConfigurationDescriptorChanged() {
			}
		});

		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator());
		menuManager.add(new PropertyDialogAction(getSite(), treeViewer));
		Menu menu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, treeViewer);
		getSite().setSelectionProvider(treeViewer);
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

}
