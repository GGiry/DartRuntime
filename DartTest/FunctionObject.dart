class A  {
  int id(int n) {
    return n;
  }
}

void main() {
  A a = new A();
  
  var foo = a.id;
  
  var b = foo(5);
}