package eu.se_bastiaan.popcorntimeremote.graphics;

import android.graphics.Color;
import java.util.Arrays;

public final class PaletteItem
{
  final int red;
  final int green;
  final int blue;
  final int rgb;
  final int population;
  private float[] hsl;

  PaletteItem(int rgbColor, int population)
  {
    this.red = Color.red(rgbColor);
    this.green = Color.green(rgbColor);
    this.blue = Color.blue(rgbColor);
    this.rgb = rgbColor;
    this.population = population;
  }

  PaletteItem(int red, int green, int blue, int population) {
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.rgb = Color.rgb(red, green, blue);
    this.population = population;
  }

  public int getRgb()
  {
    return this.rgb;
  }

  public float[] getHsl()
  {
    if (this.hsl == null)
    {
      this.hsl = new float[3];
      eu.se_bastiaan.popcorntimeremote.graphics.ColorUtils.RGBtoHSL(this.red, this.green, this.blue, this.hsl);
    }
    return this.hsl;
  }

  int getPopulation()
  {
    return this.population;
  }

  public String toString() {
    return getClass().getSimpleName() + " " + "[" + Integer.toHexString(getRgb()) + ']' + "[HSL: " + Arrays.toString(getHsl()) + ']' + "[Population: " + this.population + ']';
  }
}