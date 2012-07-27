void main () {
  var bit = 1;
  for (;;) {
    bit = bit << 1;
    //bit = bit | 1;
    print(bit);
    break;
  }
}