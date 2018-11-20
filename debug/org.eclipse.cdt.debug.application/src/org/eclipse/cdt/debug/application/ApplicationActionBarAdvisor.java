/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *    Marc Khouzam (Ericsson) - Update for remote debugging support (bug 450080)
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	public static String COREFILE_COMMAND_ID = "org.eclipse.cdt.debug.application.command.debugCore"; //$NON-NLS-1$
	public static String NEW_EXECUTABLE_COMMAND_ID = "org.eclipse.cdt.debug.application.command.debugNewExecutable"; //$NON-NLS-1$
	public static String ATTACH_EXECUTABLE_COMMAND_ID = "org.eclipse.cdt.debug.application.command.debugAttachedExecutable"; //$NON-NLS-1$
	public static String REMOTE_EXECUTABLE_COMMAND_ID = "org.eclipse.cdt.debug.application.command.debugRemoteExecutable"; //$NON-NLS-1$

	private final IWorkbenchWindow window;

	private IWorkbenchAction corefileAction;
	private IWorkbenchAction newExecutableAction;
	private IWorkbenchAction remoteExecutableAction;
	private IWorkbenchAction attachExecutableAction;
	private IWorkbenchAction quitAction;

	private IWorkbenchAction openPreferencesAction;
	private IWorkbenchAction editActionSetAction;

	private IWorkbenchAction helpContentsAction;
	private IWorkbenchAction helpSearchAction;
	private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction aboutAction;

	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction refreshAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	private IWorkbenchWindow getWindow() {
		return window;
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		quitAction = ActionFactory.QUIT.create(window);
		register(quitAction);

		newExecutableAction = NEW_EXECUTABLE.create(window);
		register(newExecutableAction);

		attachExecutableAction = ATTACH_EXECUTABLE.create(window);
		register(attachExecutableAction);

		remoteExecutableAction = REMOTE_EXECUTABLE.create(window);
		register(remoteExecutableAction);

		corefileAction = COREFILE.create(window);
		register(corefileAction);

		editActionSetAction = ActionFactory.EDIT_ACTION_SETS.create(window);
		register(editActionSetAction);

		helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpContentsAction);

		helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
		register(helpSearchAction);

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(dynamicHelpAction);

		undoAction = ActionFactory.UNDO.create(window);
		register(undoAction);

		redoAction = ActionFactory.REDO.create(window);
		register(redoAction);

		refreshAction = ActionFactory.REFRESH.create(window);
		register(refreshAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setImageDescriptor(
				IDEInternalWorkbenchImages.getImageDescriptor(IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
		register(aboutAction);

		openPreferencesAction = ActionFactory.PREFERENCES.create(window);
		register(openPreferencesAction);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(createWindowMenu());
		menuBar.add(createHelpMenu());
	}

	/**
	 * Creates and returns the File menu.
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(Messages.FileMenuName, IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));

		ActionContributionItem newExecutableItem = new ActionContributionItem(newExecutableAction);
		menu.add(newExecutableItem);

		ActionContributionItem remoteExecutableItem = new ActionContributionItem(remoteExecutableAction);
		menu.add(remoteExecutableItem);

		ActionContributionItem attachExecutableItem = new ActionContributionItem(attachExecutableAction);
		menu.add(attachExecutableItem);

		ActionContributionItem corefileItem = new ActionContributionItem(corefileAction);
		menu.add(corefileItem);

		menu.add(new Separator());

		// This is to make sure "Open File" gets added before Exit
		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));

		ActionContributionItem refreshExecutableItem = new ActionContributionItem(refreshAction);
		menu.add(refreshExecutableItem);

		// This is to make sure "Convert line delimiters" gets added before Exit
		menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));

		// If we're on OS X we shouldn't show this command in the File menu. It
		// should be invisible to the user. However, we should not remove it -
		// the carbon UI code will do a search through our menu structure
		// looking for it when Cmd-Q is invoked (or Quit is chosen from the
		// application menu.
		ActionContributionItem quitItem = new ActionContributionItem(quitAction);
		quitItem.setVisible(!Util.isMac());
		menu.add(new Separator());
		menu.add(quitItem);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(Messages.EditMenuName, IWorkbenchActionConstants.M_EDIT);
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(undoAction);
		menu.add(redoAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());

		menu.add(getCutItem());
		menu.add(getCopyItem());
		menu.add(getPasteItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());

		menu.add(getDeleteItem());
		menu.add(getSelectAllItem());
		menu.add(new Separator());

		menu.add(getFindItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		menu.add(new Separator());

		menu.add(getBookmarkItem());
		menu.add(getTaskItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Window menu.
	 */
	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager(Messages.WindowMenuName, IWorkbenchActionConstants.M_WINDOW);

		addPerspectiveActions(menu);
		Separator sep = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);
		sep.setVisible(!Util.isMac());
		menu.add(sep);

		// See the comment for quit in createFileMenu
		ActionContributionItem openPreferencesItem = new ActionContributionItem(openPreferencesAction);
		openPreferencesItem.setVisible(!Util.isMac());
		menu.add(openPreferencesItem);

		menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
		return menu;
	}

	/**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(Messages.HelpMenuName, IWorkbenchActionConstants.M_HELP);
		menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.main")); //$NON-NLS-1$
		menu.add(helpContentsAction);
		menu.add(helpSearchAction);
		menu.add(dynamicHelpAction);
		menu.add(new GroupMarker("group.assist")); //$NON-NLS-1$
		// HELP_START should really be the first item, but it was after
		// quickStartAction and tipsAndTricksAction in 2.1.
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.tutorials")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.tools")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.updates")); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom
		menu.add(new Separator("group.about")); //$NON-NLS-1$

		ActionContributionItem aboutItem = new ActionContributionItem(aboutAction);
		aboutItem.setVisible(!Util.isMac());
		menu.add(aboutItem);
		menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$
		return menu;
	}

	/**
	 * Adds the perspective actions to the specified menu.
	 */
	private void addPerspectiveActions(MenuManager menu) {
		{
			MenuManager showViewMenuMgr = new MenuManager(Messages.ShowViewMenuName, "showView"); //$NON-NLS-1$
			IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
			showViewMenuMgr.add(showViewMenu);
			menu.add(showViewMenuMgr);
		}
		menu.add(new Separator());
		menu.add(editActionSetAction);
		menu.add(getResetPerspectiveItem());
	}

	private IContributionItem getItem(String actionId, String commandId, String image, String disabledImage,
			String label, String tooltip, String helpContextId) {
		ISharedImages sharedImages = getWindow().getWorkbench().getSharedImages();

		IActionCommandMappingService acms = getWindow().getService(IActionCommandMappingService.class);
		acms.map(actionId, commandId);

		CommandContributionItemParameter commandParm = new CommandContributionItemParameter(getWindow(), actionId,
				commandId, null, sharedImages.getImageDescriptor(image), sharedImages.getImageDescriptor(disabledImage),
				null, label, null, tooltip, CommandContributionItem.STYLE_PUSH, null, false);
		return new CommandContributionItem(commandParm);
	}

	private IContributionItem getResetPerspectiveItem() {
		return getItem(ActionFactory.RESET_PERSPECTIVE.getId(), ActionFactory.RESET_PERSPECTIVE.getCommandId(), null,
				null, Messages.ResetPerspective_text, Messages.ResetPerspective_toolTip,
				IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
	}

	private IContributionItem getCutItem() {
		return getItem(ActionFactory.CUT.getId(), ActionFactory.CUT.getCommandId(), ISharedImages.IMG_TOOL_CUT,
				ISharedImages.IMG_TOOL_CUT_DISABLED, Messages.Workbench_cut, Messages.Workbench_cutToolTip, null);
	}

	private IContributionItem getCopyItem() {
		return getItem(ActionFactory.COPY.getId(), ActionFactory.COPY.getCommandId(), ISharedImages.IMG_TOOL_COPY,
				ISharedImages.IMG_TOOL_COPY_DISABLED, Messages.Workbench_copy, Messages.Workbench_copyToolTip, null);
	}

	private IContributionItem getPasteItem() {
		return getItem(ActionFactory.PASTE.getId(), ActionFactory.PASTE.getCommandId(), ISharedImages.IMG_TOOL_PASTE,
				ISharedImages.IMG_TOOL_PASTE_DISABLED, Messages.Workbench_paste, Messages.Workbench_pasteToolTip, null);
	}

	private IContributionItem getSelectAllItem() {
		return getItem(ActionFactory.SELECT_ALL.getId(), ActionFactory.SELECT_ALL.getCommandId(), null, null,
				Messages.Workbench_selectAll, Messages.Workbench_selectAllToolTip, null);
	}

	private IContributionItem getFindItem() {
		return getItem(ActionFactory.FIND.getId(), ActionFactory.FIND.getCommandId(), null, null,
				Messages.Workbench_findReplace, Messages.Workbench_findReplaceToolTip, null);
	}

	private IContributionItem getBookmarkItem() {
		return getItem(IDEActionFactory.BOOKMARK.getId(), IDEActionFactory.BOOKMARK.getCommandId(), null, null,
				Messages.Workbench_addBookmark, Messages.Workbench_addBookmarkToolTip, null);
	}

	private IContributionItem getTaskItem() {
		return getItem(IDEActionFactory.ADD_TASK.getId(), IDEActionFactory.ADD_TASK.getCommandId(), null, null,
				Messages.Workbench_addTask, Messages.Workbench_addTaskToolTip, null);
	}

	private IContributionItem getDeleteItem() {
		return getItem(ActionFactory.DELETE.getId(), ActionFactory.DELETE.getCommandId(), ISharedImages.IMG_TOOL_DELETE,
				ISharedImages.IMG_TOOL_DELETE_DISABLED, Messages.Workbench_delete, Messages.Workbench_deleteToolTip,
				IWorkbenchHelpContextIds.DELETE_RETARGET_ACTION);
	}

	private static class WorkbenchCommandAction extends CommandAction implements IWorkbenchAction {
		/**
		 * @param commandIdIn
		 * @param window
		 */
		public WorkbenchCommandAction(String commandIdIn, IWorkbenchWindow window) {
			super(window, commandIdIn);
		}
	}

	/**
	 * Workbench action (id: "corefile", commandId: "org.eclipse.cdt.debug.application.command.debugCore"):
	 * Debug an executable with a core file.  This action maintains its enablement state.
	 */
	private static final ActionFactory COREFILE = new ActionFactory("corefile", //$NON-NLS-1$
			COREFILE_COMMAND_ID) {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setId(getId());
			action.setText(Messages.CoreFileMenuName);
			action.setToolTipText(Messages.CoreFile_toolTip);
			return action;
		}
	};

	/**
	 * Workbench action (id: "newexecutable", commandId: "org.eclipse.cdt.debug.application.command.debugNewExecutable"):
	 * Debug an executable.  This action maintains its enablement state.
	 */
	private static final ActionFactory NEW_EXECUTABLE = new ActionFactory("newexecutable", //$NON-NLS-1$
			NEW_EXECUTABLE_COMMAND_ID) {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setId(getId());
			action.setText(Messages.NewExecutableMenuName);
			action.setToolTipText(Messages.NewExecutable_toolTip);
			return action;
		}
	};

	/**
	 * Workbench action (id: "remoteexecutable", commandId: "org.eclipse.cdt.debug.application.command.debugRemoteExecutable"):
	 * Debug a remote executable.  This action maintains its enablement state.
	 */
	private static final ActionFactory REMOTE_EXECUTABLE = new ActionFactory("remoteexecutable", //$NON-NLS-1$
			REMOTE_EXECUTABLE_COMMAND_ID) {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setId(getId());
			action.setText(Messages.RemoteExecutableMenuName);
			action.setToolTipText(Messages.RemoteExecutable_toolTip);
			return action;
		}
	};

	/**
	 * Workbench action (id: "attachexecutable", commandId: "org.eclipse.cdt.debug.application.command.debugAttachedExecutable"):
	 * Attach and debug an existing executable.  This action maintains its enablement state.
	 */
	private static final ActionFactory ATTACH_EXECUTABLE = new ActionFactory("attachexecutable", //$NON-NLS-1$
			ATTACH_EXECUTABLE_COMMAND_ID) {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setId(getId());
			action.setText(Messages.AttachedExecutableMenuName);
			action.setToolTipText(Messages.AttachedExecutable_toolTip);
			return action;
		}
	};

}
