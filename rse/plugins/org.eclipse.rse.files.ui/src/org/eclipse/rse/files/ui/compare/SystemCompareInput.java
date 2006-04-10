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

package org.eclipse.rse.files.ui.compare;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ZipFileStructureCreator;
import org.eclipse.compare.internal.BufferedResourceNode;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Composite;



public class SystemCompareInput extends CompareEditorInput
{ 

	private static final boolean NORMALIZE_CASE = true;
	private Object fRoot;

	class MyDiffNode extends DiffNode
	{
		private boolean fDirty = false;
		private ITypedElement fLastId;
		private String fLastName;

		public MyDiffNode(IDiffContainer parent, int description, ITypedElement ancestor, ITypedElement left, ITypedElement right)
		{
			super(parent, description, ancestor, left, right);
		}

		public void fireChange()
		{
			super.fireChange();
			setDirty(true);
			fDirty = true;
			if (_diffViewer != null)
				_diffViewer.refresh(this);
		}

		void clearDirty()
		{
			fDirty = false;
		}

		public String getName()
		{
			if (fLastName == null)
				fLastName = super.getName();
			if (fDirty)
				return '<' + fLastName + '>';
			return fLastName;
		}

		public ITypedElement getId()
		{
			ITypedElement id = super.getId();
			if (id == null)
				return fLastId;
			fLastId = id;
			return id;
		}
	}

	private List _remoteEditables;
	private DiffTreeViewer _diffViewer;
	private IResource _leftResource;
	private IResource _rightResource;

	public SystemCompareInput(CompareConfiguration configuration)
	{
		super(configuration);
		_remoteEditables = new ArrayList();
	}

	/**
	 * Creates a <code>IStructureComparator</code> for the given input.
	 * Returns <code>null</code> if no <code>IStructureComparator</code>
	 * can be found for the <code>IResource</code>.
	 */
	private IStructureComparator getStructure(IResource input)
	{
		if (input instanceof IContainer)
			return new BufferedResourceNode(input);

		if (input instanceof IFile)
		{
			IStructureComparator rn = new BufferedResourceNode(input);
			IFile file = (IFile) input;
			String type = normalizeCase(file.getFileExtension());
			if ("JAR".equals(type) || "ZIP".equals(type)) //$NON-NLS-2$ //$NON-NLS-1$
				return new ZipFileStructureCreator().getStructure(rn);
			return rn;
		}
		return null;
	}

	private String normalizeCase(String s)
	{
		if (NORMALIZE_CASE && s != null)
			return s.toUpperCase();
		return s;
	}
	
	public IResource getLeftResource()
	{
		return _leftResource;
	}
	
	public IResource getRightResource()
	{
		return _rightResource;
	}

	public Object prepareInput(IProgressMonitor monitor)
	{
		ISystemEditableRemoteObject ef1 =  (ISystemEditableRemoteObject)_remoteEditables.get(0);
		ISystemEditableRemoteObject ef2 = (ISystemEditableRemoteObject)_remoteEditables.get(1);
			
		try
		{
			ef1.download(monitor);
			ef2.download(monitor);
			ef1.addAsListener();
			ef2.addAsListener();
			ef1.setLocalResourceProperties();
			ef2.setLocalResourceProperties();
				
			_leftResource = ef1.getLocalResource();
			_rightResource = ef2.getLocalResource();
			
			String title;
			String format = Utilities.getString("ResourceCompare.twoWay.title"); //$NON-NLS-1$
			title = MessageFormat.format(format, new String[] {_leftResource.getName(), _rightResource.getName()});
			setTitle(title);
		}
		catch (Exception e)
		{
		}			

		IStructureComparator c1 = getStructure(_leftResource);
		IStructureComparator c2 = getStructure(_rightResource);

		Differencer d = new Differencer()
		{
			protected Object visit(Object parent, int description, Object ancestor, Object left, Object right)
			{
				return new MyDiffNode((IDiffContainer) parent, description, (ITypedElement) ancestor, (ITypedElement) left, (ITypedElement) right);
			}
		};

		fRoot = d.findDifferences(false, monitor, null, null, c1, c2);
		return fRoot;
	}

	public Viewer createDiffViewer(Composite parent)
	{
		_diffViewer = new DiffTreeViewer(parent, getCompareConfiguration())
		{
			protected void fillContextMenu(IMenuManager manager)
			{
				super.fillContextMenu(manager);
			}
		};
		return _diffViewer;
	}

	public void addRemoteEditable(ISystemEditableRemoteObject file)
	{
		_remoteEditables.add(file);
	}

	public void saveChanges(IProgressMonitor pm) throws CoreException
	{
		super.saveChanges(pm);
		if (fRoot instanceof DiffNode)
		{
			try
			{
				commit(pm, (DiffNode) fRoot);
			}
			finally
			{
				if (_diffViewer != null)
					_diffViewer.refresh();
				setDirty(false);
			}
		}

	}

	/*
	 * Recursively walks the diff tree and commits all changes.
	 */
	private void commit(IProgressMonitor pm, DiffNode node) throws CoreException
	{
		if (node instanceof MyDiffNode)
			 ((MyDiffNode) node).clearDirty();

		ITypedElement left = node.getLeft();
		if (left instanceof BufferedResourceNode)
			 ((BufferedResourceNode) left).commit(pm);

		ITypedElement right = node.getRight();
		if (right instanceof BufferedResourceNode)
			 ((BufferedResourceNode) right).commit(pm);

		IDiffElement[] children = node.getChildren();
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				IDiffElement element = children[i];
				if (element instanceof DiffNode)
					commit(pm, (DiffNode) element);
			}
		}
	}
}