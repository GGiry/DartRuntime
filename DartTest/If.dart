class A {
  int foo;

  void main() {
    var a = 1;
    var b = 2.5;
    var c = null;
    var d = foo;
    
    if (c != null) {
      a = 3.5;
    } else {
      b = 110;
    }
  }
}