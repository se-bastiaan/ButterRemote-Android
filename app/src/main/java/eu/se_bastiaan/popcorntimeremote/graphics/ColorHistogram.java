package eu.se_bastiaan.popcorntimeremote.graphics;

import java.util.Arrays;

final class ColorHistogram
{
  private final int[] mColors;
  private final int[] mColorCounts;
  private final int mNumberColors;

  ColorHistogram(int[] imagePixels)
  {
    int[] pixels = new int[imagePixels.length];
    System.arraycopy(imagePixels, 0, pixels, 0, pixels.length);

    Arrays.sort(pixels);

    this.mNumberColors = countDistinctColors(pixels);

    this.mColors = new int[this.mNumberColors];
    this.mColorCounts = new int[this.mNumberColors];

    countFrequencies(pixels);
  }

  int getNumberOfColors()
  {
    return this.mNumberColors;
  }

  int[] getColors()
  {
    return this.mColors;
  }

  int[] getColorCounts()
  {
    return this.mColorCounts;
  }

  static int countDistinctColors(int[] pixels) {
    if (pixels.length == 0) {
      return 0;
    }

    int colorCount = 1;
    int currentColor = pixels[0];

    for (int i = 1; i < pixels.length; i++)
    {
      if (pixels[i] != currentColor) {
        currentColor = pixels[i];
        colorCount++;
      }
    }

    return colorCount;
  }

  private void countFrequencies(int[] pixels) {
    if (pixels.length == 0) {
      return;
    }

    int currentColorIndex = 0;
    int currentColor = pixels[0];

    this.mColors[currentColorIndex] = currentColor;
    this.mColorCounts[currentColorIndex] = 1;

    for (int i = 1; i < pixels.length; i++)
      if (pixels[i] == currentColor)
      {
        this.mColorCounts[currentColorIndex] += 1;
      }
      else {
        currentColor = pixels[i];

        currentColorIndex++;
        this.mColors[currentColorIndex] = currentColor;
        this.mColorCounts[currentColorIndex] = 1;
      }
  }
}