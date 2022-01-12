namespace ns2 {
	void blah(int){};
}

namespace ns3 {
	void blah(char){};
}

using namespace ns2;
using namespace ns3;

void foo2()
{
	blah('a'); //ns3::blah(char)
}
