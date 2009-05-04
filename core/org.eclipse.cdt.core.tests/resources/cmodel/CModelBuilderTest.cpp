class Test {
   void Test::inlined() {};  // wrong label in outline: Test::inlined(): void
   void Test::decl();        // label in outline (ok):  decl(): void
};
namespace nsTest {
   void nsTest::inlined() {};  // wrong label in outline: nsTest::inlined(): void
   void nsTest::decl();        // label in outline (ok):  decl(): void
}
namespace nsTest {
   void nsTest::inlined2() {};  // wrong label in outline: nsTest::inlined(): void
   void nsTest::decl2();        // label in outline (ok):  decl(): void
}
//http://bugs.eclipse.org/262785
void Unknown1::method() {}      // no qualifier in outline
void Unknown2::method() {}      // no qualifier in outline
class Bug274490 {
    virtual int m();
    inline const char* m2() const;
};
