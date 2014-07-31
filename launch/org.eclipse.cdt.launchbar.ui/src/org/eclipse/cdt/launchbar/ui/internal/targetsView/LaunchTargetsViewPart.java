package org.eclipse.cdt.launchbar.ui.internal.targetsView;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

public class LaunchTargetsViewPart extends ViewPart {

	private TreeViewer treeViewer;

	public LaunchTargetsViewPart() {
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
				return ((LaunchBarManager) inputElement).getAllLaunchTargetTypes();
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof ILaunchTargetType)
					return ((ILaunchTargetType) parentElement).getTargets();
				return new Object[0];
			}
		});

		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchTargetType)
					return element.getClass().getSimpleName();
				else if (element instanceof ILaunchTarget)
					return ((ILaunchTarget) element).getName();
				return super.getText(element);
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
