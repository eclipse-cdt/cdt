/***********************************************************************************************************************************
 * Created on Apr 27, 2004
 * 
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 **********************************************************************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.ICPathContainerPage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchContentProvider;

public class ProjectContainerPage extends WizardPage implements ICPathContainerPage {

	private int fFilterType;
	private CheckboxTableViewer viewer;
	private ICProject fCProject;

	private class WorkbenchCPathContentProvider extends WorkbenchContentProvider {

		public Object[] getChildren(Object element) {
			if (element instanceof ICProject) {
				try {
					IPathEntry[] entries = ((ICProject) element).getRawPathEntries();
					List list = new ArrayList(entries.length);
					for (int i = 0; i < entries.length; i++) {
						if (fFilterType == entries[i].getEntryKind() && entries[i].isExported()) {
							list.add(CPElement.createFromExisting(entries[i], (ICProject) element));
						}
					}
					return list.toArray();
				} catch (CModelException e) {
					CUIPlugin.getDefault().log(e);
					return new Object[0];
				}
			}
			return super.getChildren(element);
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ICProject) {
				try {
					IPathEntry[] entries = ((ICProject) element).getRawPathEntries();
					for (int i = 0; i < entries.length; i++) {
						if (fFilterType == entries[i].getEntryKind() && entries[i].isExported()) {
							return true;
						}
					}
				} catch (CModelException e) {
					CUIPlugin.getDefault().log(e);
					return false;
				}
			}
			return super.hasChildren(element);
		}

		public Object getParent(Object element) {
			if (element instanceof CPElement) {
				return ((CPElement) element).getCProject().getProject();
			}
			return super.getParent(element);
		}
	}

	protected ProjectContainerPage(int filterType) {
		super("projectContainerPage"); //$NON-NLS-1$
		setTitle(CPathEntryMessages.getString("ProjectContainerPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("ProjectContainerPage.description")); //$NON-NLS-1$
		setImageDescriptor(CPluginImages.DESC_WIZBAN_ADD_LIBRARY);
		fFilterType = filterType;
		validatePage();
	}

	public void initialize(ICProject project, IPathEntry[] currentEntries) {
		fCProject = project;
	}

	public boolean finish() {
		return true;
	}

	public IPathEntry[] getContainerEntries() {
		return /*viewer != null ? (IPathEntry[]) viewer.getCheckedElements(): */new IPathEntry[0];
	}

	public void setSelection(IPathEntry containerEntry) {
		if (containerEntry != null) {
			viewer.setSelection(new StructuredSelection(containerEntry));
		}
	}

	public void createControl(Composite parent) {
		// create a composite with standard margins and spacing
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(CPathEntryMessages.getString("ProjectContainerPage.label")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setContentProvider(new WorkbenchCPathContentProvider());
		viewer.setLabelProvider(new CPElementLabelProvider());
		viewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				validatePage();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 400;
		gd.heightHint = 300;
		viewer.getTable().setLayoutData(gd);
		viewer.addFilter(new ViewerFilter() {

			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return true;
			}
		});
		setControl(container);
		validatePage();
	}

	/**
	 * Method validatePage.
	 */
	private void validatePage() {
		setPageComplete(getSelected() != null);
	}

	private IPathEntry getSelected() {
		return getContainerEntries().length > 0 ? getContainerEntries()[0] : null;
	}
}