

/* This is sample code to test the indenter */



class Complex {
private:
	float re;
	float im;
public:
	Complex(float re, float im) :
		re(re), im(im) {}
	float GetRe() 
	{
		return re;
	}
	float GetIm() {
		return im;
	}
	void Set(float r, float i);
	void SetRe(float r);
	void SetIm(float i);
	void Print();
};
