template<typename T>
class ATemplate {
};

template<>
class ATemplate<int> {
  int specializedDefaultVariable;
public:
  int specializedPublicVariable;
protected:
  int specializedProtectedVariable;
private:
  int specializedPrivateVariable;
};
