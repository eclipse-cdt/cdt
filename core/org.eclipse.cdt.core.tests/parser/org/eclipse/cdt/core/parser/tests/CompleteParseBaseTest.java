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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
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
import org.eclipse.cdt.core.parser.ast.IASTEnumeratorReference;
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
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTReference;
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
        public void acceptElement(ISourceElementRequestor requestor)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
         */
        public void enterScope(ISourceElementRequestor requestor)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
         */
        public void exitScope(ISourceElementRequestor requestor)
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
        
        protected CodeScope getCurrentCodeScope()
        {
        	return (CodeScope)scopes.peek();
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
            references.add( reference );
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
    
    protected IASTScope parse( String code ) throws ParserException
    {
    	return parse( code, true );
    }
    protected IASTScope parse(String code, boolean throwOnError) throws ParserException
    {
    	callback = new FullParseCallback(); 
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new StringReader( code ), "test-code", new ScannerInfo(),
    			ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, callback ), callback, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP	
    		);
    	if( ! parser.parse() && throwOnError ) throw new ParserException( "FAILURE");
        return callback.getCompilationUnit();
    }
        
    protected void assertReferences( 
    	ISourceElementCallbackDelegate element, 
    	int expectedDistinctReferenceCount, 
    	boolean allowDuplicates )
    {
    	Set matches = new HashSet(); 
    	Iterator allReferences = callback.getReferences().iterator();
    	while( allReferences.hasNext() )
    	{
    		IASTReference r = (IASTReference)allReferences.next();
    		if( r.getReferencedElement() == element )
    		{
    			if( ! matches.add( r ) && ! allowDuplicates )
    				fail( "Duplicate reference found for ISourceElementCallbackDelegate: " + element + " @ offset " + r.getOffset() );
    		}
    		else
    		{
    			if( r.getReferencedElement() instanceof IASTQualifiedNameElement && 
    				element instanceof IASTQualifiedNameElement )
    			{
					if( qualifiedNamesEquals( 
						((IASTQualifiedNameElement)r.getReferencedElement()).getFullyQualifiedName(),
						((IASTQualifiedNameElement)element).getFullyQualifiedName() 
											) 
					  )
					  { 
					  
						if( ! matches.add( r ) && ! allowDuplicates )
							fail( "Duplicate reference found for ISourceElementCallbackDelegate: " + element + " @ offset " + r.getOffset() );
					  }
					
    			}
    		}
    	}
    	
    	assertEquals( expectedDistinctReferenceCount, matches.size() );
    }
    
    protected static class Task
    {
    	private final boolean unique;
        private final int count;
        private final ISourceElementCallbackDelegate element;
    	

        public Task( ISourceElementCallbackDelegate element, int referenceCount, boolean distinct )
    	{
    		this.element = element;
    		this.count = referenceCount;
    		this.unique = distinct; 
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

    }
    
    protected void assertReferenceTask( Task task )
    {
		assertReferences( task.getElement(), task.getCount(), task.isUnique() );    	
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
