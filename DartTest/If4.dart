class A {
  int field;
}

void main() {
  A aa = new A();
  
  var a = aa.field; 
  var b = aa.field;
  
  var a1;
  var a2;
  var a3;
  var b1;
  var b2;
  var b3;

  if (a <= 20) {
    a1 = a;
    if (10 <= a) {
      a2 = a;
      if (b <= 25) {
        b1 = b;
        if (15 <= b) {
          b2 = b;
          if (a <= b) {
            a3 = a;
            b3 = b;
          }
        }
      }
    }
  }
}