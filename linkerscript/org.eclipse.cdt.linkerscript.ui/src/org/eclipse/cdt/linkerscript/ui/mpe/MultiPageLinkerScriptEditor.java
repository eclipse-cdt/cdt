package org.eclipse.cdt.linkerscript.ui.mpe;

import org.eclipse.cdt.linkerscript.ui.internal.LinkerscriptActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocumentProvider;

import com.google.inject.Injector;

public class MultiPageLinkerScriptEditor extends FormEditor {
	private static final String SYNTAX_ERROR_MSG = "Syntax error in Linker Script, please correct in the text editor before continuing.";
	private static final String SYNTAX_ERROR_KEY = "syntax error";

	private static final Image ERROR_IMAGE = PlatformUI.getWorkbench().getSharedImages()
			.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);

	private XtextEditor editor;

	private IResourceChangeListener resourceChangeListener = event -> {
		// Close editor if containing project closes
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						IEditorInput editorInput = editor.getEditorInput();
						if (editorInput instanceof IFileEditorInput) {
							IFile inputFile = ((IFileEditorInput) editorInput).getFile();
							if (inputFile.getProject().equals(event.getResource())) {
								IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
								pages[i].closeEditor(editorPart, true);
							}
						}
					}
				}
			});
		}
	};
	private LinkerSectionPage linkerSectionPage;

	private LinkerMemoryPage linkerMemoryPage;

	public MultiPageLinkerScriptEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}

	/**
	 * Creates XtextEditor of the multi-page editor
	 */
	private void createXtextEditorPage() {
		try {
			Injector myInjector = LinkerscriptActivator.getInstance()
					.getInjector("org.eclipse.cdt.linkerscript.LinkerScript");
			editor = myInjector.getInstance(XtextEditor.class);
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
			setPartName(editor.getTitle());

		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	@Override
	public boolean isSaveAsAllowed() {
		return editor.isSaveAsAllowed();
	}

	@Override
	protected void addPages() {
		createXtextEditorPage();
		try {
			linkerSectionPage = new LinkerSectionPage(this);
			addPage(linkerSectionPage);
			linkerMemoryPage = new LinkerMemoryPage(this);
			addPage(linkerMemoryPage);
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested form editor", null, e.getStatus());
		}

		// TODO: this is "pushing" error messages, but pages need to "own" this
		// themselves as the pages may not have been created before error
		// message is pushed.
		getXtextDocument().addModelListener(resource -> {
			IParseResult parseResult = resource.getParseResult();
			boolean error = parseResult != null && parseResult.hasSyntaxErrors();
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				if (error) {
					setPageImage(0, ERROR_IMAGE);
					setSyntaxError(linkerSectionPage.getManagedForm());
					setSyntaxError(linkerMemoryPage.getManagedForm());
				} else {
					setPageImage(0, null);
					clearSyntaxError(linkerSectionPage.getManagedForm());
					clearSyntaxError(linkerMemoryPage.getManagedForm());
				}
			});
		});
	}

	private void setSyntaxError(IManagedForm managedForm) {
		if (managedForm != null) {
			IMessageManager messageManager = managedForm.getMessageManager();
			if (messageManager != null) {
				messageManager.addMessage(SYNTAX_ERROR_KEY, SYNTAX_ERROR_MSG, null, IMessageProvider.ERROR);
			}
		}
	}

	private void clearSyntaxError(IManagedForm managedForm) {
		if (managedForm != null) {
			IMessageManager messageManager = managedForm.getMessageManager();
			if (messageManager != null) {
				messageManager.removeMessage(SYNTAX_ERROR_KEY);
			}
		}
	}

	public IXtextDocument getXtextDocument() {
		IEditorInput editorInput = getEditorInput();
		IDocumentProvider documentProvider = (XtextDocumentProvider) editor.getDocumentProvider();
		IDocument document = documentProvider.getDocument(editorInput);
		if (document instanceof IXtextDocument) {
			IXtextDocument document2 = (IXtextDocument) document;
			return document2;
		}

		return null;
	}
}
