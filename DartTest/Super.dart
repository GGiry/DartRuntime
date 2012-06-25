class A {
  var foo;
  A(num n) {
    super();
    this.foo = n;
  }
}

class B extends A {
  B(int n) : super(n) {
  }
}

void main() {
  A a = new A(10.5);
  B b = new B(5);
  
  var c1 = a.foo;
  var c2 = b.foo;
  
  print(c1);
  print(c2);
}