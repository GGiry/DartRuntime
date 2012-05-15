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
    return a;
  }
  
  int sum(int a, int b) {
    return a+b;
  }
  
  void main() {
    PropertyAccess pa = new PropertyAccess();
    PropertyAccess pb = new PropertyAccess.foo(10);
    PropertyAccess pc = pa.foo(5);
    
  
    var x = 2;
    var y = 3;
  
    var a = pa.length;
    var b = pa.id(pa.length);
    var c = pa.sum(x, y);
    var d = pa.coef;
    var e = pb.coef;
  }
}