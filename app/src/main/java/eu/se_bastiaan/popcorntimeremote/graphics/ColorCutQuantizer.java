package eu.se_bastiaan.popcorntimeremote.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

final class ColorCutQuantizer
{
  private final float[] mTempHsl = new float[3];
  private final int[] mColors;
  private final SparseIntArray mColorPopulations;
  private final List<PaletteItem> mQuantizedColors;
  private static final Comparator<Vbox> VBOX_COMPARATOR_VOLUME = new Comparator<Vbox>()
  {
    public int compare(Vbox lhs, Vbox rhs) {
      return rhs.getVolume() - lhs.getVolume();
    }
  };

  static ColorCutQuantizer fromBitmap(Bitmap bitmap, int maxColors)
  {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();

    int[] rgbPixels = new int[width * height];
    bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height);

    return new ColorCutQuantizer(rgbPixels, maxColors);
  }

  ColorCutQuantizer(int[] pixels, int maxColors)
  {
    ColorHistogram colorHist = new ColorHistogram(pixels);
    int rawColorCount = colorHist.getNumberOfColors();
    int[] rawColors = colorHist.getColors();
    int[] rawColorCounts = colorHist.getColorCounts();

    this.mColorPopulations = new SparseIntArray(rawColorCount);
    for (int i = 0; i < rawColors.length; i++) {
      this.mColorPopulations.append(rawColors[i], rawColorCounts[i]);
    }

    this.mColors = new int[rawColorCount];
    int validColorCount = 0;
    for (int color : rawColors) {
      if (!shouldIgnoreColor(color)) {
        this.mColors[(validColorCount++)] = color;
      }
    }

    if (validColorCount <= maxColors)
    {
      this.mQuantizedColors = new ArrayList();
      for (int color : this.mColors)
        this.mQuantizedColors.add(new PaletteItem(color, this.mColorPopulations.get(color)));
    }
    else
    {
      this.mQuantizedColors = quantizePixels(validColorCount - 1, maxColors);
    }
  }

  List<PaletteItem> getQuantizedColors()
  {
    return this.mQuantizedColors;
  }

  private List<PaletteItem> quantizePixels(int maxColorIndex, int maxColors)
  {
    PriorityQueue pq = new PriorityQueue(maxColors, VBOX_COMPARATOR_VOLUME);

    pq.offer(new Vbox(0, maxColorIndex));

    splitBoxes(pq, maxColors);

    return generateAverageColors(pq);
  }

  private void splitBoxes(PriorityQueue<Vbox> queue, int maxSize)
  {
    while (queue.size() < maxSize) {
      Vbox vbox = (Vbox)queue.poll();

      if ((vbox != null) && (vbox.canSplit()))
      {
        queue.offer(vbox.splitBox());

        queue.offer(vbox);
      }
      else {
        return;
      }
    }
  }

  private List<PaletteItem> generateAverageColors(Collection<Vbox> vboxes) {
    ArrayList colors = new ArrayList(vboxes.size());
    for (Vbox vbox : vboxes) {
      PaletteItem color = vbox.getAverageColor();
      if (!shouldIgnoreColor(color))
      {
        colors.add(color);
      }
    }
    return colors;
  }

  private void modifySignificantOctet(int dimension, int lowIndex, int highIndex)
  {
    switch (dimension)
    {
    case -3:
      break;
    case -2:
      for (int i = lowIndex; i <= highIndex; i++) {
        int color = this.mColors[i];
        this.mColors[i] = Color.rgb(color >> 8 & 0xFF, color >> 16 & 0xFF, color & 0xFF);
      }
      break;
    case -1:
      for (int i = lowIndex; i <= highIndex; i++) {
        int color = this.mColors[i];
        this.mColors[i] = Color.rgb(color & 0xFF, color >> 8 & 0xFF, color >> 16 & 0xFF);
      }
    }
  }

  private boolean shouldIgnoreColor(PaletteItem color)
  {
    return (isWhite(color.getRgb())) || (isBlack(color.getRgb())) || (isNearRedILine(color.getRgb()));
  }

  private boolean shouldIgnoreColor(int color) {
    return (isWhite(color)) || (isBlack(color)) || (isNearRedILine(color));
  }

  private boolean isBlack(int color)
  {
    return (Color.red(color) + Color.green(color) + Color.blue(color)) / 3.0F <= 22.0F;
  }

  private boolean isWhite(int color)
  {
    return (Color.red(color) + Color.green(color) + Color.blue(color)) / 3.0F >= 237.0F;
  }

  private boolean isNearRedILine(int color)
  {
    ColorUtils.RGBtoHSL(Color.red(color), Color.green(color), Color.blue(color), this.mTempHsl);
    return (this.mTempHsl[0] >= 10.0F) && (this.mTempHsl[0] <= 37.0F) && (this.mTempHsl[1] <= 0.82F);
  }

  private class Vbox
  {
    private int lowerIndex;
    private int upperIndex;
    private int minRed;
    private int maxRed;
    private int minGreen;
    private int maxGreen;
    private int minBlue;
    private int maxBlue;

    Vbox(int lowerIndex, int upperIndex)
    {
      this.lowerIndex = lowerIndex;
      this.upperIndex = upperIndex;
      fitBox();
    }

    int getVolume() {
      return (this.maxRed - this.minRed + 1) * (this.maxGreen - this.minGreen + 1) * (this.maxBlue - this.minBlue + 1);
    }

    boolean canSplit() {
      return getColorCount() > 1;
    }

    int getColorCount() {
      return this.upperIndex - this.lowerIndex;
    }

    void fitBox()
    {
      this.minRed = (this.minGreen = this.minBlue = 'ÿ');
      this.maxRed = (this.maxGreen = this.maxBlue = 0);

      for (int i = this.lowerIndex; i <= this.upperIndex; i++) {
        int color = ColorCutQuantizer.this.mColors[i];
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        if (r > this.maxRed) {
          this.maxRed = r;
        }
        if (r < this.minRed) {
          this.minRed = r;
        }
        if (g > this.maxGreen) {
          this.maxGreen = g;
        }
        if (g < this.minGreen) {
          this.minGreen = g;
        }
        if (b > this.maxBlue) {
          this.maxBlue = b;
        }
        if (b < this.minBlue)
          this.minBlue = b;
      }
    }

    Vbox splitBox()
    {
      if (!canSplit()) {
        throw new IllegalStateException("Can not split a box with only 1 color");
      }

      int splitPoint = findSplitPoint();

      Vbox newBox = new Vbox(splitPoint + 1, this.upperIndex);

      this.upperIndex = splitPoint;
      fitBox();

      return newBox;
    }

    int getLongestColorDimension()
    {
      int redLength = this.maxRed - this.minRed;
      int greenLength = this.maxGreen - this.minGreen;
      int blueLength = this.maxBlue - this.minBlue;

      if ((redLength >= greenLength) && (redLength >= blueLength))
        return -3;
      if ((greenLength >= redLength) && (greenLength >= blueLength)) {
        return -2;
      }
      return -1;
    }

    int findSplitPoint()
    {
      int longestDimension = getLongestColorDimension();

      ColorCutQuantizer.this.modifySignificantOctet(longestDimension, this.lowerIndex, this.upperIndex);

      Arrays.sort(ColorCutQuantizer.this.mColors, this.lowerIndex, this.upperIndex + 1);

      ColorCutQuantizer.this.modifySignificantOctet(longestDimension, this.lowerIndex, this.upperIndex);

      int dimensionMidPoint = midPoint(longestDimension);

      for (int i = this.lowerIndex; i < this.upperIndex; i++) {
        int color = ColorCutQuantizer.this.mColors[i];

        switch (longestDimension) {
        case -3:
          if (Color.red(color) >= dimensionMidPoint) {
            return i;
          }
          break;
        case -2:
          if (Color.green(color) >= dimensionMidPoint) {
            return i;
          }
          break;
        case -1:
          if (Color.blue(color) > dimensionMidPoint) {
            return i;
          }
          break;
        }
      }

      return this.lowerIndex;
    }

    PaletteItem getAverageColor()
    {
      int redSum = 0;
      int greenSum = 0;
      int blueSum = 0;
      int totalPopulation = 0;

      for (int i = this.lowerIndex; i <= this.upperIndex; i++) {
        int color = ColorCutQuantizer.this.mColors[i];
        int colorPopulation = ColorCutQuantizer.this.mColorPopulations.get(color);

        totalPopulation += colorPopulation;
        redSum += colorPopulation * Color.red(color);
        greenSum += colorPopulation * Color.green(color);
        blueSum += colorPopulation * Color.blue(color);
      }

      int redAverage = Math.round(redSum / totalPopulation);
      int greenAverage = Math.round(greenSum / totalPopulation);
      int blueAverage = Math.round(blueSum / totalPopulation);

      return new PaletteItem(redAverage, greenAverage, blueAverage, totalPopulation);
    }

    int midPoint(int dimension)
    {
      switch (dimension) {
      case -3:
      default:
        return (this.minRed + this.maxRed) / 2;
      case -2:
        return (this.minGreen + this.maxGreen) / 2;
      case -1:
      }return (this.minBlue + this.maxBlue) / 2;
    }
  }
}
