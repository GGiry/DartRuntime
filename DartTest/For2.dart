void main() {
  var a = 0;
  var b = 0;
  for (var i = 0; i < 10; i++) {
    b = b + a++;
  }
  print(a);
  print(b);
}