#include "class.h"

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
}
