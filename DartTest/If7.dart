class A {
  int field;
}

void main() {
  A aa = new A();
  
  var a = aa.field; 
  var b = aa.field;
  var c = 5;
  
  var a1;

  if (true) {
    a1 = a; // a1=int [-inf,+inf]
  }
}