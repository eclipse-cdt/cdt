#include "common.h"

namespace corge {
   void referencesA_inNS() {
	  ManyOverloaded m;
      ns2::quux(); ns2::quux(); ns2::quux(); ns2::quux(); 
      ns2::quux(5); ns2::quux(5); ns2::quux(5); ns2::quux(5); ns2::quux(5);
      ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f');
      ns2::quux(new ManyOverloaded()); ns2::quux(new ManyOverloaded());
      ns2::quux(new ManyOverloaded()); ns2::quux(new ManyOverloaded());
      ns2::quux(new ManyOverloaded()); ns2::quux(new ManyOverloaded());
      ns2::quux(new ManyOverloaded()); 
      ns2::quux(m); ns2::quux(m); ns2::quux(m); ns2::quux(m); ns2::quux(m);
      ns2::quux(m); ns2::quux(m); ns2::quux(m);
   }
}

void referencesA() {
  ManyOverloaded m;
  m.qux();
  m.qux(4); m.qux(4);
  m.qux(6,'f'); m.qux(6,'f'); m.qux(6,'f');
  m.qux(new ManyOverloaded()); m.qux(new ManyOverloaded());
  m.qux(new ManyOverloaded()); m.qux(new ManyOverloaded());
  m.qux(m); m.qux(m); m.qux(m); m.qux(m); m.qux(m);
  
  quux(); quux();
  quux(4); quux(4); quux(4);
  quux(6,'f'); quux(6,'f'); quux(6,'f'); quux(6,'f');
  quux(new ManyOverloaded()); quux(new ManyOverloaded());
  quux(new ManyOverloaded()); quux(new ManyOverloaded()); quux(new ManyOverloaded());
  quux(m); quux(m); quux(m); quux(m); quux(m); quux(m);
    
  corge::grault(); corge::grault(); corge::grault();
  corge::grault(4); corge::grault(4); corge::grault(4); corge::grault(4);
  corge::grault(6,'f'); corge::grault(6,'f'); corge::grault(6,'f'); corge::grault(6,'f'); corge::grault(6,'f');
  corge::grault(new ManyOverloaded()); corge::grault(new ManyOverloaded());
  corge::grault(new ManyOverloaded()); corge::grault(new ManyOverloaded());
  corge::grault(new ManyOverloaded()); corge::grault(new ManyOverloaded());
  corge::grault(m); corge::grault(m); corge::grault(m); corge::grault(m); corge::grault(m);
  corge::grault(m); corge::grault(m);
  
  ns2::quux(); ns2::quux(); ns2::quux(); ns2::quux(); 
  ns2::quux(5); ns2::quux(5); ns2::quux(5); ns2::quux(5); ns2::quux(5);
  ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f'); ns2::quux(6,'f');
  ns2::quux(new ManyOverloaded()); ns2::quux(new ManyOverloaded());
  ns2::quux(new ManyOverloaded()); ns2::quux(new ManyOverloaded());
  ns2::quux(new ManyOverloaded()); ns2::quux(new ManyOverloaded());
  ns2::quux(new ManyOverloaded()); 
  ns2::quux(m); ns2::quux(m); ns2::quux(m); ns2::quux(m); ns2::quux(m);
  ns2::quux(m); ns2::quux(m); ns2::quux(m);
  
  m.qux(UNRESOLVED_SYMBOL); // indexer should skip without error
  m.qux(4, UNRESOLVED_SYMBOL); // indexer should skip without error
  quux(UNRESOLVED_SYMBOL); // indexer should skip without error
  quux(6, UNRESOLVED_SYMBOL); // indexer should skip without error
  corge::grault(UNRESOLVED_SYMBOL); // indexer should skip without error
  corge::grault(6, UNRESOLVED_SYMBOL); // indexer should skip without error
  ns2::quux(UNRESOLVED_SYMBOL); // indexer should skip without error
  ns2::quux(4, UNRESOLVED_SYMBOL); // indexer should skip without error
}

namespace corge {
   void problemRefsA() {
      ns2::quux(UNRESOLVED_SYMBOL); // indexer should skip without error
	  ns2::quux(4, UNRESOLVED_SYMBOL); // indexer should skip without error
   }
}
