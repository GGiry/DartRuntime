class A {
  int operator[](int i) {
    return i + i;
  }
  
  void operator[]=(int index, int value) {
    throw new UnsupportedOperationException("");
  }
}

void main() {
  var a = [1, 2, 3];
  
  var b = a[0];
  var c = a[1];
  var d = a[2];
  
  a = new A();
  
  var e = a[0];
  var f = a[1];
  var g = a[2];
  
  print('$b');
  print('$c');
  print('$d');
  print('$e');
  print('$f');
  print('$g');
}