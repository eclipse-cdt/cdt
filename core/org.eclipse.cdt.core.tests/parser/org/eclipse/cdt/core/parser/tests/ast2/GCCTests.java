/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 22, 2004
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser2.c.CVisitor;

/**
 * @author aniefer
 */
public class GCCTests extends AST2BaseTest {

    public void testGCC20000113() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "struct x {                           \n" ); //$NON-NLS-1$
        buffer.append( "   unsigned x1:1;                    \n" ); //$NON-NLS-1$
        buffer.append( "   unsigned x2:2;                    \n" ); //$NON-NLS-1$
        buffer.append( "   unsigned x3:3;                    \n" ); //$NON-NLS-1$
        buffer.append( "};                                   \n" ); //$NON-NLS-1$
        buffer.append( "foobar( int x, int y, int z ){       \n" ); //$NON-NLS-1$
        buffer.append( "   struct x a = {x, y, z};           \n" ); //$NON-NLS-1$
        buffer.append( "   struct x b = {x, y, z};           \n" ); //$NON-NLS-1$
        buffer.append( "   struct x *c = &b;                 \n" ); //$NON-NLS-1$
        buffer.append( "   c->x3 += ( a.x2 - a.x1) * c->x2;  \n" ); //$NON-NLS-1$
        buffer.append( "   if( a.x1 != 1 || c->x3 != 5 )     \n" ); //$NON-NLS-1$
        buffer.append( "      return -1;                     \n" ); //$NON-NLS-1$
        buffer.append( "   return 0;                         \n" ); //$NON-NLS-1$
        buffer.append( "}                                    \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 33 );
        ICompositeType x = (ICompositeType) collector.getName( 0 ).resolveBinding();
        IField    x1 = (IField)     collector.getName( 1 ).resolveBinding();
        IField    x2 = (IField)     collector.getName( 2 ).resolveBinding();
        IField    x3 = (IField)     collector.getName( 3 ).resolveBinding();
        IVariable vx = (IVariable)  collector.getName( 5 ).resolveBinding();
        IVariable vy = (IVariable)  collector.getName( 6 ).resolveBinding();
        IVariable vz = (IVariable)  collector.getName( 7 ).resolveBinding();
        IVariable a  = (IVariable)  collector.getName( 9 ).resolveBinding();
        IVariable b  = (IVariable)  collector.getName( 14 ).resolveBinding();
        IVariable c  = (IVariable)  collector.getName( 19 ).resolveBinding();
        
        assertInstances( collector, x, 4 );
        assertInstances( collector, x1, 3);
        assertInstances( collector, x2, 3);
        assertInstances( collector, x3, 3);
        assertInstances( collector, vx, 3);
        assertInstances( collector, vy, 3);
        assertInstances( collector, vz, 3);
        assertInstances( collector, a, 4);
        assertInstances( collector, b, 2);
        assertInstances( collector, c, 4);
    }
    
    public void testGCC20000205() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "static int f( int a ) {           \n"); //$NON-NLS-1$
        buffer.append( "   if( a == 0 )                   \n"); //$NON-NLS-1$
        buffer.append( "      return 0;                   \n"); //$NON-NLS-1$
        buffer.append( "   do                             \n"); //$NON-NLS-1$
        buffer.append( "      if( a & 128 )               \n"); //$NON-NLS-1$
        buffer.append( "         return 1;                \n"); //$NON-NLS-1$
        buffer.append( "   while( f(0) );                 \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                      \n"); //$NON-NLS-1$
        buffer.append( "}                                 \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 5 );
        IFunction f = (IFunction) collector.getName( 0 ).resolveBinding();
        IVariable a = (IVariable) collector.getName( 1 ).resolveBinding();
        
        assertInstances( collector, f, 2 );
        assertInstances( collector, a, 3 );
    }
    
    public void testGCC20000217() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "unsigned short int showbug( unsigned short int * a,    \n"); //$NON-NLS-1$
        buffer.append( "                            unsigned short int * b ) { \n"); //$NON-NLS-1$
        buffer.append( "   *a += *b - 8;                                       \n"); //$NON-NLS-1$
        buffer.append( "   return (*a >= 8 );                                  \n"); //$NON-NLS-1$
        buffer.append( "}                                                      \n"); //$NON-NLS-1$
        buffer.append( "int main(){                                            \n"); //$NON-NLS-1$
        buffer.append( "   unsigned short int x = 0;                           \n"); //$NON-NLS-1$
        buffer.append( "   unsigned short int y = 10;                          \n"); //$NON-NLS-1$
        buffer.append( "   if( showbug( &x, &y ) != 0 )                        \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                       \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                                           \n"); //$NON-NLS-1$
        buffer.append( "}                                                      \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 12 );
        
        IFunction showBug = (IFunction) collector.getName( 0 ).resolveBinding();
        IVariable a = (IVariable) collector.getName( 1 ).resolveBinding();
        IVariable b = (IVariable) collector.getName( 2 ).resolveBinding();
        IVariable x = (IVariable) collector.getName( 7 ).resolveBinding();
        IVariable y = (IVariable) collector.getName( 8 ).resolveBinding();
        
        assertInstances( collector, showBug, 2 );
        assertInstances( collector, a, 3 );
        assertInstances( collector, b, 2 );
        assertInstances( collector, x, 2 );
        assertInstances( collector, y, 2 );
    }
    
    public void testGCC20000224() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "int loop_1 = 100;                         \n"); //$NON-NLS-1$
        buffer.append( "int loop_2 = 7;                           \n"); //$NON-NLS-1$
        buffer.append( "int flag = 0;                             \n"); //$NON-NLS-1$
        buffer.append( "int test( void ) {                        \n"); //$NON-NLS-1$
        buffer.append( "   int i;                                 \n"); //$NON-NLS-1$
        buffer.append( "   int counter = 0;                       \n"); //$NON-NLS-1$
        buffer.append( "   while( loop_1 > counter ) {            \n"); //$NON-NLS-1$
        buffer.append( "      if( flag & 1 ) {                    \n"); //$NON-NLS-1$
        buffer.append( "         for( i = 0; i < loop_2; i++ ) {  \n"); //$NON-NLS-1$
        buffer.append( "            counter++;                    \n"); //$NON-NLS-1$
        buffer.append( "         }                                \n"); //$NON-NLS-1$
        buffer.append( "      }                                   \n"); //$NON-NLS-1$
        buffer.append( "      flag++;                             \n"); //$NON-NLS-1$
        buffer.append( "   }                                      \n"); //$NON-NLS-1$
        buffer.append( "   return 1;                              \n"); //$NON-NLS-1$
        buffer.append( "}                                         \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 16 );
        IVariable loop1 = (IVariable) collector.getName( 0 ).resolveBinding();
        IVariable loop2 = (IVariable) collector.getName( 1 ).resolveBinding();
        IVariable flag  = (IVariable) collector.getName( 2 ).resolveBinding();
        IVariable i  = (IVariable) collector.getName( 5 ).resolveBinding();
        IVariable counter  = (IVariable) collector.getName( 6 ).resolveBinding();
        
        assertInstances( collector, loop1, 2 );
        assertInstances( collector, loop2, 2 );
        assertInstances( collector, flag, 3 );
        assertInstances( collector, i, 4 );
        assertInstances( collector, counter, 3 );
    }
    
    public void testGCC20000225() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "int main() {                        \n"); //$NON-NLS-1$
		buffer.append( "   int nResult, b = 0, i = -1;      \n"); //$NON-NLS-1$
		buffer.append( "   do {                             \n"); //$NON-NLS-1$
		buffer.append( "      if( b != 0 ) {                \n"); //$NON-NLS-1$
		buffer.append( "         nResult = 1;               \n"); //$NON-NLS-1$
		buffer.append( "      } else {                      \n"); //$NON-NLS-1$
		buffer.append( "         nResult = 0;               \n"); //$NON-NLS-1$
		buffer.append( "      }                             \n"); //$NON-NLS-1$
		buffer.append( "      i++;                          \n"); //$NON-NLS-1$
		buffer.append( "      b = ( i + 2 ) * 4;            \n"); //$NON-NLS-1$
		buffer.append( "   } while ( i < 0 );               \n"); //$NON-NLS-1$
		buffer.append( "   return -1;                       \n"); //$NON-NLS-1$
		buffer.append( "}                                   \n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 11 );
        IVariable nResult = (IVariable) collector.getName( 1 ).resolveBinding();
        IVariable b = (IVariable) collector.getName( 2 ).resolveBinding();
        IVariable i  = (IVariable) collector.getName( 3 ).resolveBinding();
        
        assertInstances( collector, nResult, 3 );
        assertInstances( collector, b, 3 );
        assertInstances( collector, i, 4 );
    }
    
    public void testGCC20000227() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "static const unsigned char f[] = \"\\0\\377\";        \n"); //$NON-NLS-1$
        buffer.append( "static const unsigned char g[] = \"\\0y\";            \n"); //$NON-NLS-1$
        buffer.append( "int main() {                                          \n"); //$NON-NLS-1$
        buffer.append( "   if( sizeof f != 3 || sizeof g != 3 )               \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                      \n"); //$NON-NLS-1$
        buffer.append( "   if( f[0] != g[0] )                                 \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                      \n"); //$NON-NLS-1$
        buffer.append( "   if( f[1] != g[1] || f[2] != g[2] )                 \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                      \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                                          \n"); //$NON-NLS-1$
        buffer.append( "}                                                     \n"); //$NON-NLS-1$
        
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 11 );
        IVariable f = (IVariable) collector.getName( 0 ).resolveBinding();
        IVariable g = (IVariable) collector.getName( 1 ).resolveBinding();
        
        assertInstances( collector, f, 5 );
        assertInstances( collector, g, 5 );
    }
    
    public void testGCC20000313() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "unsigned int buggy( unsigned int *param ) {           \n"); //$NON-NLS-1$
        buffer.append( "   unsigned int accu, zero = 0, borrow;               \n"); //$NON-NLS-1$
        buffer.append( "   accu = - *param;                                   \n"); //$NON-NLS-1$
        buffer.append( "   borrow = - ( accu > zero );                        \n"); //$NON-NLS-1$
        buffer.append( "   return borrow;                                     \n"); //$NON-NLS-1$
        buffer.append( "}                                                     \n"); //$NON-NLS-1$
        buffer.append( "int main(void){                                       \n"); //$NON-NLS-1$
        buffer.append( "   unsigned int param = 1;                            \n"); //$NON-NLS-1$
        buffer.append( "   unsigned int borrow = buggy (&param);              \n"); //$NON-NLS-1$
        buffer.append( "   if( param != 0 )                                   \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                      \n"); //$NON-NLS-1$
        buffer.append( "   if( borrow +1 != 0 )                               \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                      \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                                          \n"); //$NON-NLS-1$
        buffer.append( "}                                                     \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 19 );
        IFunction buggy = (IFunction) collector.getName(0).resolveBinding();
        IParameter param = (IParameter) collector.getName(1).resolveBinding();
        IVariable accu = (IVariable) collector.getName( 2 ).resolveBinding();
        IVariable zero = (IVariable) collector.getName( 3 ).resolveBinding();
        IVariable borrow = (IVariable) collector.getName( 4 ).resolveBinding();
        IVariable param2 = (IVariable) collector.getName( 13 ).resolveBinding();
        IVariable borrow2 = (IVariable) collector.getName( 14 ).resolveBinding();
        
        assertInstances( collector, buggy, 2 );
        assertInstances( collector, param, 2 );
        assertInstances( collector, accu, 3 );
        assertInstances( collector, zero, 2 );
        assertInstances( collector, borrow, 3 );
        assertInstances( collector, param2, 3 );
        assertInstances( collector, borrow2, 2 );
    }
    
    public void testGCC20000314_1() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "int main() {                                       \n"); //$NON-NLS-1$
        buffer.append( "   long winds = 0;                                 \n"); //$NON-NLS-1$
        buffer.append( "   while( winds != 0 ) {                           \n"); //$NON-NLS-1$
        buffer.append( "      if( *(char*)winds )                          \n"); //$NON-NLS-1$
        buffer.append( "         break;                                    \n"); //$NON-NLS-1$
        buffer.append( "   }                                               \n"); //$NON-NLS-1$
        buffer.append( "   if( winds == 0 || winds != 0 || *(char*)winds ) \n"); //$NON-NLS-1$
        buffer.append( "      return 0;                                    \n"); //$NON-NLS-1$
        buffer.append( "   return -1;                                      \n"); //$NON-NLS-1$
        buffer.append( "}                                                  \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 7 );
        IVariable winds = (IVariable) collector.getName( 1 ).resolveBinding();

        assertInstances( collector, winds, 6 );
    }
    
    public void testGCC20000314_2() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "typedef unsigned long long uint64;                \n" ); //$NON-NLS-1$
        buffer.append( "const uint64 bigconst = 1ULL << 34;               \n" ); //$NON-NLS-1$
        buffer.append( "int a = 1;                                        \n" ); //$NON-NLS-1$
        buffer.append( "static uint64 getmask(void) {                     \n" ); //$NON-NLS-1$
        buffer.append( "   if(a)  return bigconst;                        \n" ); //$NON-NLS-1$
        buffer.append( "   else   return 0;                               \n" ); //$NON-NLS-1$
        buffer.append( "}                                                 \n" ); //$NON-NLS-1$
        buffer.append( "main(){                                           \n" ); //$NON-NLS-1$
        buffer.append( "   uint64 f = getmask();                          \n" ); //$NON-NLS-1$
        buffer.append( "   if( sizeof (long long) == 8 && f != bigconst ) \n" ); //$NON-NLS-1$
        buffer.append( "      return -1;                                  \n" ); //$NON-NLS-1$
        buffer.append( "   return 0;                                      \n" ); //$NON-NLS-1$
        buffer.append( "}                                                 \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 16 );
        ITypedef uint64 = (ITypedef) collector.getName( 0 ).resolveBinding();
        IVariable bigconst = (IVariable) collector.getName( 2 ).resolveBinding();
        IVariable a = (IVariable) collector.getName( 3 ).resolveBinding();
        IFunction getmask = (IFunction) collector.getName( 5 ).resolveBinding();
        IVariable f = (IVariable) collector.getName( 11 ).resolveBinding();

        assertInstances( collector, uint64, 4 );
        assertInstances( collector, bigconst, 3 );
        assertInstances( collector, a, 2 );
        assertInstances( collector, getmask, 2 );
        assertInstances( collector, f, 2 );
    }
}
