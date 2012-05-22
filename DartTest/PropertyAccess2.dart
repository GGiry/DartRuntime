class A {
  
  int get foo() { 
    return _foo;
  }
  
  set foo(int n) {
    _foo = n;
  }
  
  compute() {
    var a = foo - 1;
    
    return a;
  }

  var _foo = 1;
}

void main() {
  var a = new A();
  
  var b = a.foo - 1;
  
  var c = a.compute();
}