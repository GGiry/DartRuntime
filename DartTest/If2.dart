class A {
  int foo;
}

void main() {
  var aa = new A();

  var a = aa.foo;
  
  var a1;
  var a2;
  var a3;
  var a4;
  
  if (a == 13) {
  // a == 13
    a1 = a;
    if (a == 3) {
      // on ne rentre pas.
      a2 = a;
    }
  } else if (a != 15) {
    // a == int[-inf; 14)] U int[16; +inf]
    a3 = a; 
  } else {
    // a = int[-inf; +inf]
    a4 = a;
  }
}