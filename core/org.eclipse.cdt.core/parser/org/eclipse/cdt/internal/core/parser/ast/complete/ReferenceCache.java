/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *  
 */
public class ReferenceCache implements IReferenceManager {

		/**
	 * @author jcamelon
	 *
	 */
	private interface IReferenceFactory {
		ASTReference createReference();
		ASTReference [] createReferenceArray( int size );
	}
	
	private static final int DEFAULT_CACHE_SIZE = 64;

	private boolean[] classReferencesAvailable;
	private ASTReference[] classReferences;
	private boolean[] variableReferencesAvailable;
	private ASTReference[] variableReferences;
	private boolean[] fieldReferencesAvailable;
	private ASTReference[] fieldReferences;
	private boolean[] functionReferencesAvailable;
	private ASTReference[] functionReferences;
	private boolean[] methodReferencesAvailable;
	private ASTReference[] methodReferences;
	private boolean[] enumerationReferencesAvailable;
	private ASTReference[] enumerationReferences;
	private boolean[] enumeratorReferencesAvailable;
	private ASTReference[] enumeratorReferences;
	private boolean[] namespaceReferencesAvailable;
	private ASTReference[] namespaceReferences;
	private boolean[] typedefReferencesAvailable;
	private ASTReference[] typedefReferences;
	private boolean[] parameterReferencesAvailable;
	private ASTReference[] parameterReferences;
	private boolean[] templateParameterReferencesAvailable;
	private ASTReference[] templateParameterReferences;
	
	
	
	private abstract static class BaseReferenceFactory implements IReferenceFactory
	{
		public ASTReference[] createReferenceArray(int size) {
			return new ASTReference[ size ];
		}
	}

	private static final IReferenceFactory TYPEDEFREF_FACTORY = 
		new BaseReferenceFactory() {
		public ASTReference createReference() { return new ASTTypedefReference(); }
	};

	private static final IReferenceFactory NAMESPACEREF_FACTORY = 
		new BaseReferenceFactory() {
		public ASTReference createReference() { return new ASTNamespaceReference(); }
	};
	
	private static final IReferenceFactory PARMREF_FACTORY = 
		new BaseReferenceFactory() {
		public ASTReference createReference() { return new ASTParameterReference(); }
	};
	
	private static final IReferenceFactory TEMPPARMREF_FACTORY = 
		new BaseReferenceFactory() {
		public ASTReference createReference() { return new ASTTemplateParameterReference(); }
	};


	
	private static final IReferenceFactory CLASSREF_FACTORY = 
		new BaseReferenceFactory() {
		public ASTReference createReference() { return new ASTClassReference(); }


	};
	
	private static final IReferenceFactory FUNCTIONREF_FACTORY = 
		new BaseReferenceFactory() {

			public ASTReference createReference() {
				return new ASTFunctionReference();
			} 
		
	};

	private static final IReferenceFactory METHODREF_FACTORY = 
		new BaseReferenceFactory() {

			public ASTReference createReference() {
				return new ASTMethodReference();
			} 
		
	};

	private static final IReferenceFactory VARIABLEREF_FACTORY = 
		new BaseReferenceFactory()	{

			public ASTReference createReference() {
				return new ASTVariableReference();
			}
		
	};
	
	private static final IReferenceFactory FIELDREF_FACTORY = new BaseReferenceFactory()
	{
		public ASTReference createReference()
		{
			return new ASTFieldReference();
		}
	};

	private static final IReferenceFactory ENUMSPECREF_FACTORY = new BaseReferenceFactory()
	{
		public ASTReference createReference()
		{
			return new ASTEnumerationReference();
		}
	};

	private static final IReferenceFactory ENUMERATORREF_FACTORY = new BaseReferenceFactory()
	{
		public ASTReference createReference()
		{
			return new ASTEnumeratorReference();
		}
	};

	
	{
		classReferences = CLASSREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		variableReferences = VARIABLEREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		fieldReferences = FIELDREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		functionReferences = FUNCTIONREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		methodReferences = METHODREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		enumerationReferences = ENUMSPECREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		enumeratorReferences = ENUMERATORREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		namespaceReferences = NAMESPACEREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		typedefReferences = TYPEDEFREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		parameterReferences = PARMREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		templateParameterReferences = TEMPPARMREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		classReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		variableReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		fieldReferencesAvailable = new boolean[ DEFAULT_CACHE_SIZE ];
		functionReferencesAvailable = new boolean[ DEFAULT_CACHE_SIZE ];
		methodReferencesAvailable = new boolean[ DEFAULT_CACHE_SIZE ];
		enumerationReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		enumeratorReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		templateParameterReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		namespaceReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		typedefReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		parameterReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		for (int i = 0; i < DEFAULT_CACHE_SIZE; ++i) {
			classReferencesAvailable[i] = true;
			variableReferencesAvailable[i] = true;
			fieldReferencesAvailable[i] = true;
			functionReferencesAvailable[i] = true;
			methodReferencesAvailable[i] = true;
			enumerationReferencesAvailable[i] = true;
			enumeratorReferencesAvailable[i] = true;
			namespaceReferencesAvailable[i] = true;
			typedefReferencesAvailable[i] = true;
			parameterReferencesAvailable[i] = true;
			templateParameterReferencesAvailable[i] = true;
			methodReferences[i] = METHODREF_FACTORY.createReference();
			classReferences[i] =  CLASSREF_FACTORY.createReference();
			variableReferences[i] = VARIABLEREF_FACTORY.createReference();
			fieldReferences[i] = FIELDREF_FACTORY.createReference();
			functionReferences[i] = FUNCTIONREF_FACTORY.createReference();
			enumerationReferences[i] = ENUMSPECREF_FACTORY.createReference();
			enumeratorReferences[i] = ENUMERATORREF_FACTORY.createReference();
			typedefReferences[i] = TYPEDEFREF_FACTORY.createReference();
			namespaceReferences[i] = NAMESPACEREF_FACTORY.createReference();
			parameterReferences[i] = PARMREF_FACTORY.createReference();
			templateParameterReferences[i] = TEMPPARMREF_FACTORY.createReference();
		}
	}

	public void returnReference(IASTReference reference) {
		if (reference instanceof IASTClassReference)
		{
			returnReference(classReferencesAvailable, classReferences,
					reference);
			return;
		}
		if( reference instanceof IASTVariableReference )
		{
			returnReference( variableReferencesAvailable, variableReferences, reference );
			return;
		}
		if( reference instanceof IASTFieldReference )
		{
			returnReference( fieldReferencesAvailable, fieldReferences, reference );
			return;
		}
		if( reference instanceof IASTFunctionReference )
		{
			returnReference( functionReferencesAvailable, functionReferences, reference );
			return;
		}
		if( reference instanceof IASTMethodReference )
		{
			returnReference( methodReferencesAvailable, methodReferences, reference );
			return;
		}
		if( reference instanceof IASTEnumerationReference )
		{
			returnReference( enumerationReferencesAvailable, enumerationReferences, reference );
			return;
		}
		if( reference instanceof IASTEnumeratorReference )
		{
			returnReference( enumeratorReferencesAvailable, enumeratorReferences, reference );
			return;
		}			
		if( reference instanceof IASTNamespaceReference )
		{
			returnReference( namespaceReferencesAvailable, namespaceReferences, reference );
			return;
		}
		if( reference instanceof IASTTypedefReference )
		{
			returnReference( typedefReferencesAvailable, typedefReferences, reference );
			return;
		}
		if( reference instanceof IASTParameterReference )
		{
			returnReference( parameterReferencesAvailable, parameterReferences, reference );
			return;
		}
		if( reference instanceof IASTTemplateParameterReference)
		{
			returnReference( templateParameterReferencesAvailable, templateParameterReferences, reference );
		}
	}

	/**
	 * @param referencesAvailable
	 * @param references
	 * @param reference
	 */
	private void returnReference(boolean[] referencesAvailable,
			ASTReference[] references, IASTReference reference) {
		for (int i = 0; i < referencesAvailable.length; ++i)
			if (references[i] == reference) {
				referencesAvailable[i] = true;
				references[i].reset();
				break;
			}

	}

	public IASTReference getReference(int offset,
			ISourceElementCallbackDelegate referencedElement) {
		if (referencedElement instanceof IASTTypedefDeclaration)
			return getTypedefReference(offset,
					(IASTTypedefDeclaration) referencedElement);
		if (referencedElement instanceof IASTEnumerationSpecifier)
			return getEnumerationReference(offset,
					(IASTEnumerationSpecifier) referencedElement);
		if (referencedElement instanceof IASTTemplateParameter)
			return getTemplateParameterReference(offset,
					(IASTTemplateParameter) referencedElement);
		if (referencedElement instanceof IASTParameterDeclaration)
			return getParameterReference(offset,
					(IASTParameterDeclaration) referencedElement);
		if (referencedElement instanceof IASTTypeSpecifier)
			return getClassReference(offset, referencedElement);
		if (referencedElement instanceof IASTNamespaceDefinition)
			return getNamespaceReference(offset,
					(IASTNamespaceDefinition) referencedElement);
		if (referencedElement instanceof IASTMethod)
			return getMethodReference(offset, (IASTMethod) referencedElement);
		if (referencedElement instanceof IASTFunction)
			return getFunctionReference(offset,
					(IASTFunction) referencedElement);
		if (referencedElement instanceof IASTField)
			return getFieldReference(offset, (IASTField) referencedElement);
		if (referencedElement instanceof IASTVariable)
			return getVariableReference(offset,
					(IASTVariable) referencedElement);
		if (referencedElement instanceof IASTEnumerator)
			return getEnumeratorReference(offset,
					(IASTEnumerator) referencedElement);
		return null;
	}

	private IASTReference getClassReference(int offset,
			ISourceElementCallbackDelegate referencedElement) {
		for (int i = 0; i < classReferencesAvailable.length; ++i) {
			if (classReferencesAvailable[i]) {
				classReferencesAvailable[i] = false;
				classReferences[i].initialize(offset, referencedElement);
				return classReferences[i];
			}
		}
		int currentSize = classReferences.length;
		GrowResult g = growArrays(classReferences, classReferencesAvailable, CLASSREF_FACTORY );
		classReferences = g.getReferences();
		classReferencesAvailable = g.getAvailables();
		classReferencesAvailable[currentSize] = false;
		classReferences[currentSize].initialize(offset, referencedElement);
		return classReferences[currentSize];
	}

	protected static class GrowResult
	{
		private boolean[] b;
		private ASTReference[] r;

		public void initialize( boolean [] bools, ASTReference [] refs )
		{
			this.b = bools;
			this.r = refs;
		}
		/**
		 * @return Returns the b.
		 */
		public boolean[] getAvailables() {
			return b;
		}
		/**
		 * @return Returns the r.
		 */
		public ASTReference[] getReferences() {
			return r;
		}
	}
	
	protected static final GrowResult growResult = new GrowResult();
	
	/**
	 * @param inReferences, boolean [] inReferencesAvailable, IReferenceFactory factory 
	 * @return
	 */
	protected static GrowResult growArrays(ASTReference[] inReferences, boolean [] inReferencesAvailable, IReferenceFactory factory ) {
		int currentSize = inReferences.length;
		boolean[] availables = new boolean[currentSize * 2];
		System.arraycopy(inReferencesAvailable, 0, availables, 0,	currentSize);
		ASTReference[] refs = factory.createReferenceArray(currentSize * 2);
		System.arraycopy(inReferences, 0, refs, 0, currentSize);
		for (int i = currentSize; i < availables.length; ++i) {
			refs[i] = factory.createReference();
			availables[i] = true;
		}
		growResult.initialize( availables, refs );
		return growResult;
	}

	/**
	 * @param offset
	 * @param declaration
	 * @return
	 */
	private IASTReference getTypedefReference(int offset,
			IASTTypedefDeclaration referencedElement) {
		for (int i = 0; i < typedefReferencesAvailable.length; ++i) {
			if (typedefReferencesAvailable[i]) {
				typedefReferencesAvailable[i] = false;
				typedefReferences[i].initialize(offset, referencedElement);
				return typedefReferences[i];
			}
		}
		int currentSize = typedefReferences.length;
		GrowResult g = growArrays( typedefReferences, typedefReferencesAvailable, TYPEDEFREF_FACTORY);
		typedefReferencesAvailable = g.getAvailables();
		typedefReferences = g.getReferences();
		typedefReferencesAvailable[currentSize] = false;
		typedefReferences[currentSize].initialize(offset, referencedElement);
		return typedefReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param variable
	 * @return
	 */
	private IASTReference getVariableReference(int offset, IASTVariable referencedElement) {
		for (int i = 0; i < variableReferencesAvailable.length; ++i) {
			if (variableReferencesAvailable[i]) {
				variableReferencesAvailable[i] = false;
				variableReferences[i].initialize(offset, referencedElement);
				return variableReferences[i];
			}
		}
		int currentSize = variableReferences.length;
		GrowResult g = growArrays( variableReferences, variableReferencesAvailable, VARIABLEREF_FACTORY );
		variableReferencesAvailable = g.getAvailables();
		variableReferences = g.getReferences();
		variableReferencesAvailable[currentSize] = false;
		variableReferences[currentSize].initialize(offset, referencedElement);
		return variableReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param declaration
	 * @return
	 */
	private IASTReference getParameterReference(int offset,
			IASTParameterDeclaration referencedElement) {
		for (int i = 0; i < parameterReferencesAvailable.length; ++i) {
			if (parameterReferencesAvailable[i]) {
				parameterReferencesAvailable[i] = false;
				parameterReferences[i].initialize(offset, referencedElement);
				return parameterReferences[i];
			}
		}
		int currentSize = parameterReferences.length;
		GrowResult g = growArrays( parameterReferences, parameterReferencesAvailable, PARMREF_FACTORY);
		parameterReferencesAvailable = g.getAvailables();
		parameterReferences = g.getReferences();
		parameterReferencesAvailable[currentSize] = false;
		parameterReferences[currentSize].initialize(offset, referencedElement);
		return parameterReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param parameter
	 * @return
	 */
	private IASTReference getTemplateParameterReference(int offset,
			IASTTemplateParameter referencedElement) {
		for (int i = 0; i < templateParameterReferencesAvailable.length; ++i) {
			if (templateParameterReferencesAvailable[i]) {
				templateParameterReferencesAvailable[i] = false;
				templateParameterReferences[i].initialize(offset, referencedElement);
				return templateParameterReferences[i];
			}
		}
		int currentSize = templateParameterReferences.length;
		GrowResult g = growArrays( templateParameterReferences, templateParameterReferencesAvailable, TEMPPARMREF_FACTORY);
		templateParameterReferencesAvailable = g.getAvailables();
		templateParameterReferences = g.getReferences();
		templateParameterReferencesAvailable[currentSize] = false;
		templateParameterReferences[currentSize].initialize(offset, referencedElement);
		return templateParameterReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param definition
	 * @return
	 */
	private IASTReference getNamespaceReference(int offset,
			IASTNamespaceDefinition referencedElement) {
		for (int i = 0; i < namespaceReferencesAvailable.length; ++i) {
			if (namespaceReferencesAvailable[i]) {
				namespaceReferencesAvailable[i] = false;
				namespaceReferences[i].initialize(offset, referencedElement);
				return namespaceReferences[i];
			}
		}
		int currentSize = namespaceReferences.length;
		GrowResult g = growArrays( namespaceReferences, namespaceReferencesAvailable, NAMESPACEREF_FACTORY);
		namespaceReferencesAvailable = g.getAvailables();
		namespaceReferences = g.getReferences();
		namespaceReferencesAvailable[currentSize] = false;
		namespaceReferences[currentSize].initialize(offset, referencedElement);
		return namespaceReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param specifier
	 * @return
	 */
	private IASTReference getEnumerationReference(int offset,
			IASTEnumerationSpecifier referencedElement) {
		for (int i = 0; i < enumerationReferencesAvailable.length; ++i) {
			if (enumerationReferencesAvailable[i]) {
				enumerationReferencesAvailable[i] = false;
				enumerationReferences[i].initialize(offset, referencedElement);
				return enumerationReferences[i];
			}
		}
		int currentSize = enumerationReferences.length;
		GrowResult g = growArrays( enumerationReferences, enumerationReferencesAvailable, ENUMSPECREF_FACTORY );
		enumerationReferencesAvailable = g.getAvailables();
		enumerationReferences = g.getReferences();
		enumerationReferencesAvailable[currentSize] = false;
		enumerationReferences[currentSize].initialize(offset, referencedElement);
		return enumerationReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param enumerator
	 * @return
	 */
	private IASTReference getEnumeratorReference(int offset,
			IASTEnumerator referencedElement ) {
		for (int i = 0; i < enumeratorReferencesAvailable.length; ++i) {
			if (enumeratorReferencesAvailable[i]) {
				enumeratorReferencesAvailable[i] = false;
				enumeratorReferences[i].initialize(offset, referencedElement);
				return enumeratorReferences[i];
			}
		}
		int currentSize = enumeratorReferences.length;
		GrowResult g = growArrays( enumeratorReferences, enumeratorReferencesAvailable, ENUMERATORREF_FACTORY );
		enumeratorReferencesAvailable = g.getAvailables();
		enumeratorReferences = g.getReferences();
		enumeratorReferencesAvailable[currentSize] = false;
		enumeratorReferences[currentSize].initialize(offset, referencedElement);
		return enumeratorReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param method
	 * @return
	 */
	private IASTReference getMethodReference(int offset, IASTMethod referencedElement ) {
		for (int i = 0; i < methodReferencesAvailable.length; ++i) {
			if (methodReferencesAvailable[i]) {
				methodReferencesAvailable[i] = false;
				methodReferences[i].initialize(offset, referencedElement);
				return methodReferences[i];
			}
		}
		int currentSize = methodReferences.length;
		GrowResult g = growArrays( methodReferences, methodReferencesAvailable, METHODREF_FACTORY );
		methodReferencesAvailable = g.getAvailables();
		methodReferences = g.getReferences();
		methodReferencesAvailable[currentSize] = false;
		methodReferences[currentSize].initialize(offset, referencedElement);
		return methodReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param function
	 * @return
	 */
	private IASTReference getFunctionReference(int offset, IASTFunction referencedElement ) {
		for (int i = 0; i < functionReferencesAvailable.length; ++i) {
			if (functionReferencesAvailable[i]) {
				functionReferencesAvailable[i] = false;
				functionReferences[i].initialize(offset, referencedElement);
				return functionReferences[i];
			}
		}
		int currentSize = functionReferences.length;
		GrowResult g = growArrays( functionReferences, functionReferencesAvailable, FUNCTIONREF_FACTORY );
		functionReferencesAvailable = g.getAvailables();
		functionReferences = g.getReferences();
		functionReferencesAvailable[currentSize] = false;
		functionReferences[currentSize].initialize(offset, referencedElement);
		return functionReferences[currentSize];
	}

	/**
	 * @param offset
	 * @param field
	 * @return
	 */
	private IASTReference getFieldReference(int offset, IASTField referencedElement) {
		for (int i = 0; i < fieldReferencesAvailable.length; ++i) {
			if (fieldReferencesAvailable[i]) {
				fieldReferencesAvailable[i] = false;
				fieldReferences[i].initialize(offset, referencedElement);
				return fieldReferences[i];
			}
		}
		int currentSize = fieldReferences.length;
		GrowResult g = growArrays( fieldReferences, fieldReferencesAvailable, FIELDREF_FACTORY );
		fieldReferencesAvailable = g.getAvailables();
		fieldReferences = g.getReferences();
		fieldReferencesAvailable[currentSize] = false;
		fieldReferences[currentSize].initialize(offset, referencedElement);
		return fieldReferences[currentSize];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#processReferences()
	 */
	public void processReferences(List references, ISourceElementRequestor requestor)
	{
		if( references == null || references.isEmpty() )
			return;
	    
	    for( int i = 0; i < references.size(); ++i )
	    {
	    	IASTReference reference = ((IASTReference)references.get(i));
			reference.acceptElement(requestor, this );
			returnReference( reference );
	    }
	    	
	    references.clear();
	}

	public abstract static class ASTReference implements IASTReference {
		protected int offset;
		private static final String EMPTY_STRING = ""; //$NON-NLS-1$
		private static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$

		public abstract void reset();
		
		protected void resetOffset() {
			offset = 0;
		}

		/**
		 * @param offset2
		 * @param re 
		 */
		public abstract void initialize(int o, ISourceElementCallbackDelegate re );


		protected void initialize(int o) {
			this.offset = o;
		}

		/**
		 *  
		 */
		public ASTReference(int offset) {
			this.offset = offset;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getOffset()
		 */
		public int getOffset() {
			return offset;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getName()
		 */
		public String getName() {
			if (getReferencedElement() instanceof IASTOffsetableNamedElement)
				return ((IASTOffsetableNamedElement) getReferencedElement())
						.getName();
			return EMPTY_STRING;
		}
		public char[] getNameCharArray() {
			if (getReferencedElement() instanceof IASTOffsetableNamedElement)
				return ((IASTOffsetableNamedElement) getReferencedElement())
						.getNameCharArray();
			return EMPTY_CHAR_ARRAY;
		}
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof IASTReference))
				return false;

			if (((IASTReference) obj).getName().equals(getName())
					&& ((IASTReference) obj).getOffset() == getOffset())
				return true;
			return false;
		}

		public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager) {
		}

		public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager) {
		}
	}
	public static class ASTClassReference extends ASTReference
			implements
				IASTClassReference {
		private IASTTypeSpecifier reference;
		/**
		 * @param i
		 * @param specifier
		 */
		public ASTClassReference(int i, IASTTypeSpecifier specifier) {
			super(i);
			reference = specifier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTReference#initialize(int)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate specifier) {
			super.initialize(o);
			reference = (IASTTypeSpecifier) specifier;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTReference#reset()
		 */
		public void reset() {
			super.resetOffset();
			reference = null;
		}
		/**
		 *  
		 */
		public ASTClassReference() {
			super(0);
			reference = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return (ISourceElementCallbackDelegate) reference;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptClassReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTClassReference#isResolved()
		 */
		public boolean isResolved() {
			return (reference instanceof IASTClassSpecifier);
		}
	}

	public static class ASTEnumerationReference extends ASTReference
			implements
				IASTEnumerationReference {
		private IASTEnumerationSpecifier referencedElement;
		/**
		 * @param offset
		 * @param specifier
		 */
		public ASTEnumerationReference(int offset,
				IASTEnumerationSpecifier specifier) {
			super(offset);
			referencedElement = specifier;
		}

		/**
		 * 
		 */
		public ASTEnumerationReference() {
			super( 0 );
			referencedElement = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedElement;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptEnumerationReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate re) {
			initialize(o);
			this.referencedElement = (IASTEnumerationSpecifier) re; 
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			super.resetOffset();
			this.referencedElement = null;
		}
	}

	public static class ASTEnumeratorReference extends ASTReference
			implements
				IASTEnumeratorReference {

		private IASTEnumerator enumerator;
		/**
		 * @param offset
		 * @param enumerator
		 */
		public ASTEnumeratorReference(int offset, IASTEnumerator enumerator) {
			super(offset);
			this.enumerator = enumerator;
		}

		/**
		 * 
		 */
		public ASTEnumeratorReference() {
			super( 0 );
			enumerator = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return enumerator;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptEnumeratorReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			super.initialize(o);
			this.enumerator = (IASTEnumerator) referencedElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			super.resetOffset();
			this.enumerator = null;
		}
	}
	public static class ASTFieldReference extends ASTReference
			implements
				IASTReference,
				IASTFieldReference {
		private IASTField referencedElement;
		/**
		 * @param offset
		 * @param field
		 */
		public ASTFieldReference(int offset, IASTField field) {
			super(offset);
			referencedElement = field;
		}
		/**
		 * 
		 */
		public ASTFieldReference() {
			super(0);
			referencedElement = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedElement;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptFieldReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate re) {
			initialize(o);
			this.referencedElement = (IASTField) re;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			resetOffset();
			this.referencedElement = null;
		}
	}
	
	public static class ASTFunctionReference extends ASTReference
			implements
				IASTReference,
				IASTFunctionReference {
		private IASTFunction declaration;
		/**
		 * @param offset
		 */
		public ASTFunctionReference(int offset,
				IASTFunction referencedDeclaration) {
			super(offset);
			this.declaration = referencedDeclaration;
		}
		/**
		 * 
		 */
		public ASTFunctionReference() {
			super(0);
			declaration = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return declaration;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptFunctionReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			super.initialize(o);
			this.declaration = (IASTFunction) referencedElement;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			resetOffset();
			this.declaration = null;			
		}
	}
	public static class ASTMethodReference extends ASTReference
			implements
				IASTMethodReference {
		private IASTMethod method;
		/**
		 * @param offset
		 */
		public ASTMethodReference(int offset, IASTMethod method) {
			super(offset);
			this.method = method;
		}
		/**
		 * 
		 */
		public ASTMethodReference() {
			super(0);
			this.method = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return method;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptMethodReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			super.initialize(o);
			this.method = (IASTMethod) referencedElement;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			resetOffset();
			this.method= null;
		}
	}
	public static class ASTNamespaceReference extends ASTReference
			implements
				IASTNamespaceReference {
		private IASTNamespaceDefinition reference;

		/**
		 * @param offset
		 * @param definition
		 */
		public ASTNamespaceReference(int offset,
				IASTNamespaceDefinition definition) {
			super(offset);
			reference = definition;
		}

		/**
		 * 
		 */
		public ASTNamespaceReference() {
			super(0);
			reference = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return reference;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptNamespaceReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			super.initialize(o);
			this.reference = (IASTNamespaceDefinition) referencedElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			resetOffset();
			this.reference = null;
		}
	}

	public static class ASTParameterReference extends ASTReference
			implements
				IASTParameterReference {
		private IASTParameterDeclaration parm;

		/**
		 * @param offset
		 * @param declaration
		 */
		public ASTParameterReference(int offset,
				IASTParameterDeclaration declaration) {
			super(offset);
			parm = declaration;
		}

		/**
		 * 
		 */
		public ASTParameterReference() {
			super(0);
			parm = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return parm;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptParameterReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			initialize(o);
			this.parm = (IASTParameterDeclaration) referencedElement; 
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			resetOffset();
			this.parm = null;
		}
	}
	public static class ASTTemplateParameterReference extends ASTReference
			implements
				IASTTemplateParameterReference {
		private IASTTemplateParameter parameter;
		/**
		 * @param offset
		 */
		public ASTTemplateParameterReference(int offset,
				IASTTemplateParameter param) {
			super(offset);
			parameter = param;
		}

		/**
		 * 
		 */
		public ASTTemplateParameterReference() {
			super(0);
			parameter = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return parameter;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptTemplateParameterReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			super.initialize(o);
			parameter = (IASTTemplateParameter) referencedElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			resetOffset();
			this.parameter = null;
		}
	}
	public static class ASTTypedefReference extends ASTReference
			implements
				IASTTypedefReference {
		private IASTTypedefDeclaration referencedItem;
		/**
		 * @param offset
		 */
		public ASTTypedefReference(int offset,
				IASTTypedefDeclaration referencedItem) {
			super(offset);
			this.referencedItem = referencedItem;
		}
		/**
		 * 
		 */
		public ASTTypedefReference() {
			super( 0 );
			this.referencedItem = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedItem;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptTypedefReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
			super.initialize(o);
			referencedItem = (IASTTypedefDeclaration) referencedElement;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			super.resetOffset();
			referencedItem = null;
		}
	}
	public static class ASTVariableReference extends ASTReference
			implements
				IASTReference,
				IASTVariableReference {

		private IASTVariable referencedElement;
		/**
		 * @param offset
		 * @param variable
		 */
		public ASTVariableReference(int offset, IASTVariable variable) {
			super(offset);
			referencedElement = variable;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate var ) {
			super.initialize(o);
			referencedElement = (IASTVariable) var;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			super.resetOffset();
			referencedElement = null;
		}
		/**
		 * 
		 */
		public ASTVariableReference() {
			super(0);
			referencedElement = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedElement;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptVariableReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	/**
	 * @return
	 */
	public boolean isBalanced() {
		return isBalanced( classReferencesAvailable ) && 
			isBalanced( variableReferencesAvailable ) && 
			isBalanced( fieldReferencesAvailable ) &&
			isBalanced( functionReferencesAvailable ) &&
			isBalanced( methodReferencesAvailable ) &&
			isBalanced( enumerationReferencesAvailable ) && 
			isBalanced( enumeratorReferencesAvailable ) &&
			isBalanced( parameterReferencesAvailable ) &&
			isBalanced( templateParameterReferencesAvailable ) &&
			isBalanced( typedefReferencesAvailable);
	}

	/**
	 * @param referencesAvailable 
	 * @return
	 */
	private boolean isBalanced(boolean[] referencesAvailable ) {
		for( int i = 0; i < referencesAvailable.length; ++i )
		{
			if( !referencesAvailable[i] ) 
				return false;
		}
		return true;
	}

}