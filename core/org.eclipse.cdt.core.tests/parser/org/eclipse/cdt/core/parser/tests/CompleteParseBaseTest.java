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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
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
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;

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
    }
    public static class FullParseCallback implements ISourceElementRequestor 
    {
    	private List references = new ArrayList(); 
    	private List forewardDecls = new ArrayList();
        private Stack inclusions = new Stack();
        private Scope compilationUnit;
        
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
            pushScope( function );
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
        public void enterCompilationUnit(IASTCompilationUnit compilationUnit)
        {
            pushScope( compilationUnit );            
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
            // TODO Auto-generated method stub
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
            // TODO Auto-generated method stub
            
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
            pushScope(method);
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
            // TODO Auto-generated method stub
            
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
            // TODO Auto-generated method stub
            
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
        public void exitCompilationUnit(IASTCompilationUnit compilationUnit)
        {
            this.compilationUnit = popScope();
        }
    
        
        
        private Stack scopes = new Stack();
        protected Scope getCurrentScope()
        {
        	return (Scope)scopes.peek();
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
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
         */
        public void acceptProblem(IProblem problem)
        {
            // TODO Auto-generated method stub
            
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
            references.add( reference );
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
         */
        public void acceptTypedefReference(IASTTypedefReference reference)
        {
    		references.add( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
         */
        public void acceptNamespaceReference(IASTNamespaceReference reference)
        {
    		references.add( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
         */
        public void acceptEnumerationReference(IASTEnumerationReference reference)
        {
    		references.add( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
         */
        public void acceptVariableReference(IASTVariableReference reference)
        {
    		references.add( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
         */
        public void acceptFunctionReference(IASTFunctionReference reference)
        {
    		references.add( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
         */
        public void acceptFieldReference(IASTFieldReference reference)
        {
    		references.add( reference );
            
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
         */
        public void acceptMethodReference(IASTMethodReference reference)
        {
    		references.add( reference );
            
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
    
    }
    protected Iterator getDeclarations(IASTScope scope)
    {
    	Scope s = callback.lookup( scope ); 
    	if( s != null )
    		return s.getDeclarations();
    	return null;
    }
    protected FullParseCallback callback;
    protected IASTScope parse(String code) throws ParserException
    {
    	callback = new FullParseCallback(); 
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new StringReader( code ), "test-code", new ScannerInfo(),
    			ParserMode.COMPLETE_PARSE, callback ), callback, ParserMode.COMPLETE_PARSE	
    		);
    	if( ! parser.parse() ) throw new ParserException( "FAILURE");
        return callback.getCompilationUnit();
    }
}
