/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.IndexToASTNameHelper;
import org.eclipse.cdt.internal.ui.refactoring.rename.ASTManager;

/**
 * Contains static helper methods concerning Pull-up and Push-down refactorings.
 * 
 * @author Simon Taddiken
 */
public final class PullUpHelper {

	private PullUpHelper() {}
	
	
	
	public static boolean bindingsEqual(IIndex index, IBinding b1, IBinding b2) {
		// HACK: use of ASTManager from RenameRefactoring
		try {
			return ASTManager.isSameBinding(index, b1, b2) == ASTManager.TRUE;
		} catch (DOMException e) {
			CUIPlugin.log(e);
			return false;
		}
	}
	
	
	
	/**
	 * Collects all template declarations from the <tt>oldFunction</tt>, copies them and
	 * adds them back in order to <tt>newFunction</tt>
	 * @param oldFunction Function from which template declarations are to be collected.
	 * @param newFunction Function to which template declarations are to be added.
	 * @return New combined declaration
	 */
	public static IASTDeclaration getTemplateDeclaration(
			IASTDeclaration oldFunction, IASTDeclaration newFunction) {
		// HACK: copy of methods in ToggleNodeHelper modified so they can operate
		//		 on all kinds of declarations
		final List<ICPPASTTemplateDeclaration> templateDeclarations = 
				getAllTemplateDeclaration(oldFunction);
		if (templateDeclarations.isEmpty()) {
			// no templates, just return the plain copy
			return newFunction;
		}
		// prepend template declarations to copy
		return addTemplateDeclarationsInOrder(templateDeclarations, newFunction);
	}

	
	
	/**
	 * Creates a template declaration from the provided list of template declarations
	 * and the provided function declaration
	 * @param templDecs List of template declarations
	 * @param newFunction A function declaration.
	 * @return Compound declaration of the provided templates and function declaration.
	 * 			<code>null</code> if list of templates is empty.
	 */
	private static ICPPASTTemplateDeclaration addTemplateDeclarationsInOrder(
			List<ICPPASTTemplateDeclaration> templDecs, IASTDeclaration newFunction) {
		ListIterator<ICPPASTTemplateDeclaration> iter1 = templDecs.listIterator();
		ICPPASTTemplateDeclaration child = null;
		while (iter1.hasNext()) {
			child = iter1.next();
			child.setDeclaration(newFunction);
			ListIterator<ICPPASTTemplateDeclaration> iter2 = iter1;
			if (iter2.hasNext()) {
				ICPPASTTemplateDeclaration parent = iter2.next();
				child.setParent(parent);
				parent.setDeclaration(child);
				child = parent;
			}
		}
		return child;
	}
	
	

	/**
	 * Collects all template declarations which are direct parents of the provided node.
	 * @param node The node to start at.
	 * @return List of template declarations if the provided node has any.
	 */
	private static List<ICPPASTTemplateDeclaration> getAllTemplateDeclaration(
			IASTNode node) {
		final List<ICPPASTTemplateDeclaration> templdecs = 
				new ArrayList<ICPPASTTemplateDeclaration>();
		while (node.getParent() != null) {
			node = node.getParent();
			if (node instanceof ICPPASTTemplateDeclaration) {
				templdecs.add((ICPPASTTemplateDeclaration) node.copy(CopyStyle.withLocations));
			}
		}
		return templdecs;
	}
	
	
	
	/**
	 * For a given node, this method iterates the parents until there is no parent which
	 * is no {@link ICPPASTTemplateDeclaration}.
	 * 
	 * @param node The node to start at.
	 * @return A node which, when is being removed, serves for removing a whole 
	 * 			declaration.
	 */
	public static IASTNode findRemovePoint(IASTNode node) {
		IASTNode current = node;
		while (current.getParent() != null && 
				current.getParent() instanceof ICPPASTTemplateDeclaration) {
			current = current.getParent();
		}
		return current;
	}
	
	
	
	/**
	 * Determines whether the provided binding refers to a constructor.
	 * @param member The member to check.
	 * @return Whether the provided member is a constructor
	 */
	public static boolean isConstructor(ICPPMember member) {
		if (member instanceof ICPPMethod) {
			ICPPMethod method = (ICPPMethod) member;
			return method.getName().equals(method.getClassOwner().getName());
		}
		return false;
	}
	
	
	
	/**
	 * Determines whether the provided binding refers to a destructor.
	 * @param member The member to check.
	 * @return Whether the provided member is a destructor
	 */
	public static boolean isDestructor(ICPPMember member) {
		if (member instanceof ICPPMethod) {
			ICPPMethod method = (ICPPMethod) member;
			return method.getName().equals("~" + member.getClassOwner().getName()); //$NON-NLS-1$
		}
		return false;
	}
	
	
	
	/**
	 * Determines whether the given class contains a declaration with the same name and 
	 * parameters as the given method.
	 * 
	 * @param cls The class to search in.
	 * @param mtd The method to search for.
	 * @return Whether the class contains a method with the same signature.
	 */
	public static boolean checkContains(ICPPClassType cls, ICPPMethod mtd) {
		final ICPPFunctionType type = mtd.getType();
		for (final ICPPMethod member : cls.getDeclaredMethods()) {
			if (member.getName().equals(mtd.getName()) && 
					functionTypesAllowOverride(type, member.getType())) {
				return true;
			}
		}
		return false;
	}
	
	

	/**
	 * Checks if the function types are consistent enough to be considered overrides.
	 */
	private static boolean functionTypesAllowOverride(ICPPFunctionType a, ICPPFunctionType b) {
		// HACK: this is a local copy of ClassTypeHelper#functionTypesAllowOverride
		if (a.isConst() != b.isConst() || a.isVolatile() != b.isVolatile()
				|| a.takesVarArgs() != b.takesVarArgs()) {
			return false;
		}

		final IType[] paramsA = a.getParameterTypes();
		final IType[] paramsB = b.getParameterTypes();

		if (paramsA.length == 1 && paramsB.length == 0) {
			if (!SemanticUtil.isVoidType(paramsA[0]))
				return false;
		} else if (paramsB.length == 1 && paramsA.length == 0) {
			if (!SemanticUtil.isVoidType(paramsB[0]))
				return false;
		} else if (paramsA.length != paramsB.length) {
			return false;
		} else {
			for (int i = 0; i < paramsA.length; i++) {
				if (paramsA[i] == null || !paramsA[i].isSameType(paramsB[i]))
					return false;
			}
		}
		return true;
	}

	
	
	/**
	 * Finds the class definition for the specified binding.
	 * @param index Reference to the index
	 * @param context Current refactoring context.
	 * @param binding Binding to find the definition for.
	 * @return The definition of the class or <code>null</code> if it was not found
	 * 			or not unique.
	 */
	public static IASTCompositeTypeSpecifier findClass(IIndex index, CRefactoringContext context, 
			IBinding binding) {
		try {
			final IASTName targetName = findName(index, context, binding, 
					IIndex.FIND_DEFINITIONS);
			if (targetName == null) {
				return null;
			}
			return CPPVisitor.findAncestorWithType(targetName, IASTCompositeTypeSpecifier.class);
		} catch (OperationCanceledException e) {
			CUIPlugin.log(e);
		}
		return null;
	}
	
	
	
	/**
	 * Finds a unique name for the provided binding. If the index returns more than one
	 * result, <code>null</code> is returned.
	 * @param index Reference to the index
	 * @param context Current refactoring context
	 * @param binding Binding to resolve the name for.
	 * @param flags Flags for {@link IIndex#findNames(IBinding, int)}
	 * @return The unique name or <code>null</code> if name does not exist or is not 
	 * 			unique
	 */
	public static IASTName findName(IIndex index, CRefactoringContext context,
			IBinding binding, int flags) {
		try {
			final IIndexName[] names = index.findNames(binding, flags);
			if (names.length > 1) {
				return null;
			}
			for (IIndexName iname : names) {
				final IASTTranslationUnit selectionAST = getASTForIndexName(iname, context);
				final IASTName astName = IndexToASTNameHelper.findMatchingASTName(
						selectionAST, iname, index);

				return astName;
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return null;
	}
	
	
	
	public static IASTFunctionDeclarator findFunctionDeclarator(IIndex index,
			CRefactoringContext context, IBinding binding) {
		final IASTName name = findName(index, context, binding, IIndex.FIND_DECLARATIONS);
		if (name == null) {
			return null;
		}
		return CPPVisitor.findAncestorWithType(name, IASTFunctionDeclarator.class);
	}
	
	
	
	public static IASTFunctionDefinition findFunctionDefinition(IIndex index,
			CRefactoringContext context, IBinding binding) {
		final IASTName name = findName(index, context, binding, IIndex.FIND_DEFINITIONS);
		if (name == null) {
			return null;
		}
		return CPPVisitor.findAncestorWithType(name, IASTFunctionDefinition.class);
	}

	
	
	public static Collection<IASTName> findReferences(IIndex index, IBinding binding, 
			CRefactoringContext context) {
		try {
			final IIndexName[] references = index.findReferences(binding);
			final Collection<IASTName> result = new ArrayList<IASTName>(references.length);
			
			for (final IIndexName reference : references) {
				final IASTTranslationUnit ast = getASTForIndexName(reference, context);
				final IASTName astName = IndexToASTNameHelper.findMatchingASTName(
						ast, reference, index);
			
				if (astName != null) {
					result.add(astName);
				}
			}
			return result;
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return Collections.emptyList();
		}
	}
	
	
	
	/**
	 * Determines whether <tt>check</tt> is a parent of <tt>child</tt>.
	 * @param check The node to check whether it is a parent.
	 * @param child The node considered as a child
	 * @return Whether check is found to be a parent of child.
	 */
	public static boolean isParent(IASTNode check, IASTNode child) {
		IASTNode current = child;
		while (current != null) {
			current = current.getParent();
			if (current == check) {
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
	 * Iterates all names within the provided root node and checks whether their 
	 * binding equals the provided one. Iterating stops after first positive match.
	 * @param index Reference to the index.
	 * @param root Root node to start the search at.
	 * @param binding Binding to search for.
	 * @return Whether the node contains at least one name which binding resolves to the
	 * 			provided one.
	 */
	public static boolean checkContainsBinding(final IIndex index, IASTNode root, 
			final IBinding binding) {
		final Container<Boolean> result = new Container<Boolean>(Boolean.FALSE);
		root.accept(new ASTVisitor() {
			{
				this.shouldVisitNames = true;
			}
			
			@Override
			public int visit(IASTName name) {
				final IBinding bind = name.resolveBinding();
				if (bindingsEqual(index, bind, binding)) {
					result.setObject(Boolean.TRUE);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
		return result.getObject();
	}
	
	
	
	/**
	 * Gets an {@link IASTTranslationUnit} instance for a provided index name.
	 * @param indexName The index name
	 * @param context The current refactoring context.
	 * @return The AST for that name.
	 * @throws CModelException
	 * @throws CoreException
	 */
	public static IASTTranslationUnit getASTForIndexName(IIndexName indexName, 
			CRefactoringContext context) throws CModelException, CoreException {
		final ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(
				indexName.getFile().getLocation(), null);
		if (tu == null) {
			return null;
		}
		return context.getAST(tu, null);
	}
	
	

	/**
	 * Collects all types which are referenced within the provided function definition.
	 * @param def The definition to search.
	 * @return Collection of referenced types
	 */
	public static Collection<IASTName> findReferencedTypes(IASTFunctionDefinition def) {
		final Set<IASTName> result = new HashSet<IASTName>();
		def.accept(new ASTVisitor() {
			{
				this.shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.getRoleOfName(true) != IASTNameOwner.r_reference) {
					return PROCESS_CONTINUE;
				}
				final IBinding binding = name.resolveBinding();
				if (binding instanceof IType) {
					result.add(name);
				}
				return PROCESS_CONTINUE;
			}
		});
		return result;
	}

	
	
	public static boolean isVisible(IASTName name, IASTTranslationUnit ast) {
		IScope parent = ast.getScope();
		try {
			while (parent.getParent() != null) {
				parent = parent.getParent();
			}
		} catch (DOMException e) {
			e.printStackTrace();
		}
		final IBinding bind = parent.getBinding(name, true);
		return bind != null;
	}
	
	
	
	/**
	 * Creates a full signature string representation of the provided member.
	 * 
	 * @param member The member
	 * @return String representation of the member.
	 */
	public static String getMemberString(ICPPMember member) {
		if (member instanceof ICPPMethod) {
			final ICPPMethod mtd = (ICPPMethod) member;
			final StringBuilder b = new StringBuilder();
			
			b.append(mtd.getName());
			b.append("("); //$NON-NLS-1$
			for (int i = 0; i < mtd.getParameters().length; ++i) {
				b.append(mtd.getParameters()[i].getType());
				if (i < mtd.getParameters().length - 1) {
					b.append(", "); //$NON-NLS-1$
				}
			}
			b.append(") : "); //$NON-NLS-1$
			b.append(mtd.getType().getReturnType());
			
			return b.toString();
		} else if (member instanceof ICPPField) {
			final ICPPField field = (ICPPField) member;
			return field.getName() + " : " + field.getType().toString(); //$NON-NLS-1$
		}
		
		// should not be reachable
		return member.getName();
	}
	
	
	
	/**
	 * Recreates a name for being inserted within a target class.
	 * 
	 * @param current The name to rewrite.
	 * @param target Target class of where the name is being inserted.
	 * @param insertion The insertion point within the target.
	 * @return A new qualified name.
	 */
	public static IASTName prepareNameForTarget(IASTName current, 
			IASTCompositeTypeSpecifier target, InsertionPoint insertion) {
		
		final ICPPNodeFactory nf = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		
		if (current instanceof ICPPASTQualifiedName) {
			current = current.getLastName();
		}
		
		current = current.copy();
		if (insertion.isWithinClassDefinition()) {
			// use unqualified name in class definition
			return current;
		} else {
			// create qualified name for target class
			final Stack<IASTName> names = new Stack<IASTName>();
			IASTNode node = target;
			while (node != null) {
				if (node instanceof IASTCompositeTypeSpecifier) {
					names.push(((IASTCompositeTypeSpecifier) node).getName());
				} else if (node instanceof ICPPASTNamespaceDefinition) {
					names.push(((ICPPASTNamespaceDefinition) node).getName());
				}
				// TODO: handle templates? See ToggleNodeHelper
				node = node.getParent();
			}
			
			final ICPPASTQualifiedName qName = nf.newQualifiedName();
			while (!names.isEmpty()) {
				qName.addName(names.pop().copy());
			}
			qName.addName(current);
			return qName;
		}
	}
	
	
	
	public static IASTNode getParentNamespace(IASTNode node) {
		IASTNode current = node;
		while (current != null) {
			if (current instanceof ICPPNamespace) {
				return current;
			}
			current = current.getParent();
		}
		return node.getTranslationUnit();
	}
	
	
	
	
	
	/**
	 * Checks whether a member binding is referenced within a certain class. Can be used 
	 * to check whether a member is being used in any subclass.
	 * @param index Reference to the index.
	 * @param context Current refactoring context. 
	 * @param member The member to check.
	 * @param ignoreList Collection of members which definitions are to be ignored when
	 * 			searching for references of the member in question. 
	 * @param target The class which is to be searched for references for the member in
	 * 			question.
	 * @return Whether there exists any definition within the target class which contains
	 * 			a reference to the provided member
	 */
	public static boolean isReferencedIn(IIndex index, CRefactoringContext context, 
			ICPPMember member, 
			Collection<ICPPMember> ignoreList,
			ICPPClassType target) {

		outer: for (final ICPPMethod mtd : target.getDeclaredMethods()) {
			// check whether declaration is to be ignored
			for (final ICPPMember ignore : ignoreList) {
				if (bindingsEqual(index, ignore, mtd)) {
					continue outer;
				}
			}
			final IASTFunctionDefinition definition = findFunctionDefinition(
					index, context, mtd);
			if (definition != null) {
				if (checkContainsBinding(index, definition, member)) {
					return true;
				}
			}
		}

		return false;
	}
	
	
	
	/**
	 * Finds all members of a class which definitions are referencing the provided member
	 * @param index Reference to the index.
	 * @param context Current refactoring context.
	 * @param member The member to check.
	 * @return A collection of all members from the same class which reference the given 
	 * 			one.
	 */
	public static Collection<ICPPMember> findReferencingMembers(IIndex index,
			CRefactoringContext context, ICPPMember member) {
		
		final ICPPClassType owner = member.getClassOwner();
		final List<ICPPMember> result = new ArrayList<ICPPMember>();
		for (final ICPPMethod mtd : owner.getDeclaredMethods()) {
			/*// check whether declaration is to be ignored
			for (final ICPPMember ignore : ignoreList) {
				if (bindingsEqual(index, ignore, mtd)) {
					continue outer;
				}
			}*/
			final IASTFunctionDefinition definition = findFunctionDefinition(
					index, context, mtd);
			if (definition != null) {
				if (checkContainsBinding(index, definition, member)) {
					result.add(mtd);
				}
			}
		}

		return result;
	}
	
	
	
	public static void findReferencedMembers(
			final IIndex index,
			final CRefactoringContext context,
			final ICPPClassType owner, 
			final ICPPMember member,
			Collection<ICPPMember> result) {
		
		final int flags = member instanceof ICPPField 
				? IIndex.FIND_DEFINITIONS
				: IIndex.FIND_DEFINITIONS;
		final IASTName defName = PullUpHelper.findName(index, context, member, flags);
		
		if (defName == null) {
			// no definition => no dependencies
			return;
		}
		
		if (member instanceof ICPPField) {
			final IASTDeclarator decl = (IASTDeclarator) defName.getParent();
			if (decl.getInitializer() == null) {
				return;
			}
			calculateDependenciesBody(index, context, owner, result, decl.getInitializer());
		} else if (member instanceof ICPPMethod) {
			final IASTFunctionDefinition def = CPPVisitor.findAncestorWithType(defName, 
					IASTFunctionDefinition.class);
			calculateDependenciesBody(index, context, owner, result, def.getBody());
		}
	}
	
	
	
	private static void calculateDependenciesBody(
			final IIndex index,
			final CRefactoringContext context,
			final ICPPClassType owner,
			final Collection<ICPPMember> result, IASTNode body) {
		
		body.accept(new ASTVisitor() {
			{
				this.shouldVisitNames = true;
			}
			@Override
			public int visit(IASTName name) {
				// name must be a reference to a member of the class #source
				if (!name.isReference()) {
					return PROCESS_CONTINUE;
				}
				final IBinding binding = name.resolveBinding();
				if (!(binding instanceof ICPPMember)) {
					return PROCESS_CONTINUE;
				}
				final ICPPMember member = (ICPPMember) binding;
				if (!bindingsEqual(index, member.getClassOwner(), owner)) {
					// this is a member of another class
					return PROCESS_CONTINUE;
				}
				
				result.add(member);
				findReferencedMembers(index, context, owner, member, result);
				return PROCESS_CONTINUE;
			}
		});
	}
	
	
	
	/**
	 * Determines whether the provided class is abstract by comparing the count of pure 
	 * abstract methods against all declared methods.
	 * @param cls The class to check.
	 * @return Whether it only contains pure virtual methods.
	 */
	public static boolean isPureAbstract(ICPPClassType cls) {
		final ICPPMethod[] allMethods = cls.getAllDeclaredMethods();
		final ICPPMethod[] pureVirtual = SemanticQueries.getPureVirtualMethods(cls, null);
		return allMethods.length != 0 && allMethods.length == pureVirtual.length;
	}
	
	
	
	/**
	 * Finds all sub classes to a given {@link ICPPClassType}.
	 * @param context Current refactoring context.
	 * @param cls Class for which derived classes should be found.
	 * @param ignoreAbstract Whether abstract classes are ignored. That is, they will not
	 * 			occur in the result.
	 * @param root InheritanceLevel instance which children will be filled 
	 * @return List of derived classes.
	 * @throws CoreException
	 */
	public static List<InheritanceLevel> findSubClasses(CRefactoringContext context, 
			ICPPClassType cls, boolean ignoreAbstract, InheritanceLevel root) 
					throws CoreException {
		final ArrayList<InheritanceLevel> result = new ArrayList<InheritanceLevel>();
		final Set<String> handled = new HashSet<String>();
		final IIndex index = context.getIndex();
		iterateInheritanceTree(index, handled, root, cls, result, 0, ignoreAbstract);
		return result;
	}
	
	
	
	private static void iterateInheritanceTree(IIndex index, Set<String> handled, 
			InheritanceLevel predecessor, ICPPClassType current, 
			List<InheritanceLevel> result, int level, boolean ignoreAbstract) 
					throws OperationCanceledException, CoreException {
		
		final String key = ASTTypeUtil.getType(current, true);
		if (!handled.add(key)) {
			return;
		}
		
		// find all references of the current class
		final IIndexName[] names = index.findNames(current, 
				IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
		for (final IIndexName name : names) {
			
			// check whether any reference is a BaseSpecifier, if so we found a subclass
			if (name.isBaseSpecifier()) {
				final IIndexName subClassDef = name.getEnclosingDefinition();
				if (subClassDef != null) {
					final IBinding subClass = index.findBinding(subClassDef);
					if (subClass instanceof ICPPClassType) {
						final ICPPClassType clazz = (ICPPClassType) subClass;
						
						if (ignoreAbstract && isPureAbstract(clazz)) {
							// all methods are declared pure virtual, so continue with
							// next class
							continue;
						}
						
						final InheritanceLevel lvl = new InheritanceLevel(predecessor, 
								clazz, null, level + 1);
						
						result.add(lvl);
						iterateInheritanceTree(index, handled, lvl, clazz, result, 
								level + 1, ignoreAbstract);
						if (predecessor != null) {
							predecessor.addChild(lvl);
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * Returns the AST for the partner file of the provided translation unit. If the
	 * provided unit has no partner, <code>null</code> is returned.
	 * @param unit The unit for which to find the partner
	 * @param context Current refactoring context.
	 * @return AST of the partner file
	 */
	public static IASTTranslationUnit getASTForPartnerFile(IASTTranslationUnit unit, 
			CRefactoringContext context) {
		final ITranslationUnit tu;
		try {
			tu = SourceHeaderPartnerFinder.getPartnerTranslationUnit(
					unit.getOriginatingTranslationUnit(), context);
			
			if (tu == null) {
				return null;
			}
			return context.getAST(tu, null);
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return null;
		}
	}
}
