package gslic;

import utils.image.DoubleArrayImage;

public class StandardSLIC extends SLICAbstract
{

	public StandardSLIC(DoubleArrayImage pDoubleImage,
											int pResolution,
											double pM)
	{
		super(new DoubleArrayImage[]{pDoubleImage}, pResolution, pM);
	}

	public void step()
	{
		assign();
		computeCentroids();
	}

	public void assign()
	{
		clearClosestCentroidDistanceField();
		final int length = mCentroidsX.length;
		for (int lCentroid = 0; lCentroid < length; lCentroid++)
			assignForOneCentroid(lCentroid);
	}

	private void assignForOneCentroid(int lCentroid)
	{
		final double x = mCentroidsX[lCentroid];
		final double y = mCentroidsY[lCentroid];
		final double v = mCentroidsV[0][lCentroid];

		final int s = S;

		for (int xi = -s; xi <= s; xi++)
			for (int yi = -s; yi <= s; yi++)
			{
				final int nx = (int) Math.round(x + xi);
				final int ny = (int) Math.round(y + yi);
				final double nv = mDoubleImages[0].getInt(nx, ny);
				final double d = distance(x, y, v, nx, ny, nv);
				processOnePixel(lCentroid, x, y, nx, ny,  d);
			}
	}

	public final double distance(	final double x1,
																final double y1,
																final double v1,
																final double x2,
																final double y2,
																final double v2)
	{

		final double dlab = Math.abs(v1 - v2);
		final double dxy = Math.sqrt((x1 - x2) * (x1 - x2)
																	+ (y1 - y2)
																	* (y1 - y2));

		// final double dxy = Math.abs(x1 - x2) + Math.abs(y1 - y2);

		final double d = dlab + (m / S) * dxy;
		return d;
	}

}
