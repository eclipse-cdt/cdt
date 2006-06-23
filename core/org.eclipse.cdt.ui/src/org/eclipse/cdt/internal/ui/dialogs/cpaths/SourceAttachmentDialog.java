/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to configure the source attachment of a library (library and zip archive).
 *
 * SourceAttachmentDialog
 */
public class SourceAttachmentDialog extends StatusDialog {
	
	private SourceAttachmentBlock fSourceAttachmentBlock;
	private boolean fApplyChanges;

	/**
	 * Creates an instance of the SourceAttachmentDialog. After
	 * <code>open</code>, the edited paths can be access with
	 * <code>getSourceAttachmentPath</code> and
	 * <code>getSourceAttachmentRootPath</code>. 
	 * @param parent Parent shell for the dialog
	 * @param entry The entry to edit
	 * @param containerPath Path of the container that contains the given entry or
	 * <code>null</code> if the entry does not belong to a container.
	 * @param project Project to which the entry belongs. Can be
	 * <code>null</code> if <code>applyChanges</code> is false and the entry
	 * does not belong to a container.
	 * @param applyChanges If set to <code>true</code>, changes are applied on
	 * OK. If set to false, no changes are commited. When changes are applied,
	 * classpath entries which are not found on the classpath will be added as
	 * new libraries.
	 */
	public SourceAttachmentDialog(Shell parent, ILibraryEntry entry, ICProject project, boolean applyChanges) {
		super(parent);
		fApplyChanges= applyChanges;

		IStatusChangeListener listener= new IStatusChangeListener() {
			public void statusChanged(IStatus status) {
				updateStatus(status);
			}
		};		
		fSourceAttachmentBlock= new SourceAttachmentBlock(listener, entry, project);			
	
		setTitle(CPathEntryMessages.getString("SourceAttachmentDialog.title")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//WorkbenchHelp.setHelp(newShell, IJavaHelpContextIds.SOURCE_ATTACHMENT_DIALOG);
	}		
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite) super.createDialogArea(parent);
			
		Control inner= createSourceAttachmentControls(composite);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));			
		applyDialogFont(composite);		
		return composite;
	}

	/**
	 * Creates the controls for the source attachment configuration.
	 */	
	protected Control createSourceAttachmentControls(Composite composite) {
		return fSourceAttachmentBlock.createControl(composite);
	}
	
	
	/**
	 * Returns the configured source attachment path.
	 */
	public IPath getSourceAttachmentPath() {
		return fSourceAttachmentBlock.getSourceAttachmentPath();
	}

	/**
	 * Returns the configured source attachment path root. Sonce 2.1 source
	 * attachment roots are autodetected. The value returned is therefore always
	 * null.
	 */	
	public IPath getSourceAttachmentRootPath() {
		return fSourceAttachmentBlock.getSourceAttachmentRootPath();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();
		if (fApplyChanges) {
			try {
				IRunnableWithProgress runnable= getRunnable();
				new ProgressMonitorDialog(getShell()).run(true, true, runnable);						
	
			} catch (InvocationTargetException e) {
				String title= CPathEntryMessages.getString("SourceAttachmentDialog.error.title"); //$NON-NLS-1$
				String message= CPathEntryMessages.getString("SourceAttachmentDialog.error.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, getShell(), title, message);
	
			} catch (InterruptedException e) {
				// cancelled
			}
		}
	}
	
	/**
	 * Creates the runnable that configures the project with the new source
	 * attachements.
     */
	protected IRunnableWithProgress getRunnable() {
		return fSourceAttachmentBlock.getRunnable(getShell());
	}

	/**
	 * Helper method that tests if an classpath entry can be found in a
	 * container. <code>null</code> is returned if the entry can not be found
	 * or if the container does not allows the configuration of source
	 * attachments
	 * @param jproject The container's parent project
	 * @param containerPath The path of the container
	 * @param libPath The path of the bibrary to be found
	 * @return IClasspathEntry A classpath entry from the container of
	 * <code>null</code> if the container can not be modified.
     */
	public static IPathEntry getPathEntryToEdit(ICProject jproject, IPath containerPath, IPath libPath) throws CModelException {
		//IPathEntryContainer container= CoreModel.getPathEntryContainer(containerPath, jproject);
		//PathEntryContainerInitializer initializer= CoreModel.getPathEntryContainerInitializer(containerPath.segment(0));
		//if (container != null && initializer != null && initializer.canUpdateClasspathContainer(containerPath, jproject)) {
		//	IPathEntry[] entries= container.getPathEntries();
		//	for (int i= 0; i < entries.length; i++) {
		//		IPathEntry curr= entries[i];
		//		IPathEntry resolved= CoreModel.getResolvedPathEntry(curr);
		//		if (resolved != null && libPath.equals(resolved.getPath())) {
		//			return curr; // return the real entry
		//		}
		//	}
		//}
		return null; // attachment not possible
	}	
	
	

}
