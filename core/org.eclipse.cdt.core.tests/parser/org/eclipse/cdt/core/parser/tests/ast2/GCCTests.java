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
    
    public void testGCC20000403() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "extern unsigned long aa[], bb[];                                     \n"); //$NON-NLS-1$
        buffer.append( "int seqgt( unsigned long a, unsigned short win, unsigned long b );   \n"); //$NON-NLS-1$
        buffer.append( "int seqgt2 ( unsigned long a, unsigned short win, unsigned long b ); \n"); //$NON-NLS-1$
        buffer.append( "main() {                                                             \n"); //$NON-NLS-1$
        buffer.append( "   if( !seqgt( *aa, 0x1000, *bb) || !seqgt2( *aa, 0x1000, *bb) )     \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                                                     \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                                                         \n"); //$NON-NLS-1$
        buffer.append( "}                                                                    \n"); //$NON-NLS-1$
        buffer.append( "int seqgt( unsigned long a, unsigned short win, unsigned long b) {   \n"); //$NON-NLS-1$
        buffer.append( "   return (long) ((a + win) - b) > 0;                                \n"); //$NON-NLS-1$
        buffer.append( "}                                                                    \n"); //$NON-NLS-1$
        buffer.append( "int seqgt2( unsigned long a, unsigned short win, unsigned long b) {  \n"); //$NON-NLS-1$
        buffer.append( "   long l = ((a + win) - b);                                         \n"); //$NON-NLS-1$
        buffer.append( "   return 1 > 0;                                                     \n"); //$NON-NLS-1$
        buffer.append( "}                                                                    \n"); //$NON-NLS-1$
        buffer.append( "unsigned long aa[] = { (1UL << (sizeof(long) *8 - 1)) = 0xfff };     \n"); //$NON-NLS-1$
        buffer.append( "unsigned long bb[] = { (1UL << (sizeof(long) *8 - 1)) = 0xfff };     \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 36 );
        IVariable aa = (IVariable) collector.getName( 0 ).resolveBinding();
        IVariable bb = (IVariable) collector.getName( 1 ).resolveBinding();
        IFunction seqgt = (IFunction) collector.getName( 2 ).resolveBinding();
        IParameter a1 = (IParameter) collector.getName( 3 ).resolveBinding();
        IParameter win1 = (IParameter) collector.getName( 4 ).resolveBinding();
        IParameter b1 = (IParameter) collector.getName( 5 ).resolveBinding();
        IFunction seqgt2 = (IFunction) collector.getName( 6 ).resolveBinding();
        IParameter a2 = (IParameter) collector.getName( 7 ).resolveBinding();
        IParameter win2 = (IParameter) collector.getName( 8 ).resolveBinding();
        IParameter b2 = (IParameter) collector.getName( 9 ).resolveBinding();
        
        assertInstances( collector, aa, 4 );
        assertInstances( collector, bb, 4 );
        assertInstances( collector, seqgt, 3 );
        assertInstances( collector, a1, 3 );
        assertInstances( collector, win1, 3 );
        assertInstances( collector, b1, 3 );
        assertInstances( collector, seqgt2, 3 );
        assertInstances( collector, a2, 3 );
        assertInstances( collector, win2, 3 );
        assertInstances( collector, b2, 3 );
    }
    
    public void testGCC20000412_1 () throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "short int i = -1;                                               \n" ); //$NON-NLS-1$
        buffer.append( "const char * const wordlist[207];                               \n" ); //$NON-NLS-1$
        buffer.append( "const char * const * foo( void ) {                              \n" ); //$NON-NLS-1$
        buffer.append( "   register const char * const *wordptr = &wordlist[207u + i];  \n" ); //$NON-NLS-1$
        buffer.append( "   return wordptr;                                              \n" ); //$NON-NLS-1$
        buffer.append( "}                                                               \n" ); //$NON-NLS-1$
        buffer.append( "int main() {                                                    \n" ); //$NON-NLS-1$
        buffer.append( "   if( foo() != &wordlist[206] )                                \n" ); //$NON-NLS-1$
        buffer.append( "      return -1;                                                \n" ); //$NON-NLS-1$
        buffer.append( "   return 0;                                                    \n" ); //$NON-NLS-1$
        buffer.append( "}                                                               \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 11 );
        IVariable i = (IVariable) collector.getName( 0 ).resolveBinding();
        IVariable wordlist = (IVariable) collector.getName( 1 ).resolveBinding();
        IFunction foo = (IFunction) collector.getName( 2 ).resolveBinding();
        IVariable wordptr = (IVariable) collector.getName( 4 ).resolveBinding();
        
        assertInstances( collector, i, 2 );
        assertInstances( collector, wordlist, 3 );
        assertInstances( collector, foo, 2 );
        assertInstances( collector, wordptr, 2 );
    }
    
    public void testGCC20000412_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "int f( int a, int *y ) {                 \n"); //$NON-NLS-1$
        buffer.append( "   int x = a;                            \n"); //$NON-NLS-1$
        buffer.append( "   if( a == 0 )  return *y;              \n"); //$NON-NLS-1$
        buffer.append( "   return f( a-1, &x );                  \n"); //$NON-NLS-1$
        buffer.append( "}                                        \n"); //$NON-NLS-1$
        buffer.append( "int main( int argc, char** argv){        \n"); //$NON-NLS-1$
        buffer.append( "   if( f(100, (int *) 0 ) != 1)          \n"); //$NON-NLS-1$
        buffer.append( "      return -1;                         \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                             \n"); //$NON-NLS-1$
        buffer.append( "}                                        \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 14 );
        IFunction f = (IFunction) collector.getName( 0 ).resolveBinding();
        IParameter a = (IParameter) collector.getName( 1 ).resolveBinding();
        IParameter y = (IParameter) collector.getName( 2 ).resolveBinding();
        IVariable x = (IVariable) collector.getName( 3 ).resolveBinding();
        
        assertInstances( collector, f, 3 );
        assertInstances( collector, a, 4 );
        assertInstances( collector, y, 2 );
        assertInstances( collector, x, 2 );
    }
    
    public void testGCC20000412_3() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "typedef struct {                         \n"); //$NON-NLS-1$
        buffer.append( "   char y;                               \n"); //$NON-NLS-1$
        buffer.append( "   char x[32];                           \n"); //$NON-NLS-1$
        buffer.append( "} X;                                     \n"); //$NON-NLS-1$
        buffer.append( "int z(void) {                            \n"); //$NON-NLS-1$
        buffer.append( "   X xxx;                                \n"); //$NON-NLS-1$
        buffer.append( "   xxx.x[0] = xxx.x[31] = '0';           \n"); //$NON-NLS-1$
        buffer.append( "   xxx.y = 0xf;                          \n"); //$NON-NLS-1$
        buffer.append( "   return f( xxx, xxx );                 \n"); //$NON-NLS-1$
        buffer.append( "}                                        \n"); //$NON-NLS-1$
        buffer.append( "int main (void) {                        \n"); //$NON-NLS-1$
        buffer.append( "   int val;                              \n"); //$NON-NLS-1$
        buffer.append( "   val = z();                            \n"); //$NON-NLS-1$
        buffer.append( "   if( val != 0x60 ) return -1;          \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                             \n"); //$NON-NLS-1$
        buffer.append( "}                                        \n"); //$NON-NLS-1$
        buffer.append( "int f( X x, X y ) {                      \n"); //$NON-NLS-1$
        buffer.append( "   if( x.y != y.y )                      \n"); //$NON-NLS-1$
        buffer.append( "      return 'F';                        \n"); //$NON-NLS-1$
        buffer.append( "   return x.x[0] + y.x[0];               \n"); //$NON-NLS-1$
        buffer.append( "}                                        \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 36 );
        IField    y = (IField) collector.getName( 1 ).resolveBinding();
        IField    x = (IField) collector.getName( 2 ).resolveBinding();
        ITypedef  X = (ITypedef) collector.getName( 3 ).resolveBinding();
        IFunction z = (IFunction) collector.getName( 4 ).resolveBinding();
        IVariable xxx = (IVariable) collector.getName( 7 ).resolveBinding();
        IVariable val = (IVariable) collector.getName( 19 ).resolveBinding();
        IParameter px = (IParameter) collector.getName( 25 ).resolveBinding();
        IParameter py = (IParameter) collector.getName( 27 ).resolveBinding();
        
        assertInstances( collector, y, 4 );
        assertInstances( collector, x, 5 );
        assertInstances( collector, X, 4 );
        assertInstances( collector, z, 2 );
        assertInstances( collector, xxx, 6 );
        assertInstances( collector, val, 3 );
        assertInstances( collector, px, 3 );
        assertInstances( collector, py, 3 );
    }
    
    public void testGCC20000412_4() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "void f(int i, int j, int radius, int width, int N) { \n"); //$NON-NLS-1$
        buffer.append( "   const int diff = i - radius;                      \n"); //$NON-NLS-1$
        buffer.append( "   const int lowk = (diff > 0 ? diff : 0 );          \n"); //$NON-NLS-1$
        buffer.append( "   int k;                                            \n"); //$NON-NLS-1$
        buffer.append( "   for( k = lowk; k <= 2; k++ ){                     \n"); //$NON-NLS-1$
        buffer.append( "      int idx = ((k-i+radius) * width - j + radius); \n"); //$NON-NLS-1$
        buffer.append( "      if( idx < 0 ) return -1;                       \n"); //$NON-NLS-1$
        buffer.append( "   }                                                 \n"); //$NON-NLS-1$
        buffer.append( "   for( k = lowk; k <= 2; k++ ) ;                    \n"); //$NON-NLS-1$
        buffer.append( "}                                                    \n"); //$NON-NLS-1$
        buffer.append( "int main (int argc, char** argv ){                   \n"); //$NON-NLS-1$
        buffer.append( "   int exc_rad = 2;                                  \n"); //$NON-NLS-1$
        buffer.append( "   int N = 8;                                        \n"); //$NON-NLS-1$
        buffer.append( "   int i;                                            \n"); //$NON-NLS-1$
        buffer.append( "   for( i = 1; i < 4; i++ )                          \n"); //$NON-NLS-1$
        buffer.append( "      f( i, 1, exc_rad, 2*exc_rad + 1, N );          \n"); //$NON-NLS-1$
        buffer.append( "   return 0;                                         \n"); //$NON-NLS-1$
        buffer.append( "}                                                    \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 43 );
        IFunction f = (IFunction) collector.getName( 0 ).resolveBinding();
        IParameter i1 = (IParameter) collector.getName( 1 ).resolveBinding();
        IParameter j = (IParameter) collector.getName( 2 ).resolveBinding();
        IParameter radius = (IParameter) collector.getName( 3 ).resolveBinding();
        IParameter width = (IParameter) collector.getName( 4 ).resolveBinding();
        IParameter N1 = (IParameter) collector.getName( 5 ).resolveBinding();
        IVariable diff = (IVariable) collector.getName( 6 ).resolveBinding();
        IVariable lowk = (IVariable) collector.getName( 9 ).resolveBinding();
        IVariable k = (IVariable) collector.getName( 12 ).resolveBinding();
        IVariable idx = (IVariable) collector.getName( 17 ).resolveBinding();
        IVariable exc_rad = (IVariable) collector.getName( 32 ).resolveBinding();
        IVariable N2 = (IVariable) collector.getName( 33 ).resolveBinding();
        IVariable i2 = (IVariable) collector.getName( 34 ).resolveBinding();

        assertInstances( collector, f, 2 );
        assertInstances( collector, i1, 3 );
        assertInstances( collector, j, 2 );
        assertInstances( collector, radius, 4 );
        assertInstances( collector, width, 2 );
        assertInstances( collector, N1, 1 );
        assertInstances( collector, diff, 3 );
        assertInstances( collector, lowk, 3 );
        assertInstances( collector, k, 8 );
        assertInstances( collector, idx, 2 );
        assertInstances( collector, exc_rad, 3 );
        assertInstances( collector, N2, 2 );
        assertInstances( collector, i2, 5 );
    }
}
