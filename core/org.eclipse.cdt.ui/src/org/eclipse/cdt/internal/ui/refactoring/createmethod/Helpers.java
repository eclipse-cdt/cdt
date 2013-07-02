/*******************************************************************************
 * Copyright (c) 2013 - Xdin AB
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Erik Johansson
 ******************************************************************************/


package org.eclipse.cdt.internal.ui.refactoring.createmethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.DeclarationGeneratorImpl;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class Helpers {
	
	
	/**
	 * Creates an IASTFunctionDefinition that matches a given IASTFunctionCallExpression.
	 * 
	 * @param factory An INodeFactory.
	 * @param functionCall The ICASTFunctionCallExpression.
	 * @param functionCallTU The IASTTranslationUnit where the function call is made.
	 * 
	 * @return The IASTFunctionDefinition.
	 */
	public static IASTFunctionDefinition createFunctionDefinitionFromFunctionCall(
			INodeFactory factory, IASTFunctionCallExpression functionCall, IASTTranslationUnit functionCallTU) {
		
		DeclarationGeneratorImpl generator = new DeclarationGeneratorImpl(factory);
		IType returnType = Helpers.getFunctionReturnType(functionCall);
		IASTStandardFunctionDeclarator declarator = Helpers.createCompleteFunctionDeclarator(factory, functionCall, returnType);
		IASTDeclSpecifier declSpecifier = generator.createDeclSpecFromType(returnType);
		if (declSpecifier == null) {
			IASTSimpleDeclSpecifier decl = factory.newSimpleDeclSpecifier();
			decl.setType(Kind.eVoid);
			declSpecifier = decl;
		}
		IASTStatement body = Helpers.getBodyStatement(factory, returnType);
		IASTFunctionDefinition def =  factory.newFunctionDefinition(declSpecifier, declarator, body);
		return def;
	}
	
	private static IASTStatement getBodyStatement(INodeFactory factory, IType returnType) {
		IASTCompoundStatement body = factory.newCompoundStatement();
		IASTExpression retValue = getDefaultInstanceForType(returnType, factory);
		if (retValue != null)
			body.addStatement(factory.newReturnStatement(retValue));
		return body;
	}
	
	/**
	 * Tries to determine the default value for a given type and return the corresponding IASTExpression.
	 * 
	 * 
	 * @param type
	 * @param factory
	 * @return The default value or null if it couldn't be determined.
	 */
	
	public static IASTExpression getDefaultInstanceForType(IType type, INodeFactory factory) {
		IASTExpression expr = null;
		if (type instanceof IBasicType || type instanceof IPointerType) {
			expr = factory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "0"); //$NON-NLS-1$
		}
		else if (type instanceof ICPPClassType) {
			// seems to catch both simple classes, template instances and structs
			ICPPClassType comp = (ICPPClassType) type;
			ICPPMethod[] methods = comp.getAllDeclaredMethods();
			ICPPMethod bestMethod = null;
			for (ICPPMethod current : methods) {
				if (current.getVisibility() == ICPPMethod.v_public) {
					//require public visibility
					if (current instanceof ICPPConstructor) {
						if (bestMethod == null ||
							!(bestMethod instanceof ICPPConstructor) ||
							current.getParameters().length < bestMethod.getParameters().length)
								bestMethod = current;
					}
					else {
						if (bestMethod == null ||
							(!(bestMethod instanceof ICPPConstructor) &&
							current.getParameters().length < bestMethod.getParameters().length))
								bestMethod = current;
					}
				}
			}
			
			if (bestMethod == null) {
				// use default ctor
				expr = factory.newFunctionCallExpression(
						factory.newIdExpression(factory.newName(comp.getName().toCharArray())),
						new IASTInitializerClause[0]);
			}
			else {
				// use ctor with fewest params 
				List<IASTInitializerClause> args = new ArrayList<IASTInitializerClause>();
				for (ICPPParameter param : bestMethod.getParameters()) {
					args.add(getDefaultInstanceForType(param.getType(), factory));
				}
				
				expr = factory.newFunctionCallExpression(
						factory.newIdExpression(factory.newName(bestMethod.getName().toCharArray())),
						args.toArray(new IASTInitializerClause[args.size()]));
			}
		}
		
		return expr;
	}
	
	public static String getParameterTypeName(IASTParameterDeclaration param) {
		
		IASTDeclSpecifier decl = param.getDeclSpecifier();
		if (decl instanceof IASTNamedTypeSpecifier) 
			return ((IASTNamedTypeSpecifier) decl).getName().toString();
		else {
			IBinding binding = param.getDeclarator().getName().resolveBinding();
			if (binding instanceof IVariable)
				return ASTTypeUtil.getType(((IVariable) binding).getType());
			return "?"; //$NON-NLS-1$
		}
	}
			

	private static String getMethodName(IASTFunctionCallExpression funcCall, String defaultName) {
		IASTExpression nameExpr = funcCall.getFunctionNameExpression();
		if (nameExpr instanceof CPPASTFieldReference)
			return ((CPPASTFieldReference) nameExpr).getFieldName().toString();
		else
			return defaultName;
	}
	
	private static IASTStandardFunctionDeclarator createCompleteFunctionDeclarator(INodeFactory factory,
			IASTFunctionCallExpression functionCall, IType returnType) {
		String methodName = Helpers.getMethodName(functionCall, Messages.Helpers_DefaultMethodName);
		IASTStandardFunctionDeclarator declarator = Helpers.createFunctionDeclarator(factory, returnType, methodName);
		Helpers.addParametersToFunctionDeclarator(factory, declarator, functionCall);
		return declarator;
	}
	
	private static IASTStandardFunctionDeclarator addParametersToFunctionDeclarator(INodeFactory factory,
			IASTStandardFunctionDeclarator declarator, IASTFunctionCallExpression functionCall) {
		
		int currentParam = 0;
		for (IASTInitializerClause clause : functionCall.getArguments()) {
			IASTParameterDeclaration parameter = Helpers.getParameterDeclarationFromInitializerClause(clause, factory, currentParam);
			if (parameter != null)
				declarator.addParameterDeclaration(parameter);
			currentParam++;
		}
		return declarator;
	}
	

	private static IASTStandardFunctionDeclarator createFunctionDeclarator(INodeFactory factory, IType type, String name) {
		IASTStandardFunctionDeclarator declarator = factory.newFunctionDeclarator(factory.newName(name.toCharArray()));
		Helpers.addPointersIfNeeded(factory, type, declarator);
		return declarator;
	}
	
	private static IASTDeclarator createDeclarator(INodeFactory factory, IType type, String name) {
		IASTDeclarator declarator = factory.newDeclarator(factory.newName(name.toCharArray()));
		Helpers.addPointersIfNeeded(factory, type, declarator);
		return declarator;
	}
	
	private static IASTDeclarator addPointersIfNeeded(INodeFactory factory, IType type, IASTDeclarator declarator) {
		
		if (declarator.getPointerOperators().length > 1)
			return declarator;
		if (type instanceof IArrayType) {
			declarator.addPointerOperator(factory.newPointer());
			while ((type = ((IArrayType) type).getType()) instanceof IArrayType)
				;
			return addPointersIfNeeded(factory, type, declarator);
		}
		else if (type instanceof IPointerType) {
			declarator.addPointerOperator(factory.newPointer());
			type = ((IPointerType) type).getType();
			while (type instanceof IArrayType)
				type = ((IArrayType) type).getType();
			return addPointersIfNeeded(factory, type, declarator);
		}
		else
			return declarator;
	}
	
	private static IASTParameterDeclaration getParameterDeclarationFromInitializerClause(
			IASTInitializerClause clause, INodeFactory factory, int paramNo) {
		
		String defaultName = NLS.bind(Messages.Helpers_DefaultParameterName, String.valueOf(paramNo));
		
		if (clause instanceof CPPASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression) clause;
			return Helpers.getParameterFromLiteralExpression(factory, literal, defaultName);
		}
		else if (clause instanceof IASTIdExpression) {
			IASTIdExpression idExpr = (IASTIdExpression) clause;
			return Helpers.getParameterFromIdExpression(factory, idExpr, defaultName);
		}
		else if (clause instanceof IASTFunctionCallExpression) {
			IASTFunctionCallExpression funcCallExpr = (IASTFunctionCallExpression) clause;
			return Helpers.getParameterFromFunctionCallExpression(factory, funcCallExpr, defaultName);
		}
		
		return null;
	}	
	
	private static IASTParameterDeclaration getParameterFromLiteralExpression(INodeFactory factory, IASTLiteralExpression literal, String name) {	
		DeclarationGeneratorImpl generator = new DeclarationGeneratorImpl(factory);
		final IType type = literal.getExpressionType();
		IASTDeclSpecifier declSpecifier = generator.createDeclSpecFromType(type);
		IASTDeclarator declarator = Helpers.createDeclarator(factory, type, name);
		IASTParameterDeclaration parameter = factory.newParameterDeclaration(declSpecifier, declarator);
		return parameter;
	}
	
	private static IASTParameterDeclaration getParameterFromIdExpression(
			INodeFactory factory, IASTIdExpression idExpr, String name) {
		
			IBinding binding = idExpr.getName().resolveBinding();
			if (binding instanceof ICPPVariable) {
				DeclarationGeneratorImpl generator = new DeclarationGeneratorImpl(factory);
				final IType type = ((ICPPVariable) binding).getType();
				IASTDeclSpecifier declSpecifier = generator.createDeclSpecFromType(type);
				IASTDeclarator declarator = Helpers.createDeclarator(factory, type, name);
				IASTParameterDeclaration parameter = factory.newParameterDeclaration(declSpecifier, declarator);
				return parameter;
			}
			return null;
	}
	
	private static IASTParameterDeclaration getParameterFromFunctionCallExpression(
			INodeFactory factory, IASTFunctionCallExpression funcCallExpr, String name) {
		
			DeclarationGeneratorImpl generator = new DeclarationGeneratorImpl(factory);
			final IType type = funcCallExpr.getExpressionType();
			IASTDeclSpecifier declSpecifier = generator.createDeclSpecFromType(type);
			IASTDeclarator declarator = Helpers.createDeclarator(factory, type, name);
			IASTParameterDeclaration parameter = factory.newParameterDeclaration(declSpecifier, declarator);
			return parameter;
	}
	
	private static IType getFunctionReturnType(IASTFunctionCallExpression functionCall) {
		
		IASTNode parent = functionCall.getParent();
		if (parent instanceof IASTEqualsInitializer) {
			if (parent.getParent() instanceof IASTDeclarator) {
				IASTDeclarator declarator = (IASTDeclarator) parent.getParent(); 
				IBinding binding = declarator.getName().resolveBinding();
				if (binding instanceof IVariable) {
					return ((IVariable) binding).getType();
				}
			}
		}
		else if (parent instanceof IASTBinaryExpression) {
			IASTBinaryExpression binaryExpr = (IASTBinaryExpression) parent;
			IASTExpression operandExpr = binaryExpr.getOperand1();
			if (operandExpr instanceof IASTIdExpression) {
				IASTIdExpression idExpr = (IASTIdExpression) operandExpr;
				IBinding binding = idExpr.getName().resolveBinding();
				if (binding instanceof IVariable) {
					return ((IVariable) binding).getType();
				}
			}
		}
		
		// Will probably be IProblemType, will result in void method in any case.
		return functionCall.getExpressionType();
	}
	
	
	/**
	 * Opens the file represented by fileName in an editor and scrolls to 
	 * the position given by offset. Does nothing if the editor cannot be opened.
	 * 
	 * @param fileName The name of the file to open.
	 * @param offset The offset in the file to scroll to.
	 */
	public static void openFileLocationInEditor(String fileName, int offset) {
		IFile fileToOpen = FileBuffers.getWorkspaceFileAtLocation(Path.fromOSString(fileName));
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().
		        getEditorRegistry().getDefaultEditor(fileToOpen.getName());
		try {
			page.openEditor(new FileEditorInput(fileToOpen), desc.getId());
			if (page.getActiveEditor() instanceof ITextEditor) {
				((ITextEditor) page.getActiveEditor()).selectAndReveal(offset, 0);
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	/**
	 * Copied from org.eclipse.cdt.internal.ui.refactoring.togglefunction.RefactoringJob
	 * 
	 * Executes a CRefactoring. Creates an undo-action and disposes of
	 * the created CRefactoringContext after use.
	 * 
	 * @param refactoring The CRefactoring to execute.
	 * @param monitor An IProgressMonitor
	 * 
	 * @return
	 */
	public static IStatus executeRefactoring(CRefactoring refactoring, IProgressMonitor monitor) {
		CRefactoringContext context = new CRefactoringContext(refactoring);
		IUndoManager undoManager = RefactoringCore.getUndoManager();
		Change change = new NullChange();
		Change undoChange = new NullChange();
		boolean success = false;
		try {
			RefactoringStatus status = refactoring.checkAllConditions(monitor);
			if (status.hasFatalError())
				return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, status.getMessageMatchingSeverity(RefactoringStatus.ERROR));
			change = refactoring.createChange(monitor);
			change.initializeValidationData(monitor);
			if (!change.isValid(monitor).isOK()) {
				return Status.CANCEL_STATUS;
			}
			undoManager.aboutToPerformChange(change);
			undoChange = change.perform(monitor);
			success = true;
		} catch (IllegalStateException e) {
			CUIPlugin.log("Another refactoring is still in progress, aborting.", e); //$NON-NLS-1$
			} catch (CoreException e) {
			CUIPlugin.log("Failure during generation of changes.", e); //$NON-NLS-1$
		} finally {
			context.dispose();
			undoChange.initializeValidationData(monitor);
			undoManager.changePerformed(change, success);
			try {
				if (success && undoChange.isValid(monitor).isOK()) {
					// Note: addUndo MUST be called AFTER changePerformed or
					// the change won't be unlocked correctly. (17.11.2010)
					undoManager.addUndo("", undoChange); //$NON-NLS-1$
				}
			} catch (OperationCanceledException e) {
			} catch (CoreException e) {
			}
		}

		return Status.OK_STATUS;
	}
	
	/**
	 * 
	 * Get the class with name given by targetClassQualifiedName in translation unit ast.
	 * 
	 * @param ast The translation unit in which to search for the class.
	 * @param targetClassQualifiedName String[] with namespace names and class name as the last element.
	 * @return The ICPPASTCompositeTypeSpecifier representing the class, or null.
	 */
	
	public static ICPPASTCompositeTypeSpecifier getClass(IASTTranslationUnit ast, final String[] targetClassQualifiedName) {
		final List<ICPPASTCompositeTypeSpecifier> targetClassList = new ArrayList<ICPPASTCompositeTypeSpecifier>();
		final List<String> currentNamespace = new ArrayList<String>();
		currentNamespace.toArray();
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
				shouldVisitNamespaces = true;
			}
			
			@Override
			public int visit(ICPPASTNamespaceDefinition ns) {
				currentNamespace.add(ns.getName().toString());
				return ASTVisitor.PROCESS_CONTINUE;
			}
			
			@Override
			public int leave(ICPPASTNamespaceDefinition ns) {
				int ind = currentNamespace.indexOf(ns.getName().toString());
				if (ind != -1) {
					while (ind < currentNamespace.size())
						currentNamespace.remove(ind);
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
			
			@Override
			public int visit(IASTDeclSpecifier declSpecifier) {
				if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) declSpecifier;
					String[] ns = (String[]) currentNamespace.toArray(new String[currentNamespace.size()]);
					// check if this class is in the correct namespace and has the correct name
					if (ns.length == targetClassQualifiedName.length - 1) {
						for (int j = 0; j < ns.length; j++) {
							if (!(ns[j].equals(targetClassQualifiedName[j])))
								return ASTVisitor.PROCESS_CONTINUE; //not correct
						}
						if (compType.getName().toString().equals(targetClassQualifiedName[targetClassQualifiedName.length - 1])) {
							targetClassList.add(compType);
							return ASTVisitor.PROCESS_ABORT; //this must be the one
						}
					}
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		});
		
	if (targetClassList.size() > 0) {
		//found the class
		return targetClassList.get(0);
	}
	return null;
	}
	
	/**
	 * Copied from Copied from org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker
	 * 
	 * @param marker
	 * @param index
	 * @return
	 */
	public static String getProblemArgument(IMarker marker, int index) {
		String[] args = getProblemArguments(marker);
		return args[index];
	}
	
	/**
	 * Copied from org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker
	 * 
	 * @param marker
	 * @return
	 */
	public static String[] getProblemArguments(IMarker marker) {
		String attrs = marker.getAttribute("args", "");  //$NON-NLS-1$//$NON-NLS-2$
		Properties prop = new Properties();
		ByteArrayInputStream bin = new ByteArrayInputStream(attrs.getBytes());
		try {
			prop.load(bin);
		} catch (IOException e) {
			// not happening
		}
		String len = prop.getProperty("len", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		int length = Integer.valueOf(len);
		String args[] = new String[length];
		for (int i = 0; i < length; i++) {
			args[i] = prop.getProperty("a" + i); //$NON-NLS-1$
		}
		return args;
	}
	
}