/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.wizards;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.core.templateengine.TemplateCategory;
import org.eclipse.cdt.core.templateengine.TemplateEngine2;
import org.eclipse.cdt.core.templateengine.TemplateInfo2;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;

/**
 * @author Doug Schaefer
 * @since 5.4
 */
public class TemplateSelectionPage extends WizardPage {

	private static class Node {
		private final Object object;
		private final Node parent;
		private final List<Node> children = new LinkedList<Node>();
		
		public Node(Node parent, Object object) {
			this.parent = parent;
			this.object = object;
			
			if (parent != null)
				parent.addChild(this);
		}

		private void addChild(Node child) {
			children.add(child);
		}
		
		public Node getChild(Object child) {
			for (Node childNode : children)
				if (childNode.getObject().equals(child))
					return childNode;
			return null;
		}
		
		public Object getObject() {
			return object;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public List<Node> getChildren() {
			return children;
		}
	}
	
	private static Node tree;
	
	private final TemplateEngine2 coreEngine = TemplateEngine2.getDefault();
	private final TemplateEngineUI uiEngine = TemplateEngineUI.getDefault();
	
	private TreeViewer templateTree;
	private Template selectedTemplate;
	private IWizardPage[] nextPages;
	
	public TemplateSelectionPage() {
		super("templateSelection"); //$NON-NLS-1$
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		
		templateTree = new TreeViewer(comp);
		templateTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		templateTree.setContentProvider(new ITreeContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof Node)
					return !((Node)element).getChildren().isEmpty();
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				if (element instanceof Node)
					return ((Node)element).getParent();
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Node)
					return ((Node)inputElement).getChildren().toArray();
				return null;
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Node)
					return ((Node)parentElement).getChildren().toArray();
				return null;
			}
		});
		templateTree.setLabelProvider(new ILabelProvider() {
			@Override
			public void removeListener(ILabelProviderListener listener) {
			}
			
			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public void addListener(ILabelProviderListener listener) {
			}
			
			@Override
			public String getText(Object element) {
				if (element instanceof Node) {
					Object object = ((Node)element).getObject();
					if (object instanceof TemplateCategory)
						return ((TemplateCategory)object).getLabel();
					else if (object instanceof Template)
						return ((Template)object).getLabel();
				}
				return element.toString();
			}
			
			@Override
			public Image getImage(Object element) {
				return null;
			}
		});
		templateTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedTemplate = null;
				nextPages = null;
				IStructuredSelection selection = (IStructuredSelection)templateTree.getSelection();
				Object selObj = selection.getFirstElement();
				if (selObj instanceof Node) {
					Object object = ((Node)selObj).getObject();
					if (object instanceof Template) {
						IWizard wizard = getWizard();
						selectedTemplate = (Template)object;
						nextPages = selectedTemplate.getTemplateWizardPages(TemplateSelectionPage.this,
								wizard.getNextPage(TemplateSelectionPage.this), wizard);
						setPageComplete(true);
					} else {
						setPageComplete(false);
					}
				} else {
					setPageComplete(false);
				}
			}
		});
		buildTree();
		templateTree.setInput(tree);
		
		setControl(comp);
	}

	public Template getSelectedTemplate() {
		return selectedTemplate;
	}
	
	@Override
	public boolean isPageComplete() {
		return selectedTemplate != null;
	}
	
	@Override
	public IWizardPage getNextPage() {
		if (nextPages != null && nextPages.length > 0)
			return nextPages[0];
		return super.getNextPage();
	}
	
	private void buildTree() {
		if (tree != null)
			return;
		tree = new Node(null, null);
		
		Template[] templates = uiEngine.getTemplates();
		for (Template template : templates) {
			List<String> parentCategoryIds = ((TemplateInfo2)template.getTemplateInfo()).getParentCategoryIds();
			boolean inTree = false;
			if (!parentCategoryIds.isEmpty()) {
				for (String parentCategoryId : parentCategoryIds) {
					List<Node> parents = getParents(parentCategoryId);
					if (!parents.isEmpty()) {
						for (Node parent : parents)
							new Node(parent, template);
						inTree = true;
					}
				}
			}
			
			if (!inTree) {
				// no parents
				new Node(tree, template);
			}
		}
	}
	
	private List<Node> getParents(String parentCategoryId) {
		List<Node> nodes = new LinkedList<Node>();
		
		TemplateCategory category = coreEngine.getCategory(parentCategoryId);
		if (category == null) {
			// undefined, log it
			CUIPlugin.log(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, "Undefined parent category " + parentCategoryId)); //$NON-NLS-1$
			return nodes;
		}
		
		// Hook me up to my parents
		List<String> parentCategoryIds = category.getParentCategoryIds();
		boolean inTree = false;
		if (!parentCategoryIds.isEmpty()) {
			for (String myParentId : parentCategoryIds) {
				List<Node> parents = getParents(myParentId);
				if (!parents.isEmpty()) {
					for (Node parent : parents) {
						Node node = parent.getChild(category);
						if (node == null)
							nodes.add(new Node(parent, category));
						else
							nodes.add(node);
					}
					inTree = true;
				}
			}
		}
		
		if (!inTree) {
			// parents not found, I'm an orphan
			Node node = tree.getChild(category);
			if (node == null)
				nodes.add(new Node(tree, category));
			else
				nodes.add(node);
		}

		return nodes;
	}
	
}
