package eu.se_bastiaan.popcorntimeremote.graphics;

import android.graphics.Color;

final class ColorUtils
{
  static float calculateXyzLuma(int color)
  {
    return (0.2126F * Color.red(color) + 0.7152F * Color.green(color) + 0.0722F * Color.blue(color)) / 255.0F;
  }

  static float calculateContrast(int color1, int color2)
  {
    return Math.abs(calculateXyzLuma(color1) - calculateXyzLuma(color2));
  }

  static void RGBtoHSL(int r, int g, int b, float[] hsl) {
    float rf = r / 255.0F;
    float gf = g / 255.0F;
    float bf = b / 255.0F;

    float max = Math.max(rf, Math.max(gf, bf));
    float min = Math.min(rf, Math.min(gf, bf));
    float deltaMaxMin = max - min;

    float l = (max + min) / 2.0F;
    float h;
    float s;
    if (max == min)
    {
      h = s = 0.0F;
    }
    else
    {
      if (max == rf) {
        h = (gf - bf) / deltaMaxMin % 6.0F;
      }
      else
      {
        if (max == gf)
          h = (bf - rf) / deltaMaxMin + 2.0F;
        else {
          h = (rf - gf) / deltaMaxMin + 4.0F;
        }
      }
      s = deltaMaxMin / (1.0F - Math.abs(2.0F * l - 1.0F));
    }

    hsl[0] = (h * 60.0F % 360.0F);
    hsl[1] = s;
    hsl[2] = l;
  }

  static int HSLtoRGB(float[] hsl) {
    float h = hsl[0];
    float s = hsl[1];
    float l = hsl[2];

    float c = (1.0F - Math.abs(2.0F * l - 1.0F)) * s;
    float m = l - 0.5F * c;
    float x = c * (1.0F - Math.abs(h / 60.0F % 2.0F - 1.0F));

    int hueSegment = (int)h / 60;

    int r = 0; int g = 0; int b = 0;

    switch (hueSegment) {
    case 0:
      r = Math.round(255.0F * (c + m));
      g = Math.round(255.0F * (x + m));
      b = Math.round(255.0F * m);
      break;
    case 1:
      r = Math.round(255.0F * (x + m));
      g = Math.round(255.0F * (c + m));
      b = Math.round(255.0F * m);
      break;
    case 2:
      r = Math.round(255.0F * m);
      g = Math.round(255.0F * (c + m));
      b = Math.round(255.0F * (x + m));
      break;
    case 3:
      r = Math.round(255.0F * m);
      g = Math.round(255.0F * (x + m));
      b = Math.round(255.0F * (c + m));
      break;
    case 4:
      r = Math.round(255.0F * (x + m));
      g = Math.round(255.0F * m);
      b = Math.round(255.0F * (c + m));
      break;
    case 5:
    case 6:
      r = Math.round(255.0F * (c + m));
      g = Math.round(255.0F * m);
      b = Math.round(255.0F * (x + m));
    }

    r = Math.max(0, Math.min(255, r));
    g = Math.max(0, Math.min(255, g));
    b = Math.max(0, Math.min(255, b));

    return Color.rgb(r, g, b);
  }
}