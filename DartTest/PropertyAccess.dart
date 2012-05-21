class A {
  var field;
  var fieldA;
  
  A() {
    fieldA = 1;
    field = 2;
  }
  
  A.foo(int fieldA) {
    this.fieldA = fieldA;
    this.field = 2;
  }
  
  int getValue() {
    return fieldA;
  }
  
  int getField() {
    return field;
  }
  
  String toString() {
    return "A";
  }
  
  int id(int n) {
    return n;
  }
}

class B extends A {
  var field;
  var fieldB;
  
  B() {
    super();
    fieldB = 3;
    field = 4;
  }
  
  B.foo(int fieldB) {
    super();
    this.fieldB = fieldB;
    this.field = 4;
  }
  
  int getValue() {
    return fieldB;
  }
  
  String toString() {
    return "B";
  }
  
  int id(int n) {
    return n;
  }
}

class PropertyAccess {
  void main() {
    var a = new A();
    var a2 = new A.foo(10);
    var b = new B();
    var b2 = new B.foo(20);
    
    var c = a.id(5);
    var d = a.toString();
    var e = a.getValue();
    var f = a.getField();
    var g = a2.getValue();
    var h = a2.getField();
    
    var i = b.id(5);
    var k = b.toString();
    var l = b.getValue();
    var m = b.getField();
    var n = b2.getValue();
    var o = b2.getField();
  }
}