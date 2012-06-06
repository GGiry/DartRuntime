void main() {
  var a = 3;
  
  var a1 = a;
  var a2 = a;

  for (var i = 0; i < 10; i++) {
    if (i % 2 == 0) {
      a = 2;
      a1 = a;
    } else {
      a = 2.5;
      a2 = a;
    }
  }
  
  var a3 = a;
}