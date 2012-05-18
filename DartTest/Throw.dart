class A implements Exception {
  var foo;
  A(int n) {
    foo = n;
  }
}

void main() {
  var a = new A(0);
  
  throw null;
}