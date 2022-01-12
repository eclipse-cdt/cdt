#include <iostream.h>
#include <stdlib.h>
#include <alloc.h>
#include <iomanip.h>

class Mail
{
public:
Mail(){}
virtual void print()=0; //Pure Virtual Function, forces redefinition
protected:
float postage;
char *type;
friend ostream& operator << (ostream& os, Mail *m);
};

class postcard : public Mail
{
public:
postcard(): Mail(){postage = 0.20; type = "Postcard";}
void print(){cout << type << ": $" << setiosflags(ios::fixed)
 <<setprecision(2) << postage <<endl;}
 };

class first_class : public Mail
{
public:
first_class() : Mail(){postage = 0.32; type = "First Class";}
void print(){cout << type << ": $" <<setiosflags(ios::fixed)
             << setprecision(2) << postage <<endl;}
 
};

class Unknown : public postcard, first_class // ??? Multiple Inheritance
{
public:
Unknown(): postcard(), first_class()
{
postcard::postage = 1.50; // MUST disambiguate
postcard::type = "Unknown";
}
void print(){cout << postcard::type << ": $" <<setiosflags(ios::fixed)
             <<setprecision(2)<<postcard::postage <<endl;}
 };

class container
{
private:
Mail **array;
int index;
int sz;
public:
container(){array = 0;}
~container(){
for(int x = 0; x <sz; x++)
 delete array[x];
free(array);
}
int size() {return sz;}
Mail* operator[](int index);
Mail* operator = (Mail* mail);
};

main()
{
container PO_Box;
PO_Box = new postcard;
PO_Box = new first_class;
PO_Box = new parcel_Post;
//PO_Box = new Unknown;
//one way of printing information
for(int x =0; x <3; x++){
 PO_Box[x]->print();
}
//Overloaded  <<
 for(int x =0; x <PO_Box.size(); x++){
 cout << PO_Box[x];
 }
}

ostream& operator << (ostream &os, Mail *m)
{
os <<setiosflags(ios::fixed) << setprecision(2)<< m->type
<< ": $" << m->postage <<endl;

return os;
}
Mail* container::operator[](int index) {return array[index];}
Mail* container::operator = (Mail* mail)
{ 
int size = sizeof(Mail*) * (++sz); 
int temp = sz -1; 
array = (Mail**)realloc(array, size); 
array[temp] = mail; 
return 0; 
}


