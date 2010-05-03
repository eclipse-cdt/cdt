/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.ui.refactoring.actions.GettersAndSettersAction;
import org.eclipse.cdt.ui.refactoring.actions.ImplementMethodAction;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;

import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.actions.CDTQuickMenuCreator;
import org.eclipse.cdt.internal.ui.editor.AddIncludeOnSelectionAction;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.editor.SortLinesAction;

/**
 * Action group that adds the source and generate actions to a part's context
 * menu and installs handlers for the corresponding global menu actions.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 4.0
 */
public class GenerateActionGroup extends ActionGroup implements ISelectionChangedListener {
	
	/**
	 * Pop-up menu: id of the source sub menu (value <code>org.eclipse.cdt.ui.source.menu</code>).
	 */
	public static final String MENU_ID= "org.eclipse.cdt.ui.source.menu"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: id of the organize group of the source sub menu (value
	 * <code>organizeGroup</code>).
	 */
	public static final String GROUP_ORGANIZE= "organizeGroup";  //$NON-NLS-1$
	
	/**
	 * Pop-up menu: id of the generate group of the source sub menu (value
	 * <code>generateGroup</code>).
	 */
	public static final String GROUP_GENERATE= "generateGroup";  //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the code group of the source sub menu (value
	 * <code>codeGroup</code>).
	 */
	public static final String GROUP_CODE= "codeGroup";  //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the externalize group of the source sub menu (value
	 * <code>externalizeGroup</code>).
	 */
	private static final String GROUP_EXTERNALIZE= "externalizeGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the comment group of the source sub menu (value
	 * <code>commentGroup</code>).
	 */
	private static final String GROUP_COMMENT= "commentGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the edit group of the source sub menu (value
	 * <code>editGroup</code>).
	 */
	private static final String GROUP_EDIT= "editGroup"; //$NON-NLS-1$
	
	private CEditor fEditor;
	private IWorkbenchSite fSite;
	private String fGroupName= IContextMenuConstants.GROUP_REORGANIZE;
	private List<ISelectionChangedListener> fRegisteredSelectionListeners;
	private List<RefactoringAction> fRefactorActions= new ArrayList<RefactoringAction>();
	
	private AddIncludeOnSelectionAction fAddInclude;
//	private OverrideMethodsAction fOverrideMethods;
//	private GenerateHashCodeEqualsAction fHashCodeEquals;
	private GettersAndSettersAction fAddGetterSetter;
	private ImplementMethodAction fImplementMethod;
//	private AddDelegateMethodsAction fAddDelegateMethods;
//	private AddUnimplementedConstructorsAction fAddUnimplementedConstructors;
//	private GenerateNewConstructorUsingFieldsAction fGenerateConstructorUsingFields;
//	private AddJavaDocStubAction fAddCppDocStub;
	private AddBookmarkAction fAddBookmark;
	private AddTaskAction fAddTaskAction;
//	private ExternalizeStringsAction fExternalizeStrings;
//	private CleanUpAction fCleanUp;	
//	
//	private OrganizeIncludesAction fOrganizeIncludes;
//	private SortMembersAction fSortMembers;
	private SortLinesAction fSortLines;
//	private FormatAllAction fFormatAll;
//	private CopyQualifiedNameAction fCopyQualifiedNameAction;
//	
	private static final String QUICK_MENU_ID= "org.eclipse.cdt.ui.edit.text.c.source.quickMenu"; //$NON-NLS-1$

	private IHandlerActivation fQuickAccessHandlerActivation;
	private IHandlerService fHandlerService;


	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * @param editor the C editor
	 * @param groupName the group name to add the action to
	 * 
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public GenerateActionGroup(CEditor editor, String groupName) {
		fSite= editor.getSite();
		fEditor= editor;
		fGroupName= groupName;
		
		fAddInclude= new AddIncludeOnSelectionAction(editor);
		fAddInclude.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_INCLUDE);
		editor.setAction("AddIncludeOnSelection", fAddInclude); //$NON-NLS-1$
		
//		fOrganizeIncludes= new OrganizeIncludesAction(editor);
//		fOrganizeIncludes.setActionDefinitionId(ICEditorActionDefinitionIds.ORGANIZE_INCLUDES);
//		editor.setAction("OrganizeIncludes", fOrganizeIncludes); //$NON-NLS-1$
//
//		fSortMembers= new SortMembersAction(editor);
//		fSortMembers.setActionDefinitionId(ICEditorActionDefinitionIds.SORT_MEMBERS);
//		editor.setAction("SortMembers", fSortMembers); //$NON-NLS-1$

		fSortLines= new SortLinesAction(editor);
		fSortLines.setActionDefinitionId(ICEditorActionDefinitionIds.SORT_LINES);
		editor.setAction("SortLines", fSortLines); //$NON-NLS-1$
		
//		IAction pastAction= editor.getAction(ITextEditorActionConstants.PASTE);//IWorkbenchActionDefinitionIds.PASTE);
//		fCopyQualifiedNameAction= new CopyQualifiedNameAction(editor, null, pastAction);
//		fCopyQualifiedNameAction.setActionDefinitionId(CopyQualifiedNameAction.JAVA_EDITOR_ACTION_DEFINITIONS_ID);
//		editor.setAction("CopyQualifiedName", fCopyQualifiedNameAction); //$NON-NLS-1$
//
//		fOverrideMethods= new OverrideMethodsAction(editor);
//		fOverrideMethods.setActionDefinitionId(ICEditorActionDefinitionIds.OVERRIDE_METHODS);
//		editor.setAction("OverrideMethods", fOverrideMethods); //$NON-NLS-1$
//		
		fAddGetterSetter= new GettersAndSettersAction(editor);
		fAddGetterSetter.setActionDefinitionId(ICEditorActionDefinitionIds.GETTERS_AND_SETTERS);
		editor.setAction("org.eclipse.cdt.ui.refactor.getters.and.setters", fAddGetterSetter); //$NON-NLS-1$
		
		fImplementMethod = new ImplementMethodAction(editor);
		fImplementMethod.setActionDefinitionId(ICEditorActionDefinitionIds.IMPLEMENT_METHOD);
		editor.setAction("org.eclipse.cdt.ui.refactor.implement.method", fImplementMethod); //$NON-NLS-1$
//
//		fAddDelegateMethods= new AddDelegateMethodsAction(editor);
//		fAddDelegateMethods.setActionDefinitionId(ICEditorActionDefinitionIds.CREATE_DELEGATE_METHODS);
//		editor.setAction("AddDelegateMethods", fAddDelegateMethods); //$NON-NLS-1$
//			
//		fAddUnimplementedConstructors= new AddUnimplementedConstructorsAction(editor);
//		fAddUnimplementedConstructors.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_UNIMPLEMENTED_CONTRUCTORS);
//		editor.setAction("AddUnimplementedConstructors", fAddUnimplementedConstructors); //$NON-NLS-1$		
//
//		fGenerateConstructorUsingFields= new GenerateNewConstructorUsingFieldsAction(editor);
//		fGenerateConstructorUsingFields.setActionDefinitionId(ICEditorActionDefinitionIds.GENERATE_CONSTRUCTOR_USING_FIELDS);
//		editor.setAction("GenerateConstructorUsingFields", fGenerateConstructorUsingFields); //$NON-NLS-1$		
//
//		fHashCodeEquals= new GenerateHashCodeEqualsAction(editor);
//		fHashCodeEquals.setActionDefinitionId(ICEditorActionDefinitionIds.GENERATE_HASHCODE_EQUALS);
//		editor.setAction("GenerateHashCodeEquals", fHashCodeEquals); //$NON-NLS-1$
//
//		fAddCppDocStub= new AddJavaDocStubAction(editor);
//		fAddCppDocStub.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_JAVADOC_COMMENT);
//		editor.setAction("AddJavadocComment", fAddCppDocStub); //$NON-NLS-1$
//		
//		fCleanUp= new CleanUpAction(editor);
//		fCleanUp.setActionDefinitionId(ICEditorActionDefinitionIds.CLEAN_UP);
//		editor.setAction("CleanUp", fCleanUp); //$NON-NLS-1$
//		
//		fExternalizeStrings= new ExternalizeStringsAction(editor);
//		fExternalizeStrings.setActionDefinitionId(ICEditorActionDefinitionIds.EXTERNALIZE_STRINGS);
//		editor.setAction("ExternalizeStrings", fExternalizeStrings); //$NON-NLS-1$	
//				
		installQuickAccessAction();
	}
	
	/**
	 * Creates a new <code>GenerateActionGroup</code>. The group 
	 * requires that the selection provided by the page's selection provider 
	 * is of type <code>org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param page the page that owns this action group
	 */
	public GenerateActionGroup(Page page) {
		this(page.getSite());
	}

	/**
	 * Creates a new <code>GenerateActionGroup</code>. The group 
	 * requires that the selection provided by the part's selection provider 
	 * is of type <code>org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param part the view part that owns this action group
	 */
	public GenerateActionGroup(IViewPart part) {
		this(part.getSite());
	}
	
	private GenerateActionGroup(IWorkbenchSite site) {
		fSite= site;
		ISelectionProvider provider= fSite.getSelectionProvider();
		ISelection selection= provider.getSelection();
		
//		fOverrideMethods= new OverrideMethodsAction(site);
//		fOverrideMethods.setActionDefinitionId(ICEditorActionDefinitionIds.OVERRIDE_METHODS);
//		
		fAddGetterSetter = new GettersAndSettersAction();
		fAddGetterSetter.setActionDefinitionId(ICEditorActionDefinitionIds.GETTERS_AND_SETTERS);
		fAddGetterSetter.setSite(fSite);
		fRefactorActions.add(fAddGetterSetter);

		fImplementMethod = new ImplementMethodAction();
		fImplementMethod.setActionDefinitionId(ICEditorActionDefinitionIds.IMPLEMENT_METHOD);
		fImplementMethod.setSite(fSite);
		fRefactorActions.add(fImplementMethod);

//		fAddDelegateMethods= new AddDelegateMethodsAction(site);
//		fAddDelegateMethods.setActionDefinitionId(ICEditorActionDefinitionIds.CREATE_DELEGATE_METHODS);
//		
//		fAddUnimplementedConstructors= new AddUnimplementedConstructorsAction(site);
//		fAddUnimplementedConstructors.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_UNIMPLEMENTED_CONTRUCTORS);
//		
//		fGenerateConstructorUsingFields= new GenerateNewConstructorUsingFieldsAction(site);
//		fGenerateConstructorUsingFields.setActionDefinitionId(ICEditorActionDefinitionIds.GENERATE_CONSTRUCTOR_USING_FIELDS);
//
//		fHashCodeEquals= new GenerateHashCodeEqualsAction(site);
//		fHashCodeEquals.setActionDefinitionId(ICEditorActionDefinitionIds.GENERATE_HASHCODE_EQUALS);
//
//		fAddCppDocStub= new AddJavaDocStubAction(site);
//		fAddCppDocStub.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_JAVADOC_COMMENT);
		
		fAddBookmark= new AddBookmarkAction(site, true);
		fAddBookmark.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_ADD_BOOKMARK);
		
		fAddTaskAction= new AddTaskAction(site);
		fAddTaskAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_ADD_TASK);
		
//		fExternalizeStrings= new ExternalizeStringsAction(site);
//		fExternalizeStrings.setActionDefinitionId(ICEditorActionDefinitionIds.EXTERNALIZE_STRINGS);
//		
//		fOrganizeIncludes= new OrganizeIncludesAction(site);
//		fOrganizeIncludes.setActionDefinitionId(ICEditorActionDefinitionIds.ORGANIZE_INCLUDES);
//		
//		fSortMembers= new SortMembersAction(site);
//		fSortMembers.setActionDefinitionId(ICEditorActionDefinitionIds.SORT_MEMBERS);
//		
//		fFormatAll= new FormatAllAction(site);
//		fFormatAll.setActionDefinitionId(ICEditorActionDefinitionIds.FORMAT);
//		
//		fCleanUp= new CleanUpAction(site);
//		fCleanUp.setActionDefinitionId(ICEditorActionDefinitionIds.CLEAN_UP);

		
//		fOverrideMethods.update(selection);
//		fAddDelegateMethods.update(selection);
//		fAddUnimplementedConstructors.update(selection);	
//		fGenerateConstructorUsingFields.update(selection);
//		fHashCodeEquals.update(selection);
//		fAddCppDocStub.update(selection);
//		fExternalizeStrings.update(selection);
//		fFindNLSProblems.update(selection);
//		fCleanUp.update(selection);
//		fOrganizeIncludes.update(selection);
//		fSortMembers.update(selection);
//		fFormatAll.update(selection);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection)selection;
			fAddBookmark.selectionChanged(ss);
			fAddTaskAction.selectionChanged(ss);
		} else {
			fAddBookmark.setEnabled(false);
			fAddTaskAction.setEnabled(false);
		}
		
//		registerSelectionListener(provider, fOverrideMethods);
//		registerSelectionListener(provider, fAddDelegateMethods);
//		registerSelectionListener(provider, fAddUnimplementedConstructors);
//		registerSelectionListener(provider, fGenerateConstructorUsingFields);
//		registerSelectionListener(provider, fHashCodeEquals);
//		registerSelectionListener(provider, fAddCppDocStub);
		registerSelectionListener(provider, fAddBookmark);
//		registerSelectionListener(provider, fExternalizeStrings);
//		registerSelectionListener(provider, fFindNLSProblems);
//		registerSelectionListener(provider, fOrganizeIncludes);
//		registerSelectionListener(provider, fFormatAll);
//		registerSelectionListener(provider, fSortMembers);
		registerSelectionListener(provider, fAddTaskAction);
//		registerSelectionListener(provider, fCleanUp);
		
		selectionChanged(new SelectionChangedEvent(provider, selection));
		registerSelectionListener(provider, this);
		
		installQuickAccessAction();
	}
	
	private void installQuickAccessAction() {
		fHandlerService= (IHandlerService)fSite.getService(IHandlerService.class);
		if (fHandlerService != null) {
			IHandler handler= new CDTQuickMenuCreator(fEditor) {
				@Override
				protected void fillMenu(IMenuManager menu) {
					fillQuickMenu(menu);
				}
			}.createHandler();
			fQuickAccessHandlerActivation= fHandlerService.activateHandler(QUICK_MENU_ID, handler);
		}
	}

	private void registerSelectionListener(ISelectionProvider provider, ISelectionChangedListener listener) {
		if (fRegisteredSelectionListeners == null)
			fRegisteredSelectionListeners= new ArrayList<ISelectionChangedListener>(10);
		provider.addSelectionChangedListener(listener);
		fRegisteredSelectionListeners.add(listener);
	}
	
	/*
	 * The state of the editor owning this action group has changed. 
	 * This method does nothing if the group's owner isn't an
	 * editor.
	 */
	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 */
	public void editorStateChanged() {
		Assert.isTrue(isEditorOwner());
	}

	/* 
	 * Method declared in ActionGroup
	 */
	@Override
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}
	
	/* 
	 * Method declared in ActionGroup
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		MenuManager subMenu= new MenuManager(ActionMessages.getString("SourceMenu_label"), MENU_ID);  //$NON-NLS-1$
		subMenu.setActionDefinitionId(QUICK_MENU_ID);
		int added= 0;
		if (isEditorOwner()) {
			added= fillEditorSubMenu(subMenu);
		} else {
			added= fillViewSubMenu(subMenu);
		}
		if (added > 0)
			menu.appendToGroup(fGroupName, subMenu);
	}

	private void fillQuickMenu(IMenuManager menu) {
		if (isEditorOwner()) {
			fillEditorSubMenu(menu);
		} else {
			fillViewSubMenu(menu);
		}
	}
	
	private int fillEditorSubMenu(IMenuManager source) {
		int added= 0;
		source.add(new Separator(GROUP_COMMENT));
		added+= addEditorAction(source, "ToggleComment"); //$NON-NLS-1$
		added+= addEditorAction(source, "AddBlockComment"); //$NON-NLS-1$
		added+= addEditorAction(source, "RemoveBlockComment"); //$NON-NLS-1$
//		added+= addAction(source, fAddCppDocStub);
		source.add(new Separator(GROUP_EDIT));
		added+= addEditorAction(source, ITextEditorActionConstants.SHIFT_RIGHT);
		added+= addEditorAction(source, ITextEditorActionConstants.SHIFT_LEFT);
		added+= addEditorAction(source, "Indent"); //$NON-NLS-1$
		added+= addEditorAction(source, "Format"); //$NON-NLS-1$
		source.add(new Separator(GROUP_ORGANIZE));
		added+= addAction(source, fAddInclude);
//		added+= addAction(source, fOrganizeIncludes);
//		added+= addAction(source, fSortMembers);
		added+= addAction(source, fSortLines);
//		added+= addAction(source, fCleanUp);
		source.add(new Separator(GROUP_GENERATE));
//		added+= addAction(source, fOverrideMethods);
		added+= addAction(source, fAddGetterSetter);
		added+= addAction(source, fImplementMethod);
//		added+= addAction(source, fAddDelegateMethods);
//		added+= addAction(source, fHashCodeEquals);
//		added+= addAction(source, fGenerateConstructorUsingFields);
//		added+= addAction(source, fAddUnimplementedConstructors);
		source.add(new Separator(GROUP_CODE));
		source.add(new Separator(GROUP_EXTERNALIZE));
//		added+= addAction(source, fExternalizeStrings);
		return added;
	}

	private int fillViewSubMenu(IMenuManager source) {
		int added= 0;
		source.add(new Separator(GROUP_COMMENT));
//		added+= addAction(source, fAddCppDocStub);
		source.add(new Separator(GROUP_EDIT));
//		added+= addAction(source, fFormatAll);
		source.add(new Separator(GROUP_ORGANIZE));
		added+= addAction(source, fAddInclude);
//		added+= addAction(source, fOrganizeIncludes);
//		added+= addAction(source, fSortMembers);
//		added+= addAction(source, fCleanUp);
		source.add(new Separator(GROUP_GENERATE));
//		added+= addAction(source, fOverrideMethods);
		added+= addAction(source, fAddGetterSetter);
		added+= addAction(source, fImplementMethod);
//		added+= addAction(source, fAddDelegateMethods);
//		added+= addAction(source, fHashCodeEquals);
//		added+= addAction(source, fGenerateConstructorUsingFields);
//		added+= addAction(source, fAddUnimplementedConstructors);
		source.add(new Separator(GROUP_CODE));
		source.add(new Separator(GROUP_EXTERNALIZE));
//		added+= addAction(source, fExternalizeStrings);
//		added+= addAction(source, fFindNLSProblems);
		return added;
	}

	/* 
	 * Method declared in ActionGroup
	 */
	@Override
	public void dispose() {
		if (fRegisteredSelectionListeners != null) {
			ISelectionProvider provider= fSite.getSelectionProvider();
			for (Iterator<ISelectionChangedListener> iter= fRegisteredSelectionListeners.iterator(); iter.hasNext();) {
				ISelectionChangedListener listener= iter.next();
				provider.removeSelectionChangedListener(listener);
			}
		}
		if (fQuickAccessHandlerActivation != null && fHandlerService != null) {
			fHandlerService.deactivateHandler(fQuickAccessHandlerActivation);
		}
		fEditor= null;
		super.dispose();
	}
	
	private void setGlobalActionHandlers(IActionBars actionBar) {
		actionBar.setGlobalActionHandler(CdtActionConstants.ADD_INCLUDE, fAddInclude);
//		actionBar.setGlobalActionHandler(CdtActionConstants.OVERRIDE_METHODS, fOverrideMethods);
		actionBar.setGlobalActionHandler(CdtActionConstants.GETTERS_AND_SETTERS, fAddGetterSetter);
		actionBar.setGlobalActionHandler(CdtActionConstants.IMPLEMENT_METHOD, fImplementMethod);
//		actionBar.setGlobalActionHandler(CdtActionConstants.GENERATE_DELEGATE_METHODS, fAddDelegateMethods);
//		actionBar.setGlobalActionHandler(CdtActionConstants.ADD_CONSTRUCTOR_FROM_SUPERCLASS, fAddUnimplementedConstructors);		
//		actionBar.setGlobalActionHandler(CdtActionConstants.GENERATE_CONSTRUCTOR_USING_FIELDS, fGenerateConstructorUsingFields);
//		actionBar.setGlobalActionHandler(CdtActionConstants.GENERATE_HASHCODE_EQUALS, fHashCodeEquals);
//		actionBar.setGlobalActionHandler(CdtActionConstants.ADD_CPP_DOC_COMMENT, fAddCppDocStub);
//		actionBar.setGlobalActionHandler(CdtActionConstants.EXTERNALIZE_STRINGS, fExternalizeStrings);
//		actionBar.setGlobalActionHandler(CdtActionConstants.CLEAN_UP, fCleanUp);
//		actionBar.setGlobalActionHandler(CdtActionConstants.ORGANIZE_INCLUDES, fOrganizeIncludes);
//		actionBar.setGlobalActionHandler(CdtActionConstants.SORT_MEMBERS, fSortMembers);
		if (!isEditorOwner()) {
			// editor provides its own implementation of these actions.
			actionBar.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), fAddBookmark);
			actionBar.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), fAddTaskAction);
//			actionBar.setGlobalActionHandler(CdtActionConstants.FORMAT, fFormatAll);
		} else {
//			actionBar.setGlobalActionHandler(CopyQualifiedNameAction.ACTION_HANDLER_ID, fCopyQualifiedNameAction);
		}
	}
	
	private int addAction(IMenuManager menu, IAction action) {
		if (action instanceof IUpdate)
			((IUpdate)action).update();
		if (action != null && action.isEnabled()) {
			menu.add(action);
			return 1;
		}
		return 0;
	}	
	
	private int addEditorAction(IMenuManager menu, String actionID) {
		if (fEditor == null)
			return 0;
		IAction action= fEditor.getAction(actionID);
		if (action == null)
			return 0;
		if (action instanceof IUpdate)
			((IUpdate)action).update();
		if (action.isEnabled()) {
			menu.add(action);
			return 1;
		}
		return 0;
	}
	
	private boolean isEditorOwner() {
		return fEditor != null;
	}
	
	private ICElement getCElement(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o = ss.getFirstElement();
				if (o instanceof ICElement && o instanceof ISourceReference) {
					return (ICElement) o;
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.2
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ICElement celem = getCElement(event.getSelection());
		for (RefactoringAction action : fRefactorActions) {
			action.updateSelection(celem);
		}
	}
}
