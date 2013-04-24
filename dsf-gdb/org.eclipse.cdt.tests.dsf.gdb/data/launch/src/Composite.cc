/*
 * Composite Pattern
 */
#include<iostream>
#include<string>
#include<vector>
#include "Leaf.cc"

using namespace std;

//The 'Composite' node
class CompositeNode: public Artifact {
public:
	CompositeNode(string name) :
			Artifact(name) {
	}

	void Add(Artifact* child) {
		child->setParent(*this);
		fChildren.push_back(child);
	}

	void setParent(Artifact &parent) {
		fPath = parent.getLocation();
		fParent = &parent;

		//Refresh the parent information path to all children
		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			Artifact* child = *it;
			child->setParent(*this);
			++it;
		}
	}

	void Remove(Artifact* child) {
		child->deleteParent();
		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			if (*it == child) {
				delete child;
				fChildren.erase(it);
				break;
			}
			++it;
		}

	}

	void print() {
		cout << getLocation() << endl;

		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			(*it)->print();
			++it;
		}
	}

	void print(char& cpad) {
		string padding(fPath.length(), cpad);
		cout << padding << "+ " << fName << endl;

		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			(*it)->print(cpad);
			++it;
		}
	}

	string toString() {
		string strAccumulator(getLocation() + "\n");

		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			strAccumulator.append((*it)->toString());
			++it;
		}

		return strAccumulator;
	}

	string toString(char& cpad) {
		string strAccumulation(fPath.length(), cpad);
		strAccumulation.append("+ " + fName + "\n");

		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			strAccumulation.append((*it)->toString(cpad));
			++it;
		}

		return strAccumulation;
	}

	virtual int getArtifactsSize() {
		return fChildren.size();
	}

	virtual Artifact* getArtifact(int index) {
		if (index < fChildren.size()) {
			return fChildren.at(index);
		}

		else
			return 0;
	}

	virtual Artifact* getArtifact(string description) {
		vector<Artifact*>::iterator it = fChildren.begin();
		while (it != fChildren.end()) {
			if ((*it)->getName().compare(description)) {
				return *it;
			}
			++it;
		}

		return 0;
	}

	virtual ~CompositeNode() {
		while (!fChildren.empty()) {
			vector<Artifact*>::iterator it = fChildren.begin();
			delete *it;
			fChildren.erase(it);
		}
	}

private:
	CompositeNode();
	vector<Artifact*> fChildren;
};

//The Main method
int main() {
	//Create a tree root
	CompositeNode* root = new CompositeNode("Dogs");

	//Create composite nodes
	CompositeNode* comp = new CompositeNode("Companion");
	comp->Add(new LeafNode("Puddle"));
	comp->Add(new LeafNode("Bichon"));

	CompositeNode* sport = new CompositeNode("Guardian");
	sport->Add(new LeafNode("Boxer"));
	sport->Add(new LeafNode("Rottweiler"));
	sport->Add(new LeafNode("Mastiff"));

	//Create a Branch
	CompositeNode* gun = new CompositeNode("Gun");
	gun->Add(new LeafNode("Cocker"));
	gun->Add(new LeafNode("Pointer"));
	gun->Add(new LeafNode("Golden Retriever"));

	CompositeNode* herd = new CompositeNode("Herding");
	herd->Add(new LeafNode("Cattle dog"));
	herd->Add(new LeafNode("Sheepdog"));

	CompositeNode* north = new CompositeNode("Northern");
	north->Add(new LeafNode("Akita"));
	north->Add(new LeafNode("Chow Chow"));

	CompositeNode* hound = new CompositeNode("Hound");
	hound->Add(new LeafNode("Basset Hound"));
	hound->Add(new LeafNode("Beagle"));

	CompositeNode* terrier = new CompositeNode("Terrier");
	terrier->Add(new LeafNode("Bull Terrier"));
	terrier->Add(new LeafNode("Border Terrier"));

	//Create some leaf nodes
	LeafNode* pe1 = new LeafNode("German Shepperd");
	LeafNode* pe2 = new LeafNode("Great Dane");

	//Add nodes to start from the same root
	root->Add(comp);
	root->Add(sport);
	root->Add(gun);
	root->Add(herd);
	root->Add(north);
	root->Add(hound);
	root->Add(terrier);
	//Add leaf nodes to root
	root->Add(pe1);
	root->Add(pe2);

	char cpad = '-';
	char cpad2 = '_';
	//Test stub + toString variants
	if (root->getArtifactsSize() > 0
			&& (root->getArtifact(0) != 0 && (root->getArtifact("Bichon") != 0))) {
		string sout = root->getArtifact(0)->toString() + "\n" + root->getArtifact(1)->toString(cpad2);
		cout <<  sout << endl;
	}

	//Test Remove primitive elements
	root->Remove(pe1);
	root->Remove(pe2);

	//Test Print variants
	root->getArtifact(2)->print();	root->getArtifact(3)->print(cpad);

	//Test toString all
	cout << "\n\nAll Tree\n" + root->toString(cpad);

	//delete the allocated memory
	delete root;

	return 0;
}
