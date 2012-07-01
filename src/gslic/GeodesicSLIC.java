package gslic;

import utils.image.DoubleArrayImage;


public class GeodesicSLIC extends SLICAbstract
{

	public GeodesicSLIC(DoubleArrayImage pDoubleImage,
											int pResolution,
											double pM)
	{
		super(new DoubleArrayImage[]
		{ pDoubleImage }, pResolution, pM);
	}

	public final void step()
	{
		assignGeodesic();
		computeCentroids();
	}

	public void assignGeodesic()
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
		final int xc = (int) Math.round(pX);
		final int yc = (int) Math.round(pY);

		//final double cv = mDoubleImages[0].getInt(xc, yc);
		final double cv =mCentroidsV[0][pCentroid];

		for (int xi = -pS; xi <= pS; xi++)
			for (int yi = -pS; yi <= pS; yi++)
			{
				final int x = xc + xi;
				final int y = yc + yi;
				final double v = mDoubleImages[0].getInt(x, y);
				pDistanceMatrix[pS + xi][pS + yi] = invS * Math.abs(v - cv);
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

}
