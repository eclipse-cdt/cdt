/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Kevin Doyle		(IBM)		 - [222831] Can't Delete User Actions/Named Types
 * Kevin Doyle		(IBM)		 - [222827] Treeview is collapsed after creating new user action      
 * Kevin Doyle	    (IBM)		 - [239702] Copy/Paste doesn't work with User Defined Actions and Named Types
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.view.SystemViewMenuListener;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsMessageIds;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Base class for tree views for both actions and types.
 */
public class SystemUDBaseTreeView extends TreeViewer implements IMenuListener, IDoubleClickListener, ISystemUDTreeView {
	protected Composite parent;
	protected MenuManager menuMgr;
	protected SystemUDBaseManager docManager;
	protected ISubSystem subsystem;
	protected ISubSystemConfiguration subsystemFactory;
	protected ISystemProfile profile;
	protected ISystemUDWorkWithDialog wwDialog;
	protected SystemUDTreeActionCopy copyAction;
	protected SystemUDTreeActionPaste pasteAction;
	protected SystemUDTreeActionDelete deleteAction;
	protected SystemUDTreeActionMoveUp moveUpAction;
	protected SystemUDTreeActionMoveDown moveDownAction;
	protected SystemUDARestoreDefaultsActions restoreAction;
	protected Clipboard clipboard;
	protected boolean menuListenerAdded;

	/**
	 * Constructor when we have a subsystem
	 */
	public SystemUDBaseTreeView(Composite parent, ISystemUDWorkWithDialog editPane, ISubSystem ss, SystemUDBaseManager docManager) {
		//super(parent);
		// I don't think multi-selection makes sense for this tree! Phil
		super(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER); // no SWT_MULTI
		//     this.shell = shell;
		this.parent = parent;
		this.subsystem = ss;
		this.subsystemFactory = subsystem.getSubSystemConfiguration();
		this.profile = subsystem.getSystemProfile();
		this.docManager = docManager;
		this.wwDialog = editPane;
		init();
		getTree().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (clipboard != null) clipboard.dispose();
			}
		});
	}

	/**
	 * Constructor when we have a subsystem factory and profile
	 */
	public SystemUDBaseTreeView(Composite parent, ISystemUDWorkWithDialog editPane, ISubSystemConfiguration ssFactory, ISystemProfile profile, SystemUDBaseManager docManager) {
		//super(parent);
		// I don't think multi-selection makes sense for this tree! Phil
		super(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER); // no SWT_MULTI
		//     this.shell = shell;
		this.parent = parent;
		this.subsystemFactory = ssFactory;
		this.profile = profile;
		this.docManager = docManager;
		this.wwDialog = editPane;
		init();
		getTree().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (clipboard != null) clipboard.dispose();
			}
		});
	}

	protected void init() {
		setContentProvider(docManager);
		setLabelProvider(new SystemUDBaseTreeViewLabelProvider(docManager));
		// For double-click on "New..." items in tree
		addDoubleClickListener(this);
		//setAutoExpandLevel(2); // does not work!!
		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		menuMgr = new MenuManager("#UDTreePopupMenu"); //$NON-NLS-1$
		//		menuMgr = new SystemSubMenuManager("#UDTreePopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(getTree());
		getTree().setMenu(menu);
		/**/
		setInput("0"); // this should trigger displaying the roots		 //$NON-NLS-1$
	}

	/**
	 * Expand the non-new domain (parent) nodes
	 */
	public void expandDomainNodes() {
		// for usability we try to auto-expand the domain (parent) nodes...
		if (docManager.getActionSubSystem().supportsDomains()) {
			TreeItem[] rootItems = getTree().getItems();
			for (int idx = 0; idx < rootItems.length; idx++) {
				if (rootItems[idx].getData() instanceof SystemXMLElementWrapper) // assume a domain node
				{
					setExpandedState(rootItems[idx].getData(), true);
				} else if (rootItems[idx].getData() instanceof SystemUDTreeViewNewItem) {
					SystemUDTreeViewNewItem newNode = (SystemUDTreeViewNewItem) rootItems[idx].getData();
					if (!newNode.isExecutable()) setExpandedState(rootItems[idx].getData(), true);
				}
			}
		}
	}

	/**
	 * Expand the given domain (parent) node, named by its
	 *  translatable name.
	 */
	public void expandDomainNode(String displayName) {
		// for usability we try to auto-expand the domain (parent) nodes...
		if (docManager.getActionSubSystem().supportsDomains()) {
			TreeItem[] rootItems = getTree().getItems();
			for (int idx = 0; idx < rootItems.length; idx++) {
				if (rootItems[idx].getData() instanceof SystemXMLElementWrapper) // assume a domain node
				{
					//System.out.println(rootItems[idx].getText());
					if (rootItems[idx].getText().equals(displayName)) {
						setExpandedState(rootItems[idx].getData(), true);
						return;
					}
				}
			}
		}
	}

	/**
	 * Called when the context menu is about to open.
	 * Calls {@link #fillContextMenu(IMenuManager)}
	 */
	public void menuAboutToShow(IMenuManager menu) {
		fillContextMenu(menu);
		if (!menuListenerAdded) {
			if (menu instanceof MenuManager) {
				Menu m = ((MenuManager) menu).getMenu();
				if (m != null) {
					menuListenerAdded = true;
					SystemViewMenuListener ml = new SystemViewMenuListener();
					//ml.setShowToolTipText(true, wwDialog.getMessageLine()); does not work for some reason
					m.addMenuListener(ml);
				}
			}
		}
	}

	/**
	 * This is method is called to populate the popup menu
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		// this code assumes single select. if we ever change to allow multiple selection,
		// this code will have to change
		int selectionCount = selection.size();
		if (selectionCount > 0) // something selected
		{
			Object firstSelection = selection.getFirstElement();
			if ((firstSelection instanceof SystemXMLElementWrapper) && !((SystemXMLElementWrapper) firstSelection).isDomain()) {
				// Partition into groups...
				createStandardGroups(menu);
				ISystemAction action = getDeleteAction(selection);
				menu.appendToGroup(action.getContextMenuGroup(), action);
				action = getCopyAction(selection);
				menu.appendToGroup(action.getContextMenuGroup(), action);
				action = getPasteAction(selection);
				menu.appendToGroup(action.getContextMenuGroup(), action);
				action = getMoveUpAction(selection);
				menu.appendToGroup(action.getContextMenuGroup(), action);
				action = getMoveDownAction(selection);
				menu.appendToGroup(action.getContextMenuGroup(), action);
				action = getRestoreAction(selection);
				if (action != null) menu.appendToGroup(action.getContextMenuGroup(), action);
			} else if ((firstSelection instanceof SystemXMLElementWrapper) && ((SystemXMLElementWrapper) firstSelection).isDomain()) {
				// Partition into groups...
				createStandardGroups(menu);
				ISystemAction action = getPasteAction(selection);
				menu.appendToGroup(action.getContextMenuGroup(), action);
			}
		}
	}

	/**
	 * Creates the Systems plugin standard groups in a context menu.
	 */
	public void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty()) return;
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE)); // rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER)); // move up, move down		
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE)); // restore
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
	}

	/**
	 * Get the delete action
	 */
	private SystemUDTreeActionDelete getDeleteAction(ISelection selection) {
		if (deleteAction == null) deleteAction = new SystemUDTreeActionDelete(this);
		deleteAction.setInputs(getShell(), this, selection);
		return deleteAction;
	}

	/**
	 * Get the move up action
	 */
	private SystemUDTreeActionMoveUp getMoveUpAction(ISelection selection) {
		if (moveUpAction == null) moveUpAction = new SystemUDTreeActionMoveUp(this);
		moveUpAction.setInputs(getShell(), this, selection);
		return moveUpAction;
	}

	/**
	 * Get the move down action
	 */
	private SystemUDTreeActionMoveDown getMoveDownAction(ISelection selection) {
		if (moveDownAction == null) moveDownAction = new SystemUDTreeActionMoveDown(this);
		moveDownAction.setInputs(getShell(), this, selection);
		return moveDownAction;
	}

	/**
	 * Get the copy action
	 */
	private SystemUDTreeActionCopy getCopyAction(ISelection selection) {
		if (copyAction == null) copyAction = new SystemUDTreeActionCopy(this);
		copyAction.setInputs(getShell(), this, selection);
		return copyAction;
	}

	/**
	 * Get the paste action
	 */
	private SystemUDTreeActionPaste getPasteAction(ISelection selection) {
		if (pasteAction == null) pasteAction = new SystemUDTreeActionPaste(this);
		pasteAction.setInputs(getShell(), this, selection);
		return pasteAction;
	}

	/**
	 * Get the restore defaults action
	 */
	protected SystemUDARestoreDefaultsActions getRestoreAction(ISelection selection) {
		if (restoreAction == null) restoreAction = new SystemUDARestoreDefaultsActions(this);
		restoreAction.setShell(getShell());
		if (selection != null) restoreAction.setSelection(selection);
		return restoreAction;
	}

	/**
	 * Convenience method for returning the shell of this viewer.
	 */
	public Shell getShell() {
		return getTree().getShell();
	}

	/**
	 * Clear the clipboard
	 */
	public void clearClipboard() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
	}

	// ----------------------------------
	// METHODS USED BY POPUP MENU ACTIONS
	// ----------------------------------
	/**
	 * Decide if we can do the delete or not.
	 * Decision deferred to work-with dialog hosting this tree
	 */
	public boolean canDelete() {
		return wwDialog.canDelete(((IStructuredSelection) getSelection()).getFirstElement());
	}

	/**
	 * Return true if the currently selected item can be moved up or not.
	 * Called by the SystemUDTreeActionMoveUp action class.
	 */
	public boolean canMoveUp() {
		return wwDialog.canMoveUp(((IStructuredSelection) getSelection()).getFirstElement());
	}

	/**
	 * Return true if the currently selected item can be moved down or not.
	 * Called by the SystemUDTreeActionMoveDown action class.
	 */
	public boolean canMoveDown() {
		return wwDialog.canMoveDown(((IStructuredSelection) getSelection()).getFirstElement());
	}

	/**
	 * Return true if the currently selected item can be copied to the clipboard or not.
	 * Called by the SystemChangeFilterActionCopyString action class.
	 */
	public boolean canCopy() {
		return wwDialog.canCopy(((IStructuredSelection) getSelection()).getFirstElement());
	}

	/**
	 * Return true if the current contents of the clipboard apply to us or not.
	 * Called by the SystemUDTreeActionPaste action class.
	 */
	public boolean canPaste() {
		if (clipboard == null) return false;
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		if (!(selection.getFirstElement() instanceof SystemXMLElementWrapper)) return false;
		SystemXMLElementWrapper firstSelect = (SystemXMLElementWrapper) selection.getFirstElement();
		TextTransfer textTransfer = TextTransfer.getInstance();
		String textData = (String) clipboard.getContents(textTransfer);
		return docManager.enablePaste(firstSelect, textData);
	}

	/**
	 * Actually do the delete of currently selected item.
	 * Return true if it worked. Return false if it didn't (eg, user cancelled confirm)
	 * Called by the SystemUDTreeActionDelete action class.
	 */
	public boolean doDelete() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		boolean deleted = false;
		SystemMessage confirmDlt = getDeleteConfirmationMessage();
		SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), confirmDlt);
		try {
			deleted = msgDlg.openQuestion();
			if (deleted) {
				docManager.delete(docManager.getCurrentProfile(), (SystemXMLElementWrapper) selection.getFirstElement());
				docManager.saveUserData(docManager.getCurrentProfile());
				deleted = true;
			}
		} catch (Exception exc) {
			SystemBasePlugin.logError("Error deleting user actions", exc); //$NON-NLS-1$
		}
		if (deleted) {
			remove(selection.getFirstElement());
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, getResourceType(), selection.getFirstElement(), null);
		}
		return deleted;
	}

	/**
	 * Return the {@link org.eclipse.rse.core.events.ISystemModelChangeEvents} constant representing the resource type managed by this tree.
	 * This must be overridden.
	 */
	protected int getResourceType() {
		return -1;
	}

	/**
	 * Return message for delete confirmation
	 */
	protected SystemMessage getDeleteConfirmationMessage() {
		SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_CONFIRM_DELETE_USERACTION,
				IStatus.ERROR, UserActionsResources.MSG_CONFIRM_DELETE_USERACTION, UserActionsResources.MSG_CONFIRM_DELETE_USERTYPE_DETAILS);
		msg.setIndicator(SystemMessage.INQUIRY);
		return msg;
	}

	/**
	 * Actually do the move up of currently selected item.
	 * Return true if all went well.
	 * Called by the SystemUDTreeActionMoveUp action class.
	 */
	public boolean doMoveUp() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		SystemXMLElementWrapper firstSelect = (SystemXMLElementWrapper) selection.getFirstElement();
		//SystemXMLElementWrapper previousElement = (SystemXMLElementWrapper) getSelectedPreviousTreeItem().getData();
		boolean moved = docManager.moveElementUp(firstSelect/*, previousElement*/);
		if (moved) {
			refreshElementParent(firstSelect);
			selectElement(firstSelect);
			docManager.saveUserData(docManager.getCurrentProfile());
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, getResourceType(), firstSelect, null);
		}
		return true;
	}

	/**
	 * Actually do the move down of currently selected item.
	 * Return true if all went well.
	 * Called by the SystemUDTreeActionMoveDown action class.
	 */
	public boolean doMoveDown() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		SystemXMLElementWrapper firstSelect = (SystemXMLElementWrapper) selection.getFirstElement();
		//SystemXMLElementWrapper nextElement = null;
		//if (nextNextItem != null) nextElement = (SystemXMLElementWrapper) nextNextItem.getData();
		boolean moved = docManager.moveElementDown(firstSelect/*, nextElement*/);
		if (moved) {
			refreshElementParent(firstSelect);
			selectElement(firstSelect);
			docManager.saveUserData(docManager.getCurrentProfile());
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, getResourceType(), firstSelect, null);
		}
		return true;
	}

	/**
	 * Actually do the copy of currently selected item to the clipboard.
	 * Return true if all went well.
	 * Called by the SystemChangeFilterActionCopyString action class.
	 */
	public boolean doCopy() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		SystemXMLElementWrapper firstSelect = (SystemXMLElementWrapper) selection.getFirstElement();
		if (clipboard == null) clipboard = new Clipboard(getShell().getDisplay());
		String id = docManager.prepareClipboardCopy(firstSelect);
		if (id == null) return false;
		TextTransfer transfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] { id }, new Transfer[] { transfer });
		return true;
	}

	/**
	 * Actually do the paste of clipboard contents relative to currently selected object.
	 * Return true if all went well.
	 * Called by the SystemUDTreeActionPaste action class.
	 */
	public boolean doPaste() {
		if (clipboard == null) return false;
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		SystemXMLElementWrapper firstSelect = (SystemXMLElementWrapper) selection.getFirstElement();
		TextTransfer textTransfer = TextTransfer.getInstance();
		String textData = (String) clipboard.getContents(textTransfer);
		SystemXMLElementWrapper pastedElementWrapper = docManager.pasteClipboardCopy(firstSelect, textData);
		if (pastedElementWrapper != null) {
			if (firstSelect.isDomain()) {
				refresh(firstSelect);
				setExpandedState(firstSelect, true); // force expansion, just in case
			} else
				refreshElementParent(firstSelect);
			selectElement(pastedElementWrapper);
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, getResourceType(), pastedElementWrapper, null);
		}
		clipboard.dispose();
		clipboard = null;
		return (pastedElementWrapper != null);
	}

	/**
	 * Return true if we are to enable the Restore Defaults actions
	 */
	public boolean canRestore() {
		if (wwDialog.areChangesPending()) return false;
		SystemXMLElementWrapper selectedElement = getSelectedElement();
		if ((selectedElement == null) || !((selectedElement instanceof SystemUDActionElement) || (selectedElement instanceof SystemUDTypeElement))) return false;
		return selectedElement.isIBM() && selectedElement.isUserChanged();
	}

	/**
	 * Restore the selected action/type to its IBM-supplied default value.
	 * Needs to be overridden by children that want to support it.
	 */
	public void doRestore() {
	}

	// --------------
	// Miscellaneous
	// --------------
	/**
	 * Return the action or type manager
	 */
	public SystemUDBaseManager getDocumentManager() {
		return docManager;
	}

	// For Interface  IDoubleClickListener
	// For double-click on "New..." items in tree
	public void doubleClick(DoubleClickEvent event) {
	}

	/**
	 * Get the selected action or type name.
	 * Returns "" if nothing selected
	 */
	public String getSelectedElementName() {
		String seldName = ""; //$NON-NLS-1$
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if ((sel != null) && (sel.getFirstElement() != null)) {
			Object selObj = sel.getFirstElement();
			if (selObj instanceof SystemXMLElementWrapper) seldName = ((SystemXMLElementWrapper) selObj).toString();
		}
		return seldName;
	}

	/**
	 * Return true if currently selected element is "ALL"
	 */
	public boolean isElementAllSelected() {
		return getSelectedElementName().equals("ALL"); //$NON-NLS-1$
	}

	/**
	 * Return true if currently selected element is vendor supplied
	 */
	public boolean isSelectionVendorSupplied() {
		SystemXMLElementWrapper selectedElement = getSelectedElement();
		if (selectedElement != null) {
			String vendor = selectedElement.getVendor();
			//System.out.println("Vendor value: '"+vendor+"'");
			return ((vendor != null) && (vendor.length() > 0));
		}
		return false;
	}

	/**
	 * Return the vendor that is responsible for pre-supplying this existing type,
	 *  or null if not applicable.
	 */
	public String getVendorOfSelection() {
		SystemXMLElementWrapper selectedElement = getSelectedElement();
		if (selectedElement != null) {
			String vendor = selectedElement.getVendor();
			if ((vendor != null) && (vendor.length() > 0)) return vendor;
		}
		return null;
	}

	/**
	 * Get the selected action or type domain.
	 * Returns -1 if nothing selected or domains not supported
	 */
	public int getSelectedElementDomain() {
		int seldDomain = -1;
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if ((sel != null) && (sel.getFirstElement() != null)) {
			Object selObj = sel.getFirstElement();
			if (selObj instanceof SystemXMLElementWrapper) seldDomain = ((SystemXMLElementWrapper) selObj).getDomain();
		}
		return seldDomain;
	}

	// ------------------------------------
	// HELPER METHODS CALLED FROM EDIT PANE
	// ------------------------------------
	/**
	 * Return the selected non-domain element, or null if an existing element
	 *  is not currently selected
	 */
	public SystemXMLElementWrapper getSelectedElement() {
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if ((sel != null) && (sel.getFirstElement() != null)) {
			Object selObj = sel.getFirstElement();
			if (selObj instanceof SystemXMLElementWrapper) {
				SystemXMLElementWrapper selEle = (SystemXMLElementWrapper) selObj;
				if (!selEle.isDomain()) return selEle;
			}
		}
		return null;
	}

	/**
	 * Select the given type
	 */
	public void selectElement(SystemXMLElementWrapper element) {
		//System.out.println("Inside selectElement of tree for action: " + element);		
		// here is our problem: 
		//  We are given an element object that wrappers an xml node object.
		//  These wrappers are re-created on the fly, whenever the tree is refreshed.
		//  So, we might not find a binary match on the wrapper.
		//  Hence, we need to see if there is such a match, and if not, then
		//    we have to walk the tree comparing the xml node objects.
		//  The assumption is that we are always given something that is in fact
		//    in the tree.
		Widget w = findItem(element);
		if (w != null) // we found it!
			super.setSelection(new StructuredSelection(element), true); // select it
		else {
			//start walking!        	   
			TreeItem matchingItem = findElement(element.getElement());
			if (matchingItem != null)
				super.setSelection(new StructuredSelection(matchingItem.getData()), true); // select it        	
			else
				super.setSelection((ISelection) null); // deselect what is currently selected
		}
	}

	/**
	 * Find the parent tree item of the given type.
	 * If it is not currently shown in the tree, or there is no parent, returns null.
	 */
	public TreeItem findParentItem(SystemXMLElementWrapper element) {
//		IPropertySet parentElement = element.getParentDomainElement();
		// Since we use PropertySet's now we don't want the parent domain element, just the parent element
		IPropertySet parentElement = element.getParentElement();
		TreeItem parentItem = null;
		if (parentElement != null)
			parentItem = findElement(parentElement);
		else {
			//System.out.println("asked to find parent item, yet there is no parent element");
		}
		return parentItem;
	}

	/**
	 * Refresh the parent of the given action.
	 * That is, find the parent and refresh the children.
	 * If the parent is not found, assume it is because it is new too,
	 *  so refresh the whole tree.
	 */
	public void refreshElementParent(SystemXMLElementWrapper element) {
		TreeItem parentItem = findParentItem(element);
		if (parentItem == null) // parent not found? 
		{
			//System.out.println("parentItem null. Refreshing tree");
			refresh(); // refresh whole tree
			// now, try again to find parent to ensure it is expanded...
			parentItem = findParentItem(element);
		} else {
			//System.out.println("parentItem not null. Refreshing it");
			refresh(parentItem.getData()); // refresh this element	 
		}
		if (parentItem != null) // should not happen
		{
			//System.out.println("parentItem not null. Expanded? " + parentItem.getExpanded());
			if (!parentItem.getExpanded()) // not expanded yet?
			{
				//System.out.println("  expanding parent... " + parentItem.getExpanded());
				setExpandedState(parentItem.getData(), true); // expand it now
			}
		}
	}

	/**
	 * Given an xml node, find the wrapper for the element in the tree,
	 *  scanning entire tree.
	 */
	private TreeItem findElement(IPropertySet searchNode) {
		TreeItem match = null;
		TreeItem[] roots = getTree().getItems();
		for (int idx = 0; (match == null) && (idx < roots.length); idx++)
			match = findElement(roots[idx], searchNode);
		return match;
	}

	/**
	 * Given an xml node and parent tree item, find the wrapper for the element in the tree
	 *  under the given parent.
	 */
	private TreeItem findElement(TreeItem parentItem, IPropertySet searchNode) {
		TreeItem match = null;
		// first, check for match on the given parent itself...
		Object itemData = parentItem.getData();
		IPropertySet itemNode = null;
		if ((itemData != null) && (itemData instanceof SystemXMLElementWrapper)) {
			itemNode = ((SystemXMLElementWrapper) itemData).getElement();
			if (itemNode == searchNode) return parentItem;
		}
		// no match on parent, check kids...
		TreeItem[] kids = parentItem.getItems();
		if (kids != null) for (int idx = 0; (match == null) && (idx < kids.length); idx++)
			match = findElement(kids[idx], searchNode);
		return match;
	}

	/**
	 * Returns the tree item of the first selected object.
	 */
	public TreeItem getSelectedTreeItem() {
		TreeItem[] selectedItems = getTree().getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0))
			return selectedItems[0];
		else
			return null;
	}

	/**
	 * Returns the tree item of the sibling before the first selected object.
	 */
	public TreeItem getSelectedPreviousTreeItem() {
		TreeItem selectedItem = getSelectedTreeItem();
		if (selectedItem == null) return null;
		TreeItem[] siblings = null;
		if (selectedItem.getParentItem() != null)
			siblings = selectedItem.getParentItem().getItems();
		else
			siblings = selectedItem.getParent().getItems();
		for (int idx = 0; idx < siblings.length; idx++) {
			if (siblings[idx] == selectedItem) {
				if (idx == 0)
					return null;
				else
					return siblings[idx - 1];
			}
		}
		return null;
	}

	/**
	 * Returns the tree item of the sibling after the first selected object.
	 */
	public TreeItem getSelectedNextTreeItem() {
		TreeItem selectedItem = getSelectedTreeItem();
		if (selectedItem == null) return null;
		TreeItem[] siblings = null;
		if (selectedItem.getParentItem() != null)
			siblings = selectedItem.getParentItem().getItems();
		else
			siblings = selectedItem.getParent().getItems();
		for (int idx = 0; idx < siblings.length; idx++) {
			if (siblings[idx] == selectedItem) {
				if (idx >= (siblings.length - 1))
					return null;
				else
					return siblings[idx + 1];
			}
		}
		return null;
	}

	/**
	 * Returns the tree item of the sibling two after the first selected object.
	 */
	public TreeItem getSelectedNextNextTreeItem() {
		TreeItem selectedItem = getSelectedTreeItem();
		if (selectedItem == null) return null;
		TreeItem[] siblings = null;
		if (selectedItem.getParentItem() != null)
			siblings = selectedItem.getParentItem().getItems();
		else
			siblings = selectedItem.getParent().getItems();
		for (int idx = 0; idx < siblings.length; idx++) {
			if (siblings[idx] == selectedItem) {
				if (idx >= (siblings.length - 2))
					return null;
				else
					return siblings[idx + 2];
			}
		}
		return null;
	}

	/**
	 * Move one tree item to a new location
	 */
	protected void moveTreeItem(Widget parentItem, Item item, Object src, int newPosition) {
		if (getExpanded(item)) {
			setExpanded(item, false);
			refresh(src); // flush items from memory  	  
		}
		createTreeItem(parentItem, src, newPosition);
		//createTreeItem(parentItem, (new String("New")), newPosition);
		//remove(src);    	
		disassociate(item);
		item.dispose();
	}

	/**
	 * Get the position of a tree item within its parent
	 */
	protected int getTreeItemPosition(Widget parentItem, Item childItem) {
		int pos = -1;
		Item[] children = null;
		if (parentItem instanceof Item)
			children = getItems((Item) parentItem);
		else
			children = getChildren(parentItem);
		for (int idx = 0; (pos == -1) && (idx < children.length); idx++) {
			if (children[idx] == childItem) pos = idx;
		}
		return pos;
	}
}
