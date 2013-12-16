/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [422844] rse.files.ui fails to compile against Eclipse 4.4 Luna I20131126
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.widgets;
import java.lang.reflect.Field;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileEmpty;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;




/**
 * This is a private-use subclass of the internal Eclipse ResourceTreeAndListGroup
 * composite widget. This is for selecting multiple local or remote files. It contains
 * a checkbox tree on the left for selecting the folder, and a checkbox list on the
 * right for selecting the files.
 * <p>
 * We subclass this to add some slight additional functionality, including support for
 * refreshing the contents.
 */
public class SystemFileTreeAndListGroup extends org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup
{
	private Object rootObject = null;

	// CONSTANTS
    private static final RemoteFileEmpty EMPTYROOT = new RemoteFileEmpty();
    	
	/**
	 * Constructor when there is initial content
	 *	@param parent org.eclipse.swt.widgets.Composite
	 *  @param rootObject java.lang.Object
	 *	@param treeContentProvider supplies the folders for the tree
	 *	@param treeLabelProvider supplies the names and icons of the folders for the tree
	 *	@param listContentProvider supplies the files for the tree
	 *	@param listLabelProvider supplies the names and icons for the files for the list
	 *	@param style int
	 *	@param width int
	 *	@param height int
	 */
	public SystemFileTreeAndListGroup(Composite parent,Object rootObject,
	                                  ITreeContentProvider treeContentProvider, ILabelProvider treeLabelProvider,
	                                  IStructuredContentProvider listContentProvider, ILabelProvider listLabelProvider,
 	                                  int style,int width,int height)
    {
		// DKM  - API change 
		//super(parent, rootObject, treeContentProvider, treeLabelProvider, 
		//      listContentProvider, listLabelProvider, style, width, height);
		super(parent, rootObject, treeContentProvider, treeLabelProvider,
				listContentProvider, listLabelProvider, style, true);      
		      
		this.rootObject = rootObject;
	}
	/**
	 * Constructor when there is no initial content
	 *	@param parent org.eclipse.swt.widgets.Composite
	 *	@param treeContentProvider supplies the folders for the tree
	 *	@param treeLabelProvider supplies the names and icons of the folders for the tree
	 *	@param listContentProvider supplies the files for the tree
	 *	@param listLabelProvider supplies the names and icons for the files for the list
	 *	@param style int
	 *	@param width int
	 *	@param height int
	 */
	public SystemFileTreeAndListGroup(Composite parent,
	                                  ITreeContentProvider treeContentProvider, ILabelProvider treeLabelProvider,
	                                  IStructuredContentProvider listContentProvider, ILabelProvider listLabelProvider,
 	                                  int style,int width,int height)
    {
		this(parent, EMPTYROOT, treeContentProvider, treeLabelProvider, 
		      listContentProvider, listLabelProvider, style, width, height);
	}

	
	// does not work! The viewers are private, and the create methods don't return them!!
	/**
	 * Create this group's list viewer.
	 * Override of parent so we can record locally the list viewer widget.
	 *
	protected void createListViewer(Composite parent, int width, int height) 
	{
		listViewer = super.createListViewer(parent, width, height);
		return listViewer;
	}*/

    /**
     * Refesh all the contents of the checkbox viewers
     */
    public void refresh()
    {
    	if (! (rootObject instanceof RemoteFileEmpty) )
    	{
    	  Object oldRoot = rootObject;
          setRoot(EMPTYROOT);
          setRoot(oldRoot);
          /*
          if ((selectionProvider!=null) && (oldRoot instanceof RemoteFileRootImpl))
          {
          	RemoteFileRootImpl root = (RemoteFileRootImpl)oldRoot;
          	IRemoteFile rootFile = root.getRootFile();
          	if (rootFile != null)
          	  super.selectionChanged(new SelectionChangedEvent(selectionProvider, 
          	                         new StructuredSelection(oldRoot)));
          }
          */
    	}
    }        
	

    /**
     * Set the root of the widget to be new Root. Regenerate all of the tables and lists from this
     * value.
     * Intercept of parent so we can refresh internal variables.
     * @param newRoot 
     */
    public void setRoot(Object newRoot) 
    {
    	rootObject = newRoot;
    	super.setRoot(newRoot);
    }
    
    /**
     * Clear the contents
     */
    public void clearAll()
    {
    	setRoot(EMPTYROOT);
    }
    
    /**
     * Added this to preserve original behaviour with Luna
     */
    public Table getListTable(){
		try {
			Field f = getClass().getDeclaredField("listViewer"); //$NON-NLS-1$
			f.setAccessible(true);
			CheckboxTableViewer tableV = (CheckboxTableViewer) f.get(this); //IllegalAccessException			
			return tableV.getTable();
		}
		catch (Exception e){			
		}
	
		return null;
    }
}
