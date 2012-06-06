void main() {
  var a = 3;

  for (var i = 0; i < 10; i++) {
    if (i % 2 == 0) {
      a = 2 + a;
    } else {
      a = 2.5 + a;
    }
  }
}