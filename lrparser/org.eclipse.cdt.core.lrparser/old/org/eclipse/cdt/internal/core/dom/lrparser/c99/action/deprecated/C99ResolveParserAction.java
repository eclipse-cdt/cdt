/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.action.deprecated;

import static org.eclipse.cdt.core.parser.util.CollectionUtils.reverseIterable;
import static org.eclipse.cdt.internal.core.dom.lrparser.symboltable.CNamespace.GOTO_LABEL;
import static org.eclipse.cdt.internal.core.dom.lrparser.symboltable.CNamespace.IDENTIFIER;
import static org.eclipse.cdt.internal.core.dom.lrparser.symboltable.CNamespace.STRUCT_TAG;

import java.util.LinkedList;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99ArrayType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99BasicType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Enumeration;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Enumerator;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Field;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Function;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99FunctionScope;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99FunctionType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Label;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Parameter;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99PointerType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99ProblemBinding;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Scope;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Structure;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Typedef;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Variable;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.IC99Binding;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.IC99Scope;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.ITypeable;
import org.eclipse.cdt.internal.core.dom.lrparser.symboltable.C99SymbolTable;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
/**
 * This class was an attempt at doing full binding resolution during the parse
 * as opposed to doing it after the parse as is normally done with the DOM parser.
 * 
 * 
 * TODO: token mapping so that this will work with UPC
 * TODO: what about function definitions, don't they count as declarations?
 * 
 * Try to resolve bindings without using the ASTStack, that way I can resolve bindings
 * without generating an AST. In the future I can remove this as a subclass of C99ParserAction.
 * 
 * TODO: if I'm calculating scopes then those scopes need to be linked to AST nodes
 * 
 * TODO: some language constructs are not handled yet: typeIds (casts), field designators
 * 
 * @author Mike Kucera
 * 
 * @deprecated Binding resolution is too hard, replacing with simpler C99TypedefTrackerParserAction
 */
@SuppressWarnings("restriction")
@Deprecated public class C99ResolveParserAction {

	private static final boolean DEBUG = true;
	private static final String NO_IDENT = ""; //$NON-NLS-1$
	
	  
	// provides limited access to the token stream
	private final ITokenStream parser;
	
	// The symbolTable currently in use 
	private C99SymbolTable symbolTable = C99SymbolTable.EMPTY_TABLE;
	
	// A stack that keeps track of scopes in the symbol table, used to "close" scopes and to undo the opening of scopes
	private final LinkedList<C99SymbolTable> symbolTableScopeStack = new LinkedList<C99SymbolTable>();
	
	// A stack that keeps track of scopes that are set on bindings
	private final LinkedList<IC99Scope> bindingScopeStack = new LinkedList<IC99Scope>();
	
	// keeps track of nested declarations
	private final LinkedList<DeclaratorFrame> declarationStack = new LinkedList<DeclaratorFrame>();
	
	// keeps track of expression types
	private final ScopedStack<IType> exprTypeStack = new ScopedStack<IType>();

	
	
	
	
	private TypeQualifiers typeQualifiers; // TODO: can this go in the declaration stack?

	private static class TypeQualifiers {
		boolean isConst, isRestrict, isVolatile;
	}
	
	
	// "For every action there is an equal and opposite reaction." - Newton's third law
	private final LinkedList<IUndoAction> undoStack = new LinkedList<IUndoAction>();
	
	
	private interface IUndoAction {
		void undo();
	}
	
	public void undo() {
		undoStack.removeLast().undo();
	}
	
	public void undo(int steps) {
		for(int i = 0; i < steps; i++) {
			undo();
		}
	}

	public IC99Scope getCurrentScope() {
		return bindingScopeStack.getLast();
	}
	
	
	public C99ResolveParserAction(ITokenStream parser) {
		this.parser = parser;
		bindingScopeStack.add(new C99Scope(EScopeKind.eGlobal)); // the global scope
		System.out.println();
	}
	

	private static IType rawType(IType type) {
		while(type instanceof ITypedef) {
			type = ((C99Typedef)type).getType();
		}
		return type;
	}

	/**
	 * Lexer feedback hack, used by the parser to identify typedefname tokens.
	 */
	public boolean isTypedef(String ident) {		
		boolean result = symbolTable.lookup(IDENTIFIER, ident) instanceof ITypedef;
		return result;
	}
	
	
	/**
	 * Methods used by tests, package local access.
	 */
	C99SymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	int undoStackSize() {
		return undoStack.size();
	}
	
	LinkedList<DeclaratorFrame> getDeclarationStack() {
		return declarationStack;
	}
	
	
	/**
	 * Called from the grammar file in places where a scope is created.
	 * 
	 * Scopes are created by compound statements, however special care
	 * must also be taken with for loops because they may contain
	 * declarations.
	 * 
	 * TODO: scope object now need to be handled explicitly
	 */
	public void openSymbolScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		symbolTableScopeStack.add(symbolTable);
		bindingScopeStack.add(new C99Scope(EScopeKind.eLocal));
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				
				bindingScopeStack.removeLast();
				symbolTable = symbolTableScopeStack.removeLast();
			}
		});
	}
	

	public IC99Scope closeSymbolScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final C99SymbolTable undoTable = symbolTable;
		symbolTable = symbolTableScopeStack.removeLast(); // close the scope

		final IC99Scope undoScope = bindingScopeStack.removeLast();
		if(!bindingScopeStack.isEmpty())
			undoScope.setParent(bindingScopeStack.getLast());
		
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				undoScope.setParent(null);
				bindingScopeStack.add(undoScope);
				
				symbolTableScopeStack.add(symbolTable);
				symbolTable = undoTable;
			}
		});
		
		return undoScope;
	}
	
	
	// TODO, this needs an undo action
	public void openPointerScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.openPointerModifierScope();
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				frame.closePointerModifierScope();
			}
		});
	}
	
	
	/**
	 * Called from the grammar before a declaration is about to be reduced.
	 */
	public void openDeclarationScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		declarationStack.add(new DeclaratorFrame());
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.removeLast();
			}
		});
	}
	
	
	public void closeDeclarationScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame undoFrame = declarationStack.removeLast();
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.add(undoFrame);
			}
		});
	}
	
	
	public void consumeFunctionDefinition() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
//		final IC99Scope undoScope = bindingScope;
//		
//		C99FunctionScope functionScope = new C99FunctionScope();
//		functionScope.setBodyScope(undoScope);
//		undoScope.setParent(functionScope);
//		bindingScope = bindingScopeStack.removeLast();
//		functionScope.setParent(bindingScope);
		
		
		final IC99Scope undoScope = bindingScopeStack.removeLast();
		
		C99FunctionScope functionScope = new C99FunctionScope();
		functionScope.setBodyScope(undoScope);
		undoScope.setParent(functionScope);
		functionScope.setParent(bindingScopeStack.getLast());
		
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		
		// the function binding needs to be available outside of the function's scope
		String functionName = frame.getDeclaratorName().toString();
		C99Function functionBinding = (C99Function) symbolTable.lookup(IDENTIFIER, functionName);
		functionBinding.setFunctionScope(functionScope);
		
		final C99SymbolTable undoTable = symbolTable;
		final C99SymbolTable outerTable = symbolTableScopeStack.removeLast();
		symbolTable = outerTable.insert(IDENTIFIER, functionName, functionBinding);
		
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				symbolTableScopeStack.add(outerTable);
				symbolTable = undoTable;
				declarationStack.add(frame);
				bindingScopeStack.add(undoScope);
			}
		});
	}


	public void consumeAbstractDeclaratorFunctionDeclarator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.setFunctionDeclarator(true);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				frame.setFunctionDeclarator(false);
			}
		});
	}


	public void consumeDirectDeclaratorFunctionDeclarator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.setFunctionDeclarator(true);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.setFunctionDeclarator(false);
			}
		});
	}


	public void consumeDeclSpecToken() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		IToken token = parser.getRightIToken();
		final int kind = token.getKind();
		
		// creates a DeclSpec if there isn't one already
		DeclaratorFrame frame = declarationStack.getLast();
		final DeclSpec declSpec = frame.getDeclSpec();
		declSpec.add(kind);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declSpec.remove(kind);
			}
		});
	}

	
	/**
	 * A labeled statement is creates an implicit declaration of the label identifier.
	 * 
	 * TODO: a label has function scope, meaning the same label cannot be declared twice
	 * in a function regardless of block scope.
	 * TODO: labels can be implicitly declared, that is a goto can exist above the label
	 */
	public IBinding consumeStatementLabeled() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		IToken token = parser.getLeftIToken();
		String ident = token.toString();
		
		IC99Binding labelBinding = new C99Label(ident);
		// TODO: strictly speaking the same label cannot be declared twice,
		// but we aren't checking that here
		final C99SymbolTable oldTable = symbolTable;
		symbolTable = symbolTable.insert(GOTO_LABEL, ident, labelBinding);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				symbolTable = oldTable;
			}
		});
		
		return labelBinding;
	}
	
	
	public IBinding consumeStatementGoto() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		List<IToken> ruleTokens = parser.getRuleTokens();
		assert ruleTokens.size() == 3 : "a goto statement must always consist of 3 tokens"; //$NON-NLS-1$
		String ident = ruleTokens.get(1).toString();
		
		final C99SymbolTable oldTable = symbolTable;
		
		IC99Binding labelBinding = symbolTable.lookup(GOTO_LABEL, ident);
		if(labelBinding == null) {
			labelBinding = new C99Label(ident);
			symbolTable = symbolTable.insert(GOTO_LABEL, ident, labelBinding);
		}

		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				symbolTable = oldTable;
			}
		});
		
		
		return labelBinding;
	}
	
	
	public IBinding consumeDeclarationSpecifiersTypedefName() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		// find the typedef token
		String typedefName = null;
		for(IToken token : parser.getRuleTokens()) {
			// the token kind will still be TK_identifier, but there can only be one
			if(token.getKind() == C99Parsersym.TK_identifier) {
				typedefName = token.toString();
				break;
			}
		}
		assert typedefName != null : "a typedef token must have been parsed for this action to fire"; //$NON-NLS-1$
		
		// we know that the binding is a typedef because the lexer feedback hack worked and got us here
		ITypedef binding = (ITypedef) symbolTable.lookup(IDENTIFIER, typedefName);
		// TODO: do I need to clone the typedef in case it is further modified like with const?
		DeclaratorFrame frame = declarationStack.getLast();
		final DeclSpec declSpec = frame.getDeclSpec();
		declSpec.setType(binding);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				declSpec.setType(null);
			}
		});
		
		return binding;
	}
	

	
	public void consumeDirectDeclaratorIdentifier() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		DebugUtil.printMethodTrace();
		frame.setDeclaratorName(parser.getRightIToken());
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				DebugUtil.printMethodTrace();
				frame.setDeclaratorName(null);
			}
		});
	}
	
	
	
	// TODO need to be careful, this is called in a lot of places in the grammar
	public void consumeDeclaratorWithPointer() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		final LinkedList<C99PointerType> scope = frame.closePointerModifierScope();
		final int scopeSize = scope.size();
		
		for(C99PointerType pt : reverseIterable(scope)) {
			frame.addTypeModifier(pt);
		}
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				for(int i = 0; i < scopeSize; i++) {
					frame.removeLastTypeModifier();
				}
				frame.openPointerModifierScope(scope);
			}
		});
	}
	
	
	
	public void consumeDirectDeclaratorBracketed() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		// Used to tell the difference between function prototype declarations and function pointer declarations
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.setDeclaratorBracketed(true);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.setDeclaratorBracketed(false);
			}
		});
	}
	
	
	public IBinding consumeDeclaratorComplete() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		
		IToken token       = frame.getDeclaratorName();
		IType type         = frame.getDeclaratorType();
		DeclSpec declSpec  = frame.getDeclSpec();
		boolean isFunction = frame.isFunctionDeclarator();
		List<IBinding> nestedDeclarators = frame.getNestedDeclarations();
		
		if(isFunction)
			type = createFunctionType(type, nestedDeclarators);
		
		String ident = (token == null) ? null : token.toString();
		
		// compute the binding
		IC99Binding binding;
		if(declSpec.isTypedef())
			binding = createTypedefBinding(ident, type);
		else if(isFunction && !frame.isDeclaratorBracketed())
			binding = createFunctionBinding(ident, (IFunctionType)type, declSpec, nestedDeclarators);
		else
			binding = createVariableBinding(ident, type, declSpec);
		
		binding.setScope(bindingScopeStack.getLast());
		
		// insert into symbol table
		final C99SymbolTable oldTable = symbolTable;
		if(ident != null)
			symbolTable = symbolTable.insert(IDENTIFIER, ident, binding);
		
		declarationStack.removeLast();
		declarationStack.add(new DeclaratorFrame(frame.getDeclSpec())); // reset the declarator
		exprTypeStack.push(type);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				exprTypeStack.pop();
				declarationStack.removeLast();
				declarationStack.add(frame);
				symbolTable = oldTable;
			}
		});
		
		return binding;
	}
	
	/**
	 * Just gets rid of the type that was on the type stack.
	 */
	public void consumeInitDeclarator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final IType type = exprTypeStack.pop();
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				exprTypeStack.push(type);
			}
		});
	}
	
	
	public IBinding consumeFunctionDefinitionHeader() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		DeclaratorFrame frame = declarationStack.getLast();
		DeclSpec declSpec = frame.getDeclSpec();
		String functionName = frame.getDeclaratorName().toString();

		final C99SymbolTable oldTable = symbolTable;
		
		// there may have been a function prototype, hence there may already be a binding for the function
		IC99Binding binding = symbolTable.lookup(IDENTIFIER, functionName);
		if(binding == null) {
			IType type = frame.getDeclaratorType();
			List<IBinding> nestedDeclarators = frame.getNestedDeclarations();
			
			IFunctionType functionType = createFunctionType(type, nestedDeclarators);
			binding = createFunctionBinding(functionName, functionType, declSpec, nestedDeclarators);
			
			// a scope has already been opened for the function body, use the outer scope
			IC99Scope topScope = bindingScopeStack.removeLast();
			binding.setScope(bindingScopeStack.getLast());
			bindingScopeStack.add(topScope);
			
			symbolTable = symbolTable.insert(IDENTIFIER, functionName, binding);
		}
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				symbolTable = oldTable;
			}
		});
		
		return binding;
	}
	
	
	public IBinding consumeDeclaratorCompleteParameter() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		IToken token = frame.getDeclaratorName();
		IType type   = frame.getDeclaratorType();
		DeclSpec declSpec = frame.getDeclSpec();
		boolean isFunction = frame.isFunctionDeclarator();
	
		// its a function pointer
		if(isFunction)
			type = createFunctionType(type, frame.getNestedDeclarations());
		
		String ident = (token == null) ? NO_IDENT : token.toString();
		
		IC99Binding parameterBinding = createParameterBinding(ident, type, declSpec);
		parameterBinding.setScope(bindingScopeStack.getLast());
		
		// check to see if there is already a parameter binding
		String functionName = declarationStack.getLast().getDeclaratorName().toString();
		C99Function function = (C99Function) symbolTable.lookup(IDENTIFIER, functionName);
		
		if(function != null) {
			// there is already a function binding for this function, that means there
			// is a function prototype and there is already a binding for this parameter
			int position = declarationStack.getLast().getNestedDeclarations().size();
			IParameter[] parameters = function.getParameters();
			if(parameters != null && position < parameters.length) {
				parameterBinding = (IC99Binding)parameters[position];
			}
		}

		// even if the binding is reused it still might be under a different name
		final C99SymbolTable oldTable = symbolTable;
		if(ident != null) {
			symbolTable = symbolTable.insert(IDENTIFIER, ident, parameterBinding);
		}
		
		declarationStack.getLast().addNestedDeclaration(parameterBinding);
		
		// parameter declarations can only have one declarator, so don't reset
		//declarationStack.add(new DeclaratorFrame()); // reset

		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				//declarationStack.removeLast();
				declarationStack.getLast().removeLastNestedDeclaration();
				declarationStack.add(frame);
				symbolTable = oldTable;
			}
		});
		
		return parameterBinding;
	}
	
	
	/**
	 * This is a special case for the rule:
	 *     parameter_declaration ::= declaration_specifiers
	 *     
     * In this case there is no declarator at all
     * 
     * TODO: creating bindings that have no identifier seems really dumb,
     * why does it need to be done? Why not just have a null binding or
     * for that matter don't even have a name node
     * 
	 */
	public IBinding consumeParameterDeclarationWithoutDeclarator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		DeclSpec declSpec = frame.getDeclSpec();
		C99Parameter param = createParameterBinding(null, declSpec.getType(), declSpec);
		declarationStack.getLast().addNestedDeclaration(param);

		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				declarationStack.getLast().removeLastNestedDeclaration();
				declarationStack.add(frame);
			}
		});
		
		return param;
	}
	
	
	public IBinding consumeDeclaratorCompleteField() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		IToken token = frame.getDeclaratorName();
		IType type   = frame.getDeclaratorType();
	
		// its a function pointer
		if(frame.isFunctionDeclarator())
			type = createFunctionType(type, frame.getNestedDeclarations());
		
		IBinding binding = createFieldBinding(token.toString(), type, frame.getDeclSpec());
		
		declarationStack.removeLast();
		declarationStack.getLast().addNestedDeclaration(binding);
		declarationStack.add(new DeclaratorFrame(frame.getDeclSpec()));  // reset the declarator
		exprTypeStack.push(type);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				declarationStack.removeLast();
				declarationStack.getLast().removeLastNestedDeclaration();
				declarationStack.add(frame);
			}
		});
		
		return binding;
	}

	
	/**
	 * An abstract declarator used as part of an expression, eg) a cast.
	 * Only need the type.
	 * 
	 * TODO: this isn't enough, I need a binding for the abstract declarator
	 * what I really need is a consumeDeclaratorCompleteTypeId similar to above
	 */
	public void consumeTypeId() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		IType type = frame.getDeclaratorType();
		if(frame.isFunctionDeclarator()) // its a function pointer
			type = createFunctionType(type, frame.getNestedDeclarations());
		
		exprTypeStack.push(type);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				declarationStack.add(frame);
			}
		});
	}

	
	public void consumeDirectDeclaratorArrayModifier() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.addTypeModifier(new C99ArrayType());
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.removeLastTypeModifier();
			}
		});
	}
	
	
	public void consumeAbstractDeclaratorArrayModifier() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.addTypeModifier(new C99ArrayType());
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.removeLastTypeModifier();
			}
		});
	}


	public void consumeDirectDeclaratorModifiedArrayModifier(boolean isStatic, boolean isVarSized, boolean hasTypeQualifierList) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		C99ArrayType arrayType = new C99ArrayType();
		arrayType.setStatic(isStatic);
		arrayType.setVariableLength(isVarSized);
		
		if(hasTypeQualifierList) {
			arrayType.setConst(typeQualifiers.isConst);
			arrayType.setRestrict(typeQualifiers.isRestrict);
			arrayType.setVolatile(typeQualifiers.isVolatile);
		}

		final DeclaratorFrame frame = declarationStack.getLast();
		frame.addTypeModifier(arrayType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.removeLastTypeModifier();
			}
		});
	}

	
	
	public void consumePointer() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.addPointerModifier(new C99PointerType());
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.removeLastPointerModifier();
			}
		});
	}
	
	
	public void consumePointerTypeQualifierList() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		C99PointerType pointerType = new C99PointerType();
		pointerType.setConst(typeQualifiers.isConst);
		pointerType.setRestrict(typeQualifiers.isRestrict);
		pointerType.setVolatile(typeQualifiers.isVolatile);
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.addPointerModifier(pointerType);

		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.removeLastPointerModifier();
			}
		});
	}
	
	
	public void consumeTypeQualifiers() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		typeQualifiers = new TypeQualifiers();
		
		for(IToken token : parser.getRuleTokens()) {
			switch(token.getKind()) {
				case C99Parsersym.TK_const:
					typeQualifiers.isConst = true;
					break;
				case C99Parsersym.TK_restrict:
					typeQualifiers.isRestrict = true;
					break;
				case C99Parsersym.TK_volatile:
					typeQualifiers.isVolatile = true;
					break;
			}
		}
		
		// I don't think this is really necessary but I need an undo action anyway
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				typeQualifiers = null;
			}
		});
	}
	
	
		
	/**
	 * Works for structs, unions and enums.
	 * If the struct tag is not in the symbol table then it is treated
	 * as a declaration.
	 */
	public IBinding consumeTypeSpecifierElaborated(int kind) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		String tag = parser.getRightIToken().toString();
		
		IC99Binding structBinding = symbolTable.lookup(STRUCT_TAG, tag);
		
		final C99SymbolTable undoTable;
		
		final boolean isDeclaration = (structBinding == null);
		
		if(isDeclaration) { // declaration of an incomplete type
			if(kind == IASTElaboratedTypeSpecifier.k_enum)
				structBinding = new C99Enumeration();
			else
				structBinding = new C99Structure(kind);
							
			undoTable = symbolTable;
			symbolTable = symbolTable.insert(STRUCT_TAG, tag, structBinding);
		}
		else {
			undoTable = null; // final variable must be initialized
		}
		
		final DeclSpec declSpec = declarationStack.getLast().getDeclSpec();
		declSpec.setType((IType)structBinding); // upcast
		
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				declSpec.setType(null);
				
				if(isDeclaration) {
					assert undoTable != null;
					symbolTable = undoTable;
				}
			}
		});
		
		return structBinding;
	}


	/**
	 * @param key A field in IASTCompositeTypeSpecifier.
	 */
	public IBinding consumeTypeSpecifierComposite(final boolean hasName, int key) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		// If the symbol table isn't updated then its still ok to undo 
		// because setting symbolTable to oldTable will effectively be a no-op.
		final C99SymbolTable oldTable = symbolTable;
		
		C99Structure struct;
		if(hasName) {
			String ident = parser.getRuleTokens().get(1).toString();
			struct = (C99Structure) symbolTable.lookup(STRUCT_TAG, ident); // structure may have already been declared
			if(struct == null) {
				struct = new C99Structure(ident, key); 
				symbolTable = symbolTable.insert(STRUCT_TAG, ident, struct);
			}
		}
		else {
			struct = new C99Structure(key);
		}
		
		final DeclaratorFrame frame = declarationStack.getLast();
		for(IBinding binding : frame.getNestedDeclarations()) {
			// the parser may allow invalid declarations like typedefs inside of structures, ignore those
			if(binding instanceof C99Field) {
				C99Field field = (C99Field)binding;
				struct.addField(field);
				field.setCompositeTypeOwner(struct);
			}
		}
		
		frame.getDeclSpec().setType(struct);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.getDeclSpec().setType(null);
				symbolTable = oldTable;
			}
		});
		
		return struct;
	}
	
	
	public IBinding consumeTypeSpecifierEnumeration(final boolean hasName) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		C99Enumeration enumeration = new C99Enumeration();
		
		final C99SymbolTable oldTable = symbolTable;
		if(hasName) {
			String ident = parser.getRuleTokens().get(1).toString(); 
			enumeration.setName(ident);
			symbolTable = symbolTable.insert(STRUCT_TAG, ident, enumeration);
		}
		
		final DeclaratorFrame frame = declarationStack.getLast();
		for(IBinding binding : frame.getNestedDeclarations()) {
			C99Enumerator enumerator = (C99Enumerator)binding;
			enumeration.addEnumerator(enumerator);
			enumerator.setType(enumeration);
		}
		
		frame.getDeclSpec().setType(enumeration);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				frame.getDeclSpec().setType(null);
				if(hasName)
					symbolTable = oldTable;
			}
		});
		
		return enumeration;
	}

	

	public IBinding consumeEnumerator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		IToken token = parser.getLeftIToken();
		String ident = token.toString();
		C99Enumerator enumerator = new C99Enumerator(ident);
		
		final C99SymbolTable oldTable = symbolTable;
		symbolTable = symbolTable.insert(IDENTIFIER, ident, enumerator);
		
		// enumerators are not declarations in the standard sense, so a scope won't be opened for them
		declarationStack.getLast().addNestedDeclaration(enumerator);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				declarationStack.getLast().removeLastNestedDeclaration();
				symbolTable = oldTable;
			}
		});
		
		return enumerator;
	}


	
	public IField consumeDesignatorBaseField() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		
		IType baseType = getInitializerType();
		String fieldName = parser.getRightIToken().toString();
		
		C99Field fieldBinding = computeFieldBinding(baseType, fieldName, false);
		IType type = fieldBinding == null ? C99ProblemBinding.badType() : fieldBinding.getType();
		
		exprTypeStack.push(type);
	
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
			}
		});
		
		return fieldBinding;
	}
	

	
	private IType getInitializerType() {
		List<IType> outerScope = exprTypeStack.outerScope();
		return outerScope.get(outerScope.size()-1);
	}
	
	
	public void consumeDesignatorBaseArray() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		IType baseType = getInitializerType();
		
		IType type = C99ProblemBinding.badType();
		if(baseType instanceof C99ArrayType) {
			type = ((C99ArrayType)baseType).getType();
		}
		
		exprTypeStack.push(type);
	
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
			}
		});
	}

	public void consumeInitializerStartPositional() {
		if(DEBUG) DebugUtil.printMethodTrace();
		DebugUtil.printMethodTrace();
		
		IType type = getInitializerType(); 
		type = rawType(type);
		
		IType positionType;
		if(type instanceof C99Structure) {
			int position = exprTypeStack.topScope().size();
			C99Field field = (C99Field)((C99Structure)type).getFields()[position];
			positionType = field.getType();
		}
		else if(type instanceof IArrayType) {
			positionType = ((C99ArrayType)type).getType();
		}
		else {
			positionType = C99ProblemBinding.badType();
		}
		
		exprTypeStack.push(positionType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
			}
		});
	}
	

	private static C99FunctionType createFunctionType(IType returnType, List<IBinding> parameterDeclarations) {
		C99FunctionType functionType = new C99FunctionType();
		functionType.setReturnType(returnType);
		for(IBinding b : parameterDeclarations) {
			C99Variable parameter = (C99Variable) b;
			functionType.addParameterType(parameter.getType());
		}
		return functionType;
	}
	
	
	
	// helper functions for creating binding objects
	
	private static C99Function createFunctionBinding(String ident, IFunctionType type, DeclSpec declSpec) {
		C99Function func = new C99Function(ident == null ? NO_IDENT : ident, type);
		declSpec.modifyBinding(func);
		return func;
	}
	
	private static C99Function createFunctionBinding(String ident, IFunctionType type, DeclSpec declSpec, List<IBinding> params) {
		C99Function func = createFunctionBinding(ident, type, declSpec);
		for(IBinding b : params) {
			func.addParameter((IParameter)b);
		}
		return func;
	}
	
	private static C99Field createFieldBinding(String ident, IType type, DeclSpec declSpec) {
		C99Field var = new C99Field(ident == null ? NO_IDENT : ident);
		var.setType(type);
		declSpec.modifyBinding(var);
		return var;
	}
	
	private static C99Parameter createParameterBinding(String ident, IType type, DeclSpec declSpec) {
		C99Parameter param = new C99Parameter(ident == null ? NO_IDENT : ident);
		param.setType(type);
		declSpec.modifyBinding(param);
		return param;
	}
	
	private static C99Variable createVariableBinding(String ident, IType type, DeclSpec declSpec) {
		C99Variable var = new C99Variable(ident == null ? NO_IDENT : ident);
		var.setType(type);
		declSpec.modifyBinding(var);
		return var;
	}
	
	private static C99Typedef createTypedefBinding(String ident, IType type) {
		return new C99Typedef(type, ident == null ? NO_IDENT : ident);
	}

	
	
	public void openTypeScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		exprTypeStack.openScope();
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.closeScope();
			}
		});
	}
	
	
	@SuppressWarnings("nls")
	public void consumeExpressionConstant(int kind) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		//super.consumeExpressionConstant(kind);
		// TODO: this is incomplete, what about double constants, int constants with long suffix etc
		String constant = parser.getRightIToken().toString();
		
		IType type = null;
		switch(kind) {
			case IASTLiteralExpression.lk_char_constant:
				if(constant.startsWith("L")) {//unsigned short int
					C99BasicType charType = new C99BasicType(IBasicType.t_int);
					charType.setShort(true);
					charType.setUnsigned(true);
					type = charType;
				}
				else
					type = new C99BasicType(IBasicType.t_char);
				break;
				
			case IASTLiteralExpression.lk_float_constant:
				C99BasicType floatType;
				if(constant.contains("f") || constant.contains("F"))
					floatType = new C99BasicType(IBasicType.t_float);
				else
					floatType = new C99BasicType(IBasicType.t_double);
				
				if(constant.contains("l") || constant.contains("L"))
					floatType.setLong(true);
				
				type = floatType;
				break;
				
			case IASTLiteralExpression.lk_integer_constant:
				C99BasicType intType = new C99BasicType(IBasicType.t_int);
				if(constant.contains("l") || constant.contains("L"))
					intType.setLong(true);
			
				if(constant.contains("ll") || constant.contains("LL")) {
					intType.setLongLong(true);
					intType.setLong(false);
				}
				if(constant.contains("u") || constant.contains("U")) 
					intType.setUnsigned(true);
			
				type = intType;
				break;
				
			case IASTLiteralExpression.lk_string_literal:
				type = new C99PointerType(new C99BasicType(IBasicType.t_char));
				break;
				
			default:
				assert false : "can't get here"; //$NON-NLS-1$
		}
		
		exprTypeStack.push(type);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
			}
		});
	}
	 
	
	public IBinding consumeExpressionID() {		
		if(DEBUG) DebugUtil.printMethodTrace();
		
		IToken token = parser.getRightIToken();
		String ident = token.toString();
		IBinding binding = symbolTable.lookup(IDENTIFIER, ident);
		
		IType type = C99ProblemBinding.badType();
		if(binding instanceof ITypeable)
			type = ((ITypeable)binding).getType();
		
		exprTypeStack.push(type);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
			}
		});
		
		return binding;
	}


	public IField consumeExpressionFieldReference(boolean isPointerDereference) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		String memberName = parser.getRightIToken().toString();
		final IType identType = exprTypeStack.pop();
				
		C99Field field = computeFieldBinding(identType, memberName, isPointerDereference);
		IType resultType = field == null ? C99ProblemBinding.badType() : field.getType();

		exprTypeStack.push(resultType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				exprTypeStack.push(identType);
			}
		});
		
		return field;
	}
	
	/**
	 * Computes the type of a field member access in an expression.
	 * eg) x.a; computes the type of a
	 */
	private C99Field computeFieldBinding(IType identType, String memberName, boolean isPointerDereference) {
		IType type = identType;
		if(isPointerDereference) {
			if(type instanceof IPointerType)
				type = ((ITypeContainer)type).getType(); // do the dereference
			else
				return null;
		}

		type = rawType(type);
		if(type instanceof ICompositeType) {
			ICompositeType struct = (ICompositeType) type;
			return (C99Field) struct.findField(memberName);
		}
		
		return null;
	}
		

	// TODO In C a function name can be used without parenthesis, the result is the address of 
	// the function (which is an int I think). This means an identifier that happens to be 
	// a function name should probably evaluate to an int, and then if it subsequently gets parsed
	// as a function call we can look up its function type from the symbol table.
	public void consumeExpressionFunctionCall(final boolean hasArgs) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final List<IType> scope = hasArgs ? exprTypeStack.closeScope() : null;
		final IType identifierType = exprTypeStack.pop();
		
		IType resultType = C99ProblemBinding.badType();
		
		if(identifierType instanceof IFunctionType) {
			// TODO: check the parameter types
			IFunctionType functionType = (IFunctionType)identifierType;
			resultType = functionType.getReturnType();

		}

		exprTypeStack.push(resultType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				exprTypeStack.push(identifierType);
				if(hasArgs)
					exprTypeStack.openScope(scope);
			}
		});
	}

	
	public void consumeExpressionArraySubscript() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		// Not doing type checking so it doesn't matter that this is integral type
		final IType subscriptType = exprTypeStack.pop();
		final IType exprType = exprTypeStack.pop();
		
		IType resultType = C99ProblemBinding.badType();
		if(exprType instanceof IArrayType) {
			IArrayType arrType = (IArrayType) exprType;
			resultType = arrType.getType(); // strip off the array type

		}
		
		exprTypeStack.push(resultType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				exprTypeStack.push(exprType);
				exprTypeStack.push(subscriptType);
			}
		});
	}


	public void consumeExpressionUnaryOperator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		// TODO: this is lazy, need to check the actual rules for types and operators
		final IType expressionType = exprTypeStack.pop();
		
		IType resultType = new C99BasicType(IBasicType.t_int);
		exprTypeStack.push(resultType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				exprTypeStack.push(expressionType);
			}
		});
	}

	
	public void consumeExpressionUnarySizeofTypeName() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final IType typeNameType = exprTypeStack.pop();
		
		IType resultType = new C99BasicType(IBasicType.t_int);
		exprTypeStack.push(resultType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				exprTypeStack.push(typeNameType);
			}
		});
	}


	public void consumeExpressionCast() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final IType exprType = exprTypeStack.pop();
		
		// pop then push is no-op
		//IType castType = exprTypeStack.pop();
		//exprTypeStack.push(castType);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.push(exprType);
			}
		});
	}
	

	public void consumeExpressionTypeIdInitializer() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		// Throw away the types of the initializer list
		final List<IType> scope = exprTypeStack.closeScope();

		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.openScope(scope);
			}
		});
	}
	
	public void consumeExpressionInitializer() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final IType type = exprTypeStack.pop();
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				exprTypeStack.push(type);
			}
		});
	}
	

	public void consumeExpressionBinaryOperator(int op) {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final IType op2type = exprTypeStack.pop();
		final IType op1type = exprTypeStack.pop();
		
		switch(op) {
			case IASTBinaryExpression.op_assign:
			case IASTBinaryExpression.op_multiplyAssign:
			case IASTBinaryExpression.op_divideAssign:
			case IASTBinaryExpression.op_moduloAssign:
			case IASTBinaryExpression.op_plusAssign:
			case IASTBinaryExpression.op_minusAssign:
			case IASTBinaryExpression.op_shiftLeftAssign:
			case IASTBinaryExpression.op_shiftRightAssign:
			case IASTBinaryExpression.op_binaryAndAssign:
			case IASTBinaryExpression.op_binaryXorAssign:
			case IASTBinaryExpression.op_binaryOrAssign:
				exprTypeStack.push(op1type);
				break;
				
			default:
				IType resultType = new C99BasicType(IBasicType.t_int);
				exprTypeStack.push(resultType);
		}
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				exprTypeStack.pop();
				exprTypeStack.push(op1type);
				exprTypeStack.push(op2type);
			}
		});
	}
	
	public void consumeExpressionConditional() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final IType expr2Type = exprTypeStack.pop();
		final IType expr1Type = exprTypeStack.pop();
		final IType condType = exprTypeStack.pop();
		exprTypeStack.push(expr1Type);
		
		undoStack.add(new IUndoAction() {
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				exprTypeStack.pop();
				exprTypeStack.push(condType);
				exprTypeStack.push(expr1Type);
				exprTypeStack.push(expr2Type);
			}
		});
	}

//	// TODO: expression lists are changing
//	public void consumeExpressionList(boolean baseCase) {		
//		// This is a hack, the type of an expression
//		// list will be the first expression in the list.
//		if(!baseCase) {
//			exprTypeStack.pop();
//		}
//	}
	
}
