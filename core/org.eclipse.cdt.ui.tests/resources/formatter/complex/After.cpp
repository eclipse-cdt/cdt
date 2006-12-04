
/* This is sample code to test the formatter */

class Complex {
private:
	float re;
	float im;
public:
	Complex(float re, float im) :
		re(re), im(im) {
	}
	float GetRe() {
		return re;
	}
	float GetIm() {
		return im;
	}
	void Set(float r, float i);
	/* Set real part */
	void SetRe(float r);
	/*
	 * Set imaginary part
	 */
	void SetIm(float i);
	void Print();
};
