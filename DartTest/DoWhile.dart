void main() {
  var a = 0;
  
  do {
    a++;
    if (a == 5) {
      break;
    }
  } while (a < 10);
  
  print(a);
}