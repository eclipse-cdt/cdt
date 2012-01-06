/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.part.PageBook;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.formatter.WhiteSpaceOptions.InnerNode;
import org.eclipse.cdt.internal.ui.preferences.formatter.WhiteSpaceOptions.Node;
import org.eclipse.cdt.internal.ui.preferences.formatter.WhiteSpaceOptions.OptionNode;


public class WhiteSpaceTabPage extends FormatterTabPage {
	
    
    /**
     * Encapsulates a view of the options tree which is structured by
     * syntactical element.
     */
	
	private final class SyntaxComponent implements ISelectionChangedListener, ICheckStateListener, IDoubleClickListener {

	    private final String PREF_NODE_KEY= CUIPlugin.PLUGIN_ID + "formatter_page.white_space_tab_page.node"; //$NON-NLS-1$
	    
	    private final List<Node> fIndexedNodeList;
		private final List<? extends Node> fTree;
		
		private ContainerCheckedTreeViewer fTreeViewer;
		private Composite fComposite;
		
	    private Node fLastSelected= null;

	    public SyntaxComponent() {
	        fIndexedNodeList= new ArrayList<Node>();
			fTree= new WhiteSpaceOptions().createAltTree(fWorkingValues);
			WhiteSpaceOptions.makeIndexForNodes(fTree, fIndexedNodeList);
		}
	    
		public void createContents(final int numColumns, final Composite parent) {
			fComposite= new Composite(parent, SWT.NONE);
			fComposite.setLayoutData(createGridData(numColumns, GridData.HORIZONTAL_ALIGN_FILL, SWT.DEFAULT));
			fComposite.setLayout(createGridLayout(numColumns, false));
		    
            createLabel(numColumns, fComposite, FormatterMessages.WhiteSpaceTabPage_insert_space); 
            
	        fTreeViewer= new ContainerCheckedTreeViewer(fComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			fTreeViewer.setContentProvider(new ITreeContentProvider() {
				@Override
				public Object[] getElements(Object inputElement) {
					return ((Collection<?>)inputElement).toArray();
				}
				@Override
				public Object[] getChildren(Object parentElement) {
					return ((Node)parentElement).getChildren().toArray();
				}
				@Override
				public Object getParent(Object element) {
				    return ((Node)element).getParent(); 
				}
				@Override
				public boolean hasChildren(Object element) {
					return ((Node)element).hasChildren();
				}
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
				@Override
				public void dispose() {}
			});
			fTreeViewer.setLabelProvider(new LabelProvider());
			fTreeViewer.getControl().setLayoutData(createGridData(numColumns, GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL, SWT.DEFAULT));
			fDefaultFocusManager.add(fTreeViewer.getControl());
	    }
		
		public void initialize() {
			fTreeViewer.addCheckStateListener(this);
			fTreeViewer.addSelectionChangedListener(this);
			fTreeViewer.addDoubleClickListener(this);
		    fTreeViewer.setInput(fTree);
		    restoreSelection();
		    refreshState();
		}
		
		public void refreshState() {
		    final ArrayList<OptionNode> checked= new ArrayList<OptionNode>(100);
		    for (Node node : fTree)
				(node).getCheckedLeafs(checked);
		    fTreeViewer.setGrayedElements(new Object[0]);
		    fTreeViewer.setCheckedElements(checked.toArray());
		    fPreview.clear();
		    if (fLastSelected != null) {
		    	fPreview.addAll(fLastSelected.getSnippets());
		    }
		    doUpdatePreview();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
		    final IStructuredSelection selection= (IStructuredSelection)event.getSelection();
		    if (selection.isEmpty())
		        return;
		    final Node node= (Node)selection.getFirstElement();
		    if (node == fLastSelected)
		        return;
		    fDialogSettings.put(PREF_NODE_KEY, node.index);
		    fPreview.clear();
		    fPreview.addAll(node.getSnippets());
		    doUpdatePreview();
		    fLastSelected= node;
		}

		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			final Node node= (Node)event.getElement();
			node.setChecked(event.getChecked());
			doUpdatePreview();
			notifyValuesModified();
		}

		public void restoreSelection() {
			int index;
			try {
				index= fDialogSettings.getInt(PREF_NODE_KEY);
			} catch (NumberFormatException ex) {
				index= -1;
			}
			if (index < 0 || index > fIndexedNodeList.size() - 1) {
				index= 0;
			}
			final Node node= fIndexedNodeList.get(index);
			if (node != null) {
			    fTreeViewer.expandToLevel(node, 0);
			    fTreeViewer.setSelection(new StructuredSelection(new Node [] {node}));
			    fLastSelected= node;
			}
		}

        @Override
		public void doubleClick(DoubleClickEvent event) {
            final ISelection selection= event.getSelection();
            if (selection instanceof IStructuredSelection) {
                final Node node= (Node)((IStructuredSelection)selection).getFirstElement();
                fTreeViewer.setExpandedState(node, !fTreeViewer.getExpandedState(node));
            }
        }
        
        public Control getControl() {
            return fComposite;
        }
	}
	
	
	
	private final class CElementComponent implements ISelectionChangedListener, ICheckStateListener {
	    
	    private final String PREF_INNER_INDEX= CUIPlugin.PLUGIN_ID + "formatter_page.white_space.c_view.inner"; //$NON-NLS-1$ 
		private final String PREF_OPTION_INDEX= CUIPlugin.PLUGIN_ID + "formatter_page.white_space.c_view.option"; //$NON-NLS-1$
		
	    private final ArrayList<Node> fIndexedNodeList;
	    private final ArrayList<InnerNode> fTree;
	    
	    private InnerNode fLastSelected;
	    
	    private TreeViewer fInnerViewer;
	    private CheckboxTableViewer fOptionsViewer;
	    
	    private Composite fComposite;
	    
	    public CElementComponent() {
			fIndexedNodeList= new ArrayList<Node>();
			fTree= new WhiteSpaceOptions().createTreeByCElement(fWorkingValues);
			WhiteSpaceOptions.makeIndexForNodes(fTree, fIndexedNodeList);
	    }

	    public void createContents(int numColumns, Composite parent) {
			
			fComposite= new Composite(parent, SWT.NONE);
			fComposite.setLayoutData(createGridData(numColumns, GridData.HORIZONTAL_ALIGN_FILL, SWT.DEFAULT));
			fComposite.setLayout(createGridLayout(numColumns, false));
			
            createLabel(numColumns, fComposite, FormatterMessages.WhiteSpaceTabPage_insert_space, GridData.HORIZONTAL_ALIGN_BEGINNING); 
			
			final SashForm sashForm= new SashForm(fComposite, SWT.VERTICAL);
			sashForm.setLayoutData(createGridData(numColumns, GridData.FILL_BOTH, SWT.DEFAULT));
			
			fInnerViewer= new TreeViewer(sashForm, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

			fInnerViewer.setContentProvider(new ITreeContentProvider() {
				@Override
				public Object[] getElements(Object inputElement) {
					return ((Collection<?>)inputElement).toArray();
				}
				@Override
				public Object[] getChildren(Object parentElement) {
				    final List<Node> children= ((Node)parentElement).getChildren();
				    final ArrayList<Object> innerChildren= new ArrayList<Object>();
				    for (Object o : children) { 
                        if (o instanceof InnerNode) innerChildren.add(o);
                    }
				    return innerChildren.toArray();
				}
				@Override
				public Object getParent(Object element) {
				    if (element instanceof InnerNode)
				        return ((InnerNode)element).getParent();
				    return null;
				}
				@Override
				public boolean hasChildren(Object element) {
				    final List<?> children= ((Node)element).getChildren();
				    for (Object child : children)
						if (child instanceof InnerNode) return true;
				    return false;
				}
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
				@Override
				public void dispose() {}
			});
			
			fInnerViewer.setLabelProvider(new LabelProvider());
			
			final GridData innerGd= createGridData(numColumns, GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL, SWT.DEFAULT);
			innerGd.heightHint= fPixelConverter.convertHeightInCharsToPixels(3);
			fInnerViewer.getControl().setLayoutData(innerGd);
			
			fOptionsViewer= CheckboxTableViewer.newCheckList(sashForm, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
			fOptionsViewer.setContentProvider(new ArrayContentProvider());
			fOptionsViewer.setLabelProvider(new LabelProvider());
			
			final GridData optionsGd= createGridData(numColumns, GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL, SWT.DEFAULT);
			optionsGd.heightHint= fPixelConverter.convertHeightInCharsToPixels(3);
			fOptionsViewer.getControl().setLayoutData(optionsGd);
	        
			fDefaultFocusManager.add(fInnerViewer.getControl());
	        fDefaultFocusManager.add(fOptionsViewer.getControl());
			
			fInnerViewer.setInput(fTree);
		}
	    
	    public void refreshState() {
	    	if (fLastSelected != null) {
	    		innerViewerChanged(fLastSelected);
	    	}
	    }
	    
	    public void initialize() {
	        fInnerViewer.addSelectionChangedListener(this);
	        fOptionsViewer.addSelectionChangedListener(this);
	        fOptionsViewer.addCheckStateListener(this);
	        restoreSelections();
	        refreshState();
	    }
	    
	    private void restoreSelections() {
	        Node node;
	        final int innerIndex= getValidatedIndex(PREF_INNER_INDEX);
			node= fIndexedNodeList.get(innerIndex);
			if (node instanceof InnerNode) {
			    fInnerViewer.expandToLevel(node, 0);
			    fInnerViewer.setSelection(new StructuredSelection(new Object[] {node}));
			    fLastSelected= (InnerNode)node;
			}
			
	        final int optionIndex= getValidatedIndex(PREF_OPTION_INDEX);
			node= fIndexedNodeList.get(optionIndex);
			if (node instanceof OptionNode) {
			    fOptionsViewer.setSelection(new StructuredSelection(new Object[] {node}));
			}

	    }
	    
	    private int getValidatedIndex(String key) {
			int index;
			try {
				index= fDialogSettings.getInt(key);
			} catch (NumberFormatException ex) {
				index= 0;
			}
			if (index < 0 || index > fIndexedNodeList.size() - 1) {
				index= 0; 
			}
			return index;
	    }
	    
	    public Control getControl() {
	        return fComposite;
	    }

        @Override
		public void selectionChanged(SelectionChangedEvent event) {
            final IStructuredSelection selection= (IStructuredSelection)event.getSelection();

            if (selection.isEmpty() || !(selection.getFirstElement() instanceof Node))
                return;

            final Node selected= (Node)selection.getFirstElement();

            if (selected == null || selected == fLastSelected) 
			    return;
            
            
            if (event.getSource() == fInnerViewer && selected instanceof InnerNode) {
                fLastSelected= (InnerNode)selected;
                fDialogSettings.put(PREF_INNER_INDEX, selected.index);
                innerViewerChanged((InnerNode)selected);
            }
            else if (event.getSource() == fOptionsViewer && selected instanceof OptionNode)
                fDialogSettings.put(PREF_OPTION_INDEX, selected.index);
        }
	
        private void innerViewerChanged(InnerNode selectedNode) {
            
			final List<Node> children= selectedNode.getChildren();
			
			final ArrayList<OptionNode> optionsChildren= new ArrayList<OptionNode>();
			for (Object o : children) {
			    if (o instanceof OptionNode) optionsChildren.add((OptionNode) o);
			}
			
			fOptionsViewer.setInput(optionsChildren.toArray());
			
			for (OptionNode child : optionsChildren) {
				fOptionsViewer.setChecked(child, child.getChecked());
			}
			
			fPreview.clear();
			fPreview.addAll(selectedNode.getSnippets());
			doUpdatePreview();
        }
        
        @Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			final OptionNode option= (OptionNode)event.getElement();
			if (option != null)
			    option.setChecked(event.getChecked());
			doUpdatePreview();
			notifyValuesModified();
        }
	}
	
	

	/**
	 * This component switches between the two view and is responsible for delegating
	 * the appropriate update requests.
	 */
	private final class SwitchComponent extends SelectionAdapter {
	    private final String PREF_VIEW_KEY= CUIPlugin.PLUGIN_ID + "formatter_page.white_space_tab_page.view"; //$NON-NLS-1$
	    private final String [] fItems= new String [] {
	        FormatterMessages.WhiteSpaceTabPage_sort_by_c_element, 
	        FormatterMessages.WhiteSpaceTabPage_sort_by_syntax_element
	    };
	    
	    private Combo fSwitchCombo; 
	    private PageBook fPageBook;
	    private final SyntaxComponent fSyntaxComponent;
	    private final CElementComponent fCElementComponent;
	    
	    public SwitchComponent() {
	        fSyntaxComponent= new SyntaxComponent();
	        fCElementComponent= new CElementComponent();
	    }
	    
        @Override
		public void widgetSelected(SelectionEvent e) {
            final int index= fSwitchCombo.getSelectionIndex();
            if (index == 0) {
    		    fDialogSettings.put(PREF_VIEW_KEY, false);
    		    fCElementComponent.refreshState();
                fPageBook.showPage(fCElementComponent.getControl());
            }
            else if (index == 1) { 
    		    fDialogSettings.put(PREF_VIEW_KEY, true);
    		    fSyntaxComponent.refreshState();
                fPageBook.showPage(fSyntaxComponent.getControl());
            }
        }

        public void createContents(int numColumns, Composite parent) {
             
            fPageBook= new PageBook(parent, SWT.NONE);
            fPageBook.setLayoutData(createGridData(numColumns, GridData.FILL_BOTH, SWT.DEFAULT));
            
            fCElementComponent.createContents(numColumns, fPageBook);		
            fSyntaxComponent.createContents(numColumns, fPageBook);
            
            fSwitchCombo= new Combo(parent, SWT.READ_ONLY);
            final GridData gd= createGridData(numColumns, GridData.HORIZONTAL_ALIGN_END, SWT.DEFAULT);
            fSwitchCombo.setLayoutData(gd);
            fSwitchCombo.setItems(fItems);
        }
        
        public void initialize() {
            fSwitchCombo.addSelectionListener(this);
    	    fCElementComponent.initialize();
    	    fSyntaxComponent.initialize();
    	    restoreSelection();
        }

        private void restoreSelection() {
            final boolean selectSyntax= fDialogSettings.getBoolean(PREF_VIEW_KEY);
            if (selectSyntax) {
            	fSyntaxComponent.refreshState();
                fSwitchCombo.setText(fItems[1]);
                fPageBook.showPage(fSyntaxComponent.getControl());
			} else {
            	fCElementComponent.refreshState();
			    fSwitchCombo.setText(fItems[0]);
			    fPageBook.showPage(fCElementComponent.getControl());
			}
        }
	}
	

	
	
	private final SwitchComponent fSwitchComponent;
	protected final IDialogSettings fDialogSettings; 

	protected SnippetPreview fPreview;


	/**
	 * Create a new white space dialog page.
	 * @param modifyDialog
	 * @param workingValues
	 */
	public WhiteSpaceTabPage(ModifyDialog modifyDialog, Map<String,String> workingValues) {
		super(modifyDialog, workingValues);
		fDialogSettings= CUIPlugin.getDefault().getDialogSettings();
		fSwitchComponent= new SwitchComponent();
	}

	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {
		fSwitchComponent.createContents(numColumns, composite);
	}

	@Override
	protected void initializePage() {
        fSwitchComponent.initialize();
	}
	
    @Override
	protected CPreview doCreateCPreview(Composite parent) {
        fPreview= new SnippetPreview(fWorkingValues, parent);
        return fPreview;
    }

    @Override
	protected void doUpdatePreview() {
    	super.doUpdatePreview();
        fPreview.update();
    }
}
