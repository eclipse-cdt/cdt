/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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
	IASTTranslationUnit tu;
	DOMASTNodeParent tuTreeParent;
	TreeViewer viewer;
	IASTName[] matchingNames;
	boolean wasForward;
	int index;

	static protected class NameCollector extends ASTVisitor {
		private static final int REGULAR_NAME_ADD = -1;
		private static final String BLANK_STRING = ""; //$NON-NLS-1$
		{
			shouldVisitNames = true;
		}
		public List<IASTName> nameList = new ArrayList<>();

		String findString = null;
		boolean caseSensitive = true;
		boolean wholeWord = true;
		boolean regExSearch = false;
		Pattern p = null;
		Matcher m = null;

		public NameCollector(String findString, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
			this.findString = findString;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.regExSearch = regExSearch;
		}

		public int processName(IASTName name, int index) {
			if (name.toString() == null || name.toString() == BLANK_STRING)
				return PROCESS_CONTINUE;
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
				if (index >= 0) {
					nameList.add(index, name);
				} else {
					nameList.add(name);
				}
			}

			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			return processName(name, REGULAR_NAME_ADD);
		}

		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}

		public int size() {
			return nameList.size();
		}

		private void mergeName(IASTName name) {
			if (name instanceof ASTNode) {
				int offset = ((ASTNode) name).getOffset();
				for (int i = 0; i < nameList.size(); i++) {
					if (nameList.get(i) instanceof ASTNode && ((ASTNode) nameList.get(i)).getOffset() > offset) {
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
			for (IASTPreprocessorStatement statement : statements) {
				if (statement instanceof IASTPreprocessorMacroDefinition) {
					IASTName name = ((IASTPreprocessorMacroDefinition) statement).getName();
					if (name != null) {
						mergeName(name);
					}
				}
			}

			// convert the array list into an array of IASTNames
			return nameList.toArray(new IASTName[nameList.size()]);
		}
	}

	public FindIASTNameTarget(TreeViewer viewer) {
		if (viewer.getContentProvider() instanceof DOMAST.ViewContentProvider) {
			tu = ((DOMAST.ViewContentProvider) viewer.getContentProvider()).getTU();
			tuTreeParent = ((DOMAST.ViewContentProvider) viewer.getContentProvider()).getTUTreeParent();
		}

		this.viewer = viewer;
	}

	@Override
	public boolean canPerformFind() {
		return true;
	}

	// recursively search for the next node
	public IASTName findNextMatchingName(String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord, boolean regExSearch) {
		if (matchingNames == null && tu != null) {
			NameCollector col = new NameCollector(findString, caseSensitive, wholeWord, regExSearch);
			tu.accept(col);
			matchingNames = col.getNameArray(tu.getAllPreprocessorStatements());
		}

		if (searchForward) {
			if (!wasForward) {
				wasForward = true;
				index += 2;
			}

			if (index >= 0 && index < matchingNames.length && matchingNames[index] != null)
				return matchingNames[index++];
		} else {
			if (wasForward) {
				wasForward = false;
				index -= 2;
			}

			if (index >= 0 && index < matchingNames.length && matchingNames[index] != null)
				return matchingNames[index--];
		}

		return null;
	}

	private TreeItem expandTreeToTreeObject(TreeItem[] treeItems, DOMASTNodeLeaf treeObj) {
		for (TreeItem treeItem : treeItems) {
			if (treeItem.getData() == treeObj) {
				return treeItem;
			}

			DOMASTNodeParent parent = treeObj.getParent();

			if (parent == null)
				return null;

			while (parent != treeItem.getData()) {
				parent = parent.getParent();
				if (parent == null)
					break;
			}

			if (parent == treeItem.getData()) {
				treeItem.setExpanded(true);
				viewer.refresh();

				return expandTreeToTreeObject(treeItem.getItems(), treeObj);
			}
		}

		return null; // nothing found
	}

	private TreeItem expandTreeToTreeObject(DOMASTNodeLeaf treeObj) {
		return expandTreeToTreeObject(viewer.getTree().getItems(), treeObj);
	}

	@Override
	public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord) {
		return findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord, false);
	}

	@Override
	public Point getSelection() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

		if (selection.isEmpty()) {
			return new Point(0, 0);
		}

		return new Point(((ASTNode) ((DOMASTNodeLeaf) selection.getFirstElement()).getNode()).getOffset(), 0);
	}

	@Override
	public String getSelectionText() {
		return null;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void replaceSelection(String text) {
	}

	public void clearMatchingNames() {
		matchingNames = null;
		index = 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension3#findAndSelect(int, java.lang.String, boolean, boolean, boolean, boolean)
	 */
	@Override
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord, boolean regExSearch) {
		// find the next name in the list of names
		IASTName foundName = null;
		foundName = findNextMatchingName(findString, searchForward, caseSensitive, wholeWord, regExSearch);

		// get the DOMASTNodeLeaf from the AST View's model corresponding to that name
		DOMASTNodeLeaf treeNode = null;
		TreeItem treeItem = null;
		treeNode = tuTreeParent.findTreeObject(foundName, true);

		if (treeNode != null && treeNode.getParent() != null) {
			// found a matching DOMASTNodeLeaf, so expand the tree to that object
			treeItem = expandTreeToTreeObject(treeNode);
		}

		// loop until the next name within the matchingNames list is found in the tree
		while ((treeNode == null || treeItem == null) && matchingNames.length > 0
				&& ((searchForward && index < matchingNames.length) || (!searchForward && index >= 0))) {
			foundName = findNextMatchingName(findString, searchForward, caseSensitive, wholeWord, regExSearch);
			treeNode = tuTreeParent.findTreeObject(foundName, true);

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
	@Override
	public void replaceSelection(String text, boolean regExReplace) {
	}
}
