// test comment

class Hello {

  int length = 10;
  int _size;

  void main() {
    var a = length;
    var b = 7;
    {
      var c = 15.4;
      c += a;
      var d = false;
    }

    b += 11;
    
    print(a);
    
  }
  
  void set size(int newSize) {
    _size = newSize;
  }
}