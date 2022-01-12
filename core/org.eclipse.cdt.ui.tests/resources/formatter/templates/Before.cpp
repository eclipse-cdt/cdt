class Key;
class Value;
class SortAlgorithm;
class DefaultSort;
class T;
class X;
class Y;
class Bar;
class Foo { template<class Bar> void fum(int i); };

// TEMPLATE_STRUCT
template<class Key, class Value, class SortAlgorithm=DefaultSort>
struct Map
{
	Key* keys;
	Value* values;
	SortAlgorithm* sortAlgorithm;
	Map();
};

// TEMPLATE_CLASS
template<class T> class nonVector {
private:	T* head;

public:
	nonVector() {head=new T();}
	int length() {return 1;}
	const T& first() const;
};

// TEMPLATE_UNION
template<class X, class Y, int size=16>
union ArrayOverlay {
public:
	X x[size];	Y y[size];

	static int numArrays;
};

// TEMPLATE_METHODS
class TemplateContainer {
	// these are in an enclosing class
	template<class Bar> void fum(int i);
	template<int> 
	void scrum(void) {}
	;
};

// TEMPLATE_FUNCTION
template<class T> const T& nonVector<T>::first() const
	{
	return *head;
}

template<class X> bool IsGreaterThan(X,X);

template<class Bar> void Foo::fum(int i) {}

// TEMPLATE_VARIABLES
template <bool   threads, int inst> char* default_alloc_template<threads, inst>::S_start_free = 0;

// an instantiation, not a template:
complex
<float> cf(0,0);
//template<class Language, class CharacterSet, class SortAlgorithm<CharacterSet> >
//Dictionary* TheSpellCheckDictionary;

int success;
