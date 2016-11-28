package examplemultipageeditor.editors;

import org.eclipse.cdt.linkerscript.ui.internal.LinkerscriptActivator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.XtextDocumentProvider;

import com.google.inject.Injector;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class MultiPageEditor extends FormEditor implements IResourceChangeListener {

	/** The text editor used in page 0. */
	private XtextEditor editor;
	private LinkerMemoryPage linkerMemoryPage;
	private LinkerSectionPage linkerSectionPage;

	/**
	 * Creates a multi-page editor example.
	 */
	public MultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates XtextEditor of the multi-page editor
	 */
	void createXtextEditorPage() {
		try {
			Injector myInjector = LinkerscriptActivator.getInstance()
					.getInjector("org.eclipse.cdt.linkerscript.LinkerScript");
			editor = myInjector.getInstance(XtextEditor.class);
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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

	/*
	 * (non-Javadoc) Method declared on IEditorPart TODO: What is this, why is
	 * it here?
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		// TODO remove this restriction, especially if XtextEditor does not have it
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		// TODO refresh controls???
		if (newPageIndex == 0) {
//			linkerSectionPage.refresh();
//			linkerSectionPage.setFocus();
//		}
//		if (newPageIndex == 1) {
			linkerMemoryPage.refresh();
			linkerMemoryPage.setFocus();
		}
	}

	/**
	 * Closes all project files on project close.
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput()).getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	@Override
	protected void addPages() {
		try {
//			linkerSectionPage = new LinkerSectionPage(this);
//			addPage(linkerSectionPage);
			linkerMemoryPage = new LinkerMemoryPage(this);
			addPage(linkerMemoryPage);
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested form editor", null, e.getStatus());
		}
		createXtextEditorPage();
	}

	// TODO what is the right way for the other page to get the document provider, e.g. via injection?
	/*package*/ XtextDocumentProvider getDocumentProvider() {
		return (XtextDocumentProvider) editor.getDocumentProvider();
	}
}
