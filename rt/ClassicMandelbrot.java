public class ClassicMandelbrot {

  public static void main(String[] args) {
    int width = 1024;
    int height = 1024;
    int maxiter = 50;
    double limit = 4.0;

    int[] list = new int[1024*1024/8];
    int index = 0;

    for (int y = 0; y < height; y++) {
      int bits = 0;
      int xcounter = 0;
      double Ci = 2.0*y/height - 1.0;

      for (int x = 0; x < width; x++){
        double Zr = 0.0;
        double Zi = 0.0;
        double Cr = 2.0*x/width - 1.5;
        int i = maxiter;

        bits = bits << 1;
        do {
          double Tr = Zr*Zr - Zi*Zi + Cr;
          Zi = 2.0*Zr*Zi + Ci;
          Zr = Tr;
          if (Zr*Zr + Zi*Zi > limit) {
            bits |= 1;
            break;
          }
        } while (--i > 0);

        if (++xcounter == 8) {
          list[index++] = bits ^ 0xff;
          bits = 0;
          xcounter = 0;
        }
      }
      if (xcounter != 0)
        list[index++] = (bits << (8 - xcounter)) ^ 0xff;
    }
  }
}

