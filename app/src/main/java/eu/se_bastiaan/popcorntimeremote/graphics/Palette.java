package eu.se_bastiaan.popcorntimeremote.graphics;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import java.util.Collections;
import java.util.List;

public final class Palette
{
  private final List<PaletteItem> mPallete;
  private final int mHighestPopulation;
  private PaletteItem mVibrantColor;
  private PaletteItem mMutedColor;
  private PaletteItem mDarkVibrantColor;
  private PaletteItem mDarkMutedColor;
  private PaletteItem mLightVibrantColor;
  private PaletteItem mLightMutedColor;

  public static Palette generate(Bitmap bitmap)
  {
    return generate(bitmap, 16);
  }

  public static Palette generate(Bitmap bitmap, int numColors)
  {
    if (bitmap == null) {
      throw new IllegalArgumentException("bitmap can not be null");
    }

    Bitmap scaledBitmap = scaleBitmapDown(bitmap);

    eu.se_bastiaan.popcorntimeremote.graphics.ColorCutQuantizer quantizer = eu.se_bastiaan.popcorntimeremote.graphics.ColorCutQuantizer.fromBitmap(scaledBitmap, numColors);

    if (scaledBitmap != bitmap) {
      scaledBitmap.recycle();
    }

    return new Palette(quantizer.getQuantizedColors());
  }

  public static AsyncTask<Void, Void, Palette> generateAsync(Bitmap bitmap, PaletteAsyncListener listener)
  {
    return generateAsync(bitmap, 16, listener);
  }

  public static AsyncTask<Void, Void, Palette> generateAsync(final Bitmap bitmap, final int numColors, final PaletteAsyncListener listener)
  {
    if (listener == null) {
      throw new IllegalArgumentException("listener can not be null");
    }

    AsyncTask<Void, Void, Palette> task = new AsyncTask<Void, Void, Palette>()
    {
      protected Palette doInBackground(Void[] voids) {
        return Palette.generate(bitmap, numColors);
      }

      protected void onPostExecute(Palette colorExtractor)
      {
        super.onPostExecute(colorExtractor);
        listener.onGenerated(colorExtractor);
      }
    };
    task.execute();
    return task;
  }

  private Palette(List<PaletteItem> palette) {
    this.mPallete = palette;
    this.mHighestPopulation = findMaxPopulation();

    this.mVibrantColor = findColor(0.5F, 0.3F, 0.7F, 1.0F, 0.35F, 1.0F);

    this.mLightVibrantColor = findColor(0.74F, 0.55F, 1.0F, 1.0F, 0.35F, 1.0F);

    this.mDarkVibrantColor = findColor(0.26F, 0.0F, 0.45F, 1.0F, 0.35F, 1.0F);

    this.mMutedColor = findColor(0.5F, 0.3F, 0.7F, 0.3F, 0.0F, 0.4F);

    this.mLightMutedColor = findColor(0.74F, 0.55F, 1.0F, 0.3F, 0.0F, 0.4F);

    this.mDarkMutedColor = findColor(0.26F, 0.0F, 0.45F, 0.3F, 0.0F, 0.4F);

    generateEmptyColors();
  }

  public List<PaletteItem> getPallete()
  {
    return Collections.unmodifiableList(this.mPallete);
  }

  public PaletteItem getVibrantColor()
  {
    return this.mVibrantColor;
  }

  public PaletteItem getLightVibrantColor()
  {
    return this.mLightVibrantColor;
  }

  public PaletteItem getDarkVibrantColor()
  {
    return this.mDarkVibrantColor;
  }

  public PaletteItem getMutedColor()
  {
    return this.mMutedColor;
  }

  public PaletteItem getLightMutedColor()
  {
    return this.mLightMutedColor;
  }

  public PaletteItem getDarkMutedColor()
  {
    return this.mDarkMutedColor;
  }

  private boolean isAlreadySelected(PaletteItem item)
  {
    return (this.mVibrantColor == item) || (this.mDarkVibrantColor == item) || (this.mLightVibrantColor == item) || (this.mMutedColor == item) || (this.mDarkMutedColor == item) || (this.mLightMutedColor == item);
  }

  private PaletteItem findColor(float targetLuma, float minLuma, float maxLuma, float targetSaturation, float minSaturation, float maxSaturation)
  {
    PaletteItem max = null;
    float maxValue = 0.0F;

    for (PaletteItem paletteItem : this.mPallete) {
      float sat = paletteItem.getHsl()[1];
      float luma = paletteItem.getHsl()[2];

      if ((sat >= minSaturation) && (sat <= maxSaturation) && (luma >= minLuma) && (luma <= maxLuma) && (!isAlreadySelected(paletteItem)))
      {
        float thisValue = createComparisonValue(sat, targetSaturation, luma, targetLuma, paletteItem.getPopulation(), this.mHighestPopulation);

        if ((max == null) || (thisValue > maxValue)) {
          max = paletteItem;
          maxValue = thisValue;
        }
      }
    }

    return max;
  }

  private void generateEmptyColors()
  {
    if (this.mVibrantColor == null)
    {
      if (this.mDarkVibrantColor != null)
      {
        float[] newHsl = copyHslValues(this.mDarkVibrantColor);
        newHsl[2] = 0.5F;
        this.mVibrantColor = new PaletteItem(eu.se_bastiaan.popcorntimeremote.graphics.ColorUtils.HSLtoRGB(newHsl), 0);
      }
    }

    if (this.mDarkVibrantColor == null)
    {
      if (this.mVibrantColor != null)
      {
        float[] newHsl = copyHslValues(this.mVibrantColor);
        newHsl[2] = 0.26F;
        this.mDarkVibrantColor = new PaletteItem(eu.se_bastiaan.popcorntimeremote.graphics.ColorUtils.HSLtoRGB(newHsl), 0);
      }
    }
  }

  private int findMaxPopulation()
  {
    int population = 0;
    for (PaletteItem item : this.mPallete) {
      population = Math.max(population, item.getPopulation());
    }
    return population;
  }

  private static Bitmap scaleBitmapDown(Bitmap bitmap)
  {
    int minDimension = Math.min(bitmap.getWidth(), bitmap.getHeight());

    if (minDimension <= 100)
    {
      return bitmap;
    }

    float scaleRatio = 100.0F / minDimension;
    return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scaleRatio), Math.round(bitmap.getHeight() * scaleRatio), false);
  }

  private static float createComparisonValue(float saturation, float targetSaturation, float luma, float targetLuma, int population, int highestPopulation)
  {
    return weightedMean(new float[] { invertDiff(saturation, targetSaturation), 3.0F, invertDiff(luma, targetLuma), 6.5F, population / highestPopulation, 0.5F });
  }

  private static float[] copyHslValues(PaletteItem color)
  {
    float[] newHsl = new float[3];
    System.arraycopy(color.getHsl(), 0, newHsl, 0, 3);
    return newHsl;
  }

  private static float invertDiff(float value, float targetValue)
  {
    return 1.0F - Math.abs(value - targetValue);
  }

  private static float weightedMean(float[] values) {
    float sum = 0.0F;
    float sumWeight = 0.0F;

    for (int i = 0; i < values.length; i += 2) {
      float value = values[i];
      float weight = values[(i + 1)];

      sum += value * weight;
      sumWeight += weight;
    }

    return sum / sumWeight;
  }

  public static abstract interface PaletteAsyncListener
  {
    public abstract void onGenerated(Palette paramPalette);
  }
}