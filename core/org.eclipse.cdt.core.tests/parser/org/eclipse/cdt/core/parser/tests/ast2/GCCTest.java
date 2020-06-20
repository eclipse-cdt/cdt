/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author aniefer
 */
public class GCCTests extends AST2TestBase {

	public GCCTests() {
	}

	public GCCTests(String name) {
		super(name);
	}

	public void testGCC20000113() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct x {                           \n");
		buffer.append("   unsigned x1:1;                    \n");
		buffer.append("   unsigned x2:2;                    \n");
		buffer.append("   unsigned x3:3;                    \n");
		buffer.append("};                                   \n");
		buffer.append("foobar(int x, int y, int z) {        \n");
		buffer.append("   struct x a = {x, y, z};           \n");
		buffer.append("   struct x b = {x, y, z};           \n");
		buffer.append("   struct x *c = &b;                 \n");
		buffer.append("   c->x3 += (a.x2 - a.x1) * c->x2;   \n");
		buffer.append("   if (a.x1 != 1 || c->x3 != 5)      \n");
		buffer.append("      return -1;                     \n");
		buffer.append("   return 0;                         \n");
		buffer.append("}                                    \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 33);
		ICompositeType x = (ICompositeType) collector.getName(0).resolveBinding();
		IField x1 = (IField) collector.getName(1).resolveBinding();
		IField x2 = (IField) collector.getName(2).resolveBinding();
		IField x3 = (IField) collector.getName(3).resolveBinding();
		IVariable vx = (IVariable) collector.getName(5).resolveBinding();
		IVariable vy = (IVariable) collector.getName(6).resolveBinding();
		IVariable vz = (IVariable) collector.getName(7).resolveBinding();
		IVariable a = (IVariable) collector.getName(9).resolveBinding();
		IVariable b = (IVariable) collector.getName(14).resolveBinding();
		IVariable c = (IVariable) collector.getName(19).resolveBinding();

		assertInstances(collector, x, 4);
		assertInstances(collector, x1, 3);
		assertInstances(collector, x2, 3);
		assertInstances(collector, x3, 3);
		assertInstances(collector, vx, 3);
		assertInstances(collector, vy, 3);
		assertInstances(collector, vz, 3);
		assertInstances(collector, a, 4);
		assertInstances(collector, b, 2);
		assertInstances(collector, c, 4);
	}

	public void testGCC20000205() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("static int f(int a) {             \n");
		buffer.append("   if (a == 0)                    \n");
		buffer.append("      return 0;                   \n");
		buffer.append("   do                             \n");
		buffer.append("      if (a & 128)                \n");
		buffer.append("         return 1;                \n");
		buffer.append("   while (f(0));                  \n");
		buffer.append("   return 0;                      \n");
		buffer.append("}                                 \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 5);
		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IVariable a = (IVariable) collector.getName(1).resolveBinding();

		assertInstances(collector, f, 2);
		assertInstances(collector, a, 3);
	}

	public void testGCC20000217() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("unsigned short int showbug(unsigned short int * a,    \n");
		buffer.append("                            unsigned short int * b) { \n");
		buffer.append("   *a += *b - 8;                                      \n");
		buffer.append("   return (*a >= 8);                                  \n");
		buffer.append("}                                                     \n");
		buffer.append("int main() {                                          \n");
		buffer.append("   unsigned short int x = 0;                          \n");
		buffer.append("   unsigned short int y = 10;                         \n");
		buffer.append("   if (showbug(&x, &y) != 0)                          \n");
		buffer.append("      return -1;                                      \n");
		buffer.append("   return 0;                                          \n");
		buffer.append("}                                                     \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 12);

		IFunction showBug = (IFunction) collector.getName(0).resolveBinding();
		IVariable a = (IVariable) collector.getName(1).resolveBinding();
		IVariable b = (IVariable) collector.getName(2).resolveBinding();
		IVariable x = (IVariable) collector.getName(7).resolveBinding();
		IVariable y = (IVariable) collector.getName(8).resolveBinding();

		assertInstances(collector, showBug, 2);
		assertInstances(collector, a, 3);
		assertInstances(collector, b, 2);
		assertInstances(collector, x, 2);
		assertInstances(collector, y, 2);
	}

	public void testGCC20000224() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int loop_1 = 100;                         \n");
		buffer.append("int loop_2 = 7;                           \n");
		buffer.append("int flag = 0;                             \n");
		buffer.append("int test(void) {                          \n");
		buffer.append("   int i;                                 \n");
		buffer.append("   int counter = 0;                       \n");
		buffer.append("   while (loop_1 > counter) {             \n");
		buffer.append("      if (flag & 1) {                     \n");
		buffer.append("         for (i = 0; i < loop_2; i++) {   \n");
		buffer.append("            counter++;                    \n");
		buffer.append("         }                                \n");
		buffer.append("      }                                   \n");
		buffer.append("      flag++;                             \n");
		buffer.append("   }                                      \n");
		buffer.append("   return 1;                              \n");
		buffer.append("}                                         \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 16);
		IVariable loop1 = (IVariable) collector.getName(0).resolveBinding();
		IVariable loop2 = (IVariable) collector.getName(1).resolveBinding();
		IVariable flag = (IVariable) collector.getName(2).resolveBinding();
		IVariable i = (IVariable) collector.getName(5).resolveBinding();
		IVariable counter = (IVariable) collector.getName(6).resolveBinding();

		assertInstances(collector, loop1, 2);
		assertInstances(collector, loop2, 2);
		assertInstances(collector, flag, 3);
		assertInstances(collector, i, 4);
		assertInstances(collector, counter, 3);
	}

	public void testGCC20000225() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int main() {                        \n");
		buffer.append("   int nResult, b = 0, i = -1;      \n");
		buffer.append("   do {                             \n");
		buffer.append("      if (b != 0) {                 \n");
		buffer.append("         nResult = 1;               \n");
		buffer.append("      } else {                      \n");
		buffer.append("         nResult = 0;               \n");
		buffer.append("      }                             \n");
		buffer.append("      i++;                          \n");
		buffer.append("      b = (i + 2) * 4;              \n");
		buffer.append("   } while (i < 0);                 \n");
		buffer.append("   return -1;                       \n");
		buffer.append("}                                   \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 11);
		IVariable nResult = (IVariable) collector.getName(1).resolveBinding();
		IVariable b = (IVariable) collector.getName(2).resolveBinding();
		IVariable i = (IVariable) collector.getName(3).resolveBinding();

		assertInstances(collector, nResult, 3);
		assertInstances(collector, b, 3);
		assertInstances(collector, i, 4);
	}

	public void testGCC20000227() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("static const unsigned char f[] = \"\\0\\377\";        \n");
		buffer.append("static const unsigned char g[] = \"\\0y\";            \n");
		buffer.append("int main() {                                          \n");
		buffer.append("   if (sizeof f != 3 || sizeof g != 3)                \n");
		buffer.append("      return -1;                                      \n");
		buffer.append("   if (f[0] != g[0])                                  \n");
		buffer.append("      return -1;                                      \n");
		buffer.append("   if (f[1] != g[1] || f[2] != g[2])                  \n");
		buffer.append("      return -1;                                      \n");
		buffer.append("   return 0;                                          \n");
		buffer.append("}                                                     \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 11);
		IVariable f = (IVariable) collector.getName(0).resolveBinding();
		IVariable g = (IVariable) collector.getName(1).resolveBinding();

		assertInstances(collector, f, 5);
		assertInstances(collector, g, 5);
	}

	public void testGCC20000313() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("unsigned int buggy(unsigned int *param) {             \n");
		buffer.append("   unsigned int accu, zero = 0, borrow;               \n");
		buffer.append("   accu = - *param;                                   \n");
		buffer.append("   borrow = - (accu > zero);                          \n");
		buffer.append("   return borrow;                                     \n");
		buffer.append("}                                                     \n");
		buffer.append("int main(void) {                                      \n");
		buffer.append("   unsigned int param = 1;                            \n");
		buffer.append("   unsigned int borrow = buggy (&param);              \n");
		buffer.append("   if (param != 0)                                    \n");
		buffer.append("      return -1;                                      \n");
		buffer.append("   if (borrow +1 != 0)                                \n");
		buffer.append("      return -1;                                      \n");
		buffer.append("   return 0;                                          \n");
		buffer.append("}                                                     \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 19);
		IFunction buggy = (IFunction) collector.getName(0).resolveBinding();
		IParameter param = (IParameter) collector.getName(1).resolveBinding();
		IVariable accu = (IVariable) collector.getName(2).resolveBinding();
		IVariable zero = (IVariable) collector.getName(3).resolveBinding();
		IVariable borrow = (IVariable) collector.getName(4).resolveBinding();
		IVariable param2 = (IVariable) collector.getName(13).resolveBinding();
		IVariable borrow2 = (IVariable) collector.getName(14).resolveBinding();

		assertInstances(collector, buggy, 2);
		assertInstances(collector, param, 2);
		assertInstances(collector, accu, 3);
		assertInstances(collector, zero, 2);
		assertInstances(collector, borrow, 3);
		assertInstances(collector, param2, 3);
		assertInstances(collector, borrow2, 2);
	}

	public void testGCC20000314_1() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int main() {                                       \n");
		buffer.append("   long winds = 0;                                 \n");
		buffer.append("   while (winds != 0) {                            \n");
		buffer.append("      if (*(char*)winds)                           \n");
		buffer.append("         break;                                    \n");
		buffer.append("   }                                               \n");
		buffer.append("   if (winds == 0 || winds != 0 || *(char*) winds) \n");
		buffer.append("      return 0;                                    \n");
		buffer.append("   return -1;                                      \n");
		buffer.append("}                                                  \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 7);
		IVariable winds = (IVariable) collector.getName(1).resolveBinding();

		assertInstances(collector, winds, 6);
	}

	public void testGCC20000314_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("typedef unsigned long long uint64;                \n");
		buffer.append("const uint64 bigconst = 1ULL << 34;               \n");
		buffer.append("int a = 1;                                        \n");
		buffer.append("static uint64 getmask(void) {                     \n");
		buffer.append("   if (a)  return bigconst;                       \n");
		buffer.append("   else   return 0;                               \n");
		buffer.append("}                                                 \n");
		buffer.append("main() {                                          \n");
		buffer.append("   uint64 f = getmask();                          \n");
		buffer.append("   if (sizeof (long long) == 8 && f != bigconst)  \n");
		buffer.append("      return -1;                                  \n");
		buffer.append("   return 0;                                      \n");
		buffer.append("}                                                 \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 15);
		ITypedef uint64 = (ITypedef) collector.getName(0).resolveBinding();
		IVariable bigconst = (IVariable) collector.getName(2).resolveBinding();
		IVariable a = (IVariable) collector.getName(3).resolveBinding();
		IFunction getmask = (IFunction) collector.getName(5).resolveBinding();
		IVariable f = (IVariable) collector.getName(11).resolveBinding();

		assertInstances(collector, uint64, 4);
		assertInstances(collector, bigconst, 3);
		assertInstances(collector, a, 2);
		assertInstances(collector, getmask, 2);
		assertInstances(collector, f, 2);
	}

	public void testGCC20000403() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("extern unsigned long aa[], bb[];                                   \n");
		buffer.append("int seqgt(unsigned long a, unsigned short win, unsigned long b);   \n");
		buffer.append("int seqgt2 (unsigned long a, unsigned short win, unsigned long b); \n");
		buffer.append("main() {                                                           \n");
		buffer.append("   if (!seqgt(*aa, 0x1000, *bb) || !seqgt2(*aa, 0x1000, *bb))      \n");
		buffer.append("      return -1;                                                   \n");
		buffer.append("   return 0;                                                       \n");
		buffer.append("}                                                                  \n");
		buffer.append("int seqgt(unsigned long a, unsigned short win, unsigned long b) {  \n");
		buffer.append("   return (long) ((a + win) - b) > 0;                              \n");
		buffer.append("}                                                                  \n");
		buffer.append("int seqgt2(unsigned long a, unsigned short win, unsigned long b) { \n");
		buffer.append("   long l = ((a + win) - b);                                       \n");
		buffer.append("   return 1 > 0;                                                   \n");
		buffer.append("}                                                                  \n");
		buffer.append("unsigned long aa[] = { (1UL << (sizeof(long) *8 - 1)) = 0xfff };   \n");
		buffer.append("unsigned long bb[] = { (1UL << (sizeof(long) *8 - 1)) = 0xfff };   \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 34);
		IVariable aa = (IVariable) collector.getName(0).resolveBinding();
		IVariable bb = (IVariable) collector.getName(1).resolveBinding();
		IFunction seqgt = (IFunction) collector.getName(2).resolveBinding();
		IParameter a1 = (IParameter) collector.getName(3).resolveBinding();
		IParameter win1 = (IParameter) collector.getName(4).resolveBinding();
		IParameter b1 = (IParameter) collector.getName(5).resolveBinding();
		IFunction seqgt2 = (IFunction) collector.getName(6).resolveBinding();
		IParameter a2 = (IParameter) collector.getName(7).resolveBinding();
		IParameter win2 = (IParameter) collector.getName(8).resolveBinding();
		IParameter b2 = (IParameter) collector.getName(9).resolveBinding();

		assertInstances(collector, aa, 4);
		assertInstances(collector, bb, 4);
		assertInstances(collector, seqgt, 3);
		assertInstances(collector, a1, 3);
		assertInstances(collector, win1, 3);
		assertInstances(collector, b1, 3);
		assertInstances(collector, seqgt2, 3);
		assertInstances(collector, a2, 3);
		assertInstances(collector, win2, 3);
		assertInstances(collector, b2, 3);
	}

	public void testGCC20000412_1() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("short int i = -1;                                               \n");
		buffer.append("const char * const wordlist[207];                               \n");
		buffer.append("const char * const * foo(void) {                                \n");
		buffer.append("   register const char * const *wordptr = &wordlist[207u + i];  \n");
		buffer.append("   return wordptr;                                              \n");
		buffer.append("}                                                               \n");
		buffer.append("int main() {                                                    \n");
		buffer.append("   if (foo() != &wordlist[206])                                 \n");
		buffer.append("      return -1;                                                \n");
		buffer.append("   return 0;                                                    \n");
		buffer.append("}                                                               \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 11);
		IVariable i = (IVariable) collector.getName(0).resolveBinding();
		IVariable wordlist = (IVariable) collector.getName(1).resolveBinding();
		IFunction foo = (IFunction) collector.getName(2).resolveBinding();
		IVariable wordptr = (IVariable) collector.getName(4).resolveBinding();

		assertInstances(collector, i, 2);
		assertInstances(collector, wordlist, 3);
		assertInstances(collector, foo, 2);
		assertInstances(collector, wordptr, 2);
	}

	public void testGCC20000412_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int f(int a, int *y) {                   \n");
		buffer.append("   int x = a;                            \n");
		buffer.append("   if (a == 0)  return *y;               \n");
		buffer.append("   return f(a-1, &x);                    \n");
		buffer.append("}                                        \n");
		buffer.append("int main(int argc, char** argv) {        \n");
		buffer.append("   if (f(100, (int *) 0) != 1)           \n");
		buffer.append("      return -1;                         \n");
		buffer.append("   return 0;                             \n");
		buffer.append("}                                        \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 14);
		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IParameter a = (IParameter) collector.getName(1).resolveBinding();
		IParameter y = (IParameter) collector.getName(2).resolveBinding();
		IVariable x = (IVariable) collector.getName(3).resolveBinding();

		assertInstances(collector, f, 3);
		assertInstances(collector, a, 4);
		assertInstances(collector, y, 2);
		assertInstances(collector, x, 2);
	}

	public void testGCC20000412_3() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("typedef struct {                         \n");
		buffer.append("   char y;                               \n");
		buffer.append("   char x[32];                           \n");
		buffer.append("} X;                                     \n");
		buffer.append("int z(void) {                            \n");
		buffer.append("   X xxx;                                \n");
		buffer.append("   xxx.x[0] = xxx.x[31] = '0';           \n");
		buffer.append("   xxx.y = 0xf;                          \n");
		buffer.append("   return f(xxx, xxx);                   \n");
		buffer.append("}                                        \n");
		buffer.append("int main (void) {                        \n");
		buffer.append("   int val;                              \n");
		buffer.append("   val = z();                            \n");
		buffer.append("   if (val != 0x60) return -1;           \n");
		buffer.append("   return 0;                             \n");
		buffer.append("}                                        \n");
		buffer.append("int f(X x, X y) {                        \n");
		buffer.append("   if (x.y != y.y)                       \n");
		buffer.append("      return 'F';                        \n");
		buffer.append("   return x.x[0] + y.x[0];               \n");
		buffer.append("}                                        \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 36);
		IField y = (IField) collector.getName(1).resolveBinding();
		IField x = (IField) collector.getName(2).resolveBinding();
		ITypedef X = (ITypedef) collector.getName(3).resolveBinding();
		IFunction z = (IFunction) collector.getName(4).resolveBinding();
		IVariable xxx = (IVariable) collector.getName(7).resolveBinding();
		IVariable val = (IVariable) collector.getName(19).resolveBinding();
		IParameter px = (IParameter) collector.getName(25).resolveBinding();
		IParameter py = (IParameter) collector.getName(27).resolveBinding();

		assertInstances(collector, y, 4);
		assertInstances(collector, x, 5);
		assertInstances(collector, X, 4);
		assertInstances(collector, z, 2);
		assertInstances(collector, xxx, 6);
		assertInstances(collector, val, 3);
		assertInstances(collector, px, 3);
		assertInstances(collector, py, 3);
	}

	public void testGCC20000412_4() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("void f(int i, int j, int radius, int width, int N) { \n");
		buffer.append("   const int diff = i - radius;                      \n");
		buffer.append("   const int lowk = (diff > 0 ? diff : 0);           \n");
		buffer.append("   int k;                                            \n");
		buffer.append("   for (k = lowk; k <= 2; k++) {                     \n");
		buffer.append("      int idx = ((k-i+radius) * width - j + radius); \n");
		buffer.append("      if (idx < 0) return -1;                        \n");
		buffer.append("   }                                                 \n");
		buffer.append("   for (k = lowk; k <= 2; k++) ;                     \n");
		buffer.append("}                                                    \n");
		buffer.append("int main (int argc, char** argv) {                   \n");
		buffer.append("   int exc_rad = 2;                                  \n");
		buffer.append("   int N = 8;                                        \n");
		buffer.append("   int i;                                            \n");
		buffer.append("   for (i = 1; i < 4; i++)                           \n");
		buffer.append("      f(i, 1, exc_rad, 2*exc_rad + 1, N);            \n");
		buffer.append("   return 0;                                         \n");
		buffer.append("}                                                    \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 43);
		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IParameter i1 = (IParameter) collector.getName(1).resolveBinding();
		IParameter j = (IParameter) collector.getName(2).resolveBinding();
		IParameter radius = (IParameter) collector.getName(3).resolveBinding();
		IParameter width = (IParameter) collector.getName(4).resolveBinding();
		IParameter N1 = (IParameter) collector.getName(5).resolveBinding();
		IVariable diff = (IVariable) collector.getName(6).resolveBinding();
		IVariable lowk = (IVariable) collector.getName(9).resolveBinding();
		IVariable k = (IVariable) collector.getName(12).resolveBinding();
		IVariable idx = (IVariable) collector.getName(17).resolveBinding();
		IVariable exc_rad = (IVariable) collector.getName(32).resolveBinding();
		IVariable N2 = (IVariable) collector.getName(33).resolveBinding();
		IVariable i2 = (IVariable) collector.getName(34).resolveBinding();

		assertInstances(collector, f, 2);
		assertInstances(collector, i1, 3);
		assertInstances(collector, j, 2);
		assertInstances(collector, radius, 4);
		assertInstances(collector, width, 2);
		assertInstances(collector, N1, 1);
		assertInstances(collector, diff, 3);
		assertInstances(collector, lowk, 3);
		assertInstances(collector, k, 8);
		assertInstances(collector, idx, 2);
		assertInstances(collector, exc_rad, 3);
		assertInstances(collector, N2, 2);
		assertInstances(collector, i2, 5);
	}

	public void testGCC20000412_5() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int main(void) {                                        \n");
		buffer.append("   struct {                                             \n");
		buffer.append("      int node;                                         \n");
		buffer.append("      int type;                                         \n");
		buffer.append("   } lastglob[1] = { { 0, 1 } };                        \n");
		buffer.append("   if (lastglob[0].node != 0 || lastglob[0].type != 1)  \n");
		buffer.append("      return -1;                                        \n");
		buffer.append("   return 0;                                            \n");
		buffer.append("}                                                       \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 10);
		IField node = (IField) collector.getName(3).resolveBinding();
		IField type = (IField) collector.getName(4).resolveBinding();
		IVariable lastglob = (IVariable) collector.getName(5).resolveBinding();

		assertInstances(collector, node, 2);
		assertInstances(collector, type, 2);
		assertInstances(collector, lastglob, 3);
	}

	public void testGCC20000419() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct foo { int a, b, c; };                          \n");
		buffer.append("void brother(int a, int b, int c) {                   \n");
		buffer.append("   if (a) return;                                     \n");
		buffer.append("}                                                     \n");
		buffer.append("void sister(struct foo f, int b, int c) {             \n");
		buffer.append("   brother((f.b == b), b, c);                         \n");
		buffer.append("}                                                     \n");
		buffer.append("int main() {                                          \n");
		buffer.append("   struct foo f = { 7, 8, 9 };                        \n");
		buffer.append("   sister(f, 1, 2);                                   \n");
		buffer.append("   return 0;                                          \n");
		buffer.append("}                                                     \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 25);
		ICompositeType foo = (ICompositeType) collector.getName(0).resolveBinding();
		IField fa = (IField) collector.getName(1).resolveBinding();
		IField fb = (IField) collector.getName(2).resolveBinding();
		IField fc = (IField) collector.getName(3).resolveBinding();
		IFunction brother = (IFunction) collector.getName(4).resolveBinding();
		IParameter pa = (IParameter) collector.getName(5).resolveBinding();
		IParameter pb = (IParameter) collector.getName(6).resolveBinding();
		IParameter pc = (IParameter) collector.getName(7).resolveBinding();
		IFunction sister = (IFunction) collector.getName(9).resolveBinding();
		IParameter sf = (IParameter) collector.getName(11).resolveBinding();
		IParameter sb = (IParameter) collector.getName(12).resolveBinding();
		IParameter sc = (IParameter) collector.getName(13).resolveBinding();
		IVariable f = (IVariable) collector.getName(22).resolveBinding();

		assertInstances(collector, foo, 3);
		assertInstances(collector, fa, 1);
		assertInstances(collector, fb, 2);
		assertInstances(collector, fc, 1);
		assertInstances(collector, brother, 2);
		assertInstances(collector, pa, 2);
		assertInstances(collector, pb, 1);
		assertInstances(collector, pc, 1);
		assertInstances(collector, sister, 2);
		assertInstances(collector, sf, 2);
		assertInstances(collector, sb, 3);
		assertInstances(collector, sc, 2);
		assertInstances(collector, f, 2);
	}

	public void testGCC20000503() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("unsigned long sub(int a) {                          \n");
		buffer.append("   return ((0 > a - 2) ? 0 : a - 2) * sizeof(long); \n");
		buffer.append("}                                                   \n");
		buffer.append("main() {                                            \n");
		buffer.append("   return (sub(0) != 0);                            \n");
		buffer.append("}                                                   \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 6);
		IFunction sub = (IFunction) collector.getName(0).resolveBinding();
		IParameter a = (IParameter) collector.getName(1).resolveBinding();

		assertInstances(collector, sub, 2);
		assertInstances(collector, a, 3);
	}

	public void testGCC20000511() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int f(int value, int expect) {                      \n");
		buffer.append("   return (value == expect);                        \n");
		buffer.append("}                                                   \n");
		buffer.append("main() {                                            \n");
		buffer.append("   int a = 7, b = 6, c = 4, d = 7, e = 2;           \n");
		buffer.append("   f(a || b % c, 1);                                \n");
		buffer.append("   f(a ? b % c : 0, 2);                             \n");
		buffer.append("   f(a = b % c, 2);                                 \n");
		buffer.append("   f(a *= b % c, 4);                                \n");
		buffer.append("   f(a /= b % c, 2);                                \n");
		buffer.append("   f(a %= b % c, 0);                                \n");
		buffer.append("   f(a += b % c, 2);                                \n");
		buffer.append("   f(d || c && e, 1);                               \n");
		buffer.append("   f(d ? c && e : 0, 1);                            \n");
		buffer.append("   f(d = c && e, 1);                                \n");
		buffer.append("   f(d *= c && e, 1);                               \n");
		buffer.append("   f(d %= c && e, 0);                               \n");
		buffer.append("   f(d += c && e, 1);                               \n");
		buffer.append("   f(d -= c && e, 0);                               \n");
		buffer.append("   f(d || c || e, 1);                               \n");
		buffer.append("   f(d ? c || e : 0, 0);                            \n");
		buffer.append("   f(d = c || e, 1);                                \n");
		buffer.append("   f(d *= c || e, 1);                               \n");
		buffer.append("   f(d %= c || e, 0);                               \n");
		buffer.append("   f(d += c || e, 1);                               \n");
		buffer.append("   f(d -= c || e, 0);                               \n");
		buffer.append("}                                                   \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 95);
		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IParameter v = (IParameter) collector.getName(1).resolveBinding();
		IParameter ex = (IParameter) collector.getName(2).resolveBinding();
		IVariable a = (IVariable) collector.getName(6).resolveBinding();
		IVariable b = (IVariable) collector.getName(7).resolveBinding();
		IVariable c = (IVariable) collector.getName(8).resolveBinding();
		IVariable d = (IVariable) collector.getName(9).resolveBinding();
		IVariable e = (IVariable) collector.getName(10).resolveBinding();

		assertInstances(collector, f, 22);
		assertInstances(collector, v, 2);
		assertInstances(collector, ex, 2);
		assertInstances(collector, a, 8);
		assertInstances(collector, b, 8);
		assertInstances(collector, c, 22);
		assertInstances(collector, d, 15);
		assertInstances(collector, e, 15);
	}

	public void testGCC20000603() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct s1 { double d; };                                \n");
		buffer.append("struct s2 { double d; };                                \n");
		buffer.append("double f(struct s1 * a, struct s2 *b) {                 \n");
		buffer.append("   a->d = 1.0;                                          \n");
		buffer.append("   return b->d + 1.0;                                   \n");
		buffer.append("}                                                       \n");
		buffer.append("int main() {                                            \n");
		buffer.append("   struct s1 a;                                         \n");
		buffer.append("   a.d = 0.0;                                           \n");
		buffer.append("   if (f(&a, (struct s2 *)&a) != 2.0)                   \n");
		buffer.append("      return -1;                                        \n");
		buffer.append("   return 0;                                            \n");
		buffer.append("}                                                       \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 22);
		ICompositeType s1 = (ICompositeType) collector.getName(0).resolveBinding();
		IField d1 = (IField) collector.getName(1).resolveBinding();
		ICompositeType s2 = (ICompositeType) collector.getName(2).resolveBinding();
		IField d2 = (IField) collector.getName(3).resolveBinding();
		IFunction f = (IFunction) collector.getName(4).resolveBinding();
		IParameter pa = (IParameter) collector.getName(6).resolveBinding();
		IParameter pb = (IParameter) collector.getName(8).resolveBinding();
		IVariable a = (IVariable) collector.getName(15).resolveBinding();

		assertInstances(collector, s1, 3);
		assertInstances(collector, s2, 3);
		assertInstances(collector, d1, 3);
		assertInstances(collector, d2, 2);
		assertInstances(collector, f, 2);
		assertInstances(collector, pa, 2);
		assertInstances(collector, pb, 2);
		assertInstances(collector, a, 4);
	}

	public void testGCC20000605_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct F { int i; };                                    \n");
		buffer.append("void f1(struct F *x, struct F * y) {                    \n");
		buffer.append("   int timeout = 0;                                     \n");
		buffer.append("   for (; ((const struct F*)x)->i < y->i; x->i++)       \n");
		buffer.append("      if (++timeout > 5)                                \n");
		buffer.append("         return;                                        \n");
		buffer.append("}                                                       \n");
		buffer.append("main() {                                                \n");
		buffer.append("   struct F x, y;                                       \n");
		buffer.append("   x.i = 0;   y.i = 1;                                  \n");
		buffer.append("   f1(&x, &y);                                          \n");
		buffer.append("}                                                       \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 27);
		ICompositeType F = (ICompositeType) collector.getName(0).resolveBinding();
		IField i = (IField) collector.getName(1).resolveBinding();
		IFunction f1 = (IFunction) collector.getName(2).resolveBinding();
		IParameter px = (IParameter) collector.getName(4).resolveBinding();
		IParameter py = (IParameter) collector.getName(6).resolveBinding();
		IVariable timeout = (IVariable) collector.getName(7).resolveBinding();
		IVariable x = (IVariable) collector.getName(18).resolveBinding();
		IVariable y = (IVariable) collector.getName(19).resolveBinding();

		assertInstances(collector, F, 5);
		assertInstances(collector, i, 6);
		assertInstances(collector, f1, 2);
		assertInstances(collector, px, 3);
		assertInstances(collector, py, 2);
		assertInstances(collector, timeout, 2);
		assertInstances(collector, x, 3);
		assertInstances(collector, y, 3);
	}

	public void testGCC20000605_3() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct F { int x; int y; };                             \n");
		buffer.append("int main() {                                            \n");
		buffer.append("   int timeout = 0, x = 0;                              \n");
		buffer.append("   while (1) {                                          \n");
		buffer.append("      const struct F i = { x++, };                      \n");
		buffer.append("      if (i.x > 0)                                      \n");
		buffer.append("         break;                                         \n");
		buffer.append("      if (++timeout > 5)                                \n");
		buffer.append("         goto die;                                      \n");
		buffer.append("   }                                                    \n");
		buffer.append("   die: return 0;                                       \n");
		buffer.append("}                                                       \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 14);
		ICompositeType F = (ICompositeType) collector.getName(0).resolveBinding();
		IField fx = (IField) collector.getName(1).resolveBinding();
		IField fy = (IField) collector.getName(2).resolveBinding();
		IVariable timeout = (IVariable) collector.getName(4).resolveBinding();
		IVariable x = (IVariable) collector.getName(5).resolveBinding();
		IVariable i = (IVariable) collector.getName(7).resolveBinding();
		ILabel die = (ILabel) collector.getName(13).resolveBinding();

		assertInstances(collector, F, 2);
		assertInstances(collector, fx, 2);
		assertInstances(collector, fy, 1);
		assertInstances(collector, timeout, 2);
		assertInstances(collector, x, 2);
		assertInstances(collector, i, 2);
		assertInstances(collector, die, 2);
	}

	public void testGCCenum_2() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("enum foo { FOO, BAR };                                  \n");
		buffer.append("int main() {                                            \n");
		buffer.append("   int i;                                               \n");
		buffer.append("   for (i = BAR; i >= FOO; --i)                         \n");
		buffer.append("      if (i == -1) return -1;                           \n");
		buffer.append("   return 0;                                            \n");
		buffer.append("}                                                       \n");

		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 11);
		IEnumeration foo = (IEnumeration) collector.getName(0).resolveBinding();
		IEnumerator FOO = (IEnumerator) collector.getName(1).resolveBinding();
		IEnumerator BAR = (IEnumerator) collector.getName(2).resolveBinding();
		IVariable i = (IVariable) collector.getName(4).resolveBinding();

		assertInstances(collector, foo, 1);
		assertInstances(collector, FOO, 2);
		assertInstances(collector, BAR, 2);
		assertInstances(collector, i, 5);
	}
}
