/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.CompleteParser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache;
import org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTTypedefReference;

/**
 * @author jcamelon
 *
 */
public class CompleteParseBaseTest extends TestCase
{
    /**
     * 
     */
    public CompleteParseBaseTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param name
     */
    public CompleteParseBaseTest(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }
    public static class Scope implements IASTScope
    {
    	private List decls = new ArrayList(); 
    	private final IASTScope scope; 
    	public Scope( IASTScope scope )
    	{
    		this.scope = scope;
    	}
    	
    	public void addDeclaration( IASTDeclaration  d )
    	{
    		decls.add(d);
    	}
    	
    	public Iterator getDeclarations()
    	{
    		return decls.iterator();
    	}
    
        /**
         * @return
         */
        public IASTScope getScope()
        {
         
            return scope;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind, org.eclipse.cdt.core.parser.ast.IASTNode)
		 */
		public ILookupResult lookup(String prefix, LookupKind[] kind, IASTNode context, IASTExpression functionParameters) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ast.IASTNode#getFileIndex()
		 */
		public int getFileIndex() {
			// TODO Auto-generated method stub
			return 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ast.IASTNode#setFileIndex(int)
		 */
		public void setFileIndex(int index) {
			// TODO Auto-generated method stub
			
		}

    }
    
    public static class CodeScope extends Scope implements IASTCodeScope
    {
		private List nestedScopes = new ArrayList(); 
        /**
         * @param scope
         */
        public CodeScope(IASTCodeScope scope)
        {
            super(scope);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ast.IASTCodeScope#getOwnerCodeScope()
         */
        public IASTCodeScope getOwnerCodeScope()
        {
            return ((IASTCodeScope)getScope()).getOwnerCodeScope();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
         */
        public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
         */
        public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
         */
        public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
        {           
        }
    	
    	public void addNewScope( IASTCodeScope s )
    	{
    		nestedScopes.add( s );
    	}
    	
		public Iterator getCodeBlocks()
		{
			return nestedScopes.iterator();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ast.IASTCodeScope#getContainingFunction()
		 */
		public IASTFunction getContainingFunction() {
			// TODO Auto-generated method stub
			return null;
		}
    }
    
    public static class FullParseCallback implements ISourceElementRequestor 
    {
    	private List references = new ArrayList(); 
    	private List forewardDecls = new ArrayList();
        private Stack inclusions = new Stack();
        private Scope compilationUnit;
        
        public FullParseCallback()
        {
//        	System.out.println( "NEW");
//        	System.out.println();
        }
        
        public void finalize()
        {
//			System.out.println( );
        }
        
        public IASTScope getCompilationUnit()
        {
        	return compilationUnit;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
         */
        public void acceptVariable(IASTVariable variable)
        {
        	getCurrentScope().addDeclaration( variable );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
         */
        public void acceptFunctionDeclaration(IASTFunction function)
        {
            getCurrentScope().addDeclaration(function);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.cdt.core.parser.ast.IASTUsingDirective)
         */
        public void acceptUsingDirective(IASTUsingDirective usageDirective)
        {
    		getCurrentScope().addDeclaration(usageDirective);
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration)
         */
        public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration)
        {
    		getCurrentScope().addDeclaration(usageDeclaration);
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
         */
        public void acceptASMDefinition(IASTASMDefinition asmDefinition)
        {
    		getCurrentScope().addDeclaration(asmDefinition);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefDeclaration(org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration)
         */
        public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef)
        {
            getCurrentScope().addDeclaration(typedef);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
         */
        public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration)
        {           
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
         */
        public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration)
        {
            getCurrentScope().addDeclaration( abstractDeclaration );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
         */
        public void enterFunctionBody(IASTFunction function)
        {
            pushCodeScope( function );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
         */
        public void exitFunctionBody(IASTFunction function)
        {
            popScope();
    		getCurrentScope().addDeclaration(function);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
         */
        public void enterCompilationUnit(IASTCompilationUnit cu)
        {
            pushScope( cu );
            this.compilationUnit = getCurrentScope();
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
         */
        public void enterInclusion(IASTInclusion inclusion)
        {
            pushInclusion( inclusion );
        }
    
        /**
         * @param inclusion
         */
        private void pushInclusion(IASTInclusion inclusion)
        {
            inclusions.push( inclusion );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
         */
        public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
        {
            pushScope( namespaceDefinition );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#entesrClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
         */
        public void enterClassSpecifier(IASTClassSpecifier classSpecification)
        {
            pushScope( classSpecification );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
         */
        public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec)
        {
        	pushScope( linkageSpec );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
         */
        public void enterTemplateDeclaration(IASTTemplateDeclaration declaration)
        {
        	pushScope( declaration );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
         */
        public void enterTemplateSpecialization(IASTTemplateSpecialization specialization)
        {
            // TODO Auto-generated method stub
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
         */
        public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation)
        {
            pushScope( instantiation );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
         */
        public void acceptMethodDeclaration(IASTMethod method)
        {
            getCurrentScope().addDeclaration( method );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
         */
        public void enterMethodBody(IASTMethod method)
        {
            pushCodeScope(method);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
         */
        public void exitMethodBody(IASTMethod method)
        {
            popScope();
    		getCurrentScope().addDeclaration(method);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
         */
        public void acceptField(IASTField field)
        {
            getCurrentScope().addDeclaration(field);
            
        }
    
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
         */
        public void exitTemplateDeclaration(IASTTemplateDeclaration declaration)
        {
            popScope();
            getCurrentScope().addDeclaration( declaration );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
         */
        public void exitTemplateSpecialization(IASTTemplateSpecialization specialization)
        {
            // TODO Auto-generated method stub
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
         */
        public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation)
        {
        	popScope();
        	getCurrentScope().addDeclaration( instantiation );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
         */
        public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec)
        {
            popScope();
            getCurrentScope().addDeclaration(linkageSpec);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
         */
        public void exitClassSpecifier(IASTClassSpecifier classSpecification)
        {
            popScope();
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
         */
        public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
        {
            popScope();
            getCurrentScope().addDeclaration(namespaceDefinition);
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
         */
        public void exitInclusion(IASTInclusion inclusion)
        {
            popInclusion(); 
        }
    
        /**
         * 
         */
        private void popInclusion()
        {
            inclusions.pop();
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
         */
        public void exitCompilationUnit(IASTCompilationUnit cu )
        {
        }
    
        
        
        private Stack scopes = new Stack();
        protected Scope getCurrentScope()
        {
        	return (Scope)scopes.peek();
        }
        
        protected CodeScope getCurrentCodeScope()
        {
        	if( scopes.peek() instanceof CodeScope )
        		return (CodeScope)scopes.peek();
        	return null;
        }
        
        protected Scope popScope()
        {
        	Scope s = (Scope)scopes.pop();
        	h.put( s.getScope(), s );
        	return s; 
        }
        
        protected void pushScope( IASTScope scope )
        {
        	scopes.push( new Scope( scope ));
        }
        
        Hashtable h = new Hashtable();
        
        public Scope lookup( IASTScope s)
        {
        	return (Scope)h.get(s);
        }
        
		public CodeScope lookup( IASTCodeScope s )
		{
			return (CodeScope)h.get(s);
		}
        
    
    	List problems = new ArrayList();
    	
    	public Iterator getProblems() { 
    		return problems.iterator(); 
    	}
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
         */
        public boolean acceptProblem(IProblem problem)
        {
            problems.add( problem );
            return true;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
         */
        public void acceptMacro(IASTMacro macro)
        {
            // TODO Auto-generated method stub
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassReference)
         */
        public void acceptClassReference(IASTClassReference reference)
        {
            processReference( reference );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
         */
        public void acceptTypedefReference(IASTTypedefReference reference)
        {
			processReference( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
         */
        public void acceptNamespaceReference(IASTNamespaceReference reference)
        {
			processReference( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
         */
        public void acceptEnumerationReference(IASTEnumerationReference reference)
        {
			processReference( reference );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
         */
        public void acceptVariableReference(IASTVariableReference reference)
        {
			processReference( reference );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
         */
        public void acceptFunctionReference(IASTFunctionReference reference)
        {
            processReference(reference);
        }
        
        protected void processReference(IASTReference reference)
        {
        	ISourceElementCallbackDelegate referencedElement = reference.getReferencedElement();
        	IASTReference r = null;
    		if (referencedElement instanceof IASTTypedefDeclaration)
    			r = new ASTTypedefReference(reference.getOffset(),
    					(IASTTypedefDeclaration) referencedElement);
    		if (referencedElement instanceof IASTEnumerationSpecifier)
    			r = new ReferenceCache.ASTEnumerationReference(reference.getOffset(),
    					(IASTEnumerationSpecifier) referencedElement);
    		if (referencedElement instanceof IASTTemplateParameter)
    			r = new ReferenceCache.ASTTemplateParameterReference(reference.getOffset(),
    					(IASTTemplateParameter) referencedElement);
    		if (referencedElement instanceof IASTParameterDeclaration)
    			r = new ReferenceCache.ASTParameterReference(reference.getOffset(),
    					(IASTParameterDeclaration) referencedElement);
    		if (referencedElement instanceof IASTTypeSpecifier)
    			r = new ReferenceCache.ASTClassReference(reference.getOffset(),
    					(IASTTypeSpecifier) referencedElement);
    		if (referencedElement instanceof IASTNamespaceDefinition)
    			r = new ReferenceCache.ASTNamespaceReference(reference.getOffset(),
    					(IASTNamespaceDefinition) referencedElement);
    		if (referencedElement instanceof IASTFunction)
    			r = new ReferenceCache.ASTFunctionReference(reference.getOffset(),
    					(IASTFunction) referencedElement);
    		if (referencedElement instanceof IASTMethod)
    			r = new ReferenceCache.ASTMethodReference(reference.getOffset(), (IASTMethod) referencedElement);
    		if (referencedElement instanceof IASTField)
    			r = new ReferenceCache.ASTFieldReference(reference.getOffset(), (IASTField) referencedElement);
    		if (referencedElement instanceof IASTVariable)
    			r = new ReferenceCache.ASTVariableReference(reference.getOffset(),
    					(IASTVariable) referencedElement);
    		if (referencedElement instanceof IASTEnumerator)
    			r = new ReferenceCache.ASTEnumeratorReference(reference.getOffset(),
    					(IASTEnumerator) referencedElement);
    		if( r != null )
    			references.add( r );
//            System.out.println( "Callback received Reference to " + reference.getName() + " @ offset " + reference.getOffset() );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
         */
        public void acceptFieldReference(IASTFieldReference reference)
        {
			processReference( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
         */
        public void acceptMethodReference(IASTMethodReference reference)
        {
			processReference( reference );
            
        }
        
        public List getReferences()
        {
        	return references;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
         */
        public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType)
        {
            forewardDecls.add( elaboratedType );
        }
        /**
         * @return
         */
        public List getForewardDecls()
        {
            return forewardDecls;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
		 */
		public void enterCodeBlock(IASTCodeScope scope) {
			pushCodeScope( scope );
		}

		/**
         * @param scope
         */
        protected void pushCodeScope(IASTCodeScope scope)
        {
			scopes.push( new CodeScope( scope ) );
        }

        /* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
		 */
		public void exitCodeBlock(IASTCodeScope scope) {
			popScope();
			if( getCurrentCodeScope() != null )
				getCurrentCodeScope().addNewScope(scope);
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumeratorReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
         */
        public void acceptEnumeratorReference(IASTEnumeratorReference reference)
        {
			processReference( reference );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterReference)
         */
        public void acceptParameterReference(IASTParameterReference reference)
        {
			processReference( reference );
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
		 */
		public CodeReader createReader(String finalPath, Iterator workingCopies) {
			return ParserUtil.createReader(finalPath,workingCopies);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTemplateParameterReference(org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference)
		 */
		public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) {
			processReference( reference );
			
		}
    
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#parserTimeout()
		 */
		public boolean parserTimeout() {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
		 */
		public void acceptFriendDeclaration(IASTDeclaration declaration) {
			getCurrentScope().addDeclaration( declaration );
		}
    }
    
    protected Iterator getNestedScopes( IASTCodeScope scope )
    {
    	CodeScope s = callback.lookup( scope );
		if( s != null )
			return s.getCodeBlocks();
		return null;
 
    }
    protected Iterator getDeclarations(IASTScope scope)
    {
    	Scope s = callback.lookup( scope ); 
    	if( s != null )
    		return s.getDeclarations();
    	return null;
    }
    protected FullParseCallback callback;
    
    protected IASTScope parse( String code ) throws ParserException, ParserFactoryError
    {
    	return parse( code, true, ParserLanguage.CPP );
    }
    
    protected IASTScope parse( String code, boolean throwOnError ) throws ParserException, ParserFactoryError
    {
    	return parse( code, throwOnError, ParserLanguage.CPP );
    }
    
    protected IASTScope parse(String code, boolean throwOnError, ParserLanguage language) throws ParserException, ParserFactoryError
    {
    	callback = new FullParseCallback(); 
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new CodeReader( code.toCharArray() ), new ScannerInfo(), //$NON-NLS-1$
    			ParserMode.COMPLETE_PARSE, language, callback, new NullLogService(), null ), callback, ParserMode.COMPLETE_PARSE, language, null 	
    		);
    	if( ! parser.parse() && throwOnError ) throw new ParserException( "FAILURE"); //$NON-NLS-1$
    	assertTrue( ((CompleteParser)parser).validateCaches());
        return callback.getCompilationUnit();
    }
        
    protected void assertReferences( 
    	ISourceElementCallbackDelegate element, 
    	int expectedDistinctReferenceCount, 
    	boolean allowDuplicates, boolean allowNameMatching )
    {
    	Set matches = new HashSet(); 
    	Iterator allReferences = callback.getReferences().iterator();
    	while( allReferences.hasNext() )
    	{
    		IASTReference r = (IASTReference)allReferences.next();
    		if( r.getReferencedElement() == element )
    		{
    			assertEquals( r.getName(), ((IASTOffsetableNamedElement)element).getName() );
    			if( ! matches.add( r ) && ! allowDuplicates )
    				fail( "Duplicate reference found for ISourceElementCallbackDelegate: " + element + " @ offset " + r.getOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
    		}
    		else
    		{
    			if( r.getReferencedElement() instanceof IASTQualifiedNameElement && 
    				element instanceof IASTQualifiedNameElement &&
					allowNameMatching )
    			{
					if( qualifiedNamesEquals( 
						((IASTQualifiedNameElement)r.getReferencedElement()).getFullyQualifiedName(),
						((IASTQualifiedNameElement)element).getFullyQualifiedName() 
											) 
					  )
					  { 
					  
						if( ! matches.add( r ) && ! allowDuplicates )
							fail( "Duplicate reference found for ISourceElementCallbackDelegate: " + element + " @ offset " + r.getOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
					  }
					
    			}
    		}
    	}
    	
    	assertEquals( expectedDistinctReferenceCount, matches.size() );
    }
    
    protected static class Task
    {
    	private final boolean allowNameMatching;
    	private final boolean unique;
        private final int count;
        private final ISourceElementCallbackDelegate element;
    	

        public Task( ISourceElementCallbackDelegate element, int referenceCount, boolean distinct, boolean matchNames ){
        	this.element = element;
    		this.count = referenceCount;
    		this.unique = distinct;
    		this.allowNameMatching = matchNames;
        }
        
        public Task( ISourceElementCallbackDelegate element, int referenceCount, boolean distinct )
    	{
        	this( element, referenceCount, distinct, true );
    	}
    	
		public Task( ISourceElementCallbackDelegate element, int referenceCount )
		{
			this( element, referenceCount, true );
		}
		
		public Task( ISourceElementCallbackDelegate element )
		{
			this( element, 1, false );
		}
		
        /**
         * @return
         */
        public int getCount()
        {
            return count;
        }

        /**
         * @return
         */
        public ISourceElementCallbackDelegate getElement()
        {
            return element;
        }

        /**
         * @return
         */
        public boolean isUnique()
        {
            return unique;
        }
        
        public boolean allowNameMatching(){
        	return allowNameMatching;
        }

    }
    
    protected void assertReferenceTask( Task task )
    {
		assertReferences( task.getElement(), task.getCount(), task.isUnique(), task.allowNameMatching() );    	
    }
    
    protected void assertAllReferences( int count, List tasks )
    {
		assertEquals( callback.getReferences().size(), count );
    	if( tasks == null ) return;
    	Iterator i = tasks.iterator();
    	while( i.hasNext() )
    	{
    		assertReferenceTask( (Task)i.next() );
    	}
    }

	protected List createTaskList( Task t1 )
	{
		List result = new ArrayList(); 
		result.add( t1 );
		return result;
	}
    
    protected List createTaskList( Task t1, Task t2 )
    {
    	List result = createTaskList(t1); 
		result.add( t2 );
		return result;
    }
    
	protected List createTaskList( Task t1, Task t2, Task t3 )
	{
		List result = createTaskList(t1, t2);
		result.add( t3 );
		return result;
	}

	protected List createTaskList( Task t1, Task t2, Task t3, Task t4 )
	{
		List result = createTaskList(t1, t2, t3);
		result.add( t4 );
		return result;
	}
	
	protected List createTaskList( Task t1, Task t2, Task t3, Task t4, Task t5 )
	{
		List result = createTaskList(t1, t2, t3, t4);
		result.add( t5 );
		return result;		
	}
    /**
         * @param task
         * @param task2
         * @param task3
         * @param task4
         * @param task5
         * @param task6
         * @return
         */
    protected List createTaskList(Task task, Task task2, Task task3, Task task4, Task task5, Task task6)
    {
        List result = createTaskList( task, task2, task3, task4, task5 );
        result.add( task6 );
        return result;
    }

	protected List createTaskList(Task task, Task task2, Task task3, Task task4, Task task5, Task task6, Task task7)
	{
		List result = createTaskList( task, task2, task3, task4, task5, task6 );
		result.add( task7 );
		return result;
	}
	
	protected List createTaskList(Task task, Task task2, Task task3, Task task4, Task task5, Task task6, Task task7, Task task8 )
	{
		List result = createTaskList( task, task2, task3, task4, task5, task6, task7 );
		result.add( task8 );
		return result;
	}

	protected List createTaskList(Task task, Task task2, Task task3, Task task4, Task task5, Task task6, Task task7, Task task8, Task task9 )
	{
		List result = createTaskList( task, task2, task3, task4, task5, task6, task7, task8 );
		result.add( task9 );
		return result;
	}

	protected List createTaskList(Task task, Task task2, Task task3, Task task4, Task task5, Task task6, Task task7, Task task8, Task task9, Task task10 )
	{
		List result = createTaskList( task, task2, task3, task4, task5, task6, task7, task8, task9 );
		result.add( task10 );
		return result;
	}
	
	public boolean qualifiedNamesEquals( String [] fromAST, String [] theTruth)
	{
		if( fromAST == null || theTruth == null ) return false;
		if( fromAST.length !=  theTruth.length ) return false;
		for( int i = 0; i < fromAST.length; ++i )
		{
			if( !( fromAST[i].equals( theTruth[i] ) ) )
				return false;
		}
		return true;
	}

	protected void assertQualifiedName(String [] fromAST, String [] theTruth)
	{
		assertTrue( qualifiedNamesEquals( fromAST, theTruth ));
	}


}
