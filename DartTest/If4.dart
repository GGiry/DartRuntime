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
  var a3;
  var a4;
  
  var b1;
  var b2;
  var b3;
  var b4;

  var c1;
  var c2;
  var c3;
  var c4;
  var c5;

  if (a != 17) {
    c1 = a; // c1=union?[int [18,+infinity], int [-infinity,16]]
    if (a <= 19) {
      c2 = a; // c2=union?[int [18,19], int [-infinity,16]]
    }
  }

  if (a <= 20) {
    a1 = a; // a1=int? [-infinity,20]
    if (a >= 10) {
      a2 = a; // a2=int? [10,20]
      if (c <= a) {
        c3 = a; // c3=int? [5,5]
      } else {
        c4 = a; // c4=null
      }
      c5 = a; // c5=int? [10,20]
      if (25 >= b) {
        b1 = b; // b1=int? [-infinity,25]
        if (15 <= b) {
          b2 = b; // b2=int? [15,25]
          if (a <= b) {
            a3 = a; // a3=int? [10,20]
            b3 = b; // b3=int? [15,25]
          }
        }
      }
    }
  } else {
    // do nothing
  }
  
  a4 = a;
  b4 = b;
}