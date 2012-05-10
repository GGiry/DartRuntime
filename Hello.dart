// test comment

class Hello {

  int length = 10;
  int _size;

  void main() {
    var a = length;
    {
      a = 15.2;
      var b = false;
    }

    a = 11;
    
    print(a);
    
  }
  
  void set size(int newSize) {
    _size = newSize;
  }
}