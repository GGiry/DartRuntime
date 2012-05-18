class A {
  var foo;
  A(int n) {
    super();
    this.foo = n;
  }
}

class B extends A {
  B() {
    super(5);
  }
}

void main() {
  A a = new A(10);
  B b = new B();
}