class A {
  int ddouble(int n) {
    return n + n;
  }
}

class Invocation {
  static int id(int i) {
    return i;
  }
}

void main() {
  var aa = new A();
  var a = Invocation.id(1);
  var b = aa.ddouble(a);
}