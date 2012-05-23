class A {
  int foo;

  void main() {
    var a = foo;
    
    var b;
    var c;
    var d;
    
    var a1;
    var a2;
    var a3;
    var a4;
    
    if (a == 13) {
    // a == 13
      a1 = a;
      if (a == 3) {
        // on ne rentre pas.
        b = 10;
        a2 = a;
      }
      c = 1.2;
    } else if (a != 15) {
      // a == int[-inf; 14)] U int[16; +inf]
      a3 = a; 
    } else {
      a4 = a;
      d = true;
    }
  }
}