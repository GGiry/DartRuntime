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

  if (20 >= a) {
    a1 = a; // a1=int? [-infinity,20]
    if (a >= 10) {
      a2 = a; // a2=int? [10,20]
      if (c >= a) {
        c3 = a; // c3=int? [5,5]
      } else {
        c4 = a; // c4=null
      }
      c5 = a; // c5=int? [10,20]
    }
  }
}