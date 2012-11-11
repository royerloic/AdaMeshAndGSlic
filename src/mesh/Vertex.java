package mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Vertex
{
	public double x;
	public double y;

	public int i;

	private final ArrayList<Triangle> mTriangles = new ArrayList<Triangle>();

	private boolean mNeighboursUpToDate = false;
	private final ArrayList<Vertex> mNeighbours = new ArrayList<Vertex>();

	public Vertex(double pX, double pY)
	{
		super();
		x = pX;
		y = pY;
	}

	public void belongsToTriangle(Triangle pTriangle)
	{
		mTriangles.add(pTriangle);
		mNeighboursUpToDate = false;
	}

	public void ensureNeighboursUpToDate()
	{
		if (!mNeighboursUpToDate)
		{
			mNeighbours.clear();
			for (Triangle lTriangle : mTriangles)
			{
				if (lTriangle.u != this)
					mNeighbours.add(lTriangle.u);
				if (lTriangle.v != this)
					mNeighbours.add(lTriangle.v);
				if (lTriangle.w != this)
					mNeighbours.add(lTriangle.w);
			}
			mNeighboursUpToDate = true;
		}
	}

	public ArrayList<Vertex> getNeighbours()
	{
		ensureNeighboursUpToDate();
		return mNeighbours;
	}

	/*
	 * This only tells whether this Vertex U states that the other Vertex V is a
	 * neighbour. It does not garantee that V also states that U is a neighbour.
	 * It is better to use the static symmetric method to be certain.
	 */
	private boolean isNeighbour(Vertex pV)
	{
		return getNeighbours().contains(pV);
	}

	public void unlinkFrom(Triangle pTriangle)
	{
		mTriangles.remove(pTriangle);
		mNeighboursUpToDate = false;
	}

	public ArrayList<Triangle> getTriangles()
	{
		return mTriangles;
	}

	@Override
	public String toString()
	{
		return "Vertex [x=" + x + ", y=" + y + ", i=" + i + "]";
	}

	public static final boolean areNeighbours(final Vertex pU,
																						final Vertex pV)
	{
		return pU.isNeighbour(pV) && pV.isNeighbour(pU);
	}

	public static final double angle(	final Vertex o,
																		final Vertex u,
																		final Vertex v)
	{
		final double v1x = u.x - o.x;
		final double v1y = u.y - o.y;
		final double v2x = v.x - o.x;
		final double v2y = v.y - o.y;

		final double nv1 = Math.sqrt(v1x * v1x + v1y * v1y);
		final double nv2 = Math.sqrt(v2x * v2x + v2y * v2y);

		final double dotprod = v1x * v2x + v1y * v2y;
		final double ndotprod = dotprod / (nv1 * nv2);

		final double alpha = Math.acos(ndotprod);
		return alpha;
	}

	public static final double distance(final Vertex u, final Vertex v)
	{
		final double ux = u.x;
		final double uy = u.y;
		final double vx = v.x;
		final double vy = v.y;
		return Math.sqrt((ux - vx) * (ux - vx) + (uy - vy) * (uy - vy));
	}

	public final boolean is(final double pX, final double pY)
	{
		return x == pX && y == pY;
	}

}
