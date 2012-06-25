class A {
  var x;
  var y;
  
  A.foo() {
    x = 0;
    y = 0;
  }
  
  A.bar(var x, var y) {
    this.x = x;
    this.y = y;
  }
  
  
}

void main() {
  A a1 = new A.foo();
  A a2 = new A.bar(5, 5);
}