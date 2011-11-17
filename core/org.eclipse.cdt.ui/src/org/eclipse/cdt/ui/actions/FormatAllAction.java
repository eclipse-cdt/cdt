/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.corext.util.Resources;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.cdt.internal.ui.text.CFormattingStrategy;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * Formats the code of the translation units contained in the selection.
 * <p>
 * The action is applicable to selections containing elements of
 * type <code>ITranslationUnit</code>, <code>ICContainer</code>
 * and <code>ICProject</code>.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 5.3
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FormatAllAction extends SelectionDispatchAction {

	/*
	 * Class implements IObjectActionDelegate
	 */
	public static class ObjectDelegate implements IObjectActionDelegate {
		private FormatAllAction fAction;
		@Override
		public void setActivePart(IAction action, IWorkbenchPart targetPart) {
			fAction= new FormatAllAction(targetPart.getSite());
		}
		@Override
		public void run(IAction action) {
			fAction.run();
		}
		@Override
		public void selectionChanged(IAction action, ISelection selection) {
			if (fAction == null)
				action.setEnabled(false);
		}
	}

	private DocumentRewriteSession fRewriteSession;

	/**
	 * Creates a new <code>FormatAllAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param site the site providing context information for this action
	 */
	public FormatAllAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.FormatAllAction_label);
		setToolTipText(ActionMessages.FormatAllAction_tooltip);
		setDescription(ActionMessages.FormatAllAction_description);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.FORMAT_ALL);
	}


	@Override
	public void selectionChanged(ITextSelection selection) {
		// do nothing
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(isEnabled(selection));
	}

	private ITranslationUnit[] getTranslationUnits(IStructuredSelection selection) {
		HashSet<ICElement> result= new HashSet<ICElement>();
		Object[] selected= selection.toArray();
		for (int i= 0; i < selected.length; i++) {
			try {
				if (selected[i] instanceof ICElement) {
					ICElement elem= (ICElement) selected[i];
					if (elem.exists()) {
						switch (elem.getElementType()) {
							case ICElement.C_UNIT:
								result.add(elem);
								break;
							case ICElement.C_CCONTAINER:
								collectTranslationUnits((ICContainer) elem, result);
								break;
							case ICElement.C_PROJECT:
								collectTranslationUnits((ICProject) elem, result);
								break;
						}
					}
				} else if (selected[i] instanceof IProject) {
					final IProject project = (IProject) selected[i];
					if (CoreModel.hasCNature(project)) {
						collectTranslationUnits(CoreModel.getDefault().create(project), result);
					}
				}
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return result.toArray(new ITranslationUnit[result.size()]);
	}

	private void collectTranslationUnits(ICProject project, Collection<ICElement> result) throws CModelException {
		ISourceRoot[] roots = project.getSourceRoots();
		for (ISourceRoot root : roots) {
			collectTranslationUnits(root, result);
		}
	}

	private void collectTranslationUnits(ICContainer container, Collection<ICElement> result) throws CModelException {
		ICElement[] children= container.getChildren();
		for (int i= 0; i < children.length; i++) {
			ICElement elem= children[i];
			if (elem.exists()) {
				switch (elem.getElementType()) {
				case ICElement.C_UNIT:
					result.add(elem);
					break;
				case ICElement.C_CCONTAINER:
					collectTranslationUnits((ICContainer) elem, result);
					break;
				}
			}
		}
	}

	private boolean isEnabled(IStructuredSelection selection) {
		Object[] selected= selection.toArray();
		for (int i= 0; i < selected.length; i++) {
			if (selected[i] instanceof ICElement) {
				ICElement elem= (ICElement) selected[i];
				if (elem.exists()) {
					switch (elem.getElementType()) {
						case ICElement.C_UNIT:
						case ICElement.C_CCONTAINER:
						case ICElement.C_PROJECT:
							return true;
					}
				}
			} else if (selected[i] instanceof IProject) {
				if (CoreModel.hasCNature((IProject) selected[i])) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run(ITextSelection selection) {
	}

	@Override
	public void run(IStructuredSelection selection) {
		ITranslationUnit[] tus= getTranslationUnits(selection);
		if (tus.length == 0)
			return;
		if (tus.length > 1) {
			int returnCode= OptionalMessageDialog.open("FormatAll",  //$NON-NLS-1$
					getShell(),
					ActionMessages.FormatAllAction_noundo_title,
					null,
					ActionMessages.FormatAllAction_noundo_message,
					MessageDialog.WARNING,
					new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
					0);
			if (returnCode != OptionalMessageDialog.NOT_SHOWN &&
					returnCode != Window.OK ) return;
		}

		IStatus status= Resources.makeCommittable(getResources(tus), getShell());
		if (!status.isOK()) {
			ErrorDialog.openError(getShell(), ActionMessages.FormatAllAction_failedvalidateedit_title, ActionMessages.FormatAllAction_failedvalidateedit_message, status);
			return;
		}

		runOnMultiple(tus);
	}

	private IResource[] getResources(ITranslationUnit[] tus) {
		IResource[] res= new IResource[tus.length];
		for (int i= 0; i < res.length; i++) {
			res[i]= tus[i].getResource();
		}
		return res;
	}

	/**
	 * Perform format all on the given translation units.
	 * @param tus The translation units to format.
	 */
	public void runOnMultiple(final ITranslationUnit[] tus) {
		try {
			String message= ActionMessages.FormatAllAction_status_description;
			final MultiStatus status= new MultiStatus(CUIPlugin.PLUGIN_ID, IStatus.OK, message, null);

			if (tus.length == 1) {
				EditorUtility.openInEditor(tus[0]);
			}

			PlatformUI.getWorkbench().getProgressService().run(true, true, new WorkbenchRunnableAdapter(new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) {
					doRunOnMultiple(tus, status, monitor);
				}
			})); // workspace lock
			if (!status.isOK()) {
				String title= ActionMessages.FormatAllAction_multi_status_title;
				ErrorDialog.openError(getShell(), title, null, status);
			}
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), ActionMessages.FormatAllAction_error_title, ActionMessages.FormatAllAction_error_message);
		} catch (InterruptedException e) {
			// Canceled by user
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), ActionMessages.FormatAllAction_error_title, ActionMessages.FormatAllAction_error_message);
		}
	}

	private static Map<String, Object> getFomatterSettings(ICProject project) {
		return new HashMap<String, Object>(project.getOptions(true));
	}

	private void doFormat(IDocument document, Map<String, Object> options) {
		final IFormattingContext context = new FormattingContext();
		try {
			context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, options);
			context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.valueOf(true));

			final MultiPassContentFormatter formatter= new MultiPassContentFormatter(ICPartitions.C_PARTITIONING, IDocument.DEFAULT_CONTENT_TYPE);
			formatter.setMasterStrategy(new CFormattingStrategy());

			try {
				startSequentialRewriteMode(document);
				formatter.format(document, context);
			} finally {
				stopSequentialRewriteMode(document);
			}
		} finally {
		    context.dispose();
		}
    }

	@SuppressWarnings("deprecation")
	private void startSequentialRewriteMode(IDocument document) {
		if (document instanceof IDocumentExtension4) {
			IDocumentExtension4 extension= (IDocumentExtension4) document;
			fRewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
		} else if (document instanceof IDocumentExtension) {
			IDocumentExtension extension= (IDocumentExtension) document;
			extension.startSequentialRewrite(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void stopSequentialRewriteMode(IDocument document) {
		if (document instanceof IDocumentExtension4) {
			IDocumentExtension4 extension= (IDocumentExtension4) document;
			extension.stopRewriteSession(fRewriteSession);
		} else if (document instanceof IDocumentExtension) {
			IDocumentExtension extension= (IDocumentExtension)document;
			extension.stopSequentialRewrite();
		}
	}

	private void doRunOnMultiple(ITranslationUnit[] tus, MultiStatus status, IProgressMonitor monitor) throws OperationCanceledException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitor.setTaskName(ActionMessages.FormatAllAction_operation_description);

		monitor.beginTask("", tus.length * 4); //$NON-NLS-1$
		try {
			Map<String, Object> lastOptions= null;
			ICProject lastProject= null;

			for (int i= 0; i < tus.length; i++) {
				ITranslationUnit tu= tus[i];
				IPath path= tu.getPath();
				if (lastProject == null || lastOptions == null|| !lastProject.equals(tu.getCProject())) {
					lastProject= tu.getCProject();
					lastOptions= getFomatterSettings(lastProject);
				}

				ILanguage language= null;
				try {
					language= tu.getLanguage();
				} catch (CoreException exc) {
					// use fallback CPP
					language= GPPLanguage.getDefault();
				}

				// use working copy if available
				ITranslationUnit wc = CDTUITools.getWorkingCopyManager().findSharedWorkingCopy(tu);
				if (wc != null) {
					tu = wc;
				}
				lastOptions.put(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT, tu);
				lastOptions.put(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE, language);
				lastOptions.put(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE, tu.getResource());

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}

				ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
				try {
					try {
						manager.connect(path, LocationKind.IFILE, new SubProgressMonitor(monitor, 1));

						monitor.subTask(path.makeRelative().toString());
						ITextFileBuffer fileBuffer= manager.getTextFileBuffer(path, LocationKind.IFILE);
						boolean wasDirty = fileBuffer.isDirty();

						formatTranslationUnit(fileBuffer, lastOptions);

						if (fileBuffer.isDirty() && !wasDirty) {
							fileBuffer.commit(new SubProgressMonitor(monitor, 2), false);
						} else {
							monitor.worked(2);
						}
					} finally {
						manager.disconnect(path, LocationKind.IFILE, new SubProgressMonitor(monitor, 1));
					}
				} catch (CoreException e) {
					status.add(e.getStatus());
				}
			}
		} finally {
			monitor.done();
		}
	}

	private void formatTranslationUnit(final ITextFileBuffer fileBuffer, final Map<String, Object> options) {
		if (fileBuffer.isShared()) {
			getShell().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					doFormat(fileBuffer.getDocument(), options);
				}
			});
		} else {
			doFormat(fileBuffer.getDocument(), options); // run in context thread
		}
	}

}
