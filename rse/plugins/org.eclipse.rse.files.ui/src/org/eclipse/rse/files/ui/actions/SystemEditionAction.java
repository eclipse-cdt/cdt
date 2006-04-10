/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.compare.EditionSelectionDialog;
import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class SystemEditionAction extends SystemBaseAction
{

	/**
	 * Implements the IStreamContentAccessor and ITypedElement protocols
	 * for a Document.
	 */
	class DocumentBufferNode implements ITypedElement, IStreamContentAccessor
	{

		private IDocument fDocument;
		private IFile fFile;

		DocumentBufferNode(IDocument document, IFile file)
		{
			fDocument = document;
			fFile = file;
		}

		public String getName()
		{
			return fFile.getName();
		}

		public String getType()
		{
			return fFile.getFileExtension();
		}

		public Image getImage()
		{
			return null;
		}

		public InputStream getContents()
		{
			return new ByteArrayInputStream(fDocument.get().getBytes());
		}
	}

	private List _selected;
	private boolean fReplaceMode; 
	private String fBundleName;
	protected boolean fPrevious = false;
	protected String fHelpContextId;

	SystemEditionAction(Shell parent, String title, String tooltip, String bundleName, boolean replaceMode)
	{
		super(title, parent);
		setToolTipText(tooltip);

		fReplaceMode = replaceMode;
		fBundleName = bundleName;
		_selected = new ArrayList();
	}

	
	/**
	 * Called when the selection changes in the systems view.  This determines
	 * the input object for the command and whether to enable or disable
	 * the action.
	 * 
	 * @param selection the current seleciton
	 * @return whether to enable or disable the action
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = false;
		_selected.clear();
		Iterator e = ((IStructuredSelection) selection).iterator();
		if (e.hasNext())
		{
			Object selected = e.next();

			if (selected != null && selected instanceof IRemoteFile)
			{
				RemoteFile file = (RemoteFile) selected;
				if (file.isFile())
				{
					ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter)file.getAdapter(ISystemRemoteElementAdapter.class);
					if (adapter != null)
					{
						ISystemEditableRemoteObject editable = adapter.getEditableRemoteObject(file);
						if (editable != null)
						{
							if (editable.getLocalResource().exists())
							{
								_selected.add(file);
								enable = true;
							}
						}
					}
					
					
					/** FIXME - no longer have a getCachedCopy() method
					try
					{
						if (file.getCachedCopy() != null)
						{
							_selected.add(file);				
							enable = true;				
						}
					}
					catch (SystemMessageException ex)
					{
					}
					*/
				}
			}
		}

		return enable;
	}

	public void run()
	{
		// get cached file
		IFile[] files = getFiles(_selected, fReplaceMode);
		for (int i = 0; i < files.length; i++)
			doFromHistory(files[i]);
	}

	private void doFromHistory(final IFile file)
	{
		//DKM - hack - needed to use compare class loader to load bundle for current locale
		ResourceBundle bundle = ResourceBundle.getBundle(fBundleName, Locale.getDefault(), CompareUIPlugin.class.getClassLoader());

		String title = Utilities.getString(bundle, "title"); //$NON-NLS-1$

		Shell parentShell = CompareUIPlugin.getShell();

		IFileState states[] = null;
		try
		{
			states = file.getHistory(null);
		}
		catch (CoreException ex)
		{
			MessageDialog.openError(parentShell, title, ex.getMessage());
			return;
		}

		if (states == null || states.length <= 0)
		{
			String msg = Utilities.getString(bundle, "noLocalHistoryError"); //$NON-NLS-1$
			MessageDialog.openInformation(parentShell, title, msg);
			return;
		}

		ITypedElement base = new ResourceNode(file);

		IDocument document = getDocument(file);
		ITypedElement target = base;
		if (document != null)
			target = new DocumentBufferNode(document, file);

		ITypedElement[] editions = new ITypedElement[states.length + 1];
		editions[0] = base;
		for (int i = 0; i < states.length; i++)
			editions[i + 1] = new HistoryItem(base, states[i]);

		EditionSelectionDialog d = new EditionSelectionDialog(parentShell, bundle);
		d.setEditionTitleArgument(file.getName());
		d.setEditionTitleImage(CompareUIPlugin.getImage(file));
		//d.setHideIdenticalEntries(false);
		if (fHelpContextId != null)
			d.setHelpContextId(fHelpContextId);

		if (fReplaceMode)
		{

			ITypedElement ti = null;
			if (fPrevious)
				ti = d.selectPreviousEdition(target, editions, null);
			else
				ti = d.selectEdition(target, editions, null);

			if (ti instanceof IStreamContentAccessor)
			{
				IStreamContentAccessor sa = (IStreamContentAccessor) ti;
				try
				{

					if (document != null)
						updateDocument(document, sa);
					else
						updateWorkspace(bundle, parentShell, sa, file);

				}
				catch (InterruptedException x)
				{
					// Do nothing. Operation has been canceled by user.

				}
				catch (InvocationTargetException x)
				{
					String reason = x.getTargetException().getMessage();
					MessageDialog.openError(parentShell, title, Utilities.getFormattedString(bundle, "replaceError", reason)); //$NON-NLS-1$
				}
			}
		}
		else
		{
			d.setCompareMode(true);

			d.selectEdition(target, editions, null);
		}
	}

	private void updateWorkspace(final ResourceBundle bundle, Shell shell, final IStreamContentAccessor sa, final IFile file) throws InvocationTargetException, InterruptedException
	{

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation()
		{
			public void execute(IProgressMonitor pm) throws InvocationTargetException
			{
				try
				{
					String taskName = Utilities.getString(bundle, "taskName"); //$NON-NLS-1$
					pm.beginTask(taskName, IProgressMonitor.UNKNOWN);
					file.setContents(sa.getContents(), false, true, pm);
				}
				catch (CoreException e)
				{
					throw new InvocationTargetException(e);
				}
				finally
				{
					pm.done();
				}
			}
		};

		ProgressMonitorDialog pmdialog = new ProgressMonitorDialog(shell);
		pmdialog.run(false, true, operation);
	}

	private void updateDocument(IDocument document, IStreamContentAccessor sa) throws InvocationTargetException
	{
		try
		{
			InputStream is = sa.getContents();
			String text = Utilities.readString(is, SystemEncodingUtil.ENCODING_UTF_8);
			document.replace(0, document.getLength(), text);
		}
		catch (CoreException e)
		{
			throw new InvocationTargetException(e);
		}
		catch (BadLocationException e)
		{
			throw new InvocationTargetException(e);
		}
	}

	private IDocument getDocument(IFile file)
	{
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null)
			return null;
		IWorkbenchWindow[] ws = wb.getWorkbenchWindows();
		if (ws == null)
			return null;

		FileEditorInput test = new FileEditorInput(file);

		for (int i = 0; i < ws.length; i++)
		{
			IWorkbenchWindow w = ws[i];
			IWorkbenchPage[] wps = w.getPages();
			if (wps != null)
			{
				for (int j = 0; j < wps.length; j++)
				{
					IWorkbenchPage wp = wps[j];
					IEditorPart ep = wp.findEditor(test);
					if (ep instanceof ITextEditor)
					{
						ITextEditor te = (ITextEditor) ep;
						IDocumentProvider dp = te.getDocumentProvider();
						if (dp != null)
						{
							IDocument doc = dp.getDocument(ep);
							if (doc != null)
								return doc;
						}
					}
				}
			}
		}
		return null;
	}

	private IFile[] getFiles(List remoteFiles, boolean modifiable)
	{
		IFile[] result = new IFile[remoteFiles.size()];
		for (int i = 0; i < remoteFiles.size(); i++)
		{
			IRemoteFile remotefile = (IRemoteFile)remoteFiles.get(i);
			SystemEditableRemoteFile eFile = new SystemEditableRemoteFile(remotefile);
			try
			{
				eFile.download(getShell());
				eFile.addAsListener();
				eFile.setLocalResourceProperties();
				IFile localFile = eFile.getLocalResource();
				result[i] = localFile;
			}
			catch (Exception e)
			{
			} 
		}
		
		return result;
	}

}