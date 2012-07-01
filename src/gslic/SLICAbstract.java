package gslic;

import utils.image.DoubleArrayImage;
import utils.utils.Arrays;

public abstract class SLICAbstract
{

	protected final DoubleArrayImage[] mDoubleImages;
	protected final int mNumberOfChannels;
	protected final double[] mChannelWeights;
	protected final int mWidth;
	protected final int mHeight;
	protected final LabelField mLabelField;
	protected final double[] mClosestCentroidDistanceField;
	protected final int mResolution;
	protected final double[] mCentroidsX;
	protected final double[] mCentroidsY;
	protected final double[][] mCentroidsV;
	protected final int[] mCentroidsN;
	protected final boolean[] mCentroidsDeleted;

	final int S;
	final double m;

	public SLICAbstract(DoubleArrayImage[] pDoubleImages,
											final int pResolution,
											final double pM)
	{
		mDoubleImages = pDoubleImages;
		mResolution = pResolution;
		m = pM;
		mNumberOfChannels = pDoubleImages.length;
		mChannelWeights = new double[mNumberOfChannels];
		Arrays.fill(mChannelWeights,1);
		mWidth = pDoubleImages[0].getWidth();
		mHeight = pDoubleImages[0].getHeight();
		mLabelField = new LabelField(mWidth,mHeight);
		mClosestCentroidDistanceField = new double[mDoubleImages[0].getLength()];

		mCentroidsX = new double[pResolution * pResolution];
		mCentroidsY = new double[pResolution * pResolution];
		mCentroidsV = new double[pDoubleImages.length][pResolution * pResolution];
		mCentroidsN = new int[pResolution * pResolution];
		mCentroidsDeleted = new boolean[pResolution * pResolution];

		S = oddNumber((int) Math.round(((double) (mWidth)) / (mResolution)));
	};

	protected final int oddNumber(final int pN)
	{
		return 2 * (pN / 2) + 1;
	}

	public void initialize()
	{
		int lCentroid = 0;
		for (int xi = 0; xi < mResolution; xi++)
			for (int yi = 0; yi < mResolution; yi++)
			{
				final int x = (int) Math.round(((double) xi * (mWidth - 1)) / (mResolution - 1));
				final int y = (int) Math.round(((double) yi * (mHeight - 1)) / (mResolution - 1));
				final int offset = x + mWidth * y;
				mCentroidsX[lCentroid] = x;
				mCentroidsY[lCentroid] = y;
				for (int channel = 0; channel < mNumberOfChannels; channel++)
				{
					final double v = mDoubleImages[channel].getArray()[offset];
					mCentroidsV[channel][lCentroid] = v;
				}
				mCentroidsN[lCentroid] = 0;
				lCentroid++;
			}
	}

	public final void gradientPerturb(final int pNumberOfSteps)
	{
		final int hS = (int) Math.round(S / 2);

		final int length = mCentroidsX.length;
		for (int lCentroid = 0; lCentroid < length; lCentroid++)
		{
			final int x = (int) mCentroidsX[lCentroid];
			final int y = (int) mCentroidsY[lCentroid];

			double mingsn = Double.POSITIVE_INFINITY;
			int lgsnx = x;
			int lgsny = y;

			for (int xi = -hS; xi <= hS; xi++)
				for (int yi = -hS; yi <= hS; yi++)
				{
					final int nx = x + xi;
					final int ny = y + yi;

					final double gsn = gradientSquaredNorm(nx, ny);
					if (gsn < mingsn)
					{
						mingsn = gsn;
						lgsnx = nx;
						lgsny = ny;
					}
				}

			mCentroidsX[lCentroid] = lgsnx < 0 ? 0
																				: (lgsnx >= mWidth ? mWidth - 1
																													: lgsnx);
			mCentroidsY[lCentroid] = lgsny < 0 ? 0
																				: (lgsny >= mHeight	? mHeight - 1
																														: lgsny);
		}
	}

	public final void steps(final int pNumberOfSteps)
	{
		for (int i = 0; i < pNumberOfSteps; i++)
			step();
	}

	public abstract void step();

	protected void processOnePixel(	final int lCentroid,
																	final double x,
																	final double y,
																	final int nx,
																	final int ny,
																	final double d)
	{
		final double ed = getClosestCentroidDistanceField(nx, ny);
		if (d < ed)
		{
			setClosestCentroidDistanceField(nx, ny, d);
			setLabelField(nx, ny, lCentroid);
		}
	}

	public final void computeCentroids()
	{
		clearCentroidPositionsAndPixelCount();

		for (int x = 0; x < mWidth; x++)
			for (int y = 0; y < mHeight; y++)
			{
				final int lCentroid = getLabelField(x, y);
				final int offset = x + mWidth * y;

				mCentroidsX[lCentroid] += x + 0.5;
				mCentroidsY[lCentroid] += y + 0.5;

				for (int channel = 0; channel < mNumberOfChannels; channel++)
				{
					final double v = mDoubleImages[channel].getArray()[offset];
					mCentroidsV[channel][lCentroid] += v;
				}
				mCentroidsN[lCentroid]++;
			}

		takeAverageForComputingCentroids();
	}

	protected final void takeAverageForComputingCentroids()
	{
		final int length = mCentroidsX.length;
		for (int lCentroid = 0; lCentroid < length; lCentroid++)
		{
			final int lNumberOfPixelsInCluster = mCentroidsN[lCentroid];
			mCentroidsX[lCentroid] /= lNumberOfPixelsInCluster;
			mCentroidsY[lCentroid] /= lNumberOfPixelsInCluster;
			for (int channel = 0; channel < mNumberOfChannels; channel++)
			{
				mCentroidsV[channel][lCentroid] /= lNumberOfPixelsInCluster;
			}
		}
	}

	public final void filterLabelField(final int pIterations)
	{
		final int length = mLabelField.array.length;
		final int[] array = mLabelField.array;
		final int[] lTempLabelField = new int[length];
		final int[][] lHistogram = new int[2][8];

		System.arraycopy(array, 0, lTempLabelField, 0, length);

		for (int i = 0; i < pIterations; i++)
		{
			for (int xi = 1; xi < mWidth - 1; xi++)
				for (int yi = 1; yi < mHeight - 1; yi++)
				{
					final int offset = xi + mWidth * yi;

					if (isIsolatedSuperPixel(xi, yi))
					{
						lTempLabelField[offset] = majorityVoteFilter(	array,
																													offset,
																													lHistogram,
																													0);
					}
					else
					{

						final int lNewLabel = majorityVoteFilter(	array,
																											offset,
																											lHistogram,
																											5);
						if (lNewLabel != -1)
							lTempLabelField[offset] = lNewLabel;
						else
							lTempLabelField[offset] = array[offset];
					}
				}
		}
		if (pIterations > 0)
			System.arraycopy(lTempLabelField, 0, array, 0, length);
	}

	private final int majorityVoteFilter(	final int[] pLabelField,
																				final int pOffset,
																				final int[][] pHistogram,
																				final int pThreshold)
	{
		clearHistogram(pHistogram);
		computeNeighbourHistogram(pLabelField, pOffset, pHistogram);
		final int lMajorityLabel = findMajority(pHistogram, pThreshold);

		return lMajorityLabel;
	}

	private final void clearHistogram(final int[][] pHistogram)
	{
		Arrays.fill(pHistogram[0], -1);
		Arrays.fill(pHistogram[1], 0);
	}

	private final void computeNeighbourHistogram(	final int[] pLabelField,
																								final int pOffset,
																								final int[][] pHistogram)
	{
		final int a = pLabelField[pOffset - mWidth - 1];
		final int b = pLabelField[pOffset - mWidth];
		final int c = pLabelField[pOffset - mWidth + 1];

		final int d = pLabelField[pOffset - 1];
		final int e = pLabelField[pOffset];
		final int f = pLabelField[pOffset + 1];

		final int g = pLabelField[pOffset + mWidth - 1];
		final int h = pLabelField[pOffset + mWidth];
		final int i = pLabelField[pOffset + mWidth + 1];

		put(pHistogram, a);
		put(pHistogram, b);
		put(pHistogram, c);
		put(pHistogram, d);
		put(pHistogram, e);
		put(pHistogram, f);
		put(pHistogram, g);
		put(pHistogram, h);
		put(pHistogram, i);
	}

	private final void put(final int[][] pHistogram, final int pLabel)
	{
		final int length = pHistogram[0].length;
		for (int i = 0; i < length; i++)
		{
			final int lLabel = pHistogram[0][i];
			if (lLabel == pLabel)
			{
				pHistogram[1][i]++;
				break;
			}
			else if (lLabel == -1)
			{
				pHistogram[0][i] = pLabel;
				pHistogram[1][i] = 1;
				break;
			}
		}
	}

	private final int findMajority(	final int[][] pHistogram,
																	final int pThreshold)
	{
		final int length = pHistogram[0].length;

		int lMajorityLabel = -1;
		int lMajorityCount = 0;

		for (int i = 0; i < length; i++)
		{
			final int lCount = pHistogram[1][i];
			if (lCount >= pThreshold && lCount > lMajorityCount)
			{
				lMajorityCount = lCount;
				lMajorityLabel = pHistogram[0][i];
			}
		}
		return lMajorityLabel;
	}

	public final void filterOutSmallComponents(final double pMinComponentSizeRelativeToExpected)
	{
		final int lMinComponentSize = (int) (pMinComponentSizeRelativeToExpected * S
																					* S + 1);

		final int[] lOffsetArray = new int[16 * (S) * (S)];
		final int[] lPixelState = new int[mLabelField.array.length];
		Arrays.fill(lPixelState, 0);
		final int[][] lHistogram = new int[2][4 * S];
		final int[] lLabelFieldArray = mLabelField.array;
				
		
		for (int xi = 0; xi < mWidth; xi++)
			for (int yi = 0; yi < mHeight; yi++)
			{
				lOffsetArray[0] = 0;
				Arrays.fill(lHistogram[0], -1);
				Arrays.fill(lHistogram[1], 0);
				final int lOffset = xi + mWidth * yi;
				final int lLabel = lLabelFieldArray[lOffset];
				int lSize = findComponentAt(lLabelFieldArray,
																		lPixelState,
																		lLabel,
																		xi,
																		yi,
																		lOffsetArray,
																		lHistogram);
				if (lSize < lMinComponentSize)
				{
					int lMajorityLabel = findMajority(lHistogram, 0);

					if (lMajorityLabel >= 0)
					{
						final int length = lOffsetArray[0];
						for (int i = 0; i < length; i++)
						{
							final int lStoredOffset = lOffsetArray[1 + i];
							// System.out.println(length);
							lLabelFieldArray[lStoredOffset] = lMajorityLabel;
						}
					}
				}
				else
				{
					int j = 0;
				}
			}

	}

	private final int findComponentAt(final int[] pLabelField,
																		final int[] pPixelState,
																		final int pLabel,
																		final int pX,
																		final int pY,
																		final int[] pOffsetArray,
																		final int[][] pHistogram)
	{
		final int lOffset = pX + mWidth * pY;
		if (pPixelState[lOffset] == 0)
		{
			final int lSize = recursiveComponentWalking(pLabelField,
																									pPixelState,
																									pLabel,
																									pX,
																									pY,
																									pOffsetArray,
																									pHistogram);
			return lSize;
		}
		else
			return 0;

	}

	private final int recursiveComponentWalking(final int[] pLabelField,
																							final int[] pPixelState,
																							final int pLabel,
																							final int pX,
																							final int pY,
																							final int[] pOffsetArray,
																							final int[][] pHistogram)
	{
		if (clampX(pX) != pX || clampY(pY) != pY)
			return 0;

		final int lOffset = pX + mWidth * pY;

		if (pPixelState[lOffset] == 0 && pLabelField[lOffset] == pLabel)
		{
			pPixelState[lOffset] = 1; // 1 -> visited
			pOffsetArray[1 + pOffsetArray[0]] = lOffset;
			pOffsetArray[0]++;

			final int c1 = recursiveComponentWalking(	pLabelField,
																								pPixelState,
																								pLabel,
																								pX,
																								pY - 1,
																								pOffsetArray,
																								pHistogram);
			final int c2 = recursiveComponentWalking(	pLabelField,
																								pPixelState,
																								pLabel,
																								pX - 1,
																								pY,
																								pOffsetArray,
																								pHistogram);
			final int c3 = recursiveComponentWalking(	pLabelField,
																								pPixelState,
																								pLabel,
																								pX + 1,
																								pY,
																								pOffsetArray,
																								pHistogram);
			final int c4 = recursiveComponentWalking(	pLabelField,
																								pPixelState,
																								pLabel,
																								pX,
																								pY + 1,
																								pOffsetArray,
																								pHistogram);

			return 1 + c1 + c2 + c3 + c4;
		}
		else
		{
			put(pHistogram, pLabelField[lOffset]);
			return 0;
		}
	}

	protected final void clearCentroidPositionsAndPixelCount()
	{
		Arrays.fill(mCentroidsX, 0);
		Arrays.fill(mCentroidsY, 0);
		for(int channel=0; channel<mNumberOfChannels; channel++)
			Arrays.fill(mCentroidsV[channel], 0);
		Arrays.fill(mCentroidsN, 0);
	}

	protected final void clearClosestCentroidDistanceField()
	{
		Arrays.fill(mClosestCentroidDistanceField,
								Double.POSITIVE_INFINITY);
	}

	protected final double getClosestCentroidDistanceField(	int pX,
																													int pY)
	{
		final int width = mWidth;
		final int height = mHeight;
		pX = pX < 0 ? 0 : (pX >= width ? width - 1 : pX);
		pY = pY < 0 ? 0 : (pY >= height ? height - 1 : pY);
		return mClosestCentroidDistanceField[pX + mWidth * pY];
	}

	protected final void setClosestCentroidDistanceField(	final int pX,
																												final int pY,
																												final double pD)
	{
		final int width = mWidth;
		final int height = mHeight;
		if (pX < 0 || pX >= width || pY < 0 || pY >= height)
			return;
		mClosestCentroidDistanceField[pX + mWidth * pY] = pD;
	}

	protected final int getLabelField(final int pX, final int pY)
	{
		final int width = mWidth;
		final int height = mHeight;
		if (pX < 0 || pX >= width || pY < 0 || pY >= height)
			return -1;
		return mLabelField.array[pX + mWidth * pY];
	}

	protected final boolean isNotAssigned(final int pX, final int pY)
	{
		final int width = mWidth;
		final int height = mHeight;
		if (pX < 0 || pX >= width || pY < 0 || pY >= height)
			return false;
		return mLabelField.array[pX + mWidth * pY] < 0;
	}

	protected final void setLabelField(	final int pX,
																			final int pY,
																			final int pL)
	{
		final int width = mWidth;
		final int height = mHeight;
		if (pX < 0 || pX >= width || pY < 0 || pY >= height)
			return;
		mLabelField.array[pX + mWidth * pY] = pL;
	}

	public final double gradientSquaredNorm(final int pX, final int pY)
	{
		double gn2s = 0;

		for (int channel = 0; channel < mNumberOfChannels; channel++)
		{

			final double vym = mDoubleImages[channel].getInt(	(int) (pX),
																												(int) (pY - 1));
			final double vyp = mDoubleImages[channel].getInt(	(int) (pX),
																												(int) (pY + 1));

			final double vxm = mDoubleImages[channel].getInt(	(int) (pX - 1),
																												(int) (pY));
			final double vxp = mDoubleImages[channel].getInt(	(int) (pX + 1),
																												(int) (pY));

			final double gn2 = (vxm - vxp) * (vxm - vxp)
													+ (vym - vyp)
													* (vym - vyp);

			gn2s += gn2;
		}

		return gn2s;
	}

	public DoubleArrayImage getDoubleImage()
	{
		return mDoubleImages[0];
	}
	
	public DoubleArrayImage getDoubleImage(final int pChannel)
	{
		return mDoubleImages[pChannel];
	}

	public final boolean isIsolatedSuperPixel(final int pX, final int pY)
	{
		final int o = getLabelField(pX, pY);

		final int t = getLabelField(pX, pY - 1);
		final int b = getLabelField(pX, pY + 1);
		final int l = getLabelField(pX - 1, pY);
		final int r = getLabelField(pX + 1, pY);

		final boolean isBoundary = (o != t) && (o != l)
																&& (o != r)
																&& (o != b);

		return isBoundary;
	}

	public final boolean isBoundaryPixel(final int pX, final int pY)
	{
		final int o = getLabelField(pX, pY);

		final int t = getLabelField(pX, pY - 1);
		final int b = getLabelField(pX, pY + 1);
		final int l = getLabelField(pX - 1, pY);
		final int r = getLabelField(pX + 1, pY);

		final boolean isBoundary = (o != t) || (o != l)
																|| (o != r)
																|| (o != b);

		return isBoundary;
	}

	public final boolean isBoundaryBottom(final int pX, final int pY)
	{
		final int o = getLabelField(pX, pY);
		final int b = getLabelField(pX, pY + 1);
		return o != b;
	}

	public final boolean isBoundaryTop(final int pX, final int pY)
	{
		final int o = getLabelField(pX, pY);
		final int t = getLabelField(pX, pY - 1);
		return o != t;
	}

	public final boolean isBoundaryLeft(final int pX, final int pY)
	{
		final int o = getLabelField(pX, pY);
		final int l = getLabelField(pX - 1, pY);
		return o != l;
	}

	public final boolean isBoundaryRight(final int pX, final int pY)
	{
		final int o = getLabelField(pX, pY);
		final int r = getLabelField(pX + 1, pY);
		return o != r;
	}

	public final int getNumberOfCentroids()
	{
		return mCentroidsX.length;
	}

	public final double[] getCentroidArrayX()
	{
		return mCentroidsX;
	}

	public final double[] getCentroidArrayY()
	{
		return mCentroidsY;
	}

	public final double[][] getCentroidArraysV()
	{
		return mCentroidsV;
	}
	
	public final double[] getCentroidArrayV(final int pChannel)
	{
		return mCentroidsV[pChannel];
	}

	public final double convertToNormalizedImageCoordinatesX(final double pX)
	{
		return pX / mWidth;
	}

	public final double convertToNormalizedImageCoordinatesY(final double pY)
	{
		return pY / mHeight;
	}

	private final int clampX(final int pX)
	{
		return pX < 0 ? 0 : (pX >= mWidth ? mWidth - 1 : pX);
	}

	private final int clampY(final int pY)
	{
		return pY < 0 ? 0 : (pY >= mHeight ? mHeight - 1 : pY);
	}

}