/*******************************************************************************
 * Copyright (c) 2007, 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;

/**
 * Finds locations of linked names. Used by Rename in File and Rename in Workspace.
 */
public class LinkedNamesFinder {
	private static final IRegion[] EMPTY_LOCATIONS_ARRAY = new IRegion[0];

	private LinkedNamesFinder() {
		super();
	}

	public static IRegion[] findByName(IASTTranslationUnit root, IASTName name) {
		IBinding target = name.resolveBinding();
		if (target == null) {
			return EMPTY_LOCATIONS_ARRAY;
		}
		BindingFinder bindingFinder = new BindingFinder(root);
		bindingFinder.find(target);
		return bindingFinder.getLocations();
	}

	private static class BindingFinder {
		private final IASTTranslationUnit root;
		private final List<IRegion> locations;

		public BindingFinder(IASTTranslationUnit root) {
			this.root = root;
			locations = new ArrayList<IRegion>();
		}

		public void find(IBinding target) {
			if (target instanceof IMacroBinding) {
				findMacro((IMacroBinding) target);
				return;
			}

			if (target instanceof ICPPConstructor ||
					target instanceof ICPPMethod && ((ICPPMethod) target).isDestructor()) {
				target = ((ICPPMethod) target).getClassOwner();
			}

			findBinding(target);
			if (target instanceof ICPPClassType) {
				ICPPConstructor[] constructors = ((ICPPClassType) target).getConstructors();
				for (ICPPConstructor ctor : constructors) {
					if (!ctor.isImplicit()) {
						findBinding(ctor);
					}
				}
				ICPPMethod[] methods = ((ICPPClassType) target).getDeclaredMethods();
				for (ICPPMethod method : methods) {
					if (method.isDestructor()) {
						findBinding(method);
					}
				}
			} else if (target instanceof ICPPMethod) {
	        	ICPPMethod method= (ICPPMethod) target;
        		for (ICPPMethod m : ClassTypeHelper.findOverridden(method)) {
					findBinding(m);
				}
        		try {
					for (ICPPMethod m : findOverridersInAST(method)) {
						findBinding(m);
					}
				} catch (DOMException e) {
					// Ignore.
				}
			}
		}

		private ICPPMethod[] findOverridersInAST(ICPPMethod method) throws DOMException {
			if (!ClassTypeHelper.isVirtual(method))
				return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

			final ICPPClassType ownerClass = method.getClassOwner();
			if (ownerClass  == null) 
				return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

			SubclassFinder subclassFinder = new SubclassFinder(ownerClass);
			root.accept(subclassFinder);
			return ClassTypeHelper.findOverriders(subclassFinder.getSubclasses(), method);
		}

		public IRegion[] getLocations() {
			if (locations.isEmpty()) {
				return EMPTY_LOCATIONS_ARRAY;
			}
			return locations.toArray(new IRegion[locations.size()]);
		}

		private void findBinding(IBinding target) {
			IASTName[] names= root.getDeclarationsInAST(target);
			for (IASTName candidate : names) {
				if (candidate.isPartOfTranslationUnitFile()) {
					addLocation(candidate);
				}
			}
			names= root.getReferences(target);
			for (IASTName candidate : names) {
				if (candidate.isPartOfTranslationUnitFile()) {
					addLocation(candidate);
				}
			}
		}

		private void addLocation(IASTName name) {
			IBinding binding = name.resolveBinding();
			if (binding != null) {
				if (name instanceof ICPPASTTemplateId) {
					name= ((ICPPASTTemplateId) name).getTemplateName();
				}
				IASTFileLocation fileLocation= name.getImageLocation();
				if (fileLocation == null || !root.getFilePath().equals(fileLocation.getFileName())) {
					fileLocation= name.getFileLocation();
				}
				if (fileLocation != null) {
					int offset= fileLocation.getNodeOffset();
					int length= fileLocation.getNodeLength();
					if (binding instanceof ICPPMethod && ((ICPPMethod) binding).isDestructor()) {
						// Skip tilde.
						offset++;
						length--;
					}
					if (offset >= 0 && length > 0) {
						locations.add(new Region(offset, length));
					}
				}
			}
		}

		/**
		 * Adds all occurrences of a macro name to the list of locations. Macro occurrences
		 * may belong to multiple macro bindings with the same name. Macro names are also
		 * looked for in the comments of #else and #endif statements.
		 * Comments of #else and #endif statements related to #ifdef or #ifndef are searched
		 * for the macro name referenced by the #if[n]def. 
		 * @param target a binding representing a macro. 
		 */
		private void findMacro(IMacroBinding target) {
			findBinding(target);
			char[] nameChars = target.getNameCharArray();
			List<IASTName> ifdefNameStack = new ArrayList<IASTName>();
			IASTPreprocessorStatement[] statements = root.getAllPreprocessorStatements();
			for (IASTPreprocessorStatement statement : statements) {
				if (!statement.isPartOfTranslationUnitFile()) {
					continue;
				}
				IASTName macroName = null;
				boolean ifStatement = false;
				if (statement instanceof IASTPreprocessorIfdefStatement) {
					macroName = ((IASTPreprocessorIfdefStatement) statement).getMacroReference();
					ifStatement = true;
				} else if (statement instanceof IASTPreprocessorIfndefStatement) {
					macroName = ((IASTPreprocessorIfndefStatement) statement).getMacroReference();
					ifStatement = true;
				} else if (statement instanceof IASTPreprocessorMacroDefinition) {
					macroName = ((IASTPreprocessorMacroDefinition) statement).getName();
				} else if (statement instanceof IASTPreprocessorUndefStatement) {
					macroName = ((IASTPreprocessorUndefStatement) statement).getMacroName();
				} else if (statement instanceof IASTPreprocessorIfStatement) {
					ifStatement = true;
				} else if (statement instanceof IASTPreprocessorEndifStatement) {
					if (!ifdefNameStack.isEmpty())
						if (ifdefNameStack.remove(ifdefNameStack.size() - 1) != null) {
							findInStatementComment(nameChars, statement);
						}
				} else if (statement instanceof IASTPreprocessorElseStatement) {
					if (!ifdefNameStack.isEmpty())
						if (ifdefNameStack.get(ifdefNameStack.size() - 1) != null) {
							findInStatementComment(nameChars, statement);
						}
				}
				if (macroName != null) {
					if (Arrays.equals(nameChars, macroName.getSimpleID())) {
						IBinding binding = macroName.resolveBinding();
						if (!target.equals(binding)) {
							findBinding(binding);
						}
					} else {
						macroName = null;
					}
				}
				if (ifStatement) {
					ifdefNameStack.add(macroName);
				}
			}
		}

		/**
		 * Finds locations of a given name in the comment of a preprocessor statement.
		 */
		private void findInStatementComment(char[] nameChars, IASTPreprocessorStatement statement) {
			IASTFileLocation location = statement.getFileLocation();
			IASTComment comment = findComment(location.getNodeOffset() + location.getNodeLength());
			if (comment != null &&
					comment.getFileLocation().getStartingLineNumber() == location.getStartingLineNumber()) {
				findInComment(nameChars, comment);
			}
		}

		/**
		 * Returns the first comment after the given offset.
		 * @param startOffset a file offset.
		 * @return a comment or <code>null</code>, if there are no comments after the offset.
		 */
		private IASTComment findComment(int startOffset) {
			IASTComment[] comments = ((ASTTranslationUnit) root).getComments();
			int low = 0;
			int high = comments.length;
			while (low < high) {
				int mid = (low + high) / 2;
				int offset = comments[mid].getFileLocation().getNodeOffset();
				if (offset < startOffset) {
					low = mid + 1;
				} else {
					high = mid;
					if (offset == startOffset) {
						break;
					}
				}
			}
			return high < comments.length ? comments[high] : null;
		}

		/**
		 * Adds all occurrences of a name in a comment to the list of locations.
		 */
		private void findInComment(char[] name, IASTComment comment) {
			char[] text = comment.getComment();
	    	int j = 0;
	    	// First two characters are either /* or //
	    	for (int i = 2; i <= text.length - name.length + j; i++) {
	    		char c = text[i];
				if (!Character.isJavaIdentifierPart(c)) {
					j = 0;
				} else if (j >= 0 && j < name.length && name[j] == c) {
					j++;
					if (j == name.length &&
							(i + 1 == text.length || !Character.isJavaIdentifierPart(text[i + 1]))) {
						int offset = comment.getFileLocation().getNodeOffset() + i + 1 - name.length;
						locations.add(new Region(offset, name.length));
						j = 0;
					}
				} else {
					j = -1;
				}
	    	}
		}
	}

	/**
	 * Finds subclasses of the given class referenced by the AST.
	 */
	static class SubclassFinder extends ASTVisitor {
		{
			shouldVisitNames= true;
		}

		private final ICPPClassType baseClass;
		private Set<ICPPClassType> subclasses = new HashSet<ICPPClassType>();
		private Set<IBinding> seenClasses = new HashSet<IBinding>();
		
		SubclassFinder(ICPPClassType baseClass) {
			this.baseClass = baseClass;
		}

		@Override
		public int visit(IASTName name) {
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPClassType) {
				if (seenClasses.add(binding)) {
					ICPPClassType candidate = (ICPPClassType) binding;
					if (ClassTypeHelper.isSubclass(candidate, baseClass)) {
						subclasses.add(candidate);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		public ICPPClassType[] getSubclasses() {
			return subclasses.toArray(new ICPPClassType[subclasses.size()]);
		}
	}
}
