#include<iostream>
#include<string>
#include "Artifact.cc"


//The 'Leaf' class
class LeafNode: public Artifact {
public:
	LeafNode(string name) :
			Artifact(name) {
	}

	void print() {
		cout << getLocation() << endl;
	}

	void print(char& cpad) {
		string padding(fPath.length(), cpad);
		cout << padding << " " << fName << endl;
	}


	string toString() {
		return getLocation() + "\n";
	}

	string toString(char& cpad) {
		string padding(fPath.length(), cpad);
		string rstr = padding + " " + fName + "\n";
		return rstr;
	}

	virtual ~LeafNode() {
	}

private:
	LeafNode();
};
