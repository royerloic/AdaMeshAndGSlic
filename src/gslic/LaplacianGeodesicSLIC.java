package gslic;

import utils.image.DoubleArrayImage;
import utils.image.convolution.Convolution;
import utils.image.convolution.Gaussian;

public class LaplacianGeodesicSLIC extends SLICAbstract
{

	Convolution mLaplacian1;
	Convolution mLaplacian2;
	Convolution mLaplacian3;

	public LaplacianGeodesicSLIC(	DoubleArrayImage pDoubleImage,
																int pResolution,
																double pM,
																int pFilterSize,
																double pSigma)
	{
		super(new DoubleArrayImage[]
					{ pDoubleImage,
						new DoubleArrayImage(	pDoubleImage.getWidth(),
																	pDoubleImage.getHeight()),
						new DoubleArrayImage(	pDoubleImage.getWidth(),
																	pDoubleImage.getHeight()),
						new DoubleArrayImage(	pDoubleImage.getWidth(),
																	pDoubleImage.getHeight()) },
					pResolution,
					pM);
		mLaplacian1 = new Gaussian(pFilterSize, pSigma);
		mLaplacian1.compute(mDoubleImages[0], mDoubleImages[1]);
		mLaplacian2 = new Gaussian(pFilterSize * 2, pSigma * 2);
		mLaplacian2.compute(mDoubleImages[0], mDoubleImages[2]);
		mLaplacian3 = new Gaussian(pFilterSize * 4, pSigma * 4);
		mLaplacian3.compute(mDoubleImages[0], mDoubleImages[3]);
		// mDoubleImages[1].mult(10);
		mChannelWeights[1] = 1;
		mChannelWeights[1] = 8;
		mChannelWeights[2] = 8 * 8;
		mChannelWeights[3] = 8 * 8 * 8;
	}

	public final void step()
	{
		assign();
		computeCentroids();
	}

	public void assign()
	{
		clearClosestCentroidDistanceField();
		final int length = mCentroidsX.length;
		for (int lCentroid = 0; lCentroid < length; lCentroid++)
			assignForOneCentroidGeodesic(lCentroid);
	}

	double[][] mDistanceMatrix;

	private void assignForOneCentroidGeodesic(int lCentroid)
	{
		final double x = mCentroidsX[lCentroid];
		final double y = mCentroidsY[lCentroid];

		final int s = S;

		if (mDistanceMatrix == null)
			mDistanceMatrix = new double[2 * S + 1][2 * S + 1];

		initialiseGeodesicDistances(lCentroid, mDistanceMatrix, x, y, S);
		computeGeodesicDistances(mDistanceMatrix, S);

		for (int xi = -s; xi <= s; xi++)
			for (int yi = -s; yi <= s; yi++)
			{
				final int nx = (int) x + xi;
				final int ny = (int) y + yi;
				final double d = mDistanceMatrix[s + xi][s + yi];
				processOnePixel(lCentroid, x, y, nx, ny, d);
			}
	}

	private void initialiseGeodesicDistances(	final int pCentroid,
																						final double[][] pDistanceMatrix,
																						final double pX,
																						final double pY,
																						final int pS)
	{
		final double invS = (1.0 / S);
		final double[] cv = new double[mNumberOfChannels];
		for (int channel = 0; channel < mNumberOfChannels; channel++)
		{
			final int x = (int) Math.round(pX);
			final int y = (int) Math.round(pY);
			cv[channel] = mCentroidsV[channel][pCentroid];
			// cv[channel] = mDoubleImages[channel].getInt(x, y);

		}

		for (int xi = 0; xi < 2 * pS + 1; xi++)
			for (int yi = 0; yi < 2 * pS + 1; yi++)
				mDistanceMatrix[xi][yi] = 0;

		for (int xi = -pS; xi <= pS; xi++)
			for (int yi = -pS; yi <= pS; yi++)
			{
				final int x = (int) Math.round(pX + xi);
				final int y = (int) Math.round(pY + yi);
				for (int channel = 0; channel < mNumberOfChannels; channel++)
				{
					final double v = mDoubleImages[channel].getInt(x, y);
					pDistanceMatrix[pS + xi][pS + yi] += mChannelWeights[channel] * invS
																								* Math.abs(v - cv[channel]);
				}
			}
	}

	private final void computeGeodesicDistances(final double[][] pDistanceMatrix,
																							final int pS)
	{
		final double deltaXY = (m / S);
		final int s = pS;
		int xi = 0;
		int yi = 0;
		for (int r = 1; r <= s; r++)
		{
			for (yi = s - r + 1; yi < s + r; yi++)
			{
				xi = s - r;
				propagateFrom(pDistanceMatrix, deltaXY, xi, yi, +1, 0);
				xi = s + r;
				propagateFrom(pDistanceMatrix, deltaXY, xi, yi, -1, 0);
			}
			for (xi = s - r + 1; xi < s + r; xi++)
			{
				yi = s - r;
				propagateFrom(pDistanceMatrix, deltaXY, xi, yi, 0, 1);
				yi = s + r;
				propagateFrom(pDistanceMatrix, deltaXY, xi, yi, 0, -1);
			}

			xi = s - r;
			yi = s - r;
			propagateFrom(pDistanceMatrix, 1.42 * deltaXY, xi, yi, +1, +1);

			xi = s - r;
			yi = s + r;
			propagateFrom(pDistanceMatrix, 1.42 * deltaXY, xi, yi, +1, -1);

			xi = s + r;
			yi = s + r;
			propagateFrom(pDistanceMatrix, 1.42 * deltaXY, xi, yi, -1, -1);

			xi = s + r;
			yi = s - r;
			propagateFrom(pDistanceMatrix, 1.42 * deltaXY, xi, yi, -1, +1);
		}
	}

	private final void propagateFrom(	final double[][] pDistanceMatrix,
																		final double pDeltaXY,
																		final int pXi,
																		final int pYi,
																		final int pDx,
																		final int pDy)
	{
		pDistanceMatrix[pXi][pYi] += pDeltaXY + pDistanceMatrix[pXi + pDx][pYi + pDy];
	}

	public DoubleArrayImage getEdgeImage()
	{
		return mDoubleImages[1];
	}

	public final int[] getLabelField()
	{
		return mLabelField.array;
	}

}
