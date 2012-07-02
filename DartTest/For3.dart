class A {
  int foo;
}

void main() {
  A aa = new A();
  var a = 0;
  var b = aa.foo;
  for (var i = 0; i < 10; i++) {
   if (b < 15) {
      if (b > 20) {
        a = b;
      }
      b = 25;
    }
  }
}
