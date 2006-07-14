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

package org.eclipse.rse.shells.ui.view;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.IRemoteLineReference;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.files.ui.actions.SystemRemoteFileLineOpenWithMenu;
import org.eclipse.rse.files.ui.resources.RemoteSourceLookupDirector;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.shells.ui.actions.SystemShowInShellViewAction;
import org.eclipse.rse.shells.ui.actions.SystemTerminateRemoveShellAction;
import org.eclipse.rse.shells.ui.actions.SystemTerminateShellAction;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.model.ISystemOutputRemoteTypes;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemDragDropAdapter;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


/**
 * This is the adapter for smart output from remote commands, such that they can support right click actions and such.
 */
public class SystemViewRemoteOutputAdapter extends AbstractSystemViewAdapter 
implements  ISystemViewElementAdapter, ISystemRemoteElementAdapter, ISystemOutputRemoteTypes
{


	protected IPropertyDescriptor[] _propertyDescriptors;
	
	private SystemCopyToClipboardAction _copyOutputAction = null;
	private SystemPasteFromClipboardAction _pasteToPromptAction = null;

	private SystemShowInShellViewAction _showInShellView = null;
	private SystemTerminateShellAction  _terminateShellAction = null;
	private SystemTerminateRemoveShellAction _terminateRemoveShellAction = null;
	private IAction _exportShellHistoryAction = null;
	private IAction _exportShellOutputAction = null;
	private List _shellActions = null;
	
	private IPropertyDescriptor _shellPropertyDescriptors[];
	private IPropertyDescriptor _outputPropertyDescriptors[];
	
	public SystemViewRemoteOutputAdapter()
	{
	    _shellActions = new ArrayList();
	}

	/**
	 * Used to add context menu actions for the given remote output
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		Object firstSelection = selection.getFirstElement();

		if (firstSelection != null)
		{
		    if (firstSelection instanceof IRemoteOutput)
		    {
				if (_copyOutputAction == null)
				{
					_copyOutputAction = new SystemCopyToClipboardAction(shell, RSEUIPlugin.getTheSystemRegistry().getSystemClipboard());
				}
				
				menu.add(menuGroup, _copyOutputAction);
	
				if (selection.size() == 1)
				{
					 if (firstSelection instanceof IRemoteLineReference)
					{
						IRemoteOutput result = (IRemoteOutput) firstSelection;
						String type = result.getType();
						if (type.equals("prompt"))
						{
						    if (_pasteToPromptAction == null)
							{
								_pasteToPromptAction = new SystemPasteFromClipboardAction(shell, RSEUIPlugin.getTheSystemRegistry().getSystemClipboard());
							}
							
							menu.add(menuGroup, _pasteToPromptAction);
						}
						else if (type.equals("directory"))
						{
							IRemoteOutput output = (IRemoteOutput)firstSelection;
							if (output.getAbsolutePath() != null)
							{
								// TODO
								// add directory actions here						
							}
						}
						else
						{
							IRemoteOutput output = (IRemoteOutput)firstSelection;
							if (output.getAbsolutePath() != null)
							{
										//SystemCreateEditLineActions createActions = new SystemCreateEditLineActions();
								    	//createActions.create(menu, selection, shell, menuGroup);			
										// open with ->
										MenuManager submenu =
											new MenuManager(org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages.ResourceNavigator_openWith,
												ISystemContextMenuConstants.GROUP_OPENWITH);
										
										SystemRemoteFileLineOpenWithMenu  openWithMenu = new SystemRemoteFileLineOpenWithMenu();
										openWithMenu.updateSelection(selection);
										submenu.add(openWithMenu);
										menu.getMenuManager().appendToGroup(ISystemContextMenuConstants.GROUP_OPENWITH, submenu);
						
							}
						}
					}
					 
					
					 
				}
		    }
		    else if (firstSelection instanceof IRemoteCommandShell)
		    {
		        IRemoteCommandShell cmdShell = (IRemoteCommandShell)firstSelection;
		        if (_showInShellView == null)
		        {
		            _showInShellView = new SystemShowInShellViewAction(shell);
		 
		        }
		        menu.add(ISystemContextMenuConstants.GROUP_OPEN,_showInShellView);
		        
		        getShellActions(cmdShell.getCommandSubSystem().getParentRemoteCmdSubSystemFactory());
		        
		        menu.add(ISystemContextMenuConstants.GROUP_CHANGE, _terminateShellAction);
		        menu.add(ISystemContextMenuConstants.GROUP_CHANGE, _terminateRemoveShellAction);
		        menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, _exportShellOutputAction);
		        menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, _exportShellHistoryAction);
		    }
		}
		else
		{
			return;
		}
	}
	
	public List getShellActions(IRemoteCmdSubSystemConfiguration factory)
	{
	    getShell();
	    _shellActions.clear();
	    if (_shellActions.size()== 0)
	    {
	        if (_terminateShellAction == null)
	        {
	            _terminateShellAction = new SystemTerminateShellAction(shell);	
	        }
            _shellActions.add(_terminateShellAction);
	        if (_terminateRemoveShellAction == null)
	        {
	            _terminateRemoveShellAction = new SystemTerminateRemoveShellAction(shell);
	        }
            _shellActions.add(_terminateRemoveShellAction);
	        
	        _shellActions.add(new Separator());
	        
	        ShellServiceSubSystemFactoryAdapter factoryAdapter = (ShellServiceSubSystemFactoryAdapter)factory.getAdapter(ISubsystemConfigurationAdapter.class);
	        
	        _exportShellOutputAction = factoryAdapter.getCommandShellOutputExportAction(shell);
	        _shellActions.add(_exportShellOutputAction);
	        _exportShellHistoryAction = factoryAdapter.getCommandShellHistoryExportAction(shell);
	        _shellActions.add(_exportShellHistoryAction);
	       
	    }
	    return _shellActions;
	}
	
	/**
	 * Returns the parent command object for a line of output
	 */
	public Object getParent(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			return output.getParent();
		}
		return null;
	}

	/**
	 * Returns the text to display in a view for this element.
	 */
	public String getText(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			String text = output.getText();

			if (text.indexOf('\t') > 0)
			{
			    text = translateTabs(text);
			}
			
			int tagIndex = text.indexOf("BEGIN-END-TAG");
			if (tagIndex == 0)
			{
			    return "";
			}
			else if (tagIndex > 0)
			{
			    //return text.substring(0, tagIndex - 6);
			    return text.substring(0, tagIndex);
			}
			
			return text;
		}
		else if (element instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell outputRoot = (IRemoteCommandShell) element;
			return outputRoot.getId();
		}
		return null;
	}

	
	protected String translateTabs(String tabbedString)
		{
		    int columnWidth = 8;

		    int currentOffset = 0;
		    StringBuffer untabbedBuf = new StringBuffer();
		    for (int i = 0; i < tabbedString.length();i++)
		    {
		        char p = '\0';
		        char c = tabbedString.charAt(i);
		        char n = '\0';
		        if (i+1 <tabbedString.length())
		        {
		         n = tabbedString.charAt(i + 1);
		        }
		        if (i - 1 > -1)
		        {
		         p = tabbedString.charAt(i - 1);
		        }
		        if (c == '\t')
		        {
		            untabbedBuf.append(' ');
		            currentOffset++;
		            while ((currentOffset % columnWidth) >0)
		            {
		                untabbedBuf.append(' ');
			            currentOffset++;
		            }
		        }
		        else if (c == ' ')
		        {
		            untabbedBuf.append(' ');
		            currentOffset++;
		        }
		        else
		        {
		            untabbedBuf.append(c);
		            currentOffset++;
		        }	        
		    }
		    
		    return untabbedBuf.toString();
		}
	/**
	 * Returns the type attribute of a line of output or command.
	 */
	public String getType(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			return output.getType();
		}
		else if (element instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell root = (IRemoteCommandShell) element;
			return root.getType();
		}
		return null;
	}

	/**
	 * Returns the children of a remote command if the element is a remote command
	 */
	public Object[] getChildren(Object element)
	{
	  
	        if (element instanceof IRemoteCommandShell)
	        {
		    	IRemoteCommandShell output = (IRemoteCommandShell) element;
				return output.listOutput();
			}
	   
		return null;
	}

	/**
	 * Returns true if the element is a remote command and false if it is a remote output.
	 */
	public boolean hasChildren(Object element)
	{
        if (element instanceof IRemoteCommandShell)
        {
            if (!(getViewer() instanceof SystemView))
    	    {
                return true;
    	    }
            return false;
        }
	    else
	    {
	        return false;
	    }		
	}

	/**
	 * Returns the associated IRemoteFile for this line of remote output if one exists
	 * @param output the line of remote output
	 * @return the associated remote file
	 */
	public static IRemoteFile outputToFile(IRemoteOutput output)
	{
		IRemoteFile file = null;
		Object parent = output.getParent();
		IRemoteFileSubSystem fs = null;
		if (parent instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell root = (IRemoteCommandShell) output.getParent();
			fs = RemoteFileUtility.getFileSubSystem(root.getCommandSubSystem().getHost());
		}
		else if (parent instanceof IRemoteFile)
		{
			return (IRemoteFile) parent;
		}

		if (fs != null)
		{
			String path = output.getAbsolutePath();
			if (path != null && path.length() > 0)
			{
				Object obj = null;
				try
				{
					obj = fs.getObjectWithAbsoluteName(path);
				}
				catch (Exception e)
				{
					return null;
				}
				if (obj != null && obj instanceof IRemoteFile)
				{
					file = (IRemoteFile) obj;

					return file;
				}
			}
		}

		return file;
	}
	
	
	
	protected IEditorRegistry getEditorRegistry()
	{
		return RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry();
	}
	
	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor");
	}
	
	/**
	 * Open workspace file associated with IRemoteCommandShell.  If there is no associated project
	 * return.
	 * @param remoteFile
	 * @param output
	 * @return
	 */
	protected boolean openWorkspaceFile(IRemoteFile remoteFile, IRemoteOutput output)
	{
		IRemoteCommandShell cmd = (IRemoteCommandShell)(output.getParent());
		IProject associatedProject = cmd.getAssociatedProject();
		if (associatedProject != null)
		{
			ProjectSourceContainer container = new ProjectSourceContainer(associatedProject, false);
			ISourceLookupDirector director = new RemoteSourceLookupDirector(); 
			container.init(director);
			try
			{
				Object[] matches = container.findSourceElements(remoteFile.getName());
				for (int i = 0; i < matches.length; i++)
				{
					//System.out.println("match="+matches[i]);
				}
				
				if (matches.length == 1)
				{
					IFile localMatch = (IFile)matches[0];
				
					
					
					IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
	
					FileEditorInput finput = new FileEditorInput(localMatch);						
				
				
					IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(remoteFile.getName());
					if (desc == null)
					{
						desc = getDefaultTextEditor();
					}
					String editorid = desc.getId();
					IEditorPart editor = activePage.openEditor(finput, editorid);
					
					int line = output.getLine();
					int charStart = output.getCharStart();
					int charEnd = output.getCharEnd();
					
					try
					{
						IMarker marker = null;
						
						// DKM - should we?  this will populate the Problems view..but resources are actually remote
						if (output instanceof IRemoteError)
						{
							IRemoteError error = (IRemoteError)output;
							String type = error.getType();
							
							marker = localMatch.createMarker(IMarker.TEXT);
					
							if (type.equals("error"))
							{
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
							}
							else if (type.equals("warning"))
							{
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
							}
							else if (type.equals("informational"))
							{
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
							}
							
							marker.setAttribute(IMarker.MESSAGE, output.getText());
							marker.setAttribute(IMarker.LINE_NUMBER, line);
							marker.setAttribute(IMarker.CHAR_START, charStart);
							marker.setAttribute(IMarker.CHAR_END, charEnd);
							
						}
						else
						{
							marker = localMatch.createMarker(IMarker.TEXT);						
							marker.setAttribute(IMarker.LINE_NUMBER, line);
							marker.setAttribute(IMarker.CHAR_START, charStart);
							marker.setAttribute(IMarker.CHAR_END, charEnd);
						}
						IDE.gotoMarker(editor, marker);
					
						
					}
					catch (CoreException e)
					{
						e.printStackTrace();
					}
					return true;
				}
			}
			catch(Exception e)
			{				
				e.printStackTrace();
			}
		}	
		
		return false;
	}

	/**
	 * Opens the appropriate editor for a remote output object
	 */
	public boolean handleDoubleClick(Object element)
	{
		boolean result = false;
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
	
			IRemoteFile file = outputToFile(output);
			if (file != null && file.isFile())
			{									
				if (!openWorkspaceFile(file, output))
				{
					ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) file).getAdapter(ISystemViewElementAdapter.class);
					result = adapter.handleDoubleClick(file);
					int line = output.getLine();
	
					if (result)
					{
						if (line > 0)
						{
							SystemRemoteFileLineOpenWithMenu.handleGotoLine(file, line, output.getCharStart(), output.getCharEnd());
						}
						return true;
					}
				}
			}
		}
		else if (element instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmdshell = (IRemoteCommandShell) element;
			if (cmdshell.getType().equals(ShellResources.RESID_SHELLS_COMMAND_SHELL_LABEL))
			{
				SystemCommandsViewPart viewPart = SystemCommandsUI.getInstance().activateCommandsView();
				viewPart.updateOutput(cmdshell);
				result = true;
			}
		}
		return result;
	}

	/**
	 * Returns the associated subsystem for this line of remote output or remote command
	 */
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmd = (IRemoteCommandShell) element;
			return cmd.getCommandSubSystem();
		}
		else if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			Object parent = output.getParent();
			if (parent instanceof IRemoteCommandShell)
			{
				return getSubSystem(parent);
			}
		}

		return null;
	}

	/**
	  * Return the fully qualified name of this remote object. 
	  */
	public String getAbsoluteName(Object element)
	{
		if (element instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmd = (IRemoteCommandShell) element;
			return cmd.getId();
		}
		else if (element instanceof IRemoteOutput)
		{
			IRemoteOutput out = (IRemoteOutput) element;
			String str = getAbsoluteParentName(element);
			return str + ":" + out.getIndex();
		}
		return null;
	}

	/**
	  * Return fully qualified name that uniquely identifies this remote object's remote parent within its subsystem
	  */
	public String getAbsoluteParentName(Object element)
	{
		return getAbsoluteName(getParent(element));
	}

	/**
	  * Return the subsystem factory id that owns this remote object
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getSubSystemFactoryId(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the type category property for this object
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteTypeCategory(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the type property for this object
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteType(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			return ((IRemoteOutput) element).getType();
		}
		else if (element instanceof IRemoteCommandShell)
		{
			return ((IRemoteCommandShell) element).getType();
		}
		return null;
	}

	/**
	  * Return a value for the subtype property for this object.
	  * Not all object types support a subtype, so returning null is ok.
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteSubType(Object element)
	{
		return null;
	}

	/**
	  * Return a value for the sub-subtype property for this object.
	  * Not all object types support a sub-subtype, so returning null is ok.
	  * The value must not be translated, so that property pages registered via xml can subset by it.
	  */
	public String getRemoteSubSubType(Object element)
	{
		return null;
	}
	/**
	 * Return the source type of the selected object. Typically, this only makes sense for compilable
	 *  source members. For non-compilable remote objects, this typically just returns null.
	 */
	public String getRemoteSourceType(Object element)
	{
		return null;
	}

	/**
	  * Some view has updated the name or properties of this remote object. As a result, the 
	  *  remote object's contents need to be refreshed. You are given the old remote object that has
	  *  old data, and you are given the new remote object that has the new data. For example, on a
	  *  rename the old object still has the old name attribute while the new object has the new 
	  *  new attribute.
	  * <p>
	  * This is called by viewers like SystemView in response to rename and property change events.
	  * <p>
	  * @param oldElement the element that was found in the tree
	  * @param newElement the updated element that was passed in the REFRESH_REMOTE event
	  * @return true if you want the viewer that called this to refresh the children of this object,
	  *   such as is needed on a rename of a folder, say.
	  */
	public boolean refreshRemoteObject(Object oldElement, Object newElement)
	{
		return false;
	}

	/**
	 * Given a remote object, returns it remote parent object. Eg, given a file, return the folder
	 *  it is contained in.
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception
	{
		if (element instanceof IRemoteOutput)
		{
			return ((IRemoteOutput) element).getParent();
		}
		return null;
	}

	/**
	  * Given a remote object, return the unqualified names of the objects contained in that parent. This is
	  *  used for testing for uniqueness on a rename operation, for example. Sometimes, it is not 
	  *  enough to just enumerate all the objects in the parent for this purpose, because duplicate
	  *  names are allowed if the types are different, such as on iSeries. In this case return only 
	  *  the names which should be used to do name-uniqueness validation on a rename operation.
	  */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element) throws Exception
	{
		return null;
	}


	/**
	* Returns the current collection of property descriptors.
	* By default returns descriptors for name and type only.
	* Override if desired.
	* @return an array containing all descriptors.  
	*/
	protected Object internalGetPropertyValue(Object key)
	{
	    return getPropertyValue(key, true);
	}

	/**
	 * Returns the icon to display for this object
	 * @param element the remote output object
	 * @return the associated image descriptor
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			ImageDescriptor imageDescriptor = null;
			IRemoteOutput output = (IRemoteOutput) element;
			String type = output.getType();
			if (type.equals(TYPE_ERROR))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_ERROR_ID);
			}
			else if (type.equals(TYPE_WARNING))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_WARNING_ID);
			}
			else if (type.equals(TYPE_INFORMATIONAL))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_INFO_ID);
			}
			else if (type.equals(TYPE_DIRECTORY))
			{
				imageDescriptor = //PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER); 
					RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FOLDER_ID);
			}
			else if (type.equals(TYPE_FILE))
			{
				imageDescriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE); 
					//RSEUIPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_FILE_ID);
			}
			else if (type.equals(TYPE_GREP))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SEARCH_RESULT_ID);
			}
			else if (type.equals(TYPE_COMMAND))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHELL_ID);
			}
			else if (type.equals(TYPE_PROMPT))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHELL_ID);
			}
			else if (type.equals(TYPE_PROCESS))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROCESS_ID);
			}
			else if (type.equals(TYPE_ENVVAR))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_ENVVAR_ID);
			}
			else if (type.equals(TYPE_ENVVAR_LIBPATH))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_ENVVAR_LIBPATH_ID);
			}
			else if (type.equals(TYPE_ENVVAR_PATH))
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_ENVVAR_PATH_ID);
			}
			else
			{
				imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_BLANK_ID);
			}

			return imageDescriptor;
		}

		else if (element instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell command = (IRemoteCommandShell) element;
			IRemoteCmdSubSystemConfiguration factory = command.getCommandSubSystem().getParentRemoteCmdSubSystemFactory();
			 ShellServiceSubSystemFactoryAdapter factoryAdapter = (ShellServiceSubSystemFactoryAdapter)factory.getAdapter(ISubsystemConfigurationAdapter.class);
			ImageDescriptor imageDescriptor = null; 			
			if (command.isActive())
			{
				imageDescriptor = factoryAdapter.getActiveCommandShellImageDescriptor();
			}
			else
			{
			    imageDescriptor = factoryAdapter.getInactiveCommandShellImageDescriptor();
			}
			return imageDescriptor;
		}

		else
		{ // return some default	 
			ImageDescriptor imageDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_BLANK_ID);
			return imageDescriptor;
		}
	}

	/**
	 * Return true if we should show the delete action in the popup for the given element.
	 * If true, then canDelete will be called to decide whether to enable delete or not.
	 */
	public boolean showDelete(Object element)
	{
		return false;
	}
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 * By default, returns false. Override if your object is deletable.
	 */
	public boolean canDelete(Object element)
	{
		return false;
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON REFRESH ACTION...
	// ------------------------------------------
	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showRefresh(Object element)
	{
		return false;
	}

	// ------------------------------------------------------------
	// METHODS TO SUPPORT COMMON OPEN-IN-NEW-PERSPECTIVE ACTIONS...
	// ------------------------------------------------------------
	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showOpenViewActions(Object element)
	{
		return false;
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON RENAME ACTION...
	// ------------------------------------------

	/**
	 * Return true if we should show the rename action in the popup for the given element.
	 * If true, then canRename will be called to decide whether to enable rename or not.
	 */
	public boolean showRename(Object element)
	{
		return false;
	}
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 * By default, returns false. Override if your object is renamable.
	 */
	public boolean canRename(Object element)
	{
		return false;
	}

	/**
	 * Perform the rename action. By default does nothing. Override if your object is renamable.
	 * Return true if this was successful. Return false if it failed and you issued a msg. 
	 * Throw an exception if it failed and you want to use the generic msg.
	 */
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		return false;
	}

	// Drag and drop

	/**
	 * Indicates whether the specified object can have another object copied to it
	 * @param element the object to copy to
	 * @return whether this object can be copied to or not
	 */
	public boolean canDrop(Object element)
	{
	    /*DKM -for now disabling - the function doesn't 
	     * make sense for other types of prompts like
	     * RAD4z
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			if (output.getType().equals(TYPE_PROMPT))
			{
				return true;
			}
		}
		*/

		return false;
	}

	/**
	 * Indicates whether the specified object can be copied 
	 * @param element the object to copy
	 */
	public boolean canDrag(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			return true;
		}
		else if (element instanceof IRemoteCommandShell)
		{
		    return true;
		}

		return false;
	}

	/**
	 * Copy the specified remote output object.  This method returns a string representing
	 * the text of the remote output;
	 * 
	 * @param element the output to copy
	 * @param sameSystemType not applicable for remote output
	 * @param monitor the progress monitor
	 */
	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor)
	{
		if (element instanceof List)
		{
			List resultSet = new ArrayList();
			List set = (List)element;
			for (int i = 0; i < set.size(); i++)
			{
				resultSet.add(getText(set.get(i)));
			}
			return resultSet;
		}
		else
		{
			return getText(element);
		}
	}

	/**
	  * Return true if it is valid for the src object to be dropped in the target
	  * @param src the object to drop
	  * @param target the object which src is dropped in
	  * @param sameSystem whether this is the same system
	  * @return whether this is a valid operation
	  */
	public boolean validateDrop(Object src, Object target, boolean sameSystem)
	{
		if (target instanceof IRemoteOutput)
		{
			IRemoteOutput targetOutput = (IRemoteOutput) target;
			if (targetOutput.getType().equals(TYPE_PROMPT))
			{
				if (src instanceof IRemoteFile)
				{
					return true;
				}
				else if (src instanceof IResource)
				{
					return true;
				}
				else if (src instanceof String)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Perform a copy via drag and drop.
	 * @param src the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param target the object to be copied to.
	 * @param sameSystem an indication whether the target and source reside on the same type of system
	 * @param indicates the type of source
	 * @param monitor the progress monitor
	 * @return an indication whether the operation was successful or not.
	 */
	public Object doDrop(Object src, Object target, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
		IRemoteFile folder = outputToFile((IRemoteOutput) target);
		if (folder != null)
		{
			ISystemDragDropAdapter adapter = (ISystemDragDropAdapter) ((IAdaptable) folder).getAdapter(ISystemDragDropAdapter.class);
			return adapter.doDrop(src, folder, sameSystemType, sameSystem, srcType, monitor);
		}
		return null;
	}

	/**
	 * Determines whether the line of remote output can be edited in an editor
	 * @param element the remote output object
	 * @return true if this can be edited in an editor
	 */
	public boolean canEdit(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			IRemoteFile file = outputToFile(output);
			if (file != null && file.isFile())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the associated remote editable object for this line of output
	 * @param element the remote output object
	 * @return the associated ediable object if one exists
	 */
	public ISystemEditableRemoteObject getEditableRemoteObject(Object element)
	{
		if (element instanceof IRemoteOutput)
		{
			IRemoteOutput output = (IRemoteOutput) element;
			IRemoteFile file = outputToFile(output);
			if (file != null && file.isFile())
			{
				return new SystemEditableRemoteFile(file);
			}
		}
		return null;
	}

	/**
	 * Return a filter string that corresponds to this object.
	 * @param object the object to obtain a filter string for
	 * @return the corresponding filter string if applicable
	 */
	public String getFilterStringFor(Object object)
	{
		return null;
	}
	
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
	    return getUniquePropertyDescriptors();
	}
	
	public IPropertyDescriptor[] getUniquePropertyDescriptors()
	{
		if (propertySourceInput instanceof IRemoteCommandShell)
		{
		    if (_shellPropertyDescriptors == null)
		    {
		        _shellPropertyDescriptors = new IPropertyDescriptor[2];
		        
		        _shellPropertyDescriptors[0] = createSimplePropertyDescriptor(P_SHELL_STATUS, SystemViewResources.RESID_PROPERTY_SHELL_STATUS_LABEL, SystemViewResources.RESID_PROPERTY_SHELL_STATUS_TOOLTIP);
		        _shellPropertyDescriptors[1] = createSimplePropertyDescriptor(P_SHELL_CONTEXT, SystemViewResources.RESID_PROPERTY_SHELL_CONTEXT_LABEL, SystemViewResources.RESID_PROPERTY_SHELL_CONTEXT_TOOLTIP);
		    }
		    return _shellPropertyDescriptors;
		}
		else
		{
		    if (_outputPropertyDescriptors == null)
		    {
		        _outputPropertyDescriptors =  new IPropertyDescriptor[0];
		    }
		    return _outputPropertyDescriptors;
		}
	}
	
	/**
	 * Returns the current value for the named property.
	 * 
	 * @param property the name or key of the property as named by its property descriptor
	 * @param formatted indication of whether to return the value in formatted or raw form
	 * @return the current value of the given property
	 */
	public Object getPropertyValue(Object property, boolean formatted)
	{
		String name = (String) property;
		if (propertySourceInput instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmdShell = (IRemoteCommandShell) propertySourceInput;
			if (name.equals(ISystemPropertyConstants.P_SHELL_STATUS))
			{
			    if (cmdShell.isActive())
			    {
			        return SystemViewResources.RESID_PROPERTY_SHELL_STATUS_ACTIVE_VALUE;
			    }
			    else
			    {
			        return SystemViewResources.RESID_PROPERTY_SHELL_STATUS_INACTIVE_VALUE;
			    }
			}
			else if (name.equals(ISystemPropertyConstants.P_SHELL_CONTEXT))
			{
			    Object context = cmdShell.getContext();
			    if (context instanceof IRemoteFile)
			    {			        
			        IRemoteFile cwd = (IRemoteFile)context;
			        if (cwd != null)
			        {	
			        	return cwd.getAbsolutePath();			        
			        }	
			    }
			    else
			     {
			        return context;
			     }
			}
		}
		return "";
	}
	

	/**
	 * Returns <code>false</code>.
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		return false;
	}
}