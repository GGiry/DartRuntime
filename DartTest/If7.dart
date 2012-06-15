class A {
  int field;
}

void main() {
  A aa = new A();
  
  var a = aa.field; 
  
  var a1;
  var a2;
  var a3;
  var a4;
  var a5;

  if (a >= 10 && a <= 20) {
    a1 = a; // a1=int [10,20]
  } else {
    a2 = a; // a2=int [-inf,+inf]
  }
  
  if (a >= 10 || a <= 20) {
    a3 = a; // a3=int [-inf,+inf]
  } else {
    a4 = a; // a4=int [-inf,9]U[20,+inf]
  }
  
  if (true) {
    a5 = a; // a5int [-inf,+inf]
  }
}