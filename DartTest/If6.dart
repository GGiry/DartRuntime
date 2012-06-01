class A {
  int field;
}

void main() {
  A aa = new A();
  
  var a = aa.field; 
  var b = aa.field;
  var c = 5;
  
  var a1;
  var a2;

  var c3;
  var c4;
  var c5;

  if (a >= 0 && 20 >= a) {
    a1 = a; // a1=int? [0,20]
  }
}