int z;

template<T> class nonVector {
	public:
	int x;
	int y;
	
	T* head;
	vector<T>() { head =new T(); }
	int length() { return 1; }
	T& first() { return *head; }
	const T& first() const { return *head; }
};


