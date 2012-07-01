package superpixel;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

public class SuperPixel
{
	final private SuperPixels mSuperPixels;
	private final ArrayList<SuperPixel> mNeighbours = new ArrayList<SuperPixel>();

	final public TDoubleArrayList[] mCoordinates;
	public double[] features;

	public SuperPixel(SuperPixels pSuperPixels)
	{
		super();
		mCoordinates = new TDoubleArrayList[pSuperPixels.dim];
		mSuperPixels = pSuperPixels;
	}

	public final void addPixel(final double... pCoordinates)
	{
		int i = 0;
		for (double lCoordinate : pCoordinates)
		{
			TDoubleArrayList lCoordinates = mCoordinates[i++];
			lCoordinates.add(lCoordinate);
		}
	}

	public final void addNeighbour(final SuperPixel pSuperPixel)
	{
		mNeighbours.add(pSuperPixel);
	}

	public final ArrayList<SuperPixel> getNeighbours()
	{
		return mNeighbours;
	}

	private final boolean isNeighbour(SuperPixel pS)
	{
		return getNeighbours().contains(pS);
	}

	public static final boolean isNeighbour(final SuperPixel pS1,
																					final SuperPixel pS2)
	{
		return pS1.isNeighbour(pS2) && pS2.isNeighbour(pS1);
	}

}
