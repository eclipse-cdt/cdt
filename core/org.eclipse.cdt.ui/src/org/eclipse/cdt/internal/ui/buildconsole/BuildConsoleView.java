package org.eclipse.cdt.internal.ui.buildconsole;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ResourceBundle;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleEvent;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextEditorAction;

/**

/**
 * Console view for the desktop. Registered using XML.
 */
public class BuildConsoleView extends ViewPart implements ISelectionListener, IBuildConsoleListener {
	private IProject selProject;
	private Font fFont;
	private IPropertyChangeListener fPropertyChangeListener;
	protected TextViewer fTextViewer;
	IBuildConsoleManager fConsoleManager;
	ClearConsoleAction fClearOutputAction;
	TextEditorAction fCopyAction;
	TextEditorAction fSelectAllAction;
	String origTitle;

	public BuildConsoleView() {
		super();
		fFont = null;
		fPropertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (fTextViewer != null && event.getProperty().equals(CPluginPreferencePage.PREF_CONSOLE_FONT)) {
					initializeWidgetFont(fTextViewer.getTextWidget());
				}
			}
		};
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(fPropertyChangeListener);
		fConsoleManager = CUIPlugin.getDefault().getConsoleManager();
	}

	private void initializeActions() {
		ResourceBundle bundle = CUIPlugin.getResourceBundle();

		IActionBars actionBars = getViewSite().getActionBars();
		fClearOutputAction = new ClearConsoleAction(this);
		fCopyAction = new BuildConsoleAction(bundle, "Editor.Copy.", fTextViewer, fTextViewer.COPY);
		fSelectAllAction = new BuildConsoleAction(bundle, "Editor.SelectAll.", fTextViewer, fTextViewer.SELECT_ALL);

		actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, fCopyAction);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, fSelectAllAction);

		fTextViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				// ensures that the copyAction updates is doability when the selections tate changes
				fCopyAction.update();
			}
		});
	}

	/**
	 * @see ViewPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		fTextViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP | SWT.MULTI);
		fTextViewer.addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
				revealEndOfDocument();
			}
		});
		fTextViewer.setEditable(false);
		origTitle = getTitle();
		initializeWidgetFont(fTextViewer.getTextWidget());

		initializeActions();
		initializeContextMenu(fTextViewer.getControl());
		initializeToolBar();

		WorkbenchHelp.setHelp(fTextViewer.getControl(), ICHelpContextIds.CLEAR_CONSOLE_VIEW);

		setProject(getSite().getPage().getSelection());
		setDocument();
		setTitle();
		getSite().getPage().addSelectionListener(this);
		fConsoleManager.addConsoleListener(this);
	}

	protected void setProject(ISelection selection) {
		selProject = convertSelectionToProject(selection);
	}

	protected void setProject(IProject project) {
		selProject = project;
	}

	protected IProject getProject() {
		return selProject;
	}
	
	protected IDocument setDocument() {
		IProject project = getProject();
		if (project != null ) {
			fTextViewer.setDocument(fConsoleManager.getConsoleDocument(project));
		}
		return null;
	}

	protected void setTitle() {
		String title = origTitle;
		IProject project = getProject();
		if (project != null ) {
			title += " [" + project.getName() + "]";
		}
		setTitle(title);
	}
	
	protected void initializeWidgetFont(StyledText styledText) {
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		String prefKey = CPluginPreferencePage.PREF_CONSOLE_FONT;
		FontData data = null;
		if (store.contains(prefKey) && !store.isDefault(prefKey)) {
			data = PreferenceConverter.getFontData(store, prefKey);
		}
		else {
			data = PreferenceConverter.getDefaultFontData(store, prefKey);
		}
		if (data != null) {
			Font font = new Font(styledText.getDisplay(), data);
			styledText.setFont(font);

			if (fFont != null)
				fFont.dispose();

			fFont = font;
		}
		else {
			// if all the preferences failed
			styledText.setFont(JFaceResources.getTextFont());
		}
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	*/
	public void setFocus() {
		fTextViewer.getControl().setFocus();
	}

	/**
	 * Initializes the context menu
	 */
	protected void initializeContextMenu(Control parent) {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(parent);
		parent.setMenu(menu);
	}

	/**
	 * Adds the text manipulation actions to the <code>ConsoleViewer</code>
	 */
	protected void fillContextMenu(IMenuManager menu) {
		fCopyAction.update();
		menu.add(fCopyAction);
		menu.add(fSelectAllAction);
		menu.add(new Separator());
		menu.add(fClearOutputAction);
	}

	/**
	 * Configures the toolBar.
	 */
	private void initializeToolBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.getToolBarManager().add(fClearOutputAction);
		actionBars.updateActionBars();
	}
	/**
	 * Reveals (makes visible) the end of the current document
	 */
	protected void revealEndOfDocument() {
		IDocument doc = fTextViewer.getDocument();
		int docLength = doc.getLength();
		if (docLength > 0) {
			fTextViewer.revealRange(docLength - 1, 1);
			StyledText widget = fTextViewer.getTextWidget();
			widget.setCaretOffset(docLength);
			widget.showSelection();
		}
	}
	/**
	 * @see WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fPropertyChangeListener != null) {
			IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
			store.removePropertyChangeListener(fPropertyChangeListener);
			fPropertyChangeListener = null;
		}
		if (fFont != null) {
			fFont.dispose();
			fFont = null;
		}		
		getSite().getPage().removeSelectionListener(this);
		fConsoleManager.removeConsoleListener(this);
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject newProject = convertSelectionToProject(selection);
		IProject oldProject = getProject();
		if (oldProject == null ||
		    (newProject != null && !newProject.equals(oldProject))) { 
			setProject(newProject);
			setDocument();
			setTitle();
		}
	}

	public void consoleChange(IBuildConsoleEvent event) {
		if ( event.getType() == IBuildConsoleEvent.CONSOLE_START ) {
			Display display = fTextViewer.getControl().getDisplay();
			selProject = event.getProject();
			display.asyncExec(new Runnable() {
				public void run() {
					setDocument();
					setTitle();
				}
			});
		}
	}

	IProject convertSelectionToProject(ISelection selection) {
		IProject project = null;
		if ( selection == null ) {
			return project;
		}
		try {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			IAdaptable input = (IAdaptable) ssel.getFirstElement();
			if (input != null) {
				IResource resource = null;
				if (input instanceof IResource) {
					resource = (IResource) input;
				} else {
					resource = (IResource) input.getAdapter(IResource.class);
				}
				if (resource != null) {
					project = resource.getProject();
				}
			}
		} catch (ClassCastException e) {
		}
		return project;
	}

	/**
	 * Clears the console
	 */
	void clear() {
		if (selProject != null) {
			fConsoleManager.getConsole(selProject).clear();
		}
	}

}
