class A {
  int foo;

  void main() {
    var a = 1;
    var b = 2.5;
    var c = null;
    var d = foo;
    
    if (c != null) {
      a = 3.5;
      d = true;
    } else {
      b = 110;
      d = 12.5;
    }
    
    var e = 1;
    
    if (a == 4) {
      e = 1.2;
    } else if (a == 2) {
      e = 10;
    } else if (a == 3) {
      e = false;
    }
  }
}