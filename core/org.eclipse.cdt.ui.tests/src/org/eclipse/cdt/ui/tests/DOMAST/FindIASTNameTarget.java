/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author dsteffle
 */
public class FindIASTNameTarget implements IFindReplaceTarget, IFindReplaceTargetExtension3 {

	IASTTranslationUnit tu = null;
	DOMASTNodeParent tuTreeParent = null;
	ParserLanguage lang = null;
	TreeViewer viewer = null;
	DOMASTNodeLeaf startingNode = null;
	IASTName[] matchingNames = null;
	boolean wasForward = true;
	int index = 0;
	
    static protected class CNameCollector extends CASTVisitor {
        private static final int REGULAR_NAME_ADD = -1;
		private static final String BLANK_STRING = ""; //$NON-NLS-1$
		{
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        
        String findString = null;
		boolean caseSensitive = true;
		boolean wholeWord = true;
		boolean regExSearch = false;
		
		public CNameCollector(String findString, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
			this.findString = findString;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.regExSearch = regExSearch;
		}

		public int processName( IASTName name, int offset) {
        	if (name.toString() == null || name.toString() == BLANK_STRING) return PROCESS_CONTINUE;
        	String searchString = null;
        	String match = null;
        	boolean addName = false;
        	
        	if (caseSensitive) {
        		searchString = findString;
        		match = name.toString();
        	} else {
        		searchString = findString.toUpperCase();
        		match = name.toString().toUpperCase();
        	}	
        	
           	if (regExSearch) {
           		if (match.matches(searchString))
           			addName = true;
           	} else if (!wholeWord) {
           		if (match.indexOf(searchString) >= 0)
           			addName = true;
           	} else {
           		if (match.equals(searchString))
           			addName = true;
           	}
           	
           	if (addName) {
           		if (offset >= 0)
           			nameList.add(offset, name);
           		else
           			nameList.add( name );
           	}
        	
            return PROCESS_CONTINUE;
		}
		
        public int visit( IASTName name ){
        	return processName(name, REGULAR_NAME_ADD);
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
        
        private void mergeName(IASTName name) {
        	if (name instanceof ASTNode) {
        		int offset = ((ASTNode)name).getOffset();
        		for( int i=0; i<nameList.size(); i++) {
        			if (nameList.get(i) instanceof ASTNode && 
        					((ASTNode)nameList.get(i)).getOffset() > offset) {
        				processName(name, i);
        				return;
        			}
        		}
        		// if couldn't find the proper place to put the name, then add default
        		visit(name);
        	}
        }
        
        public IASTName[] getNameArray(IASTPreprocessorStatement[] statements) {
        	// first merge all of the preprocessor names into the array list
        	for(int i=0; i<statements.length; i++) {
        		if (statements[i] instanceof IASTPreprocessorMacroDefinition) {
        			IASTName name = ((IASTPreprocessorMacroDefinition)statements[i]).getName();
        			if (name != null) {
        				mergeName(name);
        			}
        		}
        	}
        	
        	// convert the array list into an array of IASTNames
        	IASTName[] namedArray = new IASTName[nameList.size()];
        	
        	for(int i=0; i<nameList.size(); i++) {
        		if (nameList.get(i) instanceof IASTName)
        			namedArray[i] = (IASTName)nameList.get(i);
        	}
        	
        	return namedArray;
        }
    }
    
    static protected class CPPNameCollector extends CPPASTVisitor {
        private static final int REGULAR_NAME_ADD = -1;
		private static final String BLANK_STRING = ""; //$NON-NLS-1$
		{
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        
        String findString = null;
		boolean caseSensitive = true;
		boolean wholeWord = true;
		boolean regExSearch = false;
		Pattern p = null;
		Matcher m = null;
		
		public CPPNameCollector(String findString, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
			this.findString = findString;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.regExSearch = regExSearch;
		}
        
		public int processName( IASTName name, int index) {
        	if (name.toString() == null || name.toString() == BLANK_STRING) return PROCESS_CONTINUE;
        	String searchString = null;
        	String match = null;
        	boolean addName = false;
        	
        	if (caseSensitive) {
        		searchString = findString;
        		match = name.toString();
        	} else {
        		searchString = findString.toUpperCase();
        		match = name.toString().toUpperCase();
        	}	
        	
           	if (regExSearch) {
           		if (match.matches(searchString))
           			addName = true;
           	} else if (!wholeWord) {
           		if (match.indexOf(searchString) >= 0)
           			addName = true;
           	} else {
           		if (match.equals(searchString))
           			addName = true;
           	}
           	
           	if (addName) {
           		if (index >= 0)
           			nameList.add(index, name);
           		else
           			nameList.add( name );
           	}
        	
            return PROCESS_CONTINUE;
		}
		
        public int visit( IASTName name ){
        	return processName(name, REGULAR_NAME_ADD);
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
        
        private void mergeName(IASTName name) {
        	if (name instanceof ASTNode) {
        		int offset = ((ASTNode)name).getOffset();
        		for( int i=0; i<nameList.size(); i++) {
        			if (nameList.get(i) instanceof ASTNode && 
        					((ASTNode)nameList.get(i)).getOffset() > offset) {
        				processName(name, i);
        				return;
        			}
        		}
        		// if couldn't find the proper place to put the name, then add default
        		visit(name);
        	}
        }
        
        public IASTName[] getNameArray(IASTPreprocessorStatement[] statements) {
        	// first merge all of the preprocessor names into the array list
        	for(int i=0; i<statements.length; i++) {
        		if (statements[i] instanceof IASTPreprocessorMacroDefinition) {
        			IASTName name = ((IASTPreprocessorMacroDefinition)statements[i]).getName();
        			if (name != null) {
        				mergeName(name);
        			}
        		}
        	}
        	
        	// convert the array list into an array of IASTNames
        	IASTName[] namedArray = new IASTName[nameList.size()];
        	
        	for(int i=0; i<nameList.size(); i++) {
        		if (nameList.get(i) instanceof IASTName)
        			namedArray[i] = (IASTName)nameList.get(i);
        	}
        	
        	return namedArray;
        }
    }
	
	public FindIASTNameTarget(TreeViewer viewer, ParserLanguage lang) {
		if (viewer.getContentProvider() instanceof DOMAST.ViewContentProvider) {
			tu = ((DOMAST.ViewContentProvider)viewer.getContentProvider()).getTU();
			tuTreeParent = ((DOMAST.ViewContentProvider)viewer.getContentProvider()).getTUTreeParent();
		}
		
		this.viewer = viewer;
		this.lang = lang;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTarget#canPerformFind()
	 */
	public boolean canPerformFind() {
		return true;
	}

	// recursively search for the next node
	public IASTName findNextMatchingName(String findString,
			boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		
		if (matchingNames == null && tu != null) {
			if (lang == ParserLanguage.CPP) {
				CPPNameCollector col = new CPPNameCollector(findString, caseSensitive, wholeWord, regExSearch);
				tu.accept(col);
				matchingNames = col.getNameArray(tu.getAllPreprocessorStatements());
			} else {
				CNameCollector col = new CNameCollector(findString, caseSensitive, wholeWord, regExSearch);
				tu.accept(col);
				matchingNames = col.getNameArray(tu.getAllPreprocessorStatements());
			}
		}
	
		if (searchForward) {
			if (!wasForward) {
				wasForward = true;
				index+=2;
			}
			
			if (index >=0 && index < matchingNames.length && matchingNames[index] != null)
				return matchingNames[index++];
		} else {
			if (wasForward) {
				wasForward = false;
				index-=2;
			}
			
			if (index >= 0 && index < matchingNames.length && matchingNames[index] != null)
				return matchingNames[index--];
		}
		
		return null;
	}
		
	private TreeItem expandTreeToTreeObject(TreeItem[] treeItems, DOMASTNodeLeaf treeObj) {
		for (int i=0; i<treeItems.length; i++) {
			if (treeItems[i].getData() == treeObj) {
 				return treeItems[i];
 			}
 			
 			DOMASTNodeParent parent = treeObj.getParent();
 			
 			if (parent == null) return null; 

 			while (parent != treeItems[i].getData()) {
 				parent = parent.getParent();
 				if (parent == null) break;
 			}
 			
 			if (parent == treeItems[i].getData()) {
 				treeItems[i].setExpanded(true);
 				viewer.refresh();

 				return expandTreeToTreeObject(treeItems[i].getItems(), treeObj);
 			}
 		}
 		
 		return null; // nothing found
	}
	
 	private TreeItem expandTreeToTreeObject(DOMASTNodeLeaf treeObj) {
 		return expandTreeToTreeObject(viewer.getTree().getItems(), treeObj);
 	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTarget#findAndSelect(int, java.lang.String, boolean, boolean, boolean)
	 */
	public int findAndSelect(int widgetOffset, String findString,
			boolean searchForward, boolean caseSensitive, boolean wholeWord) {
		return findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTarget#getSelection()
	 */
	public Point getSelection() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		
		if (selection.isEmpty()) {
			if (viewer.getTree().getItems()[0].getData() instanceof DOMASTNodeLeaf);
				startingNode = (DOMASTNodeLeaf)viewer.getTree().getItems()[0].getData();
			
			return new Point(0, 0);
		}
		
		startingNode = (DOMASTNodeLeaf)selection.getFirstElement();
		return new Point(((ASTNode)((DOMASTNodeLeaf)selection.getFirstElement()).getNode()).getOffset(), 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTarget#getSelectionText()
	 */
	public String getSelectionText() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTarget#isEditable()
	 */
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTarget#replaceSelection(java.lang.String)
	 */
	public void replaceSelection(String text) {
		// TODO Auto-generated method stub

	}
	
	public void clearMatchingNames() {
		matchingNames = null;
		index = 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension3#findAndSelect(int, java.lang.String, boolean, boolean, boolean, boolean)
	 */
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		// find the next name in the list of names
		IASTName foundName = null;
		foundName = findNextMatchingName( findString, searchForward, caseSensitive, wholeWord, regExSearch );
		
		// get the DOMASTNodeLeaf from the AST View's model corresponding to that name
		DOMASTNodeLeaf treeNode = null;
		TreeItem treeItem = null;
		treeNode =  tuTreeParent.findTreeObject(foundName, true);

		if (treeNode != null && treeNode.getParent() != null) {
			// found a matching DOMASTNodeLeaf, so expand the tree to that object
			treeItem = expandTreeToTreeObject(treeNode);
		}
		
		// loop until the next name within the matchingNames list is found in the tree
		while ((treeNode == null || treeItem == null) &&  matchingNames.length > 0 &&
				((searchForward && index < matchingNames.length) ||
				(!searchForward && index >= 0))) {
			foundName = findNextMatchingName( findString, searchForward, caseSensitive, wholeWord, regExSearch );
			treeNode =  tuTreeParent.findTreeObject(foundName, true);
			
			if (treeNode != null && treeNode.getParent() != null) {
				// found a matching DOMASTNodeLeaf, so expand the tree to that object
				treeItem = expandTreeToTreeObject(treeNode);
			}
		}
		
		// select the node that was found (and is now displayed)
		if (treeItem != null) {
			TreeItem[] items = new TreeItem[1];
			items[0] = treeItem;
			treeItem.getParent().setSelection(items);
				
			return 0;
		}

		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension3#replaceSelection(java.lang.String, boolean)
	 */
	public void replaceSelection(String text, boolean regExReplace) {
		// TODO Auto-generated method stub
		
	}
	
}
