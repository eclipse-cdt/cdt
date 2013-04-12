
using namespace std;

#include<string>
//#include<>

//The 'Component' node
class Artifact {
public:
	Artifact(string name) {
		fName = name;
		fParent = NULL;
	}

	//Exercising Polymorphysm
	virtual void print() = 0;
	virtual string toString() = 0;
	virtual void print(char& padc) = 0;
	virtual string toString(char& padc) = 0;

	string getName() {
		return fName;
	}

	string getLocation() {
		return fPath + "/" + fName;
	}

	virtual void setParent(Artifact &parent) {
		fPath = parent.getLocation();
		fParent = &parent;
	}

	void deleteParent() {
		fPath = "";
		fParent = NULL;
	}

	virtual ~Artifact() {
	}

protected:
	string fName;
	string fPath;
	Artifact* fParent;

private:
	Artifact();
};
