typedef int F(int,int);

class Foo {
  F operation;
  
  Foo(int f(int a, int b)) {
    operation = f;
  }
  
  F calc() => operation;
}

int add(int a, int b) => a + b; 

main() {
  Foo foo = new Foo(add);
  
  var c = foo.calc()(2, 3);
}