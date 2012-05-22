class A {
  var _foo;

  int get foo() {
    return _foo;
  }

  set foo(value) {
    _foo = value;
  }
}

void main () {
  A a = new A();

  var bar = a.foo;

  print(bar);
}