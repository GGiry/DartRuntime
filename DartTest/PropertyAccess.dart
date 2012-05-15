class PropertyAccess {
  var length = 10;
  int coef;
  
  PropertyAccess() {
    this.coef = 1;
  }
  
  PropertyAccess.foo(int coef) {
    this.coef = coef;
  } 
  
  int id(int a) {
    return coef + a;
  }
  
  int sum(int a, int b) {
    return a+b;
  }
  
  void main() {
    var pa = new PropertyAccess();
    var pb = new PropertyAccess.foo(10);
    var pc = new PropertyAccess.foo(5);
    
  
    var x = 2;
    var y = 3;
  
    var a = pa.length;
    var b = pa.id(pa.length);
    var c = pa.sum(x, y);
    var d = pa.coef;
    var e = pb.coef;
    var f = pa.id(1);
  }
}